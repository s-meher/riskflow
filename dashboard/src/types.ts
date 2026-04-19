export interface MetricsSummary {
  totalTransactions: number
  approvedCount: number
  flaggedCount: number
  rejectedCount: number
  failedPaymentsCount: number
}

export interface TransactionRow {
  id: string
  transactionId: string
  idempotencyKey: string
  userId: string
  amount: number
  currency: string
  paymentMethod: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface EventIngestPayload {
  transactionId: string
  userId: string
  amount: number
  currency: string
  paymentMethod: string
  eventType: string
  status: string
  eventTimestamp: string
  reason?: string
}

export interface EventIngestResult {
  transactionPk: string
  transactionId: string
  paymentEventId: string
  decision: string
  triggeredRules: string
  reason: string
  transactionStatus: string
}
