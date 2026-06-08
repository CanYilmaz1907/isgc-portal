import type { TokenResponse, AuthUser } from './types'

const KEY = 'isgc.auth'

export type StoredAuth = {
  accessToken: string
  refreshToken: string
  accessTokenExpiresAt: string
  user: AuthUser
}

export function loadAuth(): StoredAuth | null {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredAuth
  } catch {
    return null
  }
}

export function saveAuth(tokens: TokenResponse) {
  const stored: StoredAuth = {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    accessTokenExpiresAt: tokens.accessTokenExpiresAt,
    user: { userId: tokens.userId, username: tokens.username, role: tokens.role }
  }
  localStorage.setItem(KEY, JSON.stringify(stored))
}

export function clearAuth() {
  localStorage.removeItem(KEY)
}


