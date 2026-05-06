<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="favorites" />
      <div class="flex-1">
        <h2 class="font-bold text-lg mb-4">我的收藏</h2>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-16">
          <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- Favorites grid -->
        <div v-else-if="favorites.length > 0">
          <div class="grid grid-cols-4 gap-4">
            <div
              v-for="fav in favorites"
              :key="fav.id"
              class="bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group relative"
              @click="navigateTo(`/product/${fav.productId}`)"
            >
              <div class="aspect-square bg-gray-100 overflow-hidden">
                <img
                  v-if="fav.productImage"
                  :src="fav.productImage"
                  :alt="fav.productName"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
              </div>
              <div class="p-4">
                <h3 class="text-sm text-gray-700 line-clamp-2 mb-2">{{ fav.productName }}</h3>
                <div class="flex items-baseline justify-between">
                  <span class="text-lg font-bold text-promo">&yen;{{ fav.price.toFixed(2) }}</span>
                  <button
                    class="text-gray-300 hover:text-red-500 transition-colors"
                    @click.stop="handleRemove(fav.productId)"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="total > pageSize" class="mt-6 flex justify-center">
            <div class="flex items-center gap-2">
              <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page <= 1" @click="page--">上一页</button>
              <span class="text-sm text-gray-500">{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
              <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page >= Math.ceil(total / pageSize)" @click="page++">下一页</button>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="bg-white rounded-lg text-center py-16 text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
          <p class="mb-4">暂无收藏商品</p>
          <NuxtLink to="/" class="btn-primary inline-block">去逛逛</NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Favorite } from '~/composables/types'

const { get, delete: del } = useRequest()

const favorites = ref<Favorite[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 20
const total = ref(0)

const loadFavorites = async () => {
  loading.value = true
  try {
    const data = await get<{ list: Favorite[]; total: number }>('/favorite/list', {
      page: page.value,
      pageSize,
    })
    favorites.value = data.list || []
    total.value = data.total || 0
  } catch {
    favorites.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleRemove = async (productId: number) => {
  if (!confirm('确定取消收藏？')) return
  try {
    await del(`/favorite/remove/${productId}`)
    loadFavorites()
  } catch {
    // ignore
  }
}

watch(page, () => loadFavorites())

onMounted(() => loadFavorites())
</script>
