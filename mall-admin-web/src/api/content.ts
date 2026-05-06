import { request } from '@/utils/request'

export interface ContentBanner {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  linkType: number
  sort: number
  startTime: string
  endTime: string
  status: number
}

export interface BannerSaveParams {
  title: string
  imageUrl: string
  linkUrl: string
  linkType: number
  sort: number
  startTime: string
  endTime: string
  status: number
}

export interface ContentFloor {
  id: number
  floorName: string
  categoryId: number
  style: number
  productCount: number
  sort: number
  status: number
}

export interface FloorSaveParams {
  floorName: string
  categoryId: number
  style: number
  productCount: number
  sort: number
  status: number
}

// Banner
export function getBannerList() {
  return request.get<ContentBanner[]>('/v1/platform/content/banner/list')
}

export function getBannerDetail(id: number) {
  return request.get<ContentBanner>(`/v1/platform/content/banner/${id}`)
}

export function createBanner(data: BannerSaveParams) {
  return request.post<number>('/v1/platform/content/banner/create', data)
}

export function updateBanner(id: number, data: BannerSaveParams) {
  return request.put(`/v1/platform/content/banner/update/${id}`, data)
}

export function deleteBanner(id: number) {
  return request.delete(`/v1/platform/content/banner/delete/${id}`)
}

// Floor
export function getFloorList() {
  return request.get<ContentFloor[]>('/v1/platform/content/floor/list')
}

export function getFloorDetail(id: number) {
  return request.get<ContentFloor>(`/v1/platform/content/floor/${id}`)
}

export function createFloor(data: FloorSaveParams) {
  return request.post<number>('/v1/platform/content/floor/create', data)
}

export function updateFloor(id: number, data: FloorSaveParams) {
  return request.put(`/v1/platform/content/floor/update/${id}`, data)
}

export function deleteFloor(id: number) {
  return request.delete(`/v1/platform/content/floor/delete/${id}`)
}
