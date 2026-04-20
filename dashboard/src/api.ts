import type { EventIngestPayload, EventIngestResult, MetricsSummary, PaymentEventRow, TransactionRow } from './types'

const prefix = import.meta.env.VITE_API_BASE ?? ''

async function parseJson<T>(res: Response): Promise<T> {
  const text = await res.text()
  if (!text) return undefined as T
  return JSON.parse(text) as T
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (init?.body != null) {
    headers['Content-Type'] = 'application/json'
  }
  const res = await fetch(`${prefix}${path}`, {
    ...init,
    headers: { ...headers, ...init?.headers },
  })
  if (!res.ok) {
    let detail = res.statusText
    try {
      const err = await res.json()
      detail = err.message ?? JSON.stringify(err)
    } catch {
      detail = await res.text().catch(() => detail)
    }
    throw new Error(detail || `HTTP ${res.status}`)
  }
  return parseJson<T>(res)
}

export function fetchMetrics(): Promise<MetricsSummary> {
  return request<MetricsSummary>('/api/metrics/summary')
}

export function fetchTransactions(): Promise<TransactionRow[]> {
  return request<TransactionRow[]>('/api/transactions')
}

export function fetchFlagged(): Promise<TransactionRow[]> {
  return request<TransactionRow[]>('/api/transactions/flagged')
}

export function fetchEvents(): Promise<PaymentEventRow[]> {
  return request<PaymentEventRow[]>('/api/events')
}

export function submitEvent(payload: EventIngestPayload): Promise<EventIngestResult> {
  const body = JSON.stringify(payload)
  return request<EventIngestResult>('/api/events', { method: 'POST', body })
}
