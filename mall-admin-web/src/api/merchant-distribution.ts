import { request } from '@/utils/request'

export const getMerchantDistributionConfig = () => request.get('/merchant/distribution/config')
export const saveMerchantDistributionConfig = (data: any) => request.put('/merchant/distribution/config', data)
export const getMerchantDistributorList = (params: any) => request.get('/merchant/distribution/distributor/list', { params })
export const getMerchantCommissionList = (params: any) => request.get('/merchant/distribution/commission/list', { params })
export const getMerchantDistributionProductList = (params: any) => request.get('/merchant/distribution/product/list', { params })
export const updateMerchantDistributionProduct = (spuId: number, data: any) => request.put(`/merchant/distribution/product/${spuId}`, data)
