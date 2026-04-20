import type { PaymentEventRow } from '../types'

function fmtMoney(n: number, ccy: string) {
  try {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: ccy }).format(n)
  } catch {
    return `${n} ${ccy}`
  }
}

function fmtTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

function pillClass(status: string) {
  const s = status.toLowerCase()
  if (s === 'failed') return 'pill pill--failed'
  if (s === 'completed') return 'pill pill--approved'
  if (s === 'pending') return 'pill pill--pending'
  return 'pill'
}

export function LatestEvents({ rows }: { rows: PaymentEventRow[] | null }) {
  if (!rows) {
    return (
      <div className="card table-card">
        <div className="section-head">
          <h2>Latest events</h2>
          <p className="muted">GET /api/events</p>
        </div>
        <div className="empty muted">Loading…</div>
      </div>
    )
  }

  const top = rows.slice(0, 10)

  return (
    <div className="card table-card">
      <div className="section-head">
        <h2>Latest events</h2>
        <p className="muted">Last {top.length} events</p>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>When</th>
              <th>Transaction</th>
              <th>User</th>
              <th>Type</th>
              <th>Status</th>
              <th className="td-right">Amount</th>
            </tr>
          </thead>
          <tbody>
            {top.length === 0 ? (
              <tr>
                <td colSpan={6} className="muted empty">
                  No events yet. Submit an event above to generate activity.
                </td>
              </tr>
            ) : (
              top.map((e) => (
                <tr key={e.id}>
                  <td className="muted small td-nowrap">{fmtTime(e.eventTimestamp)}</td>
                  <td className="mono">{e.transactionId}</td>
                  <td>{e.userId}</td>
                  <td className="td-nowrap">{e.eventType}</td>
                  <td>
                    <span className={pillClass(e.status)}>{e.status}</span>
                  </td>
                  <td className="td-right td-nowrap">{fmtMoney(e.amount, e.currency)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

