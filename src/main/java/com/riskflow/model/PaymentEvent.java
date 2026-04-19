package com.riskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "payment_events")
public class PaymentEvent extends AuditedEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_pk", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 48)
    private PaymentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentEventStatus status;

    @Column(length = 512)
    private String reason;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    protected PaymentEvent() {
    }

    public PaymentEvent(
            Transaction transaction,
            PaymentEventType eventType,
            PaymentEventStatus status,
            String reason,
            Instant eventTimestamp,
            String rawPayload
    ) {
        this.transaction = transaction;
        this.eventType = eventType;
        this.status = status;
        this.reason = reason;
        this.eventTimestamp = eventTimestamp;
        this.rawPayload = rawPayload != null ? rawPayload : "{}";
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public PaymentEventType getEventType() {
        return eventType;
    }

    public PaymentEventStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getRawPayload() {
        return rawPayload;
    }
}
