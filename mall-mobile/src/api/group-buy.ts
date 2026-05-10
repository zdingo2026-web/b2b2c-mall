import { http } from '@/utils/request'

export interface GroupBuyProductVO {
  id: number
  activityId: number
  spuId: number
  productName: string
  mainImage: string
  groupPrice: string
  originalPrice: string
  groupSize: number
  joinedCount: number
  totalStock: number
  limitPerUser: number
  endTime: string
}

export interface GroupBuyDetailVO {
  activityId: number
  spuId: number
  productName: string
  mainImage: string
  images: string[]
  groupPrice: string
  originalPrice: string
  groupSize: number
  joinedCount: number
  totalStock: number
  limitPerUser: number
  endTime: string
  description: string
  skuList: GroupBuySkuVO[]
  groups: GroupBuyGroupVO[]
}

export interface GroupBuySkuVO {
  id: number
  skuId: number
  specValues: string
  groupPrice: string
  stock: number
}

export interface GroupBuyGroupVO {
  groupId: number
  leaderNickname: string
  leaderAvatar: string
  joinedCount: number
  groupSize: number
  remainCount: number
  expireTime: string
}

/** 获取拼团商品列表 */
export function getGroupBuyProducts(params: { page?: number; limit?: number }) {
  return http.get<{ list: GroupBuyProductVO[]; total: number }>('/v1/member/group-buy/products', params)
}

/** 获取拼团活动详情 */
export function getGroupBuyDetail(activityId: number) {
  return http.get<GroupBuyDetailVO>(`/v1/member/group-buy/${activityId}`)
}

/** 参与拼团 */
export function joinGroupBuy(activityId: number, data: { skuId: number; groupId?: number }) {
  return http.post<{ orderNo: string; groupId: number }>(`/v1/member/group-buy/${activityId}/join`, data)
}
