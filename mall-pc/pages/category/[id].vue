<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="text-sm text-gray-400 mb-4">
      <NuxtLink to="/" class="hover:text-primary-500">首页</NuxtLink>
      <span class="mx-2">/</span>
      <NuxtLink to="/category" class="hover:text-primary-500">分类</NuxtLink>
      <span class="mx-2">/</span>
      <span class="text-gray-700">{{ categoryName || '分类详情' }}</span>
    </div>

    <!-- Sort bar -->
    <div class="bg-white rounded-lg p-4 mb-6">
      <div class="flex items-center gap-3">
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
            v-model="priceMin"
            type="number"
            placeholder="最低价"
            class="w-24 h-8 px-2 border border-gray-300 rounded text-sm"
          />
          <span class="text-gray-400">-</span>
          <input
            v-model="priceMax"
            type="number"
            placeholder="最高价"
            class="w-24 h-8 px-2 border border-gray-300 rounded text-sm"
          />
          <button class="px-4 py-1 border rounded text-sm hover:bg-gray-50" @click="loadProducts">筛选</button>
        </div>
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

    <!-- Empty -->
    <div v-else class="bg-white rounded-lg p-6 text-center">
      <p class="text-gray-400 py-16">该分类下暂无商品</p>
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
        <span class="text-sm text-gray-500">{{ page }} / {{ totalPages }}</span>
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
import type { ProductListItem, Category } from '~/composables/types'

const route = useRoute()
const { get } = useRequest()

const categoryId = computed(() => Number(route.params.id))
const categoryName = ref('')
const products = ref<ProductListItem[]>([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const currentSort = ref('default')
const priceOrder = ref<'asc' | 'desc'>('asc')
const priceMin = ref('')
const priceMax = ref('')

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' },
  { label: '新品', value: 'new' },
]

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const handleSortChange = (sort: string) => {
  currentSort.value = sort
  page.value = 1
  loadProducts()
}

const loadCategoryName = async () => {
  try {
    const tree = await get<Category[]>('/category/tree')
    for (const cat of tree) {
      if (cat.id === categoryId.value) {
        categoryName.value = cat.name
        return
      }
      if (cat.children) {
        const child = cat.children.find((c) => c.id === categoryId.value)
        if (child) {
          categoryName.value = child.name
          return
        }
      }
    }
  } catch {
    // ignore
  }
}

const loadProducts = async () => {
  try {
    const params: Record<string, string | number | undefined> = {
      categoryId: categoryId.value,
      page: page.value,
      pageSize,
    }
    if (currentSort.value !== 'default') {
      params.sort = currentSort.value
      if (currentSort.value === 'price') {
        params.sort = priceOrder.value === 'asc' ? 'price_asc' : 'price_desc'
      }
    }
    if (priceMin.value) params.priceMin = Number(priceMin.value)
    if (priceMax.value) params.priceMax = Number(priceMax.value)

    const data = await get<{ list: ProductListItem[]; total: number }>('/category/products', params)
    products.value = data.list || []
    total.value = data.total || 0
  } catch {
    products.value = []
    total.value = 0
  }
}

watch(page, () => loadProducts())
watch(categoryId, () => {
  page.value = 1
  currentSort.value = 'default'
  priceMin.value = ''
  priceMax.value = ''
  loadCategoryName()
  loadProducts()
}, { immediate: true })
</script>
