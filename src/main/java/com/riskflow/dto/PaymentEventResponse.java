package com.riskflow.dto;

import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.PaymentEventType;
import com.riskflow.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEventResponse(
        UUID id,
        String transactionId,
        String userId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentEventType eventType,
        PaymentEventStatus status,
        String reason,
        Instant eventTimestamp,
        Instant createdAt,
        Instant updatedAt
) {
}
