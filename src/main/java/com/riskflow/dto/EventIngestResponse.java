package com.riskflow.dto;

import com.riskflow.model.DecisionType;
import com.riskflow.model.TransactionStatus;

import java.util.UUID;

public record EventIngestResponse(
        UUID transactionPk,
        String transactionId,
        UUID paymentEventId,
        DecisionType decision,
        String triggeredRules,
        String reason,
        TransactionStatus transactionStatus
) {
}
