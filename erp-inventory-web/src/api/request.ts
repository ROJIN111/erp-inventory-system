import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearStoredAuth, getStoredToken } from '@/utils/auth-storage'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = getStoredToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      if (res.code === 401) {
        clearStoredAuth()
        window.location.replace('/login')
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  error => {
    const responseData = error.response?.data
    const responseText = typeof responseData === 'string' ? responseData : ''
    const isProxyFailure =
      error.code === 'ERR_NETWORK' ||
      responseText.includes('http proxy error') ||
      responseText.includes('ECONNREFUSED')

    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      if (error.response.status === 401) {
        ElMessage.warning('登录已过期，请重新登录')
        clearStoredAuth()
        window.location.replace('/login')
      } else {
        ElMessage.error('没有权限执行当前操作')
      }
    } else if (isProxyFailure) {
      ElMessage.error('后端服务未启动或代理不可用，请先启动 Spring Boot 服务')
    } else {
      ElMessage.error(error.message || '网络错误')
    }

    return Promise.reject(error)
  }
)

export default request
