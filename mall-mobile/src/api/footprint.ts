import { http } from '@/utils/request'

export interface FootprintVO {
  id: string
  spuId: string
  productName: string
  mainImage: string
  minPrice: string
  createTime: string
}

export interface FootprintGroupVO {
  label: string
  items: FootprintVO[]
}

export function getFootprintList(params?: { page?: number; limit?: number }) {
  return http.get<{ list: FootprintVO[]; total: number }>('/v1/member/footprint/list', params)
}

export function getFootprintGrouped() {
  return http.get<FootprintGroupVO[]>('/v1/member/footprint/grouped')
}

export function clearFootprint() {
  return http.delete('/v1/member/footprint/clear')
}
