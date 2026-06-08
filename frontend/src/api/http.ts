import axios from 'axios'
import { loadAuth, saveAuth, clearAuth } from '../state/auth/storage'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
})

http.interceptors.request.use((config) => {
  const auth = loadAuth()
  if (auth?.accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let isRefreshing = false
let refreshQueue: Array<(token: string | null) => void> = []

function flushQueue(token: string | null) {
  refreshQueue.forEach((cb) => cb(token))
  refreshQueue = []
}

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config
    if (!original || original._retry) throw error

    if (error.response?.status === 401) {
      const auth = loadAuth()
      if (!auth?.refreshToken) {
        clearAuth()
        throw error
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          refreshQueue.push((token) => {
            if (!token) return reject(error)
            original.headers.Authorization = `Bearer ${token}`
            resolve(http(original))
          })
        })
      }

      original._retry = true
      isRefreshing = true
      try {
        const resp = await axios.post(
          `${http.defaults.baseURL}/api/auth/refresh`,
          { refreshToken: auth.refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        )
        saveAuth(resp.data)
        const newToken = resp.data.accessToken as string
        flushQueue(newToken)
        original.headers.Authorization = `Bearer ${newToken}`
        return http(original)
      } catch (e) {
        flushQueue(null)
        clearAuth()
        throw e
      } finally {
        isRefreshing = false
      }
    }

    throw error
  }
)

/** Get message from axios error; works when responseType was 'arraybuffer' and server returned JSON error. */
export function getErrorMessage(error: any, fallback: string): string {
  const data = error?.response?.data
  if (data == null) return error?.message || fallback
  if (typeof data === 'string') return data || fallback
  if (data.message) return data.message
  if (data instanceof ArrayBuffer) {
    try {
      const json = JSON.parse(new TextDecoder().decode(data))
      return json?.message || fallback
    } catch {
      return fallback
    }
  }
  if (typeof data === 'object' && data.message) return data.message
  return fallback
}


