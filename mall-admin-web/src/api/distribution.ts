import { request } from '@/utils/request'

export interface DistributionConfig {
  id: number | string
  enabled: boolean
  autoAudit: boolean
  commissionBase: number
  rateLevel1: number
  rateLevel2: number
  rateLevel3: number
  minWithdraw: number
  freezeDays: number
  dailyWithdrawLimit: number
  withdrawMethods: string
}

export interface Distributor {
  id: number | string
  memberId: number
  realName: string
  phone: string
  status: number
  totalCommission: number
  availableCommission: number
  frozenCommission: number
  createTime: string
}

export interface WithdrawRecord {
  id: number | string
  distributorId: number
  amount: number
  withdrawMethod: string
  status: number
  rejectReason: string
  createTime: string
}

export interface DistributorRank {
  distributorId: number
  realName: string
  phone: string
  totalCommission: number
  orderCount: number
}

export const getDistributionConfig = () => request.get('/platform/distribution/config')
export const saveDistributionConfig = (data: any) => request.put('/platform/distribution/config', data)
export const getDistributorList = (params: any) => request.get('/platform/distribution/distributor/list', { params })
export const auditDistributor = (id: number, status: number, rejectReason?: string) =>
  request.put(`/platform/distribution/distributor/${id}/audit`, null, { params: { status, rejectReason } })
export const getWithdrawList = (params: any) => request.get('/platform/distribution/withdraw/list', { params })
export const auditWithdraw = (id: number, status: number, rejectReason?: string) =>
  request.put(`/platform/distribution/withdraw/${id}/audit`, null, { params: { status, rejectReason } })
export const markWithdrawPaid = (id: number, paymentRemark: string) =>
  request.put(`/platform/distribution/withdraw/${id}/pay`, null, { params: { paymentRemark } })
export const getDistributorRank = (limit: number = 10) => request.get('/platform/distribution/rank', { params: { limit } })
export const getCommissionList = (params: any) => request.get('/platform/distribution/commission/list', { params })
