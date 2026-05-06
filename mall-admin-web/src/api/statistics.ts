import { request } from '@/utils/request'

export interface PlatformOverview {
  todayOrderCount: number
  todayOrderAmount: string
  todayNewMember: number
  todayVisitCount: number
  yesterdayOrderCount: number
  yesterdayOrderAmount: string
}

export interface MerchantOverview {
  todayOrderCount: number
  todayOrderAmount: string
  todayAvgOrderPrice: string
  yesterdayOrderCount: number
  yesterdayOrderAmount: string
  yesterdayAvgOrderPrice: string
  totalOrderAmount: string
  shopConversionRate: string
}

export function getPlatformOverview() {
  return request.get<PlatformOverview>('/v1/platform/statistics/overview')
}

export function getMerchantOverview() {
  return request.get<MerchantOverview>('/v1/merchant/statistics/overview')
}

export interface TrendData {
  date: string
  orderCount: number
  orderAmount: string
}

export interface ProductRank {
  spuId: number | string
  productName: string
  mainImage: string
  totalSales: number
  totalAmount: string
}

export function getMerchantTrend(days?: number) {
  return request.get<TrendData[]>('/v1/merchant/statistics/trend', { days })
}

export function getMerchantProductRanking() {
  return request.get<ProductRank[]>('/v1/merchant/statistics/product-ranking')
}
