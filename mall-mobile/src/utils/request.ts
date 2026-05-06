interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: any
  header?: Record<string, string>
}

interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

const TOKEN_KEY = 'mall_mobile_token'
const REFRESH_TOKEN_KEY = 'mall_mobile_refresh_token'
const BASE_URL = '/api'

/** Get stored access token */
export function getToken(): string | null {
  return uni.getStorageSync(TOKEN_KEY) || null
}

/** Get stored refresh token */
export function getRefreshToken(): string | null {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || null
}

/** Store tokens */
export function setTokens(accessToken: string, refreshToken?: string): void {
  uni.setStorageSync(TOKEN_KEY, accessToken)
  if (refreshToken) {
    uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken)
  }
}

/** Clear tokens */
export function clearTokens(): void {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
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

/** Attempt to refresh the access token */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }

  const [error, response] = await new Promise<[any, any]>((resolve) => {
    uni.request({
      url: `${BASE_URL}/v1/auth/refresh`,
      method: 'POST',
      data: { refreshToken },
      success: (res: any) => resolve([null, res]),
      fail: (err: any) => resolve([err, null]),
    })
  })

  if (error || !response) {
    throw new Error('Token refresh request failed')
  }

  const res = response.data as ApiResponse<{ accessToken: string; refreshToken?: string }>
  if (res.code === 0 && res.data?.accessToken) {
    setTokens(res.data.accessToken, res.data.refreshToken)
    return res.data.accessToken
  }

  throw new Error(res.msg || 'Token refresh failed')
}

/** Unified request wrapper based on uni.request */
export function request<T = any>(url: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', data, header = {} } = options

  const token = getToken()
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  header['Content-Type'] = header['Content-Type'] || 'application/json'

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: async (response: any) => {
        const res = response.data as ApiResponse<T>

        if (res.code === 0) {
          resolve(res.data)
          return
        }

        if (res.code === 401) {
          const hadToken = !!token

          if (!hadToken) {
            // No token was present - user is simply not logged in, reject silently
            reject(new Error(res.msg || '未登录'))
            return
          }

          // Had a token but got 401 - token expired, attempt refresh
          if (!isRefreshing) {
            isRefreshing = true
            try {
              const newToken = await refreshAccessToken()
              isRefreshing = false
              onTokenRefreshed(newToken)

              // Retry original request
              header['Authorization'] = `Bearer ${newToken}`
              const retryResult = await request<T>(url, options)
              resolve(retryResult)
            } catch (refreshError) {
              isRefreshing = false
              onRefreshFailed(refreshError)
              clearTokens()
              uni.showToast({ title: '登录已过期', icon: 'none' })
              setTimeout(() => {
                uni.reLaunch({ url: '/pages/login/index' })
              }, 1500)
              reject(refreshError)
            }
            return
          }

          // Queue while refresh is in progress
          new Promise<T>((queueResolve, queueReject) => {
            pendingRequests.push({
              resolve: async (newToken: string) => {
                try {
                  header['Authorization'] = `Bearer ${newToken}`
                  const retryResult = await request<T>(url, options)
                  queueResolve(retryResult)
                } catch (err) {
                  queueReject(err)
                }
              },
              reject: queueReject,
            })
          }).then(resolve).catch(reject)
          return
        }

        // Business error - reject without toast so callers can handle gracefully
        reject(new Error(res.msg || '请求失败'))
      },
      fail: (error: any) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(error)
      },
    })
  })
}

/** Convenient method shortcuts */
export const http = {
  get: <T = any>(url: string, data?: any) => request<T>(url, { method: 'GET', data }),

  post: <T = any>(url: string, data?: any) => request<T>(url, { method: 'POST', data }),

  put: <T = any>(url: string, data?: any) => request<T>(url, { method: 'PUT', data }),

  delete: <T = any>(url: string, data?: any) => request<T>(url, { method: 'DELETE', data }),

  patch: <T = any>(url: string, data?: any) => request<T>(url, { method: 'PATCH', data }),
}
