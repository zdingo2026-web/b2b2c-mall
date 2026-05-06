import { request } from '@/utils/request'

export interface OrderItemVO {
  id: number
  spuId: number
  skuId: number
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
}

export interface OrderMainVO {
  id: number
  orderNo: string
  memberId: number
  parentId: number
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
  tenantId: number
  createTime: string
  invoiceType: number
  invoiceTitle: string
  deliveryNo: string
  deliveryCompany: string
  deliveryStatus: number
  items: OrderItemVO[]
  address: OrderAddressVO
}

export interface OrderQueryParams {
  orderNo?: string
  orderStatus?: number
  payStatus?: number
  beginTime?: string
  endTime?: string
  page?: number
  limit?: number
}

export interface OrderRefund {
  id: number
  orderNo: string
  refundNo: string
  refundAmount: string
  reason: string
  status: number
  createTime: string
}

// Platform order
export function getPlatformOrderList(params?: OrderQueryParams) {
  return request.get<{ list: OrderMainVO[]; total: number }>('/v1/platform/order/list', params)
}

export function getPlatformOrderDetail(orderNo: string) {
  return request.get<OrderMainVO>(`/v1/platform/order/${orderNo}`)
}

export function shipPlatformOrder(orderNo: string, logisticsCompany: string, logisticsNo: string) {
  return request.put(`/v1/platform/order/${orderNo}/ship`, null, {
    params: { logisticsCompany, logisticsNo },
  })
}

export function cancelPlatformOrder(orderNo: string) {
  return request.put(`/v1/platform/order/${orderNo}/cancel`)
}

// Merchant order
export function getMerchantOrderList(params?: OrderQueryParams) {
  return request.get<{ list: OrderMainVO[]; total: number }>('/v1/merchant/order/list', params)
}

export function getMerchantOrderDetail(orderNo: string) {
  return request.get<OrderMainVO>(`/v1/merchant/order/${orderNo}`)
}

export function shipOrderItem(id: number, logisticsCompany: string, logisticsNo: string) {
  return request.put(`/v1/merchant/order/item/${id}/ship`, null, {
    params: { logisticsCompany, logisticsNo },
  })
}

export function cancelMerchantOrder(orderNo: string) {
  return request.put(`/v1/merchant/order/${orderNo}/cancel`)
}

// Refund
export function getRefundList(params?: { page?: number; limit?: number }) {
  return request.get<{ list: OrderRefund[]; total: number }>('/v1/merchant/order/refund/list', params)
}

export function handleRefund(id: number, pass: boolean, reason?: string) {
  return request.put(`/v1/merchant/order/refund/${id}/handle`, null, { params: { pass, reason } })
}
