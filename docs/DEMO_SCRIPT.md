# RiskFlow — recruiter demo script (~5 minutes)

**You say:** “RiskFlow is a small event-driven payment pipeline: we ingest payment events, persist them, run rule-based risk checks, store decisions in Postgres, and expose metrics plus an internal ops UI.”

**Prereqs running:** Spring Boot on `:8080`, dashboard on `:5173` (see root `README.md`). Optional: start backend with demo seed for instant numbers.

---

## 1) Dashboard landing view

**Open:** `http://localhost:5173`

**You say:** “This is an internal ops dashboard—not a consumer app. It’s meant to show how operators would watch volume, submit or replay events, and inspect what the backend decided.”

**They should see:** header, **Metrics** row, **Submit payment event** form, **Latest events**, **Search / filter** for transactions, **Transactions** + **Flagged queue**.

---

## 2) Current metrics

**Point at the metric cards.**

**You say:** “These counters are read models over Postgres: total transactions, approvals, items in manual review, rejections, and failed payment events. They’re cheap aggregates you’d wire to alerts or a BI tool later.”

**Optional click:** **Refresh all** (or top **Refresh**) to show data is live.

---

## 3) Submitting a sample payment event

**In the form:** click **Load sample → Approved**, then **Submit event**.

**You say:** “The UI POSTs JSON to `/api/events`. The backend validates the payload, stores the raw event, runs risk rules, writes a `RiskDecision`, and upserts the `Transaction` aggregate.”

**They should see:** a green success banner (decision + triggered rules) and **Latest events** / **Transactions** updating after refresh (auto-refresh happens on submit in the app).

**You say (pipeline angle):** “Think ingestion → persistence → rules → state update. That’s the same backbone as larger systems, just without queues and microservices.”

---

## 4) How a risky event gets flagged

**Click:** **Load sample → High amount**, then **Submit event**.

**You say:** “This crosses a simple threshold rule: amount over 1000. The decision becomes review, and the transaction moves to manual review—what an ops team would queue for a human.”

**Show:** **Flagged queue** gains/updates a row; metrics **Flagged** increments.

---

## 5) How transactions and flagged items update

**Scroll to:** **Transactions** + **Flagged queue**.

**You say:** “Transactions are the aggregate state; events are the append-style log. The UI reads `/api/transactions` and `/api/transactions/flagged` so you can separate ‘everything’ from ‘needs attention’.”

**Try:** search by `transactionId` or `userId`, filter status to **Approved** vs **Flagged**.

**You say:** “This is the same pattern as operational read APIs in payments: slice the world by risk posture.”

---

## 6) Why this demonstrates backend + data pipeline thinking

**Close with:**

- **Ingestion contract:** stable JSON + validation errors that are readable.
- **Persistence:** raw payload stored (`PaymentEvent.rawPayload`) for audit/debug.
- **Rules engine (MVP):** deterministic, explainable rules with explicit outcomes.
- **Storage model:** normalized entities (`Transaction`, `PaymentEvent`, `RiskDecision`) instead of one mystery table.
- **Observability:** metrics endpoint + simple UI for demos.

**One-liner:** “It’s a thin vertical slice of what payment platforms do—events in, decisions out, Postgres as source of truth, and ops visibility on top.”

---

## Sample payloads (`POST /api/events`)

Use `Content-Type: application/json`. Adjust `eventTimestamp` if you re-run the same payload (idempotency is `transactionId + eventType + eventTimestamp`).

### A) Approved (small USD)

```json
{
  "transactionId": "demo-approved-001",
  "userId": "user-alice",
  "amount": 49.99,
  "currency": "USD",
  "paymentMethod": "CARD",
  "eventType": "PAYMENT_SUBMITTED",
  "status": "COMPLETED",
  "eventTimestamp": "2026-04-19T15:00:00Z",
  "reason": "checkout_completed"
}
```

### B) High amount → flagged / manual review

```json
{
  "transactionId": "demo-flagged-001",
  "userId": "user-bob",
  "amount": 1250.0,
  "currency": "USD",
  "paymentMethod": "CARD",
  "eventType": "PAYMENT_SUBMITTED",
  "status": "COMPLETED",
  "eventTimestamp": "2026-04-19T15:05:00Z",
  "reason": "large_purchase"
}
```

### C) Unsupported currency → rejected

```json
{
  "transactionId": "demo-rejected-001",
  "userId": "user-chen",
  "amount": 75.0,
  "currency": "NGN",
  "paymentMethod": "WALLET",
  "eventType": "PAYMENT_SUBMITTED",
  "status": "COMPLETED",
  "eventTimestamp": "2026-04-19T15:10:00Z",
  "reason": "intl_wallet_attempt"
}
```

**curl (optional):**

```bash
curl -sS -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d @payload.json
```
