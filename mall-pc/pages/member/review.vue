<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="review" />
      <div class="flex-1">
        <h2 class="font-bold text-lg mb-4">我的评价</h2>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-16">
          <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- Review list -->
        <div v-else-if="reviews.length > 0" class="space-y-4">
          <div v-for="review in reviews" :key="review.id" class="bg-white rounded-lg p-4">
            <div class="flex items-start gap-4">
              <div class="w-16 h-16 bg-gray-100 rounded overflow-hidden flex-shrink-0">
                <img v-if="review.productImage" :src="review.productImage" :alt="review.productName" class="w-full h-full object-cover" />
              </div>
              <div class="flex-1">
                <NuxtLink :to="`/product/${review.productId}`" class="text-sm text-gray-700 hover:text-primary-500 line-clamp-1">{{ review.productName }}</NuxtLink>
                <div class="flex items-center gap-1 mt-1">
                  <span v-for="i in 5" :key="i" class="text-sm" :class="i <= review.score ? 'text-yellow-400' : 'text-gray-200'">&#9733;</span>
                </div>
                <p class="text-sm text-gray-600 mt-2">{{ review.content }}</p>
                <div v-if="review.images?.length" class="flex gap-2 mt-2">
                  <img v-for="(img, idx) in review.images" :key="idx" :src="img" class="w-16 h-16 rounded object-cover" />
                </div>
                <p class="text-xs text-gray-400 mt-2">{{ review.createTime }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="bg-white rounded-lg text-center py-16 text-gray-400">
          <p>暂无评价记录</p>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Review } from '~/composables/types'

const { get } = useRequest()

const reviews = ref<Review[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const loadReviews = async () => {
  loading.value = true
  try {
    const data = await get<{ list: Review[]; total: number }>('/review/mine', { page: page.value, pageSize })
    reviews.value = data.list || []
    total.value = data.total || 0
  } catch {
    reviews.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch(page, () => loadReviews())

onMounted(() => loadReviews())
</script>
