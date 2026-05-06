import { request } from '@/utils/request'

export interface Notice {
  id: number
  title: string
  noticeType: number
  status: number
  sort: number
  createTime: string
}

export function getNoticeList(params?: { page?: number; limit?: number; status?: number }) {
  return request.get<{ list: Notice[]; total: number }>('/v1/platform/content/notice/list', params)
}

export function getNoticeDetail(id: number) {
  return request.get<Notice>(`/v1/platform/content/notice/${id}`)
}

export function createNotice(data: { title: string; noticeType: number; status: number; sort: number }) {
  return request.post<number>('/v1/platform/content/notice/create', data)
}

export function updateNotice(id: number, data: { title?: string; noticeType?: number; status?: number; sort?: number }) {
  return request.put(`/v1/platform/content/notice/${id}`, data)
}

export function deleteNotice(id: number) {
  return request.delete(`/v1/platform/content/notice/${id}`)
}
