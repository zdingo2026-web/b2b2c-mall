import { request } from '@/utils/request'

export const getMemberLevelList = () => request.get('/platform/member/level/list')
export const createMemberLevel = (data: any) => request.post('/platform/member/level/create', data)
export const updateMemberLevel = (id: number, data: any) => request.put(`/platform/member/level/${id}`, data)
export const deleteMemberLevel = (id: number) => request.delete(`/platform/member/level/${id}`)
export const getRealnameList = (params: any) => request.get('/platform/member/realname/list', { params })
export const auditRealname = (id: number, status: number, rejectReason?: string) =>
  request.put(`/platform/member/realname/${id}/audit`, null, { params: { status, rejectReason } })
export const getPaypwdResetList = (params: any) => request.get('/platform/member/paypwd-reset/list', { params })
export const approvePaypwdReset = (id: number) => request.put(`/platform/member/paypwd-reset/${id}/approve`)
export const rejectPaypwdReset = (id: number, reason: string) =>
  request.put(`/platform/member/paypwd-reset/${id}/reject`, null, { params: { reason } })
