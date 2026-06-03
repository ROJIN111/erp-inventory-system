import request from './request'

export const statisticsApi = {
  getProductStats() {
    return request.get('/statistics/product/stats')
  },

  getInventoryStats() {
    return request.get('/statistics/inventory/stats')
  },

  getInboundStats() {
    return request.get('/statistics/inbound/stats')
  },

  getOutboundStats() {
    return request.get('/statistics/outbound/stats')
  }
}
