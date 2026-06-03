import request from './request'

export interface InboundOrder {
  id?: number
  orderNo?: string
  productId: number
  quantity: number
  price?: number
  totalAmount?: number
  supplier?: string
  status?: number
  remark?: string
  productName?: string
  createTime?: string
  updateTime?: string
}

export const inboundApi = {
  list() {
    return request.get('/inbound/list')
  },

  page(params: { pageNum: number; pageSize: number }) {
    return request.get('/inbound/page', { params })
  },

  getById(id: number) {
    return request.get(`/inbound/${id}`)
  },

  create(data: InboundOrder) {
    return request.post('/inbound', data)
  },

  audit(id: number) {
    return request.put(`/inbound/${id}/audit`)
  },

  delete(id: number) {
    return request.delete(`/inbound/${id}`)
  }
}
