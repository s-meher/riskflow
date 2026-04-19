package com.riskflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riskflow.dto.EventIngestResponse;
import com.riskflow.dto.PaymentEventIngestRequest;
import com.riskflow.model.DecisionType;
import com.riskflow.model.PaymentEvent;
import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.RiskDecision;
import com.riskflow.model.Transaction;
import com.riskflow.model.TransactionStatus;
import com.riskflow.repository.PaymentEventRepository;
import com.riskflow.repository.RiskDecisionRepository;
import com.riskflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ingests payment events: persists the payload, applies risk rules, updates the transaction aggregate.
 */
@Service
public class EventIngestionService {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("1000");
    private static final Duration RECENT_FAILURE_WINDOW = Duration.ofHours(24);

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "INR");

    private static final String RULE_HIGH_AMOUNT = "HIGH_AMOUNT";
    private static final String RULE_UNSUPPORTED_CURRENCY = "UNSUPPORTED_CURRENCY";
    private static final String RULE_REPEATED_FAILURES = "REPEATED_FAILURES";

    private final TransactionRepository transactionRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final RiskDecisionRepository riskDecisionRepository;
    private final ObjectMapper objectMapper;

    public EventIngestionService(
            TransactionRepository transactionRepository,
            PaymentEventRepository paymentEventRepository,
            RiskDecisionRepository riskDecisionRepository,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.riskDecisionRepository = riskDecisionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EventIngestResponse ingest(PaymentEventIngestRequest request) {
        String rawPayload = serializeRequest(request);

        Instant since = Instant.now().minus(RECENT_FAILURE_WINDOW);
        long priorFailedCount = paymentEventRepository.countFailedEventsForUserSince(
                request.userId(),
                PaymentEventStatus.FAILED,
                since
        );
        boolean thisEventFailed = request.status() == PaymentEventStatus.FAILED;
        long effectiveFailureCount = priorFailedCount + (thisEventFailed ? 1 : 0);

        Transaction transaction = upsertTransaction(request);

        PaymentEvent savedEvent = paymentEventRepository.save(new PaymentEvent(
                transaction,
                request.eventType(),
                request.status(),
                request.reason(),
                request.eventTimestamp(),
                rawPayload
        ));

        List<String> triggeredRules = evaluateRules(request, effectiveFailureCount);

        DecisionType decision = decide(triggeredRules);
        TransactionStatus txStatus = toTransactionStatus(decision);

        transaction.setStatus(txStatus);
        transactionRepository.save(transaction);

        String triggeredRuleField = triggeredRules.isEmpty() ? "NONE" : String.join(",", triggeredRules);
        String reason = buildDecisionReason(triggeredRules, request);

        RiskDecision riskDecision = new RiskDecision(transaction, decision, triggeredRuleField, reason);
        riskDecisionRepository.save(riskDecision);

        return new EventIngestResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                savedEvent.getId(),
                decision,
                triggeredRuleField,
                reason,
                txStatus
        );
    }

    private String serializeRequest(PaymentEventIngestRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event payload", e);
        }
    }

    private Transaction upsertTransaction(PaymentEventIngestRequest request) {
        return transactionRepository.findByTransactionId(request.transactionId())
                .map(existing -> {
                    existing.setUserId(request.userId());
                    existing.setAmount(request.amount());
                    existing.setCurrency(request.currency().toUpperCase());
                    existing.setPaymentMethod(request.paymentMethod());
                    existing.setStatus(TransactionStatus.PENDING);
                    return transactionRepository.save(existing);
                })
                .orElseGet(() -> transactionRepository.save(new Transaction(
                        request.transactionId(),
                        request.transactionId(),
                        request.userId(),
                        request.amount(),
                        request.currency().toUpperCase(),
                        request.paymentMethod(),
                        TransactionStatus.PENDING
                )));
    }

    /**
     * Rules are independent checks; results are combined in {@link #decide(List)}.
     */
    private List<String> evaluateRules(PaymentEventIngestRequest request, long effectiveFailureCount) {
        List<String> rules = new ArrayList<>();
        if (request.amount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            rules.add(RULE_HIGH_AMOUNT);
        }
        if (!SUPPORTED_CURRENCIES.contains(request.currency().toUpperCase())) {
            rules.add(RULE_UNSUPPORTED_CURRENCY);
        }
        if (effectiveFailureCount >= 3) {
            rules.add(RULE_REPEATED_FAILURES);
        }
        return rules;
    }

    /**
     * Decline beats review beats approve. Unsupported currency or repeated failures decline; high amount goes to review.
     */
    private DecisionType decide(List<String> triggeredRules) {
        if (triggeredRules.isEmpty()) {
            return DecisionType.APPROVE;
        }
        if (triggeredRules.contains(RULE_REPEATED_FAILURES) || triggeredRules.contains(RULE_UNSUPPORTED_CURRENCY)) {
            return DecisionType.DECLINE;
        }
        if (triggeredRules.contains(RULE_HIGH_AMOUNT)) {
            return DecisionType.REVIEW;
        }
        return DecisionType.APPROVE;
    }

    private TransactionStatus toTransactionStatus(DecisionType decision) {
        return switch (decision) {
            case APPROVE -> TransactionStatus.APPROVED;
            case DECLINE -> TransactionStatus.DECLINED;
            case REVIEW -> TransactionStatus.MANUAL_REVIEW;
        };
    }

    private String buildDecisionReason(List<String> triggeredRules, PaymentEventIngestRequest request) {
        if (triggeredRules.isEmpty()) {
            return "No risk rules triggered.";
        }
        return triggeredRules.stream()
                .map(rule -> describeRule(rule, request))
                .collect(Collectors.joining(" "));
    }

    private String describeRule(String rule, PaymentEventIngestRequest request) {
        return switch (rule) {
            case RULE_HIGH_AMOUNT -> "[HIGH_AMOUNT] Amount " + request.amount() + " exceeds 1000. ";
            case RULE_UNSUPPORTED_CURRENCY -> "[UNSUPPORTED_CURRENCY] Currency " + request.currency()
                    + " is not USD, EUR, or INR. ";
            case RULE_REPEATED_FAILURES -> "[REPEATED_FAILURES] User has 3+ failed events in the last "
                    + RECENT_FAILURE_WINDOW.toHours() + " hours. ";
            default -> "[" + rule + "] ";
        };
    }
}
