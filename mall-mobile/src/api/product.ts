import { http } from '@/utils/request'

export interface SpuVO {
  id: string
  categoryId: string
  brandId: string
  productName: string
  subTitle: string
  mainImage: string
  minPrice: string
  maxPrice: string
  originalPrice: string
  totalStock: number
  totalSales: number
  status: number
  tagType: number
  tenantId: string
  tenantName: string
  categoryName: string
  brandName: string
  createTime: string
}

export interface SkuVO {
  id: string
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

export interface SpuDetailVO extends SpuVO {
  images: string[]
  description: string
  skuList: SkuVO[]
  couponTag: string
  commentSummary: CommentSummaryVO
  latestComments: ProductCommentVO[]
  attributeList: { attributeId: string; attributeName: string; attributeValue: string; attributeType: number }[]
  imageList: { id: string; imageUrl: string; sortOrder: number; imageType: number }[]
}

export interface CategoryTreeVO {
  id: string
  parentId: string
  categoryName: string
  icon: string
  image: string
  sortOrder: number
  level: number
  status: number
  children: CategoryTreeVO[]
}

export interface ProductBrand {
  id: string
  brandName: string
  brandLogo: string
}

export function getSpuList(params?: {
  categoryId?: string
  brandId?: string
  keyword?: string
  status?: number
  page?: number
  limit?: number
}) {
  return http.get<{ list: SpuVO[]; total: number }>('/v1/member/product/spu/list', params)
}

export function getSpuDetail(id: string) {
  return http.get<SpuDetailVO>(`/v1/member/product/spu/${id}`)
}

export function getCategoryTree(tenantId?: string) {
  return http.get<CategoryTreeVO[]>('/v1/member/product/category/tree', { tenantId: tenantId || '0' })
}

export function getBrandList() {
  return http.get<ProductBrand[]>('/v1/member/product/brand/list')
}

export interface CommentTagVO {
  tagName: string
  count: number
}

export interface CommentSummaryVO {
  totalCount: number
  goodRate: number
  tags: CommentTagVO[]
}

export interface ProductCommentVO {
  id: string
  memberId: string
  memberName: string
  memberAvatar: string
  score: number
  content: string
  images: string[]
  isAnonymous: boolean
  replyContent: string
  replyTime: string
  likeCount: number
  createTime: string
}

export function getCommentSummary(spuId: string) {
  return http.get<CommentSummaryVO>(`/v1/member/product/spu/${spuId}/comment-summary`)
}

export function getCommentList(params: { spuId: string; page: number; limit: number }) {
  return http.get<{ list: ProductCommentVO[]; total: number }>('/v1/member/product/comment/list', params)
}

export function likeComment(commentId: string) {
  return http.post(`/v1/member/product/comment/${commentId}/like`)
}
