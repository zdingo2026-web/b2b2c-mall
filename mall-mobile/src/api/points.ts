import { http } from '@/utils/request'

export interface PointsAccountVO {
  totalPoints: number
  availablePoints: number
  usedPoints: number
  frozenPoints: number
}

export interface PointsDetailVO {
  id: number
  points: number
  type: number // 1-收入 2-支出
  source: string
  description: string
  createTime: string
}

export interface PointsProductVO {
  id: number
  productName: string
  mainImage: string
  pointsPrice: number
  originalPrice: string
  totalStock: number
  availableStock: number
}

export interface PointsProductDetailVO {
  id: number
  productName: string
  mainImage: string
  images: string[]
  pointsPrice: number
  originalPrice: string
  totalStock: number
  availableStock: number
  description: string
  skuList: PointsProductSkuVO[]
}

export interface PointsProductSkuVO {
  id: number
  skuId: number
  specValues: string
  pointsPrice: number
  stock: number
}

export interface PointsOrderVO {
  id: number
  orderNo: string
  productName: string
  mainImage: string
  pointsAmount: number
  status: number // 0-待发货 1-已发货 2-已完成 3-已取消
  createTime: string
}

export interface CheckinStatusVO {
  checkedToday: boolean
  continuousDays: number
  todayPoints: number
}

/** 获取积分账户 */
export function getPointsAccount() {
  return http.get<PointsAccountVO>('/v1/member/points/account')
}

/** 获取积分明细 */
export function getPointsDetails(params: { page?: number; limit?: number }) {
  return http.get<{ list: PointsDetailVO[]; total: number }>('/v1/member/points/details', params)
}

/** 获取积分商品列表 */
export function getPointsProductList(params: { page?: number; limit?: number }) {
  return http.get<{ list: PointsProductVO[]; total: number }>('/v1/member/points/product/list', params)
}

/** 获取积分商品详情 */
export function getPointsProductDetail(id: number) {
  return http.get<PointsProductDetailVO>(`/v1/member/points/product/${id}`)
}

/** 创建积分兑换订单 */
export function createPointsOrder(data: { productId: number; skuId: number; quantity: number; addressId: number }) {
  return http.post<{ orderNo: string }>('/v1/member/points/order/create', data)
}

/** 获取积分订单列表 */
export function getPointsOrderList(params: { page?: number; limit?: number }) {
  return http.get<{ list: PointsOrderVO[]; total: number }>('/v1/member/points/order/list', params)
}

/** 获取积分订单详情 */
export function getPointsOrderDetail(id: number) {
  return http.get<PointsOrderVO>(`/v1/member/points/order/${id}`)
}

/** 取消积分订单 */
export function cancelPointsOrder(id: number) {
  return http.put<void>(`/v1/member/points/order/${id}/cancel`)
}

/** 确认收货积分订单 */
export function receivePointsOrder(id: number) {
  return http.put<void>(`/v1/member/points/order/${id}/receive`)
}

/** 签到 */
export function doCheckin() {
  return http.post<{ points: number }>('/v1/member/points/checkin')
}

/** 获取签到状态 */
export function getCheckinStatus() {
  return http.get<CheckinStatusVO>('/v1/member/points/checkin/status')
}
