import { http } from '@/utils/request'

export interface RealnameStatusVO {
  verified: boolean
  realName: string
  idCard: string
  status: number // 0-未认证 1-审核中 2-已认证 3-已拒绝
}

export interface RedPacketVO {
  id: number
  batchId: number
  name: string
  amount: string
  minAmount: string
  status: number // 0-未领取 1-已领取 2-已使用 3-已过期
  expireTime: string
  useTime: string
}

export interface MemberLevelVO {
  level: number
  name: string
  minPoints: number
  discount: number
  icon: string
}

export interface MemberCouponVO {
  id: number
  templateId: number
  couponName: string
  couponType: number // 1-满减 2-折扣 3-无门槛
  couponValue: string
  minAmount: string
  status: number // 0-未使用 1-已使用 2-已过期
  expireTime: string
}

/** 提交实名认证 */
export function submitRealname(data: { realName: string; idCard: string }) {
  return http.post<void>('/v1/member/account/realname', data)
}

/** 获取实名认证状态 */
export function getRealnameStatus() {
  return http.get<RealnameStatusVO>('/v1/member/account/realname/status')
}

/** 获取红包列表 */
export function getRedPacketList(params: { page?: number; limit?: number; status?: number }) {
  return http.get<{ list: RedPacketVO[]; total: number }>('/v1/member/account/red-packet/list', params)
}

/** 领取红包 */
export function claimRedPacket(batchId: number) {
  return http.post<{ amount: string }>(`/v1/member/account/red-packet/${batchId}/claim`)
}

/** 获取会员等级列表 */
export function getMemberLevelList() {
  return http.get<MemberLevelVO[]>('/v1/member/account/level/list')
}

/** 获取会员优惠券列表 */
export function getMemberCouponList(params: { page?: number; limit?: number; status?: number }) {
  return http.get<{ list: MemberCouponVO[]; total: number }>('/v1/member/account/coupon/list', params)
}

/** 领取优惠券 */
export function claimCoupon(templateId: number) {
  return http.post<void>(`/v1/member/account/coupon/${templateId}/claim`)
}

/** 获取可用优惠券(下单时) */
export function getAvailableCoupons(params: { spuId?: number; amount?: number }) {
  return http.get<MemberCouponVO[]>('/v1/member/account/coupon/available', params)
}
