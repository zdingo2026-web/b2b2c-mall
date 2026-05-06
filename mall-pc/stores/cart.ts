import { defineStore } from 'pinia'
import type { CartItem } from '~/composables/types'

export const useCartStore = defineStore('cart', {
  state: (): { items: CartItem[]; loaded: boolean } => ({
    items: [],
    loaded: false,
  }),

  getters: {
    totalCount: (state): number => state.items.reduce((sum, item) => sum + item.quantity, 0),

    selectedItems: (state): CartItem[] => state.items.filter((item) => item.selected),

    selectedCount: (state): number =>
      state.items.filter((item) => item.selected).reduce((sum, item) => sum + item.quantity, 0),

    selectedTotal: (state): string =>
      state.items
        .filter((item) => item.selected)
        .reduce((sum, item) => sum + item.price * item.quantity, 0)
        .toFixed(2),

    /** Group items by store */
    storeGroups: (state): Map<number, { storeId: number; storeName: string; items: CartItem[] }> => {
      const map = new Map<number, { storeId: number; storeName: string; items: CartItem[] }>()
      for (const item of state.items) {
        const group = map.get(item.storeId)
        if (group) {
          group.items.push(item)
        } else {
          map.set(item.storeId, { storeId: item.storeId, storeName: item.storeName, items: [item] })
        }
      }
      return map
    },

    allSelected: (state): boolean => state.items.length > 0 && state.items.every((i) => i.selected),
  },

  actions: {
    async fetchCart() {
      const { get } = useRequest()
      try {
        const data = await get<CartItem[]>('/cart/list')
        this.items = data.map((item: CartItem) => ({ ...item, selected: false }))
        this.loaded = true
      } catch {
        this.items = []
        this.loaded = true
      }
    },

    async addItem(product: { productId: number; skuId: number; quantity: number }) {
      const { post } = useRequest()
      await post('/cart/add', product)
      await this.fetchCart()
    },

    async updateQuantity(itemId: number, quantity: number) {
      const { put } = useRequest()
      await put('/cart/update', { itemId, quantity })
      await this.fetchCart()
    },

    async increaseQuantity(itemId: number) {
      const item = this.items.find((i) => i.id === itemId)
      if (item) {
        await this.updateQuantity(itemId, item.quantity + 1)
      }
    },

    async decreaseQuantity(itemId: number) {
      const item = this.items.find((i) => i.id === itemId)
      if (item && item.quantity > 1) {
        await this.updateQuantity(itemId, item.quantity - 1)
      }
    },

    async removeItem(itemId: number) {
      const { delete: del } = useRequest()
      await del(`/cart/remove/${itemId}`)
      this.items = this.items.filter((i) => i.id !== itemId)
    },

    toggleSelect(itemId: number) {
      const item = this.items.find((i) => i.id === itemId)
      if (item) {
        item.selected = !item.selected
      }
    },

    toggleSelectAll(selected: boolean) {
      this.items.forEach((item) => {
        item.selected = selected
      })
    },

    toggleStoreSelect(storeId: number, selected: boolean) {
      this.items.filter((i) => i.storeId === storeId).forEach((item) => {
        item.selected = selected
      })
    },
  },
})
