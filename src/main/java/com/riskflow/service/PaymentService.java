package com.riskflow.service;

import com.riskflow.dto.PaymentRequest;
import com.riskflow.dto.PaymentResponse;
import com.riskflow.exception.ResourceNotFoundException;
import com.riskflow.model.DecisionType;
import com.riskflow.model.PaymentEvent;
import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.PaymentEventType;
import com.riskflow.model.RiskDecision;
import com.riskflow.model.Transaction;
import com.riskflow.model.TransactionStatus;
import com.riskflow.repository.PaymentEventRepository;
import com.riskflow.repository.RiskDecisionRepository;
import com.riskflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final RiskDecisionRepository riskDecisionRepository;
    private final RiskEvaluationService riskEvaluationService;

    public PaymentService(
            TransactionRepository transactionRepository,
            PaymentEventRepository paymentEventRepository,
            RiskDecisionRepository riskDecisionRepository,
            RiskEvaluationService riskEvaluationService
    ) {
        this.transactionRepository = transactionRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.riskDecisionRepository = riskDecisionRepository;
        this.riskEvaluationService = riskEvaluationService;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments() {
        return transactionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        return toResponse(tx);
    }

    @Transactional
    public PaymentResponse ingest(PaymentRequest request) {
        return transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(this::toResponse)
                .orElseGet(() -> persistNew(request));
    }

    private PaymentResponse persistNew(PaymentRequest request) {
        RiskEvaluationService.RiskOutcome outcome = riskEvaluationService.evaluate(
                request.amount(),
                request.currency()
        );

        String businessTransactionId = UUID.randomUUID().toString();

        Transaction tx = new Transaction(
                businessTransactionId,
                request.idempotencyKey(),
                request.userId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                outcome.status()
        );
        tx = transactionRepository.save(tx);

        Instant eventTime = Instant.now();
        PaymentEvent submitted = new PaymentEvent(
                tx,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                "Payment accepted for processing.",
                eventTime,
                "{}"
        );
        paymentEventRepository.save(submitted);

        RiskDecision decision = new RiskDecision(
                tx,
                toDecisionType(outcome.status()),
                outcome.triggeredRule(),
                outcome.reason()
        );
        riskDecisionRepository.save(decision);

        PaymentEvent riskEvent = new PaymentEvent(
                tx,
                PaymentEventType.RISK_EVALUATED,
                PaymentEventStatus.COMPLETED,
                outcome.reason(),
                eventTime,
                "{}"
        );
        paymentEventRepository.save(riskEvent);

        return toResponse(tx);
    }

    private static DecisionType toDecisionType(TransactionStatus status) {
        return switch (status) {
            case APPROVED -> DecisionType.APPROVE;
            case DECLINED -> DecisionType.DECLINE;
            case MANUAL_REVIEW -> DecisionType.REVIEW;
            case PENDING -> DecisionType.REVIEW;
        };
    }

    private PaymentResponse toResponse(Transaction tx) {
        RiskDecision decision = riskDecisionRepository.findFirstByTransaction_IdOrderByCreatedAtDesc(tx.getId())
                .orElseThrow(() -> new IllegalStateException("Missing risk decision for transaction " + tx.getId()));
        PaymentEvent firstEvent = paymentEventRepository.findFirstByTransaction_IdOrderByCreatedAtAsc(tx.getId())
                .orElseThrow(() -> new IllegalStateException("Missing payment events for transaction " + tx.getId()));

        return new PaymentResponse(
                tx.getId(),
                tx.getTransactionId(),
                tx.getIdempotencyKey(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getPaymentMethod(),
                tx.getStatus(),
                decision.getDecision(),
                decision.getTriggeredRule(),
                decision.getReason(),
                firstEvent.getEventTimestamp(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }
}
