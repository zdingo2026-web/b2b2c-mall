import { http } from '@/utils/request'

export interface BankCardVO {
  id: string
  bankName: string
  bankCode: string
  cardNoMask: string
  cardType: number
  cardColor: string
  bankLogo: string
  expiryDate: string
  isDefault: number
}

export interface BankCardAddDTO {
  bankName: string
  bankCode?: string
  cardNo: string
  cardType: number
  cardColor?: string
  bankLogo?: string
  expiryDate?: string
}

export const getBankCardList = () => {
  return http.get<BankCardVO[]>('/v1/member/bank-card/list')
}

export const addBankCard = (data: BankCardAddDTO) => {
  return http.post<BankCardVO>('/v1/member/bank-card/add', data)
}

export const deleteBankCard = (id: string) => {
  return http.delete(`/v1/member/bank-card/${id}`)
}

export const setDefaultBankCard = (id: string) => {
  return http.put(`/v1/member/bank-card/${id}/default`)
}
