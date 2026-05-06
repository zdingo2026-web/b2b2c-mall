<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- Breadcrumb -->
    <div class="text-sm text-gray-400 mb-4">
      <NuxtLink to="/" class="hover:text-primary-500">首页</NuxtLink>
      <span class="mx-2">/</span>
      <span class="text-gray-700">{{ keyword ? `搜索: ${keyword}` : '全部商品' }}</span>
    </div>

    <!-- Filters & Sort -->
    <div class="bg-white rounded-lg p-4 mb-6">
      <div class="flex items-center gap-4 flex-wrap">
        <button
          v-for="sort in sortOptions"
          :key="sort.value"
          class="px-4 py-2 rounded text-sm transition-colors"
          :class="currentSort === sort.value ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          @click="handleSortChange(sort.value)"
        >
          {{ sort.label }}
        </button>
        <div v-if="currentSort === 'price'" class="flex items-center gap-1 ml-2">
          <button
            class="px-3 py-1 border rounded text-xs"
            :class="priceOrder === 'asc' ? 'border-primary-500 text-primary-500' : 'border-gray-300'"
            @click="priceOrder = 'asc'"
          >
            低到高
          </button>
          <button
            class="px-3 py-1 border rounded text-xs"
            :class="priceOrder === 'desc' ? 'border-primary-500 text-primary-500' : 'border-gray-300'"
            @click="priceOrder = 'desc'"
          >
            高到低
          </button>
        </div>
        <div class="ml-auto flex items-center gap-2">
          <input
            v-model="priceMinInput"
            type="number"
            placeholder="最低价"
            class="w-24 h-8 px-2 border border-gray-300 rounded text-sm"
          />
          <span class="text-gray-400">-</span>
          <input
            v-model="priceMaxInput"
            type="number"
            placeholder="最高价"
            class="w-24 h-8 px-2 border border-gray-300 rounded text-sm"
          />
          <button class="px-4 py-1 border rounded text-sm hover:bg-gray-50" @click="applyPriceFilter">筛选</button>
        </div>
      </div>
      <!-- Active filters -->
      <div v-if="keyword || priceMinInput || priceMaxInput" class="mt-3 flex items-center gap-2 text-sm">
        <span class="text-gray-500">筛选条件:</span>
        <span v-if="keyword" class="bg-primary-50 text-primary-500 px-3 py-1 rounded">
          关键词: {{ keyword }}
          <button class="ml-1" @click="clearKeyword">&times;</button>
        </span>
        <span v-if="priceMinInput" class="bg-primary-50 text-primary-500 px-3 py-1 rounded">
          最低价: {{ priceMinInput }}
          <button class="ml-1" @click="priceMinInput = ''">&times;</button>
        </span>
        <span v-if="priceMaxInput" class="bg-primary-50 text-primary-500 px-3 py-1 rounded">
          最高价: {{ priceMaxInput }}
          <button class="ml-1" @click="priceMaxInput = ''">&times;</button>
        </span>
        <button class="text-gray-400 hover:text-gray-600" @click="clearAllFilters">清除全部</button>
      </div>
    </div>

    <!-- Product Grid -->
    <div v-if="products.length > 0" class="grid grid-cols-5 gap-4">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
      <p class="text-gray-400 mt-2">加载中...</p>
    </div>

    <!-- Empty -->
    <div v-if="!loading && products.length === 0" class="text-center py-20 text-gray-400">
      <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
      </svg>
      <p>{{ keyword ? `未找到与"${keyword}"相关的商品` : '暂无商品' }}</p>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="mt-6 flex justify-center">
      <div class="flex items-center gap-2">
        <button
          class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50"
          :disabled="page <= 1"
          @click="page--"
        >
          上一页
        </button>
        <template v-for="p in displayPages" :key="p">
          <button
            v-if="p !== '...'"
            class="w-10 h-10 border rounded text-sm"
            :class="p === page ? 'bg-primary-500 text-white border-primary-500' : 'hover:bg-gray-50'"
            @click="page = p as number"
          >
            {{ p }}
          </button>
          <span v-else class="text-gray-400">...</span>
        </template>
        <button
          class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50"
          :disabled="page >= totalPages"
          @click="page++"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProductListItem } from '~/composables/types'

const route = useRoute()
const router = useRouter()
const { get } = useRequest()

const keyword = computed(() => (route.query.keyword as string) || '')
const categoryId = computed(() => route.query.categoryId ? Number(route.query.categoryId) : undefined)

const products = ref<ProductListItem[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const currentSort = ref('default')
const priceOrder = ref<'asc' | 'desc'>('asc')
const priceMinInput = ref('')
const priceMaxInput = ref('')

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' },
  { label: '新品', value: 'new' },
]

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const displayPages = computed(() => {
  const pages: (number | string)[] = []
  const tp = totalPages.value
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) pages.push(i)
  } else {
    pages.push(1)
    if (page.value > 3) pages.push('...')
    const start = Math.max(2, page.value - 1)
    const end = Math.min(tp - 1, page.value + 1)
    for (let i = start; i <= end; i++) pages.push(i)
    if (page.value < tp - 2) pages.push('...')
    pages.push(tp)
  }
  return pages
})

const handleSortChange = (sort: string) => {
  currentSort.value = sort
  page.value = 1
  loadProducts()
}

const applyPriceFilter = () => {
  page.value = 1
  loadProducts()
}

const clearKeyword = () => {
  router.replace({ query: { ...route.query, keyword: undefined } })
}

const clearAllFilters = () => {
  priceMinInput.value = ''
  priceMaxInput.value = ''
  currentSort.value = 'default'
  page.value = 1
  router.replace({ query: {} })
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number | undefined> = {
      page: page.value,
      pageSize,
    }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    if (currentSort.value !== 'default') {
      if (currentSort.value === 'price') {
        params.sort = priceOrder.value === 'asc' ? 'price_asc' : 'price_desc'
      } else {
        params.sort = currentSort.value
      }
    }
    if (priceMinInput.value) params.priceMin = Number(priceMinInput.value)
    if (priceMaxInput.value) params.priceMax = Number(priceMaxInput.value)

    const data = await get<{ list: ProductListItem[]; total: number }>('/product/list', params)
    products.value = data.list || []
    total.value = data.total || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch(page, () => loadProducts())
watch(() => route.query, () => {
  page.value = 1
  loadProducts()
}, { immediate: true })
</script>
