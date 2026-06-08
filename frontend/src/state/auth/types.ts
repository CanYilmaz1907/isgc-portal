export type Role = 'ADMIN' | 'ISG_C' | 'YONETICI' | 'PERSONEL' | 'READ_ONLY'

export type AuthUser = {
  userId: string
  username: string
  role: Role
}

export type TokenResponse = {
  accessToken: string
  refreshToken: string
  accessTokenExpiresAt: string
  userId: string
  username: string
  role: Role
}


