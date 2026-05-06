interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  body?: any
  params?: Record<string, string | number | undefined>
  headers?: Record<string, string>
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const REFRESH_TOKEN_KEY = 'mall_pc_refresh_token'

// In-memory access token (not persisted to localStorage for security)
let accessTokenMemory: string | null = null

/** Get stored access token (from memory, not localStorage) */
export function getToken(): string | null {
  return accessTokenMemory
}

/** Get stored refresh token (from sessionStorage, cleared on tab close) */
export function getRefreshToken(): string | null {
  if (import.meta.client) {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY)
  }
  return null
}

/** Store tokens - access token in memory, refresh token in sessionStorage */
export function setTokens(accessToken: string, refreshToken?: string): void {
  accessTokenMemory = accessToken
  if (import.meta.client && refreshToken) {
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

/** Clear tokens from memory and sessionStorage */
export function clearTokens(): void {
  accessTokenMemory = null
  if (import.meta.client) {
    sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  }
}

/** Whether a token refresh is currently in progress */
let isRefreshing = false
/** Pending requests that should retry after token refresh */
let pendingRequests: Array<{ resolve: (token: string) => void; reject: (error: any) => void }> = []

function onTokenRefreshed(newToken: string): void {
  pendingRequests.forEach(({ resolve }) => resolve(newToken))
  pendingRequests = []
}

function onRefreshFailed(error: any): void {
  pendingRequests.forEach(({ reject }) => reject(error))
  pendingRequests = []
}

/** Attempt to refresh the access token */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }

  const config = useRuntimeConfig()
  const response = await $fetch<ApiResponse<{ accessToken: string; refreshToken?: string }>>(
    `${config.public.apiBase}/auth/refresh`,
    {
      method: 'POST',
      body: { refreshToken },
    },
  )

  if (response.code === 200 && response.data?.accessToken) {
    setTokens(response.data.accessToken, response.data.refreshToken)
    return response.data.accessToken
  }

  throw new Error(response.message || 'Token refresh failed')
}

/** Unified request wrapper based on $fetch */
export function useRequest() {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  async function request<T = any>(url: string, options: RequestOptions = {}): Promise<T> {
    const { method = 'GET', body, params, headers = {} } = options

    const token = getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    try {
      const response = await $fetch<ApiResponse<T>>(`${apiBase}${url}`, {
        method,
        body,
        params,
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
      })

      if (response.code === 200) {
        return response.data
      }

      // Token expired - attempt refresh
      if (response.code === 401) {
        if (!isRefreshing) {
          isRefreshing = true
          try {
            const newToken = await refreshAccessToken()
            isRefreshing = false
            onTokenRefreshed(newToken)

            // Retry the original request with new token
            headers['Authorization'] = `Bearer ${newToken}`
            const retryResponse = await $fetch<ApiResponse<T>>(`${apiBase}${url}`, {
              method,
              body,
              params,
              headers: {
                'Content-Type': 'application/json',
                ...headers,
              },
            })

            if (retryResponse.code === 200) {
              return retryResponse.data
            }
            throw new Error(retryResponse.message || 'Request failed after token refresh')
          } catch (refreshError) {
            isRefreshing = false
            onRefreshFailed(refreshError)
            clearTokens()
            if (import.meta.client) {
              navigateTo('/login')
            }
            throw refreshError
          }
        }

        // If refresh is already in progress, queue this request
        return new Promise<T>((resolve, reject) => {
          pendingRequests.push({
            resolve: async (newToken: string) => {
              try {
                headers['Authorization'] = `Bearer ${newToken}`
                const retryResponse = await $fetch<ApiResponse<T>>(`${apiBase}${url}`, {
                  method,
                  body,
                  params,
                  headers: {
                    'Content-Type': 'application/json',
                    ...headers,
                  },
                })
                if (retryResponse.code === 200) {
                  resolve(retryResponse.data)
                } else {
                  reject(new Error(retryResponse.message))
                }
              } catch (err) {
                reject(err)
              }
            },
            reject,
          })
        })
      }

      throw new Error(response.message || '请求失败')
    } catch (error: any) {
      if (error?.response?.status === 401) {
        clearTokens()
        if (import.meta.client) {
          navigateTo('/login')
        }
      }
      throw error
    }
  }

  return {
    get: <T = any>(url: string, params?: Record<string, string | number | undefined>) =>
      request<T>(url, { method: 'GET', params }),
    post: <T = any>(url: string, body?: any) =>
      request<T>(url, { method: 'POST', body }),
    put: <T = any>(url: string, body?: any) =>
      request<T>(url, { method: 'PUT', body }),
    delete: <T = any>(url: string, params?: Record<string, string | number | undefined>) =>
      request<T>(url, { method: 'DELETE', params }),
    patch: <T = any>(url: string, body?: any) =>
      request<T>(url, { method: 'PATCH', body }),
  }
}
