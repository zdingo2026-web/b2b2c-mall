import { http } from '@/utils/request'

export interface SeckillTimeSlotVO {
  slotId: number
  startTime: string
  endTime: string
  label: string
  status: number // 0-未开始 1-进行中 2-已结束
}

export interface SeckillProductVO {
  id: number
  activityId: number
  spuId: number
  skuId: number
  productName: string
  mainImage: string
  seckillPrice: string
  originalPrice: string
  totalStock: number
  availableStock: number
  limitPerUser: number
  salesCount: number
}

/** 获取秒杀时段列表 */
export function getSeckillTimeSlots() {
  return http.get<SeckillTimeSlotVO[]>('/v1/member/seckill/time-slots')
}

/** 获取秒杀商品列表 */
export function getSeckillProducts(params: { slotId: number; page?: number; limit?: number }) {
  return http.get<{ list: SeckillProductVO[]; total: number }>('/v1/member/seckill/products', params)
}

/** 执行秒杀 */
export function doSeckill(activityId: number, skuId: number) {
  return http.post<{ orderNo: string }>(`/v1/member/seckill/${activityId}/seckill`, { skuId })
}

/** 查询秒杀结果 */
export function getSeckillResult(params: { activityId: number; skuId: number }) {
  return http.get<{ status: number; orderNo: string }>('/v1/member/seckill/result', params)
}
