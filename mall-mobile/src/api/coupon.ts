import { http } from '@/utils/request'

export interface CouponVO {
  id: string
  couponName: string
  couponType: number  // 1-满减 2-折扣 3-无门槛
  couponValue: string
  minAmount: string
  status: number  // 0-未使用 1-已使用 2-已过期
  expireTime: string
  useTime: string
}

export function getCouponList(params?: { status?: number; page?: number; limit?: number }) {
  return http.get<{ list: CouponVO[]; total: number }>('/v1/member/coupon/list', params)
}
