package com.riskflow.dto;

import com.riskflow.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        int riskScore,
        String riskReason,
        Instant createdAt
) {
}
