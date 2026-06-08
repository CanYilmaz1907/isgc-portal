import type { AuthUser, Role } from '../../state/auth/types'

export function canWrite(user: AuthUser | null | undefined): boolean {
  if (!user) return false
  return user.role === 'ADMIN' || user.role === 'ISG_C'
}

export function canUpload(user: AuthUser | null | undefined): boolean {
  return canWrite(user)
}

export function isReadOnly(user: AuthUser | null | undefined): boolean {
  return user?.role === 'READ_ONLY'
}

export function roleLabel(role: Role, t: (key: string) => string): string {
  return t(`roles.${role}`)
}
