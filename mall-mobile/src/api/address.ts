import { http } from '@/utils/request'

export interface MemberAddressVO {
  id?: string
  receiverName: string
  receiverPhone: string
  provinceId?: string
  cityId?: string
  districtId?: string
  provinceName: string
  cityName: string
  districtName: string
  detailAddress: string
  fullAddress?: string
  isDefault: number
  tag?: string
}

export function getAddressList() {
  return http.get<MemberAddressVO[]>('/v1/member/address/list')
}

export function getAddress(id: string) {
  return http.get<MemberAddressVO>(`/v1/member/address/${id}`)
}

export function addAddress(data: MemberAddressVO) {
  return http.post<MemberAddressVO>('/v1/member/address/add', data)
}

export function updateAddress(id: string, data: MemberAddressVO) {
  return http.put(`/v1/member/address/${id}`, data)
}

export function deleteAddress(id: string) {
  return http.delete(`/v1/member/address/${id}`)
}
