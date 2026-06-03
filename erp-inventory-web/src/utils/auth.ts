import { getActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import {
  clearStoredAuth,
  getStoredToken,
  getStoredUserInfo,
  normalizePermissions,
  setStoredToken,
  setStoredUserInfo,
  type AuthUserInfo
} from '@/utils/auth-storage'

export type { AuthUserInfo } from '@/utils/auth-storage'

function resolveAuthStore() {
  if (!getActivePinia()) {
    return null
  }
  return useAuthStore()
}

export function getToken() {
  const authStore = resolveAuthStore()
  return authStore?.token || getStoredToken()
}

export function setToken(token: string) {
  setStoredToken(token)
  resolveAuthStore()?.hydrateFromStorage()
}

export function removeToken() {
  clearStoredAuth()
  resolveAuthStore()?.hydrateFromStorage()
}

export function getUserInfo(): AuthUserInfo | null {
  const authStore = resolveAuthStore()
  return authStore?.userInfo || getStoredUserInfo()
}

export function setUserInfo(userInfo: AuthUserInfo) {
  setStoredUserInfo(userInfo)
  resolveAuthStore()?.hydrateFromStorage()
}

export function removeUserInfo() {
  clearStoredAuth()
  resolveAuthStore()?.hydrateFromStorage()
}

export function clearAuth() {
  const authStore = resolveAuthStore()
  if (authStore) {
    authStore.clearSession()
    return
  }
  clearStoredAuth()
}

export function isLoggedIn() {
  const authStore = resolveAuthStore()
  return authStore ? authStore.isLoggedIn : !!getStoredToken()
}

export function getPermissions() {
  const authStore = resolveAuthStore()
  return authStore ? authStore.permissions : normalizePermissions(getStoredUserInfo()?.permissions)
}

export function hasPermission(permission?: string) {
  const authStore = resolveAuthStore()
  if (authStore) {
    return authStore.hasPermission(permission)
  }
  if (!permission) {
    return true
  }
  return getPermissions().includes(permission)
}

export function hasAnyPermission(permissions?: string[]) {
  const authStore = resolveAuthStore()
  if (authStore) {
    return authStore.hasAnyPermission(permissions)
  }
  if (!permissions || permissions.length === 0) {
    return true
  }
  const currentPermissions = getPermissions()
  return permissions.some(permission => currentPermissions.includes(permission))
}

export function hasRole(roleCode?: string) {
  const authStore = resolveAuthStore()
  if (authStore) {
    return authStore.hasRole(roleCode)
  }
  if (!roleCode) {
    return true
  }
  return getUserInfo()?.roleCode === roleCode
}
