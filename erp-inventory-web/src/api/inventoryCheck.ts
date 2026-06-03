import request from './request'

export interface InventoryCheckItem {
  id?: number
  checkId?: number
  productId: number | null
  productName?: string
  sku?: string
  systemStock?: number
  actualStock: number | null
  difference?: number | null
  reasonType?: number | null
  remark?: string
  createTime?: string
}

export interface InventoryCheck {
  id?: number
  checkNo?: string
  checkDate: string
  checkMode: number
  scopeName: string
  blindCheck: number
  status?: number
  remark?: string
  items: InventoryCheckItem[]
  totalItems?: number
  differenceItems?: number
  profitQuantity?: number
  lossQuantity?: number
  netChangeQuantity?: number
  createTime?: string
  updateTime?: string
  snapshotTime?: string
  reviewTime?: string
}

export interface InventoryDistributionItem {
  key: number
  label: string
  value: number
}

export interface InventoryTrendPoint {
  date: string
  label: string
  inbound: number
  outbound: number
  checkGain: number
  checkLoss: number
  netChange: number
}

export interface InventoryTransactionRecord {
  id?: number
  transactionNo?: string
  productId?: number
  productName?: string
  sku?: string
  transactionType?: number
  quantity?: number
  beforeStock?: number
  afterStock?: number
  relatedId?: number
  relatedNo?: string
  operatorId?: number
  operatorName?: string
  remark?: string
  createTime?: string
}

export interface InventoryCheckDashboard {
  summary: {
    totalChecks: number
    pendingChecks: number
    approvedChecks: number
    differenceChecks: number
    totalItems: number
    differenceItems: number
    pendingDifferenceItems: number
    confirmedProfitQuantity: number
    confirmedLossQuantity: number
    confirmedNetChangeQuantity: number
    differenceRate: number
  }
  modeDistribution: InventoryDistributionItem[]
  reasonDistribution: InventoryDistributionItem[]
  trend: InventoryTrendPoint[]
  recentTransactions: InventoryTransactionRecord[]
}

export interface InventoryCheckAnalysis {
  check: InventoryCheck
  overview: {
    checkNo: string
    checkDate: string
    status: number
    checkMode: number
    scopeName: string
    blindCheck: number
    snapshotTime?: string
    reviewTime?: string
    totalItems: number
    differenceItems: number
    profitQuantity: number
    lossQuantity: number
    netChangeQuantity: number
    differenceRate: number
  }
  reasonDistribution: InventoryDistributionItem[]
  differenceRanking: Array<{
    productId: number
    productName: string
    sku: string
    systemStock: number
    actualStock: number
    difference: number
    reasonType?: number | null
    reasonText?: string
    remark?: string
  }>
  relatedTransactions: InventoryTransactionRecord[]
}

export const inventoryCheckApi = {
  page(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get('/inventory-check/page', { params })
  },

  dashboard() {
    return request.get('/inventory-check/dashboard')
  },

  getById(id: number) {
    return request.get(`/inventory-check/${id}`)
  },

  analysis(id: number) {
    return request.get(`/inventory-check/${id}/analysis`)
  },

  create(data: InventoryCheck) {
    return request.post('/inventory-check', data)
  },

  update(data: InventoryCheck) {
    return request.put('/inventory-check', data)
  },

  submit(id: number) {
    return request.put(`/inventory-check/${id}/submit`)
  },

  audit(id: number) {
    return request.put(`/inventory-check/${id}/audit`)
  },

  cancel(id: number) {
    return request.put(`/inventory-check/${id}/cancel`)
  },

  delete(id: number) {
    return request.delete(`/inventory-check/${id}`)
  }
}
