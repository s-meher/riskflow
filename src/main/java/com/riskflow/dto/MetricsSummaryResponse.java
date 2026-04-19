package com.riskflow.dto;

public record MetricsSummaryResponse(
        long totalTransactions,
        long approvedCount,
        long flaggedCount,
        long rejectedCount,
        long failedPaymentsCount
) {
}
