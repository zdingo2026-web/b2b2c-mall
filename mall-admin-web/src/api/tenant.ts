import { request } from '@/utils/request'

export interface Tenant {
  id: number | string
  tenantName: string
  contactName: string
  contactPhone: string
  contactEmail: string
  businessLicense: string
  licenseImage: string
  address: string
  status: number
  createTime: string
  scoreProduct: number
  scoreService: number
  scoreLogistics: number
  brandVerified: number
  commissionRate: number
  auditRemark: string
  logo: string
  description: string
}

export interface TenantApply {
  id: number | string
  tenantName: string
  contactName: string
  contactPhone: string
  contactEmail: string
  businessLicense: string
  licenseImage: string
  address: string
  applyStatus: number
  auditRemark: string
  auditTime: string
  auditUserId: number
  tenantId: number
  createTime: string
}

export interface TenantApplyParams {
  tenantName: string
  contactName: string
  contactPhone: string
  contactEmail: string
  businessLicense: string
  licenseImage: string
  address: string
}

// Tenant apply APIs (for audit)
export function getTenantApplyList(params?: { status?: number; page?: number; limit?: number }) {
  return request.get<{ list: TenantApply[]; total: number }>('/v1/platform/tenant/apply/list', params)
}

export function getTenantApplyDetail(id: number | string) {
  return request.get<TenantApply>(`/v1/platform/tenant/apply/${id}`)
}

export function auditTenantApply(id: number | string, pass: boolean, reason?: string) {
  return request.put(`/v1/platform/tenant/apply/${id}/audit`, null, { params: { pass, reason } })
}

export function getTenantList(params?: { status?: number; keyword?: string; page?: number; limit?: number }) {
  return request.get<{ list: Tenant[]; total: number }>('/v1/platform/tenant/list', params)
}

export function getTenantDetail(id: number | string) {
  return request.get<Tenant>(`/v1/platform/tenant/${id}`)
}

export function applyTenant(data: TenantApplyParams) {
  return request.post<number>('/v1/platform/tenant/apply', data)
}

export function auditTenant(id: number | string, pass: boolean, reason?: string) {
  return request.put(`/v1/platform/tenant/${id}/audit`, null, { params: { pass, reason } })
}

export function enableTenant(id: number | string) {
  return request.put(`/v1/platform/tenant/${id}/enable`)
}

export function disableTenant(id: number | string) {
  return request.put(`/v1/platform/tenant/${id}/disable`)
}

export function setTenantScore(id: number | string, data: { scoreProduct: number; scoreService: number; scoreLogistics: number; brandVerified: number }) {
  return request.put(`/v1/platform/tenant/${id}/score`, data)
}

// Merchant shop APIs (self-service, JWT-based)
export function getMerchantShop() {
  return request.get<Tenant>('/v1/merchant/shop')
}

export function updateMerchantShop(data: {
  tenantName: string
  logo?: string
  description?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
}) {
  return request.put<void>('/v1/merchant/shop', data)
}
