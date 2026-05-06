import { defineStore } from 'pinia'
import { http } from '@/utils/request'

export interface CartItem {
  id: string
  productId: string
  skuId: string
  name: string
  spec: string
  price: number
  quantity: number
  image: string
  storeName: string
  storeId: string
  selected: boolean
}

export const useCartStore = defineStore('cart', {
  state: (): { items: CartItem[] } => ({
    items: [],
  }),

  getters: {
    totalCount: (state): number => state.items.reduce((sum, item) => sum + item.quantity, 0),

    selectedItems: (state): CartItem[] => state.items.filter((item) => item.selected),

    selectedCount: (state): number =>
      state.items.filter((item) => item.selected).reduce((sum, item) => sum + item.quantity, 0),

    selectedTotal: (state): number =>
      state.items
        .filter((item) => item.selected)
        .reduce((sum, item) => sum + item.price * item.quantity, 0),
  },

  actions: {
    async fetchCart() {
      try {
        const data = await http.get<CartItem[]>('/v1/member/cart/list')
        this.items = data.map((item) => ({ ...item, selected: false }))
      } catch {
        // Cart may be empty or user not logged in
      }
    },

    async addItem(params: { productId: string; skuId: string; quantity: number }) {
      await http.post('/v1/member/cart/add', params)
      await this.fetchCart()
    },

    async updateQuantity(itemId: string, quantity: number) {
      await http.put('/v1/member/cart/update', { itemId, quantity })
      await this.fetchCart()
    },

    increaseQuantity(itemId: string) {
      const item = this.items.find((i) => i.id === itemId)
      if (item) {
        item.quantity++
        this.updateQuantity(itemId, item.quantity)
      }
    },

    decreaseQuantity(itemId: string) {
      const item = this.items.find((i) => i.id === itemId)
      if (item && item.quantity > 1) {
        item.quantity--
        this.updateQuantity(itemId, item.quantity)
      }
    },

    async removeItem(itemId: string) {
      await http.delete(`/v1/member/cart/remove/${itemId}`)
      this.items = this.items.filter((i) => i.id !== itemId)
    },

    toggleSelectAll(selected: boolean) {
      this.items.forEach((item) => {
        item.selected = selected
      })
    },
  },
})
