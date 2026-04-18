package com.riskflow.service;

import com.riskflow.dto.PaymentRequest;
import com.riskflow.dto.PaymentResponse;
import com.riskflow.exception.ResourceNotFoundException;
import com.riskflow.model.Payment;
import com.riskflow.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RiskEvaluationService riskEvaluationService;

    public PaymentService(PaymentRepository paymentRepository, RiskEvaluationService riskEvaluationService) {
        this.paymentRepository = paymentRepository;
        this.riskEvaluationService = riskEvaluationService;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Transactional
    public PaymentResponse ingest(PaymentRequest request) {
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(this::toResponse)
                .orElseGet(() -> persistNew(request));
    }

    private PaymentResponse persistNew(PaymentRequest request) {
        RiskEvaluationService.RiskOutcome outcome = riskEvaluationService.evaluate(
                request.amount(),
                request.currency()
        );

        Payment payment = new Payment(
                request.idempotencyKey(),
                request.amount(),
                request.currency(),
                outcome.status(),
                outcome.score(),
                outcome.reason(),
                Instant.now()
        );
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getIdempotencyKey(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getRiskScore(),
                p.getRiskReason(),
                p.getCreatedAt()
        );
    }
}
