package com.riskflow.dto;

import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.PaymentEventType;
import com.riskflow.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEventIngestRequest(
        @NotBlank @Size(max = 64) String transactionId,
        @NotBlank @Size(max = 64) String userId,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code") String currency,
        @NotNull PaymentMethod paymentMethod,
        @NotNull PaymentEventType eventType,
        @NotNull PaymentEventStatus status,
        @NotNull Instant eventTimestamp,
        @Size(max = 512) String reason
) {
}
