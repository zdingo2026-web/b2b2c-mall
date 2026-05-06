import { http } from '@/utils/request'

export interface BannerVO {
  id: string
  title: string
  imageUrl: string
  linkUrl: string
  linkType: number
}

export interface FloorVO {
  id: string
  floorName: string
  categoryId: string
  style: number
  productCount: number
  sort: number
  products: { id: string; productName: string; mainImage: string; minPrice: string }[]
}

export interface NoticeVO {
  id: string
  title: string
}

export interface QuickEntryVO {
  id: string
  name: string
  icon: string
  linkUrl: string
  linkType: number
  sort: number
}

export interface HomeVO {
  banners: BannerVO[]
  floors: FloorVO[]
  notices: NoticeVO[]
  quickEntries: QuickEntryVO[]
}

export function getHomeData() {
  return http.get<HomeVO>('/v1/member/home')
}

export function getBannerList() {
  return http.get<BannerVO[]>('/v1/member/content/banner/list')
}

export function getFloorList() {
  return http.get<FloorVO[]>('/v1/member/content/floor/list')
}
