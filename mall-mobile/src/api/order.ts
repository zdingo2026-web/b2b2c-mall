import { http } from '@/utils/request'

export interface OrderItemVO {
  id: string
  spuId: string
  skuId: string
  productName: string
  specValues: string
  productImage: string
  price: string
  quantity: number
  subtotal: string
  payAmount: string
}

export interface OrderAddressVO {
  receiverName: string
  receiverPhone: string
  fullAddress: string
  isDefault?: boolean
}

export interface OrderMainVO {
  id: string
  orderNo: string
  memberId: string
  parentId: string
  orderType: number
  orderStatus: number
  payStatus: number
  totalAmount: string
  freightAmount: string
  discountAmount: string
  payAmount: string
  payType: number
  payTime: string
  deliveryType: number
  logisticsCompany: string
  logisticsNo: string
  deliveryTime: string
  receiveTime: string
  remark: string
  expireTime: string
  tenantId: string
  tenantName?: string
  isReviewed?: number
  invoiceType?: number
  invoiceTitle?: string
  deliveryNo?: string
  deliveryCompany?: string
  deliveryStatus?: number
  createTime: string
  items: OrderItemVO[]
  address: OrderAddressVO
}

export interface OrderLogisticsVO {
  deliveryNo: string
  deliveryCompany: string
  deliveryStatus: number
  deliveryStatusText: string
  traces: { time: string; description: string }[]
}

export function createOrder(data: {
  addressId: string
  items: { skuId: string; quantity: number }[]
  remark?: string
  couponId?: string
  invoiceType?: number
  invoiceTitle?: string
}) {
  return http.post<OrderMainVO>('/v1/member/order/create', data)
}

export function getOrderList(params?: { status?: number; page?: number; limit?: number }) {
  return http.get<{ list: OrderMainVO[]; total: number }>('/v1/member/order/list', params)
}

export function getOrderDetail(orderNo: string) {
  return http.get<OrderMainVO>(`/v1/member/order/${orderNo}`)
}

export function getOrderLogistics(orderNo: string) {
  return http.get<OrderLogisticsVO>(`/v1/member/order/${orderNo}/logistics`)
}

export function cancelOrder(orderNo: string) {
  return http.put(`/v1/member/order/${orderNo}/cancel`)
}

export function confirmReceive(id: string) {
  return http.put(`/v1/member/order/item/${id}/confirm`)
}

export function applyRefund(data: { orderItemId: string; reason: string; amount: string }) {
  return http.post('/v1/member/order/refund/apply', data)
}

/** Order status tab definitions */
export const ORDER_TABS = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '待评价', value: 7 },
] as const

/** Status label map */
export const ORDER_STATUS_MAP: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款',
  7: '待评价',
}

/** Status color map */
export const ORDER_STATUS_COLOR: Record<number, string> = {
  0: '#F97316',
  1: '#3B82F6',
  2: '#8B5CF6',
  3: '#10B981',
  4: '#9CA3AF',
  5: '#EF4444',
  6: '#9CA3AF',
  7: '#2563EB',
}
