import request from './request'

export interface ProductWarningMessage {
  id?: number
  productId?: number
  productName?: string
  sku?: string
  currentStock?: number
  minStock?: number
  maxStock?: number
  warningType?: number
  message?: string
  status?: number
  handleSource?: number
  handleType?: number
  handleBy?: number
  handleByName?: string
  ownerName?: string
  handleNote?: string
  followUpTime?: string
  createTime?: string
  handleTime?: string
}

export interface WarningHandleRequest {
  handleType: number
  ownerName?: string
  handleNote: string
  followUpTime?: string
}

export interface WarningSummary {
  totalWarnings: number
  pendingWarnings: number
  handledWarnings: number
  lowWarnings: number
  highWarnings: number
  affectedProducts: number
}

export const warningApi = {
  page(params: { pageNum: number; pageSize: number; status?: number | null; warningType?: number | null; keyword?: string }) {
    return request.get('/warning/page', { params })
  },

  summary() {
    return request.get('/warning/summary')
  },

  getById(id: number) {
    return request.get(`/warning/${id}`)
  },

  check() {
    return request.post('/warning/check')
  },

  handle(id: number, data: WarningHandleRequest) {
    return request.put(`/warning/${id}/handle`, data)
  },

  handleAll(data: WarningHandleRequest) {
    return request.put('/warning/handle-all', data)
  },

  delete(id: number) {
    return request.delete(`/warning/${id}`)
  }
}
