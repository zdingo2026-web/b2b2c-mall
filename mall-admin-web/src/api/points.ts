import { request } from '@/utils/request'

export interface PointsRule {
  id: number | string
  ruleType: number
  ruleName: string
  pointsValue: number
  multiplier: number
  dailyLimit: number
  enabled: boolean
  sortOrder: number
}

export interface PointsConsumeRule {
  id: number | string
  exchangeRate: number
  maxDeductRate: number
  validityType: number
  validityDays: number
}

export interface PointsProduct {
  id: number | string
  categoryId: number
  productName: string
  productImage: string
  exchangeType: number
  pointsPrice: number
  cashPrice: number
  stock: number
  sales: number
  status: number
}

export const getPointsRuleList = () => request.get('/platform/points/rule/list')
export const savePointsRule = (data: any) => request.post('/platform/points/rule/save', data)
export const togglePointsRule = (id: number) => request.put(`/platform/points/rule/${id}/toggle`)
export const getPointsConsumeRule = () => request.get('/platform/points/consume-rule')
export const savePointsConsumeRule = (data: any) => request.put('/platform/points/consume-rule', data)
export const getPointsCategoryList = () => request.get('/platform/points/category/list')
export const savePointsCategory = (data: any) => request.post('/platform/points/category/save', data)
export const deletePointsCategory = (id: number) => request.delete(`/platform/points/category/${id}`)
export const getPointsProductList = (params: any) => request.get('/platform/points/product/list', { params })
export const createPointsProduct = (data: any) => request.post('/platform/points/product/create', data)
export const updatePointsProduct = (id: number, data: any) => request.put(`/platform/points/product/${id}`, data)
export const deletePointsProduct = (id: number) => request.delete(`/platform/points/product/${id}`)
export const updatePointsProductStatus = (id: number, status: number) => request.put(`/platform/points/product/${id}/status`, null, { params: { status } })
export const getPointsAccountList = (params: any) => request.get('/platform/points/account/list', { params })
export const getPointsDetailList = (params: any) => request.get('/platform/points/detail/list', { params })
export const getPointsOrderList = (params: any) => request.get('/platform/points/order/list', { params })
