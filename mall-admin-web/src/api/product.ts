import { request } from '@/utils/request'

export interface SpuVO {
  id: number | string
  categoryId: number | string
  brandId: number | string
  productName: string
  subTitle: string
  mainImage: string
  minPrice: string
  maxPrice: string
  totalStock: number
  totalSales: number
  status: number
  tenantId: number | string
  tenantName: string
  categoryName: string
  brandName: string
  createTime: string
}

export interface SkuVO {
  id: number | string
  skuName: string
  skuCode: string
  specValues: string
  price: string
  originalPrice: string
  stock: number
  image: string
  weight: string
  status: number
}

export interface AttributeValueVO {
  attributeId: number
  attributeName: string
  attributeValue: string
  attributeType: number
}

export interface ProductImageVO {
  id: number | string
  imageUrl: string
  sortOrder: number
  imageType: number
}

export interface SpuDetailVO extends SpuVO {
  images: string[]
  description: string
  skuList: SkuVO[]
  attributeList: AttributeValueVO[]
  imageList: ProductImageVO[]
}

export interface SpuCreateParams {
  categoryId: number | string
  brandId: number | string
  productName: string
  subTitle: string
  mainImage: string
  images: string[]
  description: string
  skuList: {
    skuName: string
    skuCode: string
    specValues: string
    price: string
    originalPrice: string
    stock: number
    image: string
    weight: string
  }[]
  attributeList: {
    attributeId: number
    attributeValue: string
    attributeType: number
  }[]
}

export interface SpuQueryParams {
  categoryId?: number
  brandId?: number
  keyword?: string
  status?: number
  minPrice?: string
  maxPrice?: string
  sortField?: string
  sortOrder?: string
  page?: number
  limit?: number
}

export interface CategoryTreeVO {
  id: number | string
  parentId: number | string
  categoryName: string
  icon: string
  image: string
  sortOrder: number
  level: number
  status: number
  children: CategoryTreeVO[]
}

export interface ProductCategory {
  id: number | string
  parentId: number | string
  categoryName: string
  icon: string
  image: string
  sortOrder: number
  level: number
  status: number
}

export interface ProductBrand {
  id: number | string
  brandName: string
  logo: string
  description: string
  sortOrder: number
  status: number
}

// Platform SPU
export function getPlatformSpuList(params?: SpuQueryParams) {
  return request.get<{ list: SpuVO[]; total: number }>('/v1/platform/product/spu/list', params)
}

export function getPlatformSpuDetail(id: number | string) {
  return request.get<SpuDetailVO>(`/v1/platform/product/spu/${id}`)
}

export function createPlatformSpu(data: SpuCreateParams) {
  return request.post<number>('/v1/platform/product/spu', data)
}

export function updatePlatformSpu(id: number | string, data: SpuCreateParams) {
  return request.put(`/v1/platform/product/spu/${id}`, data)
}

export function publishPlatformSpu(id: number | string) {
  return request.put(`/v1/platform/product/spu/${id}/publish`)
}

export function unpublishPlatformSpu(id: number | string) {
  return request.put(`/v1/platform/product/spu/${id}/unpublish`)
}

export function auditPlatformSpu(id: number | string, pass: boolean, reason?: string) {
  return request.put(`/v1/platform/product/spu/${id}/audit`, null, { params: { pass, reason } })
}

// Merchant SPU
export function getMerchantSpuList(params?: SpuQueryParams) {
  return request.get<{ list: SpuVO[]; total: number }>('/v1/merchant/product/spu/list', params)
}

export function getMerchantSpuDetail(id: number | string) {
  return request.get<SpuDetailVO>(`/v1/merchant/product/spu/${id}`)
}

export function createMerchantSpu(data: SpuCreateParams) {
  return request.post<number>('/v1/merchant/product/spu', data)
}

export function updateMerchantSpu(id: number | string, data: SpuCreateParams) {
  return request.put(`/v1/merchant/product/spu/${id}`, data)
}

export function publishMerchantSpu(id: number | string) {
  return request.put(`/v1/merchant/product/spu/${id}/publish`)
}

export function unpublishMerchantSpu(id: number | string) {
  return request.put(`/v1/merchant/product/spu/${id}/unpublish`)
}

export function deleteMerchantSpu(id: number | string) {
  return request.delete(`/v1/merchant/product/spu/${id}`)
}

// Platform Category
export function getCategoryTree() {
  return request.get<CategoryTreeVO[]>('/v1/platform/product/category/tree')
}

export function getCategoryList() {
  return request.get<ProductCategory[]>('/v1/platform/product/category/list')
}

export function createCategory(data: Partial<ProductCategory>) {
  return request.post<ProductCategory>('/v1/platform/product/category', data)
}

export function updateCategory(id: number | string, data: Partial<ProductCategory>) {
  return request.put(`/v1/platform/product/category/${id}`, data)
}

export function deleteCategory(id: number | string) {
  return request.delete(`/v1/platform/product/category/${id}`)
}

// Merchant Category (read-only)
export function getMerchantCategoryTree() {
  return request.get<CategoryTreeVO[]>('/v1/merchant/product/category/tree')
}

export function getMerchantCategoryList() {
  return request.get<ProductCategory[]>('/v1/merchant/product/category/list')
}

// Brand
export function getBrandList(params?: { page?: number; limit?: number }) {
  const userType = Number(localStorage.getItem('mall_admin_user_type') || '1')
  const endpoint = userType === 2 ? '/v1/merchant/product/brand/list' : '/v1/platform/product/brand/list'
  return request.get<{ list: ProductBrand[]; total: number }>(endpoint, params)
}

export function createBrand(data: Partial<ProductBrand>) {
  return request.post<ProductBrand>('/v1/platform/product/brand', data)
}

export function updateBrand(id: number | string, data: Partial<ProductBrand>) {
  return request.put(`/v1/platform/product/brand/${id}`, data)
}

export function deleteBrand(id: number | string) {
  return request.delete(`/v1/platform/product/brand/${id}`)
}
