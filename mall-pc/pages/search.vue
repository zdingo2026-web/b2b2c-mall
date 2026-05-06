<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- Search bar -->
    <div class="mb-8">
      <div class="flex max-w-2xl">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索商品"
          class="flex-1 h-12 px-4 border border-r-0 border-gray-300 rounded-l text-sm focus:outline-none focus:border-primary-500"
          @keyup.enter="handleSearch"
        />
        <button class="btn-primary rounded-l-none h-12 px-8" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- No search active: show hot & history -->
    <template v-if="!hasSearched">
      <!-- Hot keywords -->
      <div v-if="hotKeywords.length > 0" class="mb-8">
        <h2 class="text-lg font-bold mb-4">热门搜索</h2>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="(kw, idx) in hotKeywords"
            :key="idx"
            class="px-4 py-2 bg-white border rounded-full text-sm text-gray-600 hover:border-primary-500 hover:text-primary-500 transition-colors"
            @click="quickSearch(kw)"
          >
            {{ kw }}
          </button>
        </div>
      </div>

      <!-- Search history -->
      <div v-if="searchHistory.length > 0" class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold">搜索历史</h2>
          <button class="text-sm text-gray-400 hover:text-red-500" @click="handleClearHistory">清空</button>
        </div>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="(item, idx) in searchHistory"
            :key="idx"
            class="px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600 hover:text-primary-500 transition-colors"
            @click="quickSearch(item.keyword)"
          >
            {{ item.keyword }}
          </button>
        </div>
      </div>

      <!-- Discover -->
      <div>
        <h2 class="text-lg font-bold mb-4">发现好物</h2>
        <div class="grid grid-cols-5 gap-4">
          <ProductCard
            v-for="product in discoverProducts"
            :key="product.id"
            :product="product"
          />
        </div>
      </div>
    </template>

    <!-- Search results -->
    <template v-if="hasSearched">
      <div class="mb-4">
        <span class="text-gray-500">搜索"</span>
        <span class="text-primary-500 font-bold">{{ currentKeyword }}</span>
        <span class="text-gray-500">" 共找到 {{ total }} 件商品</span>
      </div>

      <!-- Sort bar -->
      <div class="bg-white rounded-lg p-4 mb-6">
        <div class="flex items-center gap-3">
          <button
            v-for="sort in sortOptions"
            :key="sort.value"
            class="px-4 py-2 rounded text-sm transition-colors"
            :class="currentSort === sort.value ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
            @click="currentSort = sort.value; page = 1; searchProducts()"
          >
            {{ sort.label }}
          </button>
        </div>
      </div>

      <!-- Results grid -->
      <div v-if="products.length > 0" class="grid grid-cols-5 gap-4">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
        />
      </div>

      <!-- Loading -->
      <div v-if="searchLoading" class="text-center py-10">
        <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- Empty results -->
      <div v-if="!searchLoading && products.length === 0" class="text-center py-20 text-gray-400">
        <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <p>未找到与"{{ currentKeyword }}"相关的商品</p>
      </div>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="mt-6 flex justify-center">
        <div class="flex items-center gap-2">
          <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page <= 1" @click="page--; searchProducts()">上一页</button>
          <span class="text-sm text-gray-500">{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
          <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page >= Math.ceil(total / pageSize)" @click="page++; searchProducts()">下一页</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { ProductListItem, SearchHistory } from '~/composables/types'

const route = useRoute()
const router = useRouter()
const { get, post: postReq } = useRequest()

const searchKeyword = ref('')
const currentKeyword = ref('')
const hasSearched = ref(false)
const searchLoading = ref(false)
const products = ref<ProductListItem[]>([])
const hotKeywords = ref<string[]>([])
const searchHistory = ref<SearchHistory[]>([])
const discoverProducts = ref<ProductListItem[]>([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const currentSort = ref('default')

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price_asc' },
  { label: '新品', value: 'new' },
]

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  currentKeyword.value = keyword
  hasSearched.value = true
  page.value = 1
  searchProducts()
  // Save to history
  searchHistory.value = [
    { keyword, time: new Date().toISOString() },
    ...searchHistory.value.filter((h) => h.keyword !== keyword),
  ].slice(0, 10)
  router.replace({ query: { keyword } })
}

const quickSearch = (keyword: string) => {
  searchKeyword.value = keyword
  handleSearch()
}

const handleClearHistory = async () => {
  searchHistory.value = []
  try {
    await get('/search/clear-history')
  } catch {
    // ignore
  }
}

const searchProducts = async () => {
  if (!currentKeyword.value) return
  searchLoading.value = true
  try {
    const params: Record<string, string | number | undefined> = {
      keyword: currentKeyword.value,
      page: page.value,
      pageSize,
    }
    if (currentSort.value !== 'default') params.sort = currentSort.value
    const data = await get<{ list: ProductListItem[]; total: number }>('/product/list', params)
    products.value = data.list || []
    total.value = data.total || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    searchLoading.value = false
  }
}

const loadInitData = async () => {
  const results = await Promise.allSettled([
    get<string[]>('/search/hot'),
    get<SearchHistory[]>('/search/history'),
    get<ProductListItem[]>('/home/recommend-products'),
  ])
  if (results[0].status === 'fulfilled') hotKeywords.value = results[0].value
  if (results[1].status === 'fulfilled') searchHistory.value = results[1].value
  if (results[2].status === 'fulfilled') discoverProducts.value = results[2].value.slice(0, 10)
}

onMounted(() => {
  // Check if there's a keyword in the URL
  const keyword = route.query.keyword as string
  if (keyword) {
    searchKeyword.value = keyword
    currentKeyword.value = keyword
    hasSearched.value = true
    searchProducts()
  }
  loadInitData()
})
</script>
