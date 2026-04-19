package com.riskflow.service;

import com.riskflow.dto.MetricsSummaryResponse;
import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.TransactionStatus;
import com.riskflow.repository.PaymentEventRepository;
import com.riskflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService {

    private final TransactionRepository transactionRepository;
    private final PaymentEventRepository paymentEventRepository;

    public MetricsService(
            TransactionRepository transactionRepository,
            PaymentEventRepository paymentEventRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.paymentEventRepository = paymentEventRepository;
    }

    @Transactional(readOnly = true)
    public MetricsSummaryResponse summary() {
        long total = transactionRepository.count();
        long approved = transactionRepository.countByStatus(TransactionStatus.APPROVED);
        long flagged = transactionRepository.countByStatus(TransactionStatus.MANUAL_REVIEW);
        long rejected = transactionRepository.countByStatus(TransactionStatus.DECLINED);
        long failedPayments = paymentEventRepository.countByStatus(PaymentEventStatus.FAILED);

        return new MetricsSummaryResponse(total, approved, flagged, rejected, failedPayments);
    }
}
