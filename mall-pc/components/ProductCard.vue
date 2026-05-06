<template>
  <div
    class="bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group"
    @click="navigateTo(`/product/${product.id}`)"
  >
    <div class="aspect-square bg-gray-100 overflow-hidden relative">
      <img
        v-if="product.mainImage"
        :src="product.mainImage"
        :alt="product.name"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-300 text-4xl">
        <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      </div>
      <!-- TagBadge -->
      <span
        v-if="product.tagType && tagConfig[product.tagType]"
        class="absolute top-2 left-2 text-xs px-2 py-0.5 rounded font-medium"
        :class="tagConfig[product.tagType].class"
      >
        {{ tagConfig[product.tagType].label }}
      </span>
    </div>
    <div class="p-4">
      <h3 class="text-sm text-gray-700 line-clamp-2 mb-2 min-h-[2.5rem]">{{ product.name }}</h3>
      <div class="flex items-baseline gap-2">
        <span class="text-lg font-bold text-promo">&yen;{{ product.price }}</span>
        <span v-if="product.originalPrice && product.originalPrice > product.price" class="text-xs text-gray-400 line-through">&yen;{{ product.originalPrice }}</span>
      </div>
      <div class="flex items-center justify-between mt-1">
        <p class="text-xs text-gray-400 truncate">{{ product.storeName }}</p>
        <span v-if="product.sales" class="text-xs text-gray-400">{{ product.sales }}人付款</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProductListItem } from '~/composables/types'

defineProps<{
  product: ProductListItem
}>()

const tagConfig: Record<number, { label: string; class: string }> = {
  1: { label: '百亿补贴', class: 'bg-[#FEF2F2] text-[#E11148]' },
  2: { label: '品牌特卖', class: 'bg-[#EFF6FF] text-[#2563EB]' },
  3: { label: '新品首发', class: 'bg-[#FFF7ED] text-[#F97316]' },
  4: { label: '以旧换新', class: 'bg-[#F0FDF4] text-[#16A34A]' },
}
</script>
