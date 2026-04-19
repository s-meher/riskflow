package com.riskflow.repository;

import com.riskflow.model.RiskDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskDecisionRepository extends JpaRepository<RiskDecision, UUID> {

    List<RiskDecision> findByTransaction_IdOrderByCreatedAtDesc(UUID transactionId);

    Optional<RiskDecision> findFirstByTransaction_IdOrderByCreatedAtDesc(UUID transactionId);
}
