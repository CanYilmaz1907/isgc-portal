import { createContext } from 'react'
import type { AuthUser } from './types'

export type AuthState = {
  user: AuthUser | null
  accessToken: string | null
  isAuthenticated: boolean
}

export type AuthActions = {
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<(AuthState & AuthActions) | null>(null)


