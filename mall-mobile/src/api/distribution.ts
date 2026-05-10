import { http } from '@/utils/request'

export interface DistributionCenterVO {
  isDistributor: boolean
  totalCommission: string
  availableCommission: string
  frozenCommission: string
  withdrawnCommission: string
  teamCount: number
  orderCount: number
}

export interface DistributionProductVO {
  id: number
  spuId: number
  productName: string
  mainImage: string
  price: string
  commissionRate: number
  commissionAmount: string
  salesCount: number
}

export interface CommissionVO {
  id: number
  orderNo: string
  amount: string
  type: number // 1-直推 2-间推
  status: number // 0-待结算 1-已结算 2-已冻结
  memberNickname: string
  createTime: string
}

export interface DistributionOrderVO {
  id: number
  orderNo: string
  productName: string
  mainImage: string
  orderAmount: string
  commissionAmount: string
  status: number
  createTime: string
}

export interface DistributionStatVO {
  todayCommission: string
  weekCommission: string
  monthCommission: string
  todayOrders: number
  weekOrders: number
  monthOrders: number
}

export interface CommissionTrendVO {
  date: string
  amount: string
}

export interface WithdrawVO {
  id: number
  amount: string
  status: number // 0-审核中 1-已通过 2-已拒绝 3-已打款
  applyTime: string
  auditTime: string
  remark: string
}

export interface TeamMemberVO {
  id: number
  nickname: string
  avatar: string
  level: number // 1-直推 2-间推
  orderCount: number
  commissionAmount: string
  joinTime: string
}

/** 申请成为分销员 */
export function applyDistributor(data: { realName: string; phone: string }) {
  return http.post<void>('/v1/member/distribution/apply', data)
}

/** 获取分销中心概览 */
export function getDistributionCenter() {
  return http.get<DistributionCenterVO>('/v1/member/distribution/center')
}

/** 获取分销商品列表 */
export function getDistributionProducts(params: { page?: number; limit?: number }) {
  return http.get<{ list: DistributionProductVO[]; total: number }>('/v1/member/distribution/product/list', params)
}

/** 获取佣金明细 */
export function getCommissionList(params: { page?: number; limit?: number }) {
  return http.get<{ list: CommissionVO[]; total: number }>('/v1/member/distribution/commission/list', params)
}

/** 获取分销订单列表 */
export function getDistributionOrders(params: { page?: number; limit?: number }) {
  return http.get<{ list: DistributionOrderVO[]; total: number }>('/v1/member/distribution/order/list', params)
}

/** 获取分销统计 */
export function getDistributionStat() {
  return http.get<DistributionStatVO>('/v1/member/distribution/stat')
}

/** 获取佣金趋势 */
export function getCommissionTrend(days: number = 7) {
  return http.get<CommissionTrendVO[]>('/v1/member/distribution/trend', { days })
}

/** 申请提现 */
export function applyWithdraw(data: { amount: number; bankCardId: number }) {
  return http.post<void>('/v1/member/distribution/withdraw', data)
}

/** 获取提现记录 */
export function getWithdrawList(params: { page?: number; limit?: number }) {
  return http.get<{ list: WithdrawVO[]; total: number }>('/v1/member/distribution/withdraw/list', params)
}

/** 获取我的团队 */
export function getTeamList(params: { page?: number; limit?: number; level?: number }) {
  return http.get<{ list: TeamMemberVO[]; total: number }>('/v1/member/distribution/team', params)
}
