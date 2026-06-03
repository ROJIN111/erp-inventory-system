const TOKEN_KEY = 'erp_token'
const USER_INFO_KEY = 'erp_user_info'

export interface AuthUserInfo {
  id?: number
  username?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  roleId?: number
  roleCode?: string
  roleName?: string
  permissions?: string[]
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string) {
  return localStorage.setItem(TOKEN_KEY, token)
}

export function removeStoredToken() {
  return localStorage.removeItem(TOKEN_KEY)
}

export function getStoredUserInfo(): AuthUserInfo | null {
  const raw = localStorage.getItem(USER_INFO_KEY)
  if (!raw) {
    return null
  }

  try {
    return normalizeUserInfo(JSON.parse(raw))
  } catch {
    return null
  }
}

export function setStoredUserInfo(userInfo: AuthUserInfo) {
  return localStorage.setItem(USER_INFO_KEY, JSON.stringify(normalizeUserInfo(userInfo)))
}

export function removeStoredUserInfo() {
  return localStorage.removeItem(USER_INFO_KEY)
}

export function clearStoredAuth() {
  removeStoredToken()
  removeStoredUserInfo()
}

export function normalizePermissions(permissions?: string[]) {
  return Array.from(new Set((permissions || []).filter(Boolean)))
}

export function normalizeUserInfo(userInfo?: AuthUserInfo | null): AuthUserInfo | null {
  if (!userInfo) {
    return null
  }

  return {
    ...userInfo,
    permissions: normalizePermissions(userInfo.permissions)
  }
}
