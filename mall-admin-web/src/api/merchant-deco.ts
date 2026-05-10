import { request } from '@/utils/request'

export const getDecoPageList = (params: any) => request.get('/merchant/deco/page/list', { params })
export const getDecoPageDetail = (id: number) => request.get(`/merchant/deco/page/${id}`)
export const saveDecoPage = (data: any) => request.post('/merchant/deco/page/save', data)
export const publishDecoPage = (id: number) => request.put(`/merchant/deco/page/${id}/publish`)
export const deleteDecoPage = (id: number) => request.delete(`/merchant/deco/page/${id}`)

export const getDecoAlbumList = () => request.get('/merchant/deco/album/list')
export const createDecoAlbum = (data: any) => request.post('/merchant/deco/album/create', data)
export const updateDecoAlbum = (id: number, data: any) => request.put(`/merchant/deco/album/${id}`, data)
export const deleteDecoAlbum = (id: number) => request.delete(`/merchant/deco/album/${id}`)
export const getDecoImages = (albumId: number, params: any) => request.get(`/merchant/deco/album/${albumId}/images`, { params })
export const uploadDecoImage = (albumId: number, data: any) => request.post(`/merchant/deco/album/${albumId}/image`, data)
export const deleteDecoImage = (imageId: number) => request.delete(`/merchant/deco/image/${imageId}`)

export const getDecoTemplateList = (params: any) => request.get('/merchant/deco/template/list', { params })
export const getDecoTemplateDetail = (id: number) => request.get(`/merchant/deco/template/${id}`)
export const createDecoTemplate = (data: any) => request.post('/merchant/deco/template/create', data)
