package com.riskflow.service;

import com.riskflow.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskEvaluationService {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("10000.00");

    public record RiskOutcome(int score, String reason, PaymentStatus status) {
    }

    /**
     * Deterministic placeholder rules for an MVP risk pass.
     */
    public RiskOutcome evaluate(BigDecimal amount, String currency) {
        int score = 10;
        String reason;

        boolean highAmount = amount.compareTo(HIGH_AMOUNT) > 0;
        boolean nonUsd = !"USD".equals(currency);

        if (highAmount && nonUsd) {
            score += 55;
            reason = "amount_above_threshold,non_usd_currency";
        } else if (highAmount) {
            score += 40;
            reason = "amount_above_threshold";
        } else if (nonUsd) {
            score += 15;
            reason = "non_usd_currency";
        } else {
            reason = "baseline";
        }

        PaymentStatus status;
        if (score >= 60) {
            status = PaymentStatus.DECLINED;
        } else if (score >= 40) {
            status = PaymentStatus.MANUAL_REVIEW;
        } else {
            status = PaymentStatus.APPROVED;
        }

        return new RiskOutcome(Math.min(score, 100), reason, status);
    }
}
