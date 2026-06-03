import request from './request'

export interface Product {
  id?: number
  name?: string
  description?: string
  sku?: string
  price?: number
  category?: string
  unit?: string
  status?: number
  stock?: number
  minStock?: number
  maxStock?: number
  createTime?: string
  updateTime?: string
}

export const productApi = {
  list() {
    return request.get('/product/list')
  },

  page(data: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get('/product/page', { params: data })
  },

  getById(id: number) {
    return request.get(`/product/${id}`)
  },

  add(data: Partial<Product>) {
    return request.post('/product', data)
  },

  update(data: Partial<Product>) {
    return request.put('/product', data)
  },

  delete(id: number) {
    return request.delete(`/product/${id}`)
  }
}
