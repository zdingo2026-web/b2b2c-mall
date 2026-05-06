import { defineStore } from 'pinia'
import type { UserInfo } from '~/composables/types'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: null as string | null,
    refreshToken: null as string | null,
    userInfo: null as UserInfo | null,
  }),

  getters: {
    isLoggedIn: (state): boolean => !!state.token,
    nickname: (state): string => state.userInfo?.nickname || state.userInfo?.phone || '',
    phone: (state): string => state.userInfo?.phone || '',
  },

  actions: {
    async login(credentials: { account: string; password: string }) {
      const { post } = useRequest()
      const data = await post<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>(
        '/auth/login',
        credentials,
      )
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      setTokens(data.accessToken, data.refreshToken)
    },

    async smsLogin(data: { phone: string; code: string }) {
      const { post } = useRequest()
      const res = await post<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>(
        '/auth/sms/login',
        data,
      )
      this.token = res.accessToken
      this.refreshToken = res.refreshToken
      this.userInfo = res.userInfo
      setTokens(res.accessToken, res.refreshToken)
    },

    async register(data: { phone: string; code: string; password: string }) {
      const { post } = useRequest()
      const res = await post<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>(
        '/auth/register',
        data,
      )
      this.token = res.accessToken
      this.refreshToken = res.refreshToken
      this.userInfo = res.userInfo
      setTokens(res.accessToken, res.refreshToken)
    },

    async fetchUserInfo() {
      const { get } = useRequest()
      const data = await get<UserInfo>('/auth/info')
      this.userInfo = data
    },

    async logout() {
      try {
        const { post } = useRequest()
        await post('/auth/logout')
      } catch {
        // Ignore logout API errors
      } finally {
        this.token = null
        this.refreshToken = null
        this.userInfo = null
        clearTokens()
      }
    },

    hydrate() {
      if (import.meta.client) {
        const token = getToken()
        const refreshToken = getRefreshToken()
        if (token) {
          this.token = token
          this.refreshToken = refreshToken
        }
      }
    },
  },
})
