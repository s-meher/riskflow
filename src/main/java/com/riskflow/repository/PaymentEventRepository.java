package com.riskflow.repository;

import com.riskflow.model.PaymentEvent;
import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.PaymentEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {

    List<PaymentEvent> findByTransaction_IdOrderByEventTimestampDesc(UUID transactionId);

    Optional<PaymentEvent> findFirstByTransaction_IdOrderByEventTimestampDesc(UUID transactionId);

    Optional<PaymentEvent> findFirstByTransaction_IdOrderByCreatedAtAsc(UUID transactionId);

    /**
     * Failed events for this user in the lookback window (excludes the row not yet inserted).
     */
    @Query("""
            SELECT COUNT(e) FROM PaymentEvent e
            JOIN e.transaction t
            WHERE t.userId = :userId
            AND e.status = :failed
            AND e.eventTimestamp >= :since
            """)
    long countFailedEventsForUserSince(
            @Param("userId") String userId,
            @Param("failed") PaymentEventStatus failed,
            @Param("since") Instant since
    );

    List<PaymentEvent> findAllByOrderByEventTimestampDesc();

    long countByStatus(PaymentEventStatus status);

    Optional<PaymentEvent> findByTransaction_TransactionIdAndEventTypeAndEventTimestamp(
            String transactionId,
            PaymentEventType eventType,
            Instant eventTimestamp
    );
}
