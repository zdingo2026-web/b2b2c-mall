import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const REFRESH_TOKEN_KEY = 'mall_admin_refresh_token'

// In-memory access token (not persisted to localStorage for security)
let accessTokenMemory: string | null = null

interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/** Get stored access token (from memory, not localStorage) */
export function getToken(): string | null {
  return accessTokenMemory
}

/** Get stored refresh token (from sessionStorage, cleared on tab close) */
export function getRefreshToken(): string | null {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY)
}

/** Store tokens - access token in memory, refresh token in sessionStorage */
export function setTokens(accessToken: string, refreshToken?: string): void {
  accessTokenMemory = accessToken
  if (refreshToken) {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

/** Clear tokens from memory and sessionStorage */
export function clearTokens(): void {
  accessTokenMemory = null
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}

/** Whether a token refresh is currently in progress */
let isRefreshing = false
let pendingRequests: Array<{ resolve: (token: string) => void; reject: (error: any) => void }> = []

function onTokenRefreshed(newToken: string): void {
  pendingRequests.forEach(({ resolve }) => resolve(newToken))
  pendingRequests = []
}

function onRefreshFailed(error: any): void {
  pendingRequests.forEach(({ reject }) => reject(error))
  pendingRequests = []
}

/** Create axios instance */
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/** Request interceptor */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/** Response interceptor */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    if (res.code === 0) {
      return res.data as any
    }

    // Token expired
    if (res.code === 401) {
      const originalRequest = response.config

      if (!isRefreshing) {
        isRefreshing = true
        const refreshToken = getRefreshToken()

        if (!refreshToken) {
          isRefreshing = false
          clearTokens()
          window.location.href = '/login'
          return Promise.reject(new Error('登录已过期，请重新登录'))
        }

        return axios
          .post('/api/auth/refresh', { refreshToken })
          .then((refreshRes) => {
            const data = refreshRes.data as ApiResponse<{ accessToken: string; refreshToken?: string }>
            if (data.code === 0 && data.data?.accessToken) {
              setTokens(data.data.accessToken, data.data.refreshToken)
              onTokenRefreshed(data.data.accessToken)

              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`
              }
              return service(originalRequest)
            }

            throw new Error(data.msg || 'Token refresh failed')
          })
          .catch((error) => {
            onRefreshFailed(error)
            clearTokens()
            window.location.href = '/login'
            return Promise.reject(error)
          })
          .finally(() => {
            isRefreshing = false
          })
      }

      // Queue the request while refresh is in progress
      return new Promise((resolve, reject) => {
        pendingRequests.push({
          resolve: (newToken: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`
            }
            resolve(service(originalRequest))
          },
          reject,
        })
      })
    }

    // Business error
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      clearTokens()
      window.location.href = '/login'
    } else {
      const message = error.response?.data?.msg || error.message || '网络错误'
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

/** Request methods */
export const request = {
  get<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, { params, ...config })
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, { params, ...config })
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.patch(url, data, config)
  },
}

export default service
