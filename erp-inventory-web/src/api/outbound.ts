import request from './request'

export interface OutboundOrder {
  id?: number
  orderNo?: string
  productId: number
  quantity: number
  price?: number
  totalAmount?: number
  customer?: string
  status?: number
  remark?: string
  productName?: string
  createTime?: string
  updateTime?: string
}

export const outboundApi = {
  list() {
    return request.get('/outbound/list')
  },

  page(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get('/outbound/page', { params })
  },

  getById(id: number) {
    return request.get(`/outbound/${id}`)
  },

  create(data: OutboundOrder) {
    return request.post('/outbound', data)
  },

  audit(id: number) {
    return request.put(`/outbound/${id}/audit`)
  },

  delete(id: number) {
    return request.delete(`/outbound/${id}`)
  }
}
