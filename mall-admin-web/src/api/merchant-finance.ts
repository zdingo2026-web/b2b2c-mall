import { request } from '@/utils/request'

export interface BalanceVO {
  totalSettled: string
  pendingWithdrawal: string
  availableBalance: string
}

export interface TenantSettleVO {
  id: number | string
  settleNo: string
  orderAmount: string
  commissionAmount: string
  settleAmount: string
  status: number  // 0=待结算, 1=已结算
  periodStart: string
  periodEnd: string
  createTime: string
}

export function getMerchantBalance() {
  return request.get<BalanceVO>('/v1/merchant/finance/balance')
}

export function getMerchantSettleList(params?: { page?: number; limit?: number }) {
  return request.get<{ list: TenantSettleVO[]; total: number }>('/v1/merchant/finance/settle/list', params)
}

export function requestWithdraw(amount: number | string) {
  return request.post<void>('/v1/merchant/finance/withdraw', { amount })
}
