import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../state/auth/useAuth'
import type { Role } from '../state/auth/types'

type Props = {
  children: React.ReactNode
  roles?: Role[]
}

export function RequireAuth({ children, roles }: Props) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (roles && user && !roles.includes(user.role)) {
    return <Navigate to="/forbidden" replace />
  }

  return <>{children}</>
}


