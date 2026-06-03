import request from './request'
import { getToken } from '@/utils/auth'

export interface InventoryTransaction {
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

export interface InventoryTransactionQuery {
  pageNum?: number
  pageSize?: number
  productId?: number
  transactionType?: number
  sku?: string
  category?: string
  relatedNo?: string
  operatorName?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

export const inventoryTransactionApi = {
  page(params: InventoryTransactionQuery) {
    return request.get('/inventory-transaction/page', { params })
  },

  getById(id: number) {
    return request.get(`/inventory-transaction/${id}`)
  },

  async export(params: InventoryTransactionQuery) {
    const queryString = new URLSearchParams()

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        queryString.append(key, String(value))
      }
    })

    const query = queryString.toString()
    const url = `/api/inventory-transaction/export${query ? `?${query}` : ''}`
    const token = getToken()
    const response = await fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })

    if (!response.ok) {
      let message = '导出库存流水失败'
      const contentType = response.headers.get('content-type') || ''

      if (contentType.includes('application/json')) {
        const data = await response.json().catch(() => null)
        message = data?.message || message
      } else {
        const text = await response.text().catch(() => '')
        if (text) {
          message = text
        }
      }

      throw new Error(message)
    }

    const blob = await response.blob()
    const disposition = response.headers.get('content-disposition')
    const fileName = resolveFileName(disposition) || `inventory-transactions-${Date.now()}.xlsx`
    const objectUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = objectUrl
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(objectUrl)
  }
}

function resolveFileName(disposition: string | null) {
  if (!disposition) {
    return ''
  }

  const utf8Match = disposition.match(/filename\*=utf-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const basicMatch = disposition.match(/filename="?([^"]+)"?/i)
  return basicMatch?.[1] || ''
}
