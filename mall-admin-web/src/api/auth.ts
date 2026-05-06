import { request } from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  userType: number
  tenantId: number
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  userInfo: UserInfo
}

export function platformLogin(params: LoginParams): Promise<LoginResult> {
  return request.post('/v1/platform/auth/login', null, { params })
}

export function merchantLogin(params: LoginParams): Promise<LoginResult> {
  return request.post('/v1/merchant/auth/login', null, { params })
}
