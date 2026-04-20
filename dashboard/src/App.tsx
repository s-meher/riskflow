import { useCallback, useEffect, useState } from 'react'
import { fetchEvents, fetchFlagged, fetchMetrics, fetchTransactions } from './api'
import type { MetricsSummary, PaymentEventRow, TransactionRow } from './types'
import { EventForm } from './components/EventForm'
import { FlaggedTable } from './components/FlaggedTable'
import { LatestEvents } from './components/LatestEvents'
import { MetricsCards } from './components/MetricsCards'
import { TransactionsTable } from './components/TransactionsTable'
import './App.css'

type TxStatusFilter = 'ALL' | 'APPROVED' | 'MANUAL_REVIEW' | 'DECLINED' | 'PENDING'

export default function App() {
  const [metrics, setMetrics] = useState<MetricsSummary | null>(null)
  const [transactions, setTransactions] = useState<TransactionRow[] | null>(null)
  const [flagged, setFlagged] = useState<TransactionRow[] | null>(null)
  const [events, setEvents] = useState<PaymentEventRow[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [lastRefreshedAt, setLastRefreshedAt] = useState<Date | null>(null)
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<TxStatusFilter>('ALL')

  const loadAll = useCallback(async () => {
    setError(null)
    try {
      const [m, t, f, e] = await Promise.all([fetchMetrics(), fetchTransactions(), fetchFlagged(), fetchEvents()])
      setMetrics(m)
      setTransactions(t)
      setFlagged(f)
      setEvents(e)
      setLastRefreshedAt(new Date())
    } catch (e) {
      setMetrics(null)
      setTransactions(null)
      setFlagged(null)
      setEvents(null)
      setError(e instanceof Error ? e.message : 'Could not load data')
    }
  }, [])

  useEffect(() => {
    void loadAll()
  }, [loadAll])

  const normalizedQuery = query.trim().toLowerCase()
  const filteredTransactions =
    transactions == null
      ? null
      : transactions.filter((t) => {
          const matchesQuery =
            !normalizedQuery ||
            t.transactionId.toLowerCase().includes(normalizedQuery) ||
            t.userId.toLowerCase().includes(normalizedQuery)
          const matchesStatus = status === 'ALL' || t.status === status
          return matchesQuery && matchesStatus
        })

  return (
    <div className="dash">
      <header className="topbar">
        <div>
          <h1>RiskFlow</h1>
          <p className="muted">Internal ops · payments & risk</p>
        </div>
        <div className="topbar-actions">
          <span className="chip" title={lastRefreshedAt ? lastRefreshedAt.toISOString() : 'Not loaded yet'}>
            <span className={error ? 'dot' : 'dot ok'} />
            {lastRefreshedAt ? `Updated ${lastRefreshedAt.toLocaleTimeString()}` : 'Loading…'}
          </span>
          <button type="button" className="btn" onClick={() => void loadAll()}>
            Refresh
          </button>
        </div>
      </header>

      {error ? (
        <div className="banner banner--err banner--wide">
          <strong>API error.</strong> {error} — is Spring Boot running on{' '}
          <code className="mono">localhost:8080</code>?
        </div>
      ) : null}

      <section className="block">
        <div className="block-title-row">
          <h2 className="block-title">Metrics</h2>
          <p className="muted">Quick health snapshot</p>
        </div>
        <MetricsCards data={metrics} />
      </section>

      <section className="block">
        <EventForm onSubmitted={() => void loadAll()} />
      </section>

      <section className="block">
        <LatestEvents rows={events} />
      </section>

      <section className="block">
        <div className="controls card">
          <div className="controls-row">
            <div className="control">
              <label className="control-label" htmlFor="q">
                Search
              </label>
              <input
                id="q"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="transactionId or userId…"
              />
            </div>
            <div className="control">
              <label className="control-label" htmlFor="status">
                Status
              </label>
              <select id="status" value={status} onChange={(e) => setStatus(e.target.value as TxStatusFilter)}>
                <option value="ALL">All</option>
                <option value="APPROVED">Approved</option>
                <option value="MANUAL_REVIEW">Flagged</option>
                <option value="DECLINED">Rejected</option>
                <option value="PENDING">Pending</option>
              </select>
            </div>
            <div className="controls-actions">
              <button type="button" className="btn" onClick={() => setQuery('')} disabled={!query}>
                Clear
              </button>
              <button type="button" className="btn" onClick={() => void loadAll()}>
                Refresh all
              </button>
            </div>
          </div>
          <p className="muted controls-foot">
            Showing {filteredTransactions?.length ?? 0} / {transactions?.length ?? 0} transactions
          </p>
        </div>
      </section>

      <section className="grid-2">
        <TransactionsTable rows={filteredTransactions} />
        <FlaggedTable rows={flagged} />
      </section>

      <footer className="footer muted">
        Vite dev server proxies <code>/api</code> → Spring Boot. CORS enabled for direct API calls.
      </footer>
    </div>
  )
}
