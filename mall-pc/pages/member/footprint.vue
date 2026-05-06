<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="footprint" />
      <div class="flex-1">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-bold text-lg">浏览历史</h2>
          <button
            v-if="groups.length > 0"
            class="text-sm text-gray-400 hover:text-red-500 transition-colors"
            @click="handleClearAll"
          >
            清空
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-16">
          <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- Grouped footprints -->
        <div v-else-if="groups.length > 0" class="space-y-6">
          <section v-for="group in groups" :key="group.label">
            <h3 class="text-sm font-medium text-gray-500 mb-3 flex items-center gap-2">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ group.label }}
            </h3>
            <div class="grid grid-cols-4 gap-4">
              <div
                v-for="item in group.items"
                :key="item.id"
                class="bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group"
                @click="navigateTo(`/product/${item.id}`)"
              >
                <div class="aspect-square bg-gray-100 overflow-hidden">
                  <img
                    v-if="item.mainImage"
                    :src="item.mainImage"
                    :alt="item.productName"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                </div>
                <div class="p-4">
                  <h4 class="text-sm text-gray-700 line-clamp-2 mb-2">{{ item.productName }}</h4>
                  <div class="flex items-center justify-between">
                    <span class="text-lg font-bold text-promo">&yen;{{ item.minPrice }}</span>
                    <button
                      class="w-8 h-8 bg-primary-50 hover:bg-primary-100 rounded-full flex items-center justify-center text-primary-500 transition-colors"
                      @click.stop="handleAddCart(item)"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- Empty -->
        <div v-else class="bg-white rounded-lg text-center py-16 text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p class="mb-4">暂无浏览记录</p>
          <NuxtLink to="/" class="btn-primary inline-block">去逛逛</NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FootprintGroup } from '~/composables/types'

const { get, delete: del } = useRequest()
const cartStore = useCartStore()

const groups = ref<FootprintGroup[]>([])
const loading = ref(true)

const loadFootprints = async () => {
  loading.value = true
  try {
    groups.value = await get<FootprintGroup[]>('/footprint/grouped')
  } catch {
    groups.value = []
  } finally {
    loading.value = false
  }
}

const handleAddCart = async (item: { id: number }) => {
  try {
    await cartStore.addItem({ productId: item.id, skuId: 0, quantity: 1 })
    alert('已加入购物车')
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : '加入购物车失败')
  }
}

const handleClearAll = async () => {
  if (!confirm('确定清空所有浏览记录？')) return
  try {
    await del('/footprint/clear')
    groups.value = []
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : '清空失败')
  }
}

onMounted(() => loadFootprints())
</script>
