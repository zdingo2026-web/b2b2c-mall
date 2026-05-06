import { defineStore } from 'pinia'
import { request, setTokens, clearTokens, getToken, getRefreshToken } from '@/utils/request'

const USER_TYPE_KEY = 'mall_admin_user_type'

interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  userType: number
  tenantId?: number
}

interface LoginParams {
  username: string
  password: string
}

interface LoginResult {
  accessToken: string
  refreshToken: string
  userInfo: UserInfo
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || null as string | null,
    refreshToken: getRefreshToken() || null as string | null,
    userInfo: null as UserInfo | null,
    cachedUserType: sessionStorage.getItem(USER_TYPE_KEY) ? Number(sessionStorage.getItem(USER_TYPE_KEY)) : null as number | null,
  }),

  getters: {
    isLoggedIn: (state): boolean => !!state.token,
    isAdmin: (state): boolean => state.userInfo?.userType === 1,
    isMerchant: (state): boolean => state.userInfo?.userType === 2,
    role: (state): 'platform' | 'merchant' | '' => {
      if (state.userInfo?.userType === 1) return 'platform'
      if (state.userInfo?.userType === 2) return 'merchant'
      return ''
    },
    tenantId: (state): number | undefined => state.userInfo?.tenantId,
    adminInfo: (state): any => {
      if (!state.userInfo) return null
      return {
        id: state.userInfo.userId,
        username: state.userInfo.username,
        nickname: state.userInfo.nickname,
        avatar: state.userInfo.avatar,
        role: state.userInfo.userType === 1 ? 'platform' : 'merchant',
        tenantId: state.userInfo.tenantId,
      }
    },
  },

  actions: {
    async login(params: LoginParams) {
      const data = await request.post<LoginResult>('/v1/platform/auth/login', null, { params })
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      this.cachedUserType = data.userInfo.userType
      sessionStorage.setItem(USER_TYPE_KEY, String(data.userInfo.userType))
      setTokens(data.accessToken, data.refreshToken)
    },

    async fetchAdminInfo() {
      const userType = this.userInfo?.userType ?? this.cachedUserType
      const endpoint = userType === 2 ? '/v1/merchant/auth/info' : '/v1/platform/auth/info'
      const data = await request.get<UserInfo>(endpoint)
      this.userInfo = data
      this.cachedUserType = data.userType
      sessionStorage.setItem(USER_TYPE_KEY, String(data.userType))
    },

    async logout() {
      try {
        const userType = this.userInfo?.userType ?? this.cachedUserType
        const endpoint = userType === 2 ? '/v1/merchant/auth/logout' : '/v1/platform/auth/logout'
        await request.post(endpoint)
      } catch {
        // Ignore logout API errors
      } finally {
        this.token = null
        this.refreshToken = null
        this.userInfo = null
        this.cachedUserType = null
        sessionStorage.removeItem(USER_TYPE_KEY)
        clearTokens()
      }
    },

    /** Hydrate store from memory/sessionStorage */
    hydrate() {
      const token = getToken()
      const refreshToken = getRefreshToken()
      const cachedType = sessionStorage.getItem(USER_TYPE_KEY)
      if (token) {
        this.token = token
        this.refreshToken = refreshToken
      }
      if (cachedType) {
        this.cachedUserType = Number(cachedType)
      }
    },
  },
})
