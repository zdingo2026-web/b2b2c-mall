import { http } from '@/utils/request'

export function createPayment(orderId: string, payType: number) {
  return http.post<Record<string, any>>(`/v1/member/payment/create?orderId=${orderId}&payType=${payType}`)
}

export function payOrder(orderNo: string, payType: number) {
  return http.post<Record<string, any>>(`/v1/member/payment/pay?orderNo=${orderNo}&payType=${payType}`)
}

export function getPaymentStatus(paymentNo: string) {
  return http.get<Record<string, any>>(`/v1/member/payment/${paymentNo}/status`)
}
