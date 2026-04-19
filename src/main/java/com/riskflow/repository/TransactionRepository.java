package com.riskflow.repository;

import com.riskflow.model.Transaction;
import com.riskflow.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Transaction> findAllByOrderByCreatedAtDesc();

    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);

    long countByStatus(TransactionStatus status);
}
