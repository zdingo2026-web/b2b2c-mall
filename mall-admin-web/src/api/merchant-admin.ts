import { request } from '@/utils/request'

export interface TenantAdminVO {
  id: number | string
  username: string
  realName: string
  phone: string
  email: string
  avatar: string
  roleType: number  // 1=主管理员, 2=子管理员
  status: number    // 0=禁用, 1=正常
  lastLoginTime: string
  createTime: string
}

export function getMerchantAdminList() {
  return request.get<TenantAdminVO[]>('/v1/merchant/admin/list')
}

export function createMerchantAdmin(data: {
  username: string
  password: string
  realName?: string
  phone?: string
  email?: string
}) {
  return request.post<number>('/v1/merchant/admin', data)
}

export function updateMerchantAdmin(id: number | string, data: {
  realName?: string
  phone?: string
  email?: string
}) {
  return request.put<void>(`/v1/merchant/admin/${id}`, data)
}

export function updateMerchantAdminStatus(id: number | string, status: number) {
  return request.put(`/v1/merchant/admin/${id}/status`, null, { params: { status } })
}

export function deleteMerchantAdmin(id: number | string) {
  return request.delete(`/v1/merchant/admin/${id}`)
}
