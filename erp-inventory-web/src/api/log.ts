import request from './request'

export const logApi = {
  page(params: { pageNum: number; pageSize: number }) {
    return request.get('/log/page', { params })
  },

  delete(id: number) {
    return request.delete(`/log/${id}`)
  }
}
