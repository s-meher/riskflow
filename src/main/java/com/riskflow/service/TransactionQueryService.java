package com.riskflow.service;

import com.riskflow.dto.TransactionResponse;
import com.riskflow.model.Transaction;
import com.riskflow.model.TransactionStatus;
import com.riskflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public TransactionQueryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listAll() {
        return transactionRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Transactions flagged for manual review (risk pipeline review queue).
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> listFlagged() {
        return transactionRepository.findByStatusOrderByUpdatedAtDesc(TransactionStatus.MANUAL_REVIEW).stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionId(),
                tx.getIdempotencyKey(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getPaymentMethod(),
                tx.getStatus(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }
}
