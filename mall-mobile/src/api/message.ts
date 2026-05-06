import { http } from '@/utils/request'

export interface MessageVO {
  id: string
  title: string
  content: string
  msgType: number
  isRead: number
  createTime: string
}

export function getMessageList(params?: { page?: number; limit?: number; msgType?: number }) {
  return http.get<{ list: MessageVO[]; total: number }>('/v1/member/message/list', params)
}

export function getUnreadCount() {
  return http.get<number>('/v1/member/message/unread-count')
}

export function markRead(id: string) {
  return http.put(`/v1/member/message/${id}/read`)
}
