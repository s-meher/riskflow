package com.riskflow.dto;

import com.riskflow.model.PaymentMethod;
import com.riskflow.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String transactionId,
        String idempotencyKey,
        String userId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        TransactionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
