import { http } from '@/utils/request'

export interface CollectVO {
  id: string
  spuId: string
  productName: string
  mainImage: string
  minPrice: string
  originalPrice?: string
  tagType?: number
  totalSales?: number
}

export function getCollectList(params?: { page?: number; limit?: number; collectType?: number }) {
  return http.get<{ list: CollectVO[]; total: number }>('/v1/member/collect/list', params)
}

export function toggleCollect(spuId: string, collectType: number = 1) {
  return http.post('/v1/member/collect/toggle', { spuId, collectType })
}
