import { request } from '@/utils/request'

export interface CouponTemplate {
  id: number | string
  tenantId: number | string
  couponName: string
  couponType: number
  couponValue: number
  minAmount: number
  totalCount: number
  remainCount: number
  limitPerUser: number
  applyScope: number
  validType: number
  validStartTime: string
  validEndTime: string
  validDays: number
  canStackSeckill: boolean
  status: number
  createTime: string
}

export interface SeckillActivity {
  id: number | string
  activityName: string
  startTime: string
  endTime: string
  paymentTimeout: number
  status: number
  createTime: string
}

export interface GroupBuyActivity {
  id: number | string
  activityName: string
  spuId: number
  groupPrice: number
  groupNum: number
  limitPerUser: number
  duration: number
  startTime: string
  endTime: string
  status: number
}

export interface GrouponActivity {
  id: number | string
  activityName: string
  spuId: number
  grouponPrice: number
  limitPerUser: number
  startTime: string
  endTime: string
  status: number
}

export const getCouponList = (params: any) => request.get('/platform/coupon/list', { params })
export const getCouponDetail = (id: number) => request.get(`/platform/coupon/${id}`)
export const createCoupon = (data: any) => request.post('/platform/coupon/create', data)
export const updateCoupon = (id: number, data: any) => request.put(`/platform/coupon/${id}`, data)
export const deleteCoupon = (id: number) => request.delete(`/platform/coupon/${id}`)

export const getSeckillList = (params: any) => request.get('/platform/seckill/list', { params })
export const getSeckillDetail = (id: number) => request.get(`/platform/seckill/${id}`)
export const createSeckill = (data: any) => request.post('/platform/seckill/create', data)
export const updateSeckill = (id: number, data: any) => request.put(`/platform/seckill/${id}`, data)
export const deleteSeckill = (id: number) => request.delete(`/platform/seckill/${id}`)
export const startSeckill = (id: number) => request.put(`/platform/seckill/${id}/start`)
export const endSeckill = (id: number) => request.put(`/platform/seckill/${id}/end`)

export const getGroupBuyList = (params: any) => request.get('/platform/group-buy/list', { params })
export const getGroupBuyDetail = (id: number) => request.get(`/platform/group-buy/${id}`)
export const createGroupBuy = (data: any) => request.post('/platform/group-buy/create', data)
export const updateGroupBuy = (id: number, data: any) => request.put(`/platform/group-buy/${id}`, data)
export const deleteGroupBuy = (id: number) => request.delete(`/platform/group-buy/${id}`)
export const startGroupBuy = (id: number) => request.put(`/platform/group-buy/${id}/start`)
export const endGroupBuy = (id: number) => request.put(`/platform/group-buy/${id}/end`)

export const getGrouponList = (params: any) => request.get('/platform/groupon/list', { params })
export const createGroupon = (data: any) => request.post('/platform/groupon/create', data)
export const updateGroupon = (id: number, data: any) => request.put(`/platform/groupon/${id}`, data)
export const deleteGroupon = (id: number) => request.delete(`/platform/groupon/${id}`)

export const getDiscountList = (params: any) => request.get('/platform/discount/list', { params })
export const createDiscount = (data: any) => request.post('/platform/discount/create', data)
export const updateDiscount = (id: number, data: any) => request.put(`/platform/discount/${id}`, data)
export const deleteDiscount = (id: number) => request.delete(`/platform/discount/${id}`)
export const getNewcomerPack = () => request.get('/platform/promotion/newcomer-pack')
export const saveNewcomerPack = (data: any) => request.put('/platform/promotion/newcomer-pack', data)
export const getFirstOrderConfig = () => request.get('/platform/promotion/first-order')
export const saveFirstOrderConfig = (data: any) => request.put('/platform/promotion/first-order', data)
