import { http } from '@/utils/request'

export interface CartGroupVO {
  tenantId: string
  tenantName: string
  items: CartItemVO[]
  subtotal: string
}

export interface CartItemVO {
  id: string
  spuId: string
  skuId: string
  productName: string
  specValues: string
  productImage: string
  price: string
  quantity: number
  isChecked: number
  stock: number
  status: number
}

export function getCartList() {
  return http.get<CartGroupVO[]>('/v1/member/cart/list')
}

export function addCartItem(data: { spuId: string; skuId: string; quantity: number }) {
  return http.post('/v1/member/cart/add', data)
}

export function updateCartItem(id: string, quantity: number) {
  return http.put(`/v1/member/cart/item/${id}`, null, { quantity } as any)
}

export function deleteCartItem(id: string) {
  return http.delete(`/v1/member/cart/item/${id}`)
}
