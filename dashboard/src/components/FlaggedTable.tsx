import type { TransactionRow } from '../types'

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

export function FlaggedTable({ rows }: { rows: TransactionRow[] | null }) {
  if (!rows) {
    return (
      <div className="card table-card">
        <div className="section-head">
          <h2>Flagged queue</h2>
        </div>
        <p className="muted">Loading…</p>
      </div>
    )
  }

  return (
    <div className="card table-card table-card--accent">
      <div className="section-head">
        <h2>Flagged queue</h2>
        <p className="muted">GET /api/transactions/flagged · manual review ({rows.length})</p>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Transaction ID</th>
              <th>User</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={5} className="muted">
                  No flagged transactions.
                </td>
              </tr>
            ) : (
              rows.map((r) => (
                <tr key={r.id}>
                  <td className="mono">{r.transactionId}</td>
                  <td>{r.userId}</td>
                  <td>{fmtMoney(r.amount, r.currency)}</td>
                  <td>
                    <span className="pill pill--manual_review">{r.status}</span>
                  </td>
                  <td className="muted small">{fmtTime(r.updatedAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
