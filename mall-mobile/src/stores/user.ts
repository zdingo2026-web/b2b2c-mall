import { defineStore } from 'pinia'
import { http, setTokens, clearTokens, getToken, getRefreshToken } from '@/utils/request'

const TOKEN_EXPIRY_KEY = 'mall_mobile_token_expiry'

/**
 * Check if the stored access token has expired.
 * Returns true if the token expiry timestamp has passed or is not set.
 */
function isTokenExpired(): boolean {
  const expiry = uni.getStorageSync(TOKEN_EXPIRY_KEY)
  if (!expiry) return false // no expiry recorded, assume valid
  return Date.now() > Number(expiry)
}

interface UserInfo {
  id: string
  phone: string
  nickname: string
  avatar: string
  memberLevel?: number
  points?: number
  balance?: string
  redPacketBalance?: string
  couponCount?: number
  memberNo?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || null as string | null,
    refreshToken: getRefreshToken() || null as string | null,
    userInfo: null as UserInfo | null,
  }),

  getters: {
    isLoggedIn: (state): boolean => {
      if (!state.token) return false
      // Check if token has expired
      if (isTokenExpired()) {
        return false
      }
      return true
    },
  },

  actions: {
    async login(params: { phone: string; code: string }) {
      const data = await http.post<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>(
        '/v1/member/auth/login/phone',
        params,
      )
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      setTokens(data.accessToken, data.refreshToken)
      // Set token expiry to 2 hours from now (matches typical JWT access token lifetime)
      uni.setStorageSync(TOKEN_EXPIRY_KEY, String(Date.now() + 2 * 60 * 60 * 1000))
    },

    async wechatLogin(params: { code: string }) {
      const data = await http.post<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>(
        '/v1/member/auth/wechat-login',
        params,
      )
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      setTokens(data.accessToken, data.refreshToken)
      uni.setStorageSync(TOKEN_EXPIRY_KEY, String(Date.now() + 2 * 60 * 60 * 1000))
    },

    async fetchUserInfo() {
      const data = await http.get<UserInfo>('/v1/member/auth/info')
      this.userInfo = data
    },

    async logout() {
      try {
        await http.post('/v1/member/auth/logout')
      } catch {
        // Ignore logout API errors
      } finally {
        this.token = null
        this.refreshToken = null
        this.userInfo = null
        clearTokens()
        uni.removeStorageSync(TOKEN_EXPIRY_KEY)
      }
    },

    hydrate() {
      const token = getToken()
      const refreshToken = getRefreshToken()
      if (token && !isTokenExpired()) {
        this.token = token
        this.refreshToken = refreshToken
      } else if (token && isTokenExpired()) {
        // Token expired - clear stale tokens
        this.token = null
        this.refreshToken = null
        clearTokens()
        uni.removeStorageSync(TOKEN_EXPIRY_KEY)
      }
    },
  },
})
