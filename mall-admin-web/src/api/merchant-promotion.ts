import { request } from '@/utils/request'

export const getMerchantCouponList = (params: any) => request.get('/merchant/coupon/list', { params })
export const getMerchantCouponDetail = (id: number) => request.get(`/merchant/coupon/${id}`)
export const createMerchantCoupon = (data: any) => request.post('/merchant/coupon/create', data)
export const updateMerchantCoupon = (id: number, data: any) => request.put(`/merchant/coupon/${id}`, data)
export const deleteMerchantCoupon = (id: number) => request.delete(`/merchant/coupon/${id}`)

export const getMerchantSeckillList = (params: any) => request.get('/merchant/seckill/list', { params })
export const getMerchantSeckillDetail = (id: number) => request.get(`/merchant/seckill/${id}`)
export const createMerchantSeckill = (data: any) => request.post('/merchant/seckill/create', data)
export const updateMerchantSeckill = (id: number, data: any) => request.put(`/merchant/seckill/${id}`, data)
export const deleteMerchantSeckill = (id: number) => request.delete(`/merchant/seckill/${id}`)
export const startMerchantSeckill = (id: number) => request.put(`/merchant/seckill/${id}/start`)
export const endMerchantSeckill = (id: number) => request.put(`/merchant/seckill/${id}/end`)

export const getMerchantGroupBuyList = (params: any) => request.get('/merchant/group-buy/list', { params })
export const getMerchantGroupBuyDetail = (id: number) => request.get(`/merchant/group-buy/${id}`)
export const createMerchantGroupBuy = (data: any) => request.post('/merchant/group-buy/create', data)
export const updateMerchantGroupBuy = (id: number, data: any) => request.put(`/merchant/group-buy/${id}`, data)
export const deleteMerchantGroupBuy = (id: number) => request.delete(`/merchant/group-buy/${id}`)
export const startMerchantGroupBuy = (id: number) => request.put(`/merchant/group-buy/${id}/start`)
export const endMerchantGroupBuy = (id: number) => request.put(`/merchant/group-buy/${id}/end`)

export const getMerchantGrouponList = (params: any) => request.get('/merchant/groupon/list', { params })
export const createMerchantGroupon = (data: any) => request.post('/merchant/groupon/create', data)
export const updateMerchantGroupon = (id: number, data: any) => request.put(`/merchant/groupon/${id}`, data)
export const deleteMerchantGroupon = (id: number) => request.delete(`/merchant/groupon/${id}`)

export const getMerchantDiscountList = (params: any) => request.get('/merchant/discount/list', { params })
export const createMerchantDiscount = (data: any) => request.post('/merchant/discount/create', data)
export const updateMerchantDiscount = (id: number, data: any) => request.put(`/merchant/discount/${id}`, data)
export const deleteMerchantDiscount = (id: number) => request.delete(`/merchant/discount/${id}`)
