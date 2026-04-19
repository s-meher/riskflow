import { useCallback, useEffect, useState } from 'react'
import { fetchFlagged, fetchMetrics, fetchTransactions } from './api'
import type { MetricsSummary, TransactionRow } from './types'
import { EventForm } from './components/EventForm'
import { FlaggedTable } from './components/FlaggedTable'
import { MetricsCards } from './components/MetricsCards'
import { TransactionsTable } from './components/TransactionsTable'
import './App.css'

export default function App() {
  const [metrics, setMetrics] = useState<MetricsSummary | null>(null)
  const [transactions, setTransactions] = useState<TransactionRow[] | null>(null)
  const [flagged, setFlagged] = useState<TransactionRow[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  const loadAll = useCallback(async () => {
    setError(null)
    try {
      const [m, t, f] = await Promise.all([fetchMetrics(), fetchTransactions(), fetchFlagged()])
      setMetrics(m)
      setTransactions(t)
      setFlagged(f)
    } catch (e) {
      setMetrics(null)
      setTransactions(null)
      setFlagged(null)
      setError(e instanceof Error ? e.message : 'Could not load data')
    }
  }, [])

  useEffect(() => {
    void loadAll()
  }, [loadAll])

  return (
    <div className="dash">
      <header className="topbar">
        <div>
          <h1>RiskFlow</h1>
          <p className="muted">Internal ops · payments & risk</p>
        </div>
        <button type="button" className="btn" onClick={() => void loadAll()}>
          Refresh
        </button>
      </header>

      {error ? (
        <div className="banner banner--err banner--wide">
          <strong>API error.</strong> {error} — is Spring Boot running on{' '}
          <code className="mono">localhost:8080</code>?
        </div>
      ) : null}

      <section className="block">
        <h2 className="block-title">Metrics</h2>
        <MetricsCards data={metrics} />
      </section>

      <section className="block">
        <EventForm onSubmitted={() => void loadAll()} />
      </section>

      <section className="grid-2">
        <TransactionsTable rows={transactions} />
        <FlaggedTable rows={flagged} />
      </section>

      <footer className="footer muted">
        Vite dev server proxies <code>/api</code> → Spring Boot. CORS enabled for direct API calls.
      </footer>
    </div>
  )
}
