import type { MetricsSummary } from '../types'

const items: { key: keyof MetricsSummary; label: string }[] = [
  { key: 'totalTransactions', label: 'Transactions' },
  { key: 'approvedCount', label: 'Approved' },
  { key: 'flaggedCount', label: 'Flagged (review)' },
  { key: 'rejectedCount', label: 'Rejected' },
  { key: 'failedPaymentsCount', label: 'Failed events' },
]

export function MetricsCards({ data }: { data: MetricsSummary | null }) {
  if (!data) {
    return (
      <div className="metrics metrics--placeholder">
        {items.map(({ key }) => (
          <div key={key} className="card card--skeleton" aria-hidden>
            <span className="skeleton-line" />
            <span className="skeleton-line short" />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="metrics">
      {items.map(({ key, label }) => (
        <div key={key} className="card metric-card">
          <div className="metric-label">{label}</div>
          <div className="metric-value">{data[key]}</div>
        </div>
      ))}
    </div>
  )
}
