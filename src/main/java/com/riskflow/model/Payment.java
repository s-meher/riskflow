package com.riskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(nullable = false)
    private int riskScore;

    @Column(nullable = false)
    private String riskReason;

    @Column(nullable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public Payment(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            int riskScore,
            String riskReason,
            Instant createdAt
    ) {
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.riskScore = riskScore;
        this.riskReason = riskReason;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
