import { request } from '@/utils/request'

export interface TenantLevel {
  id: number | string
  levelName: string
  levelIcon: string
  commissionDiscount: number
  minScore: number
  sortWeight: number
}

export interface CategoryCommission {
  id: number | string
  categoryId: number
  categoryName: string
  rateLevel1: number
  rateLevel2: number
  rateLevel3: number
}

export interface TenantSettlement {
  id: number | string
  settlementNo: string
  tenantId: number
  tenantName: string
  periodStart: string
  periodEnd: string
  orderCount: number
  orderTotalAmount: number
  platformCommission: number
  merchantAmount: number
  status: number
  settleTime: string
}

export interface TenantFreezeRecord {
  id: number | string
  tenantId: number
  tenantName: string
  actionType: number
  reason: string
  notifyMerchant: boolean
  unfreezeTime: string
  createTime: string
}

export interface TenantSettleConfig {
  id: number | string
  enabled: boolean
  settleNotice: string
  settleAgreement: string
  autoAudit: boolean
}

export const getTenantLevelList = () => request.get('/platform/tenant-manage/level/list')
export const createTenantLevel = (data: any) => request.post('/platform/tenant-manage/level/create', data)
export const updateTenantLevel = (id: number, data: any) => request.put(`/platform/tenant-manage/level/${id}`, data)
export const deleteTenantLevel = (id: number) => request.delete(`/platform/tenant-manage/level/${id}`)
export const getCategoryCommissionList = () => request.get('/platform/tenant-manage/commission/list')
export const batchSaveCategoryCommission = (data: any) => request.put('/platform/tenant-manage/commission/batch', data)
export const getSettlementList = (params: any) => request.get('/platform/tenant-manage/settlement/list', { params })
export const getSettlementDetail = (id: number) => request.get(`/platform/tenant-manage/settlement/${id}`)
export const settleSettlement = (id: number) => request.put(`/platform/tenant-manage/settlement/${id}/settle`)
export const getFreezeList = (params: any) => request.get('/platform/tenant-manage/freeze/list', { params })
export const freezeTenant = (tenantId: number, data: any) => request.post(`/platform/tenant-manage/freeze/${tenantId}`, data)
export const unfreezeTenant = (tenantId: number) => request.put(`/platform/tenant-manage/freeze/${tenantId}/unfreeze`)
export const getSettleConfig = () => request.get('/platform/tenant-manage/settle-config')
export const saveSettleConfig = (data: any) => request.put('/platform/tenant-manage/settle-config', data)
