import { request } from '@/utils/request'

export interface MemberVO {
  id: number
  username: string
  phone: string
  nickname: string
  avatar: string
  gender: number
  email: string
  status: number
  createTime: string
}

export function getMemberList(params?: { page?: number; limit?: number }) {
  return request.get<{ list: MemberVO[]; total: number }>('/v1/platform/member/list', params)
}

export function getMemberDetail(id: number) {
  return request.get<MemberVO>(`/v1/platform/member/${id}`)
}
