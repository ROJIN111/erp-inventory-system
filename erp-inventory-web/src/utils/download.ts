import { getToken } from './auth'

export async function downloadWithAuth(url: string, fallbackName: string) {
  const token = getToken()
  const response = await fetch(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })

  if (!response.ok) {
    throw new Error(`下载失败 (${response.status})`)
  }

  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition')
  const fileName = resolveFileName(disposition) || fallbackName
  const objectUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = objectUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(objectUrl)
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
