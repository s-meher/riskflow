package com.riskflow.service;

import com.riskflow.model.TransactionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskEvaluationService {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("10000.00");

    public record RiskOutcome(int score, String triggeredRule, String reason, TransactionStatus status) {
    }

    /**
     * Deterministic placeholder rules for an MVP risk pass.
     */
    public RiskOutcome evaluate(BigDecimal amount, String currency) {
        int score = 10;
        String triggeredRule;
        String reason;

        boolean highAmount = amount.compareTo(HIGH_AMOUNT) > 0;
        boolean nonUsd = !"USD".equals(currency);

        if (highAmount && nonUsd) {
            score += 55;
            triggeredRule = "HIGH_AMOUNT_AND_NON_USD";
            reason = "Amount above threshold and non-USD currency.";
        } else if (highAmount) {
            score += 40;
            triggeredRule = "HIGH_AMOUNT";
            reason = "Amount above threshold.";
        } else if (nonUsd) {
            score += 15;
            triggeredRule = "NON_USD";
            reason = "Non-USD currency.";
        } else {
            triggeredRule = "BASELINE";
            reason = "No elevated rules triggered.";
        }

        TransactionStatus status;
        if (score >= 60) {
            status = TransactionStatus.DECLINED;
        } else if (score >= 40) {
            status = TransactionStatus.MANUAL_REVIEW;
        } else {
            status = TransactionStatus.APPROVED;
        }

        return new RiskOutcome(Math.min(score, 100), triggeredRule, reason, status);
    }
}
