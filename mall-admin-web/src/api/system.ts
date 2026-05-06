import { request } from '@/utils/request'

export interface ConfigVO {
  id: number
  configGroup: string
  configKey: string
  configValue: string
  configDesc: string
  sort: number
}

export interface ConfigUpdateParams {
  id: number
  configValue: string
}

export function getConfigList(configGroup?: string) {
  return request.get<ConfigVO[]>('/v1/platform/config/list', { configGroup })
}

export function updateConfig(data: ConfigUpdateParams) {
  return request.put('/v1/platform/config/update', data)
}

export function uploadFile(file: File) {
  const userType = Number(localStorage.getItem('mall_admin_user_type') || '1')
  const endpoint = userType === 2 ? '/v1/merchant/file/upload' : '/v1/platform/file/upload'
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>(endpoint, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
