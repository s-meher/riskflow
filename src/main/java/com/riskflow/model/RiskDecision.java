package com.riskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "risk_decisions")
public class RiskDecision extends AuditedEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_pk", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DecisionType decision;

    @Column(name = "triggered_rule", nullable = false, length = 128)
    private String triggeredRule;

    @Column(nullable = false, length = 512)
    private String reason;

    protected RiskDecision() {
    }

    public RiskDecision(
            Transaction transaction,
            DecisionType decision,
            String triggeredRule,
            String reason
    ) {
        this.transaction = transaction;
        this.decision = decision;
        this.triggeredRule = triggeredRule;
        this.reason = reason;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public String getTriggeredRule() {
        return triggeredRule;
    }

    public String getReason() {
        return reason;
    }
}
