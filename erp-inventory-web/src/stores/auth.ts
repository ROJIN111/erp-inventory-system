import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { userApi } from '@/api/user'
import {
  clearStoredAuth,
  getStoredToken,
  getStoredUserInfo,
  normalizePermissions,
  normalizeUserInfo,
  setStoredToken,
  setStoredUserInfo,
  type AuthUserInfo
} from '@/utils/auth-storage'

interface AuthApiResult {
  code?: number
  message?: string
  data?: (AuthUserInfo & { token?: string }) | null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getStoredToken() || '')
  const userInfo = ref<AuthUserInfo | null>(getStoredUserInfo())
  const hydrated = ref(false)
  const restoringPromise = ref<Promise<void> | null>(null)

  const permissions = computed(() => normalizePermissions(userInfo.value?.permissions))
  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const displayRole = computed(() => userInfo.value?.roleName || userInfo.value?.roleCode || '')

  function hydrateFromStorage() {
    token.value = getStoredToken() || ''
    userInfo.value = getStoredUserInfo()
  }

  function applySession(nextUserInfo: AuthUserInfo | null, nextToken?: string) {
    const normalizedUserInfo = normalizeUserInfo(nextUserInfo)
    const normalizedToken = nextToken ?? token.value

    token.value = normalizedToken || ''
    userInfo.value = normalizedUserInfo

    if (token.value) {
      setStoredToken(token.value)
    } else {
      clearStoredAuth()
      return
    }

    if (normalizedUserInfo) {
      setStoredUserInfo(normalizedUserInfo)
    }
  }

  function clearSession() {
    token.value = ''
    userInfo.value = null
    hydrated.value = true
    clearStoredAuth()
  }

  async function restoreSession() {
    hydrateFromStorage()

    if (!token.value) {
      hydrated.value = true
      return
    }

    try {
      const res = await userApi.getUserInfo() as AuthApiResult
      if (res.code === 200 && res.data) {
        applySession(res.data, res.data.token || token.value)
      } else {
        clearSession()
      }
    } catch {
      clearSession()
    } finally {
      hydrated.value = true
    }
  }

  async function ensureSessionInitialized() {
    if (hydrated.value) {
      return
    }

    if (restoringPromise.value) {
      return restoringPromise.value
    }

    restoringPromise.value = restoreSession().finally(() => {
      restoringPromise.value = null
    })
    return restoringPromise.value
  }

  async function login(credentials: { username: string; password: string }) {
    const res = await userApi.login(credentials) as AuthApiResult
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '登录失败')
    }

    applySession(res.data, res.data.token || 'demo-token')
    hydrated.value = true
    return res.data
  }

  function hasPermission(permission?: string) {
    if (!permission) {
      return true
    }
    return permissions.value.includes(permission)
  }

  function hasAnyPermission(targetPermissions?: string[]) {
    if (!targetPermissions || targetPermissions.length === 0) {
      return true
    }
    return targetPermissions.some(permission => permissions.value.includes(permission))
  }

  function hasRole(roleCode?: string) {
    if (!roleCode) {
      return true
    }
    return userInfo.value?.roleCode === roleCode
  }

  return {
    token,
    userInfo,
    hydrated,
    permissions,
    isLoggedIn,
    displayName,
    displayRole,
    hydrateFromStorage,
    applySession,
    clearSession,
    restoreSession,
    ensureSessionInitialized,
    login,
    hasPermission,
    hasAnyPermission,
    hasRole
  }
})
