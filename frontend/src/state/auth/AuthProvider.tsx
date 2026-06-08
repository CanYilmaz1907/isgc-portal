import React, { useCallback, useMemo, useState } from 'react'
import { AuthContext } from './AuthContext'
import { clearAuth, loadAuth, saveAuth } from './storage'
import type { AuthUser, TokenResponse } from './types'
import axios from 'axios'

type Props = { children: React.ReactNode }

export function AuthProvider({ children }: Props) {
  const initial = loadAuth()
  const [user, setUser] = useState<AuthUser | null>(initial?.user ?? null)
  const [accessToken, setAccessToken] = useState<string | null>(initial?.accessToken ?? null)

  const login = useCallback(async (username: string, password: string) => {
    const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
    const resp = await axios.post<TokenResponse>(
      `${baseURL}/api/auth/login`,
      { username, password },
      { headers: { 'Content-Type': 'application/json' } }
    )
    saveAuth(resp.data)
    setAccessToken(resp.data.accessToken)
    setUser({ userId: resp.data.userId, username: resp.data.username, role: resp.data.role })
  }, [])

  const logout = useCallback(async () => {
    const stored = loadAuth()
    const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
    try {
      if (stored?.refreshToken) {
        await axios.post(
          `${baseURL}/api/auth/logout`,
          { refreshToken: stored.refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        )
      }
    } finally {
      clearAuth()
      setAccessToken(null)
      setUser(null)
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      accessToken,
      isAuthenticated: !!user && !!accessToken,
      login,
      logout
    }),
    [user, accessToken, login, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}


