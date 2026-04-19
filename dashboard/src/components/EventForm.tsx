import { useMemo, useState } from 'react'
import { submitEvent } from '../api'
import type { EventIngestPayload } from '../types'

const PAYMENT_METHODS = ['CARD', 'ACH', 'WALLET', 'OTHER'] as const
const EVENT_TYPES = ['PAYMENT_SUBMITTED', 'RISK_EVALUATED', 'SETTLEMENT_INITIATED'] as const
const EVENT_STATUSES = ['PENDING', 'COMPLETED', 'FAILED'] as const

function defaultPayload(): EventIngestPayload {
  const id = `demo-${crypto.randomUUID().slice(0, 8)}`
  return {
    transactionId: id,
    userId: 'demo-user',
    amount: 50,
    currency: 'USD',
    paymentMethod: 'CARD',
    eventType: 'PAYMENT_SUBMITTED',
    status: 'COMPLETED',
    eventTimestamp: new Date().toISOString(),
    reason: '',
  }
}

export function EventForm({
  onSubmitted,
}: {
  onSubmitted: () => void
}) {
  const initial = useMemo(() => defaultPayload(), [])
  const [form, setForm] = useState<EventIngestPayload>(initial)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<{ type: 'ok' | 'err'; text: string } | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setMessage(null)
    const payload: EventIngestPayload = {
      ...form,
      reason: form.reason?.trim() ? form.reason.trim() : undefined,
    }
    try {
      const res = await submitEvent(payload)
      setMessage({
        type: 'ok',
        text: `Stored event · decision ${res.decision} · rules: ${res.triggeredRules}`,
      })
      onSubmitted()
      setForm(defaultPayload())
    } catch (err) {
      setMessage({
        type: 'err',
        text: err instanceof Error ? err.message : 'Request failed',
      })
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card form-card" onSubmit={handleSubmit}>
      <div className="section-head">
        <h2>Submit payment event</h2>
        <p className="muted">POST /api/events — drives risk rules and transaction upsert.</p>
      </div>

      <div className="form-grid">
        <label>
          Transaction ID
          <input
            required
            value={form.transactionId}
            onChange={(e) => setForm({ ...form, transactionId: e.target.value })}
          />
        </label>
        <label>
          User ID
          <input
            required
            value={form.userId}
            onChange={(e) => setForm({ ...form, userId: e.target.value })}
          />
        </label>
        <label>
          Amount
          <input
            required
            type="number"
            step="0.01"
            min={0}
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: Number(e.target.value) })}
          />
        </label>
        <label>
          Currency (ISO)
          <input
            required
            maxLength={3}
            pattern="[A-Z]{3}"
            title="Three uppercase letters, e.g. USD"
            value={form.currency}
            onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })}
          />
        </label>
        <label>
          Payment method
          <select
            value={form.paymentMethod}
            onChange={(e) => setForm({ ...form, paymentMethod: e.target.value })}
          >
            {PAYMENT_METHODS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </label>
        <label>
          Event type
          <select
            value={form.eventType}
            onChange={(e) => setForm({ ...form, eventType: e.target.value })}
          >
            {EVENT_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
        <label>
          Event status
          <select
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value })}
          >
            {EVENT_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </label>
        <label className="span-2">
          Event timestamp (ISO-8601)
          <input
            required
            value={form.eventTimestamp}
            onChange={(e) => setForm({ ...form, eventTimestamp: e.target.value })}
          />
        </label>
        <label className="span-2">
          Reason <span className="muted">(optional)</span>
          <input
            value={form.reason ?? ''}
            onChange={(e) => setForm({ ...form, reason: e.target.value })}
            placeholder="e.g. issuer_declined"
          />
        </label>
      </div>

      {message ? (
        <p className={message.type === 'ok' ? 'banner banner--ok' : 'banner banner--err'}>{message.text}</p>
      ) : null}

      <div className="form-actions">
        <button type="submit" className="btn btn--primary" disabled={busy}>
          {busy ? 'Submitting…' : 'Submit event'}
        </button>
      </div>
    </form>
  )
}
