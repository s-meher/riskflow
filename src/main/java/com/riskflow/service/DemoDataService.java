package com.riskflow.service;

import com.riskflow.dto.DemoSeedResult;
import com.riskflow.dto.PaymentEventIngestRequest;
import com.riskflow.model.PaymentEventStatus;
import com.riskflow.model.PaymentEventType;
import com.riskflow.model.PaymentMethod;
import com.riskflow.repository.PaymentEventRepository;
import com.riskflow.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Small, deterministic demo dataset for local runs.
 *
 * Seeding is opt-in (see {@code riskflow.demo.seed}) and safe by default: it will not run if data exists
 * unless {@code riskflow.demo.force=true}.
 */
@Service
public class DemoDataService {

    private final EventIngestionService eventIngestionService;
    private final TransactionRepository transactionRepository;
    private final PaymentEventRepository paymentEventRepository;

    private final boolean force;

    public DemoDataService(
            EventIngestionService eventIngestionService,
            TransactionRepository transactionRepository,
            PaymentEventRepository paymentEventRepository,
            @Value("${riskflow.demo.force:false}") boolean force
    ) {
        this.eventIngestionService = eventIngestionService;
        this.transactionRepository = transactionRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.force = force;
    }

    @Transactional
    public DemoSeedResult seedDemoData() {
        long existingTx = transactionRepository.count();
        List<String> notes = new ArrayList<>();
        if (existingTx > 0 && !force) {
            notes.add("Existing data detected (" + existingTx + " transactions). Seeding will append demo rows idempotently.");
        }
        if (force) {
            notes.add("riskflow.demo.force=true is set. Note: this does not wipe data; it only bypasses safety checks.");
        }

        Instant base = Instant.now().minusSeconds(60 * 30); // 30 minutes ago

        List<PaymentEventIngestRequest> events = new ArrayList<>();

        // 1) Approved: small USD amount
        events.add(new PaymentEventIngestRequest(
                "txn-1001",
                "user-a",
                new BigDecimal("49.99"),
                "USD",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                base.plusSeconds(10),
                "approved_small_purchase"
        ));

        // 2) Flagged: HIGH_AMOUNT (> 1000) -> manual review
        events.add(new PaymentEventIngestRequest(
                "txn-2001",
                "user-b",
                new BigDecimal("1250.00"),
                "USD",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                base.plusSeconds(30),
                "high_amount_checkout"
        ));

        // 3) Rejected: unsupported currency -> decline
        events.add(new PaymentEventIngestRequest(
                "txn-3001",
                "user-c",
                new BigDecimal("75.00"),
                "NGN",
                PaymentMethod.WALLET,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                base.plusSeconds(50),
                "unsupported_currency_example"
        ));

        // 4) Repeated failures: 3 failed payment events for the same user within the lookback window.
        //    The third one should trigger REPEATED_FAILURES.
        events.add(new PaymentEventIngestRequest(
                "txn-4001",
                "user-d",
                new BigDecimal("19.00"),
                "USD",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.FAILED,
                base.plusSeconds(70),
                "issuer_declined"
        ));
        events.add(new PaymentEventIngestRequest(
                "txn-4002",
                "user-d",
                new BigDecimal("21.00"),
                "USD",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.FAILED,
                base.plusSeconds(90),
                "insufficient_funds"
        ));
        events.add(new PaymentEventIngestRequest(
                "txn-4003",
                "user-d",
                new BigDecimal("23.00"),
                "USD",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.FAILED,
                base.plusSeconds(110),
                "velocity_check"
        ));

        // 5) Supported currency example: INR transaction (should approve unless other rules hit)
        events.add(new PaymentEventIngestRequest(
                "txn-5001",
                "user-e",
                new BigDecimal("199.00"),
                "INR",
                PaymentMethod.ACH,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                base.plusSeconds(130),
                "inr_supported_currency"
        ));

        // 6) Another flagged: high amount in EUR (still supported currency, but high amount triggers review)
        events.add(new PaymentEventIngestRequest(
                "txn-6001",
                "user-f",
                new BigDecimal("2500.00"),
                "EUR",
                PaymentMethod.CARD,
                PaymentEventType.PAYMENT_SUBMITTED,
                PaymentEventStatus.COMPLETED,
                base.plusSeconds(150),
                "high_amount_eur"
        ));

        int attempted = events.size();
        int created = 0;

        for (PaymentEventIngestRequest e : events) {
            long before = paymentEventRepository.count();
            eventIngestionService.ingest(e);
            long after = paymentEventRepository.count();
            if (after > before) {
                created++;
            } else {
                notes.add("Duplicate skipped: " + e.transactionId() + " " + e.eventType() + " " + e.eventTimestamp());
            }
        }

        return new DemoSeedResult(
                true,
                attempted,
                created,
                (int) transactionRepository.count(),
                notes
        );
    }
}

