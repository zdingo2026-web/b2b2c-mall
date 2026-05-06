<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="text-sm text-gray-400 mb-4">
      <NuxtLink to="/" class="hover:text-primary-500">首页</NuxtLink>
      <span class="mx-2">/</span>
      <span class="text-gray-700">全部商品分类</span>
    </div>

    <div class="flex gap-6">
      <!-- Left: Category tree -->
      <aside class="w-64 bg-white rounded-lg p-4 flex-shrink-0">
        <h3 class="font-bold text-gray-800 mb-3">商品分类</h3>
        <div v-if="categories.length === 0" class="text-center py-8 text-gray-400">加载中...</div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="mb-1"
        >
          <div
            class="px-3 py-2 rounded cursor-pointer hover:bg-primary-50 hover:text-primary-500 transition-colors flex items-center justify-between"
            :class="{ 'bg-primary-50 text-primary-500 font-bold': activeCategory === cat.id }"
            @click="handleCategoryClick(cat)"
          >
            <span>{{ cat.name }}</span>
            <svg v-if="cat.children?.length" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </div>
          <div v-if="cat.children?.length && activeCategory === cat.id" class="ml-4 mt-1 space-y-1">
            <div
              v-for="child in cat.children"
              :key="child.id"
              class="px-3 py-1.5 text-sm text-gray-500 rounded cursor-pointer hover:text-primary-500 hover:bg-primary-50"
              @click="navigateTo(`/category/${child.id}`)"
            >
              {{ child.name }}
            </div>
          </div>
        </div>
      </aside>

      <!-- Right: Product grid -->
      <div class="flex-1">
        <!-- Category header when one is selected -->
        <div v-if="currentCategoryName" class="mb-4">
          <h2 class="text-lg font-bold">{{ currentCategoryName }}</h2>
        </div>

        <!-- Product grid -->
        <div v-if="products.length > 0" class="grid grid-cols-4 gap-4">
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
          />
        </div>

        <!-- Empty state -->
        <div v-else class="bg-white rounded-lg p-6 text-center">
          <p class="text-gray-400 py-16">{{ activeCategory ? '该分类下暂无商品' : '请选择左侧分类查看商品' }}</p>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Category, ProductListItem } from '~/composables/types'

const { get } = useRequest()

const categories = ref<Category[]>([])
const products = ref<ProductListItem[]>([])
const activeCategory = ref<number | null>(null)
const currentCategoryName = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const handleCategoryClick = (cat: Category) => {
  if (activeCategory.value === cat.id) {
    activeCategory.value = null
    currentCategoryName.value = ''
    products.value = []
    return
  }
  activeCategory.value = cat.id
  currentCategoryName.value = cat.name
  page.value = 1
  loadProducts(cat.id)
}

const loadCategories = async () => {
  try {
    categories.value = await get<Category[]>('/category/tree')
  } catch {
    categories.value = []
  }
}

const loadProducts = async (categoryId: number) => {
  try {
    const data = await get<{ list: ProductListItem[]; total: number }>('/category/products', {
      categoryId,
      page: page.value,
      pageSize,
    })
    products.value = data.list || []
    total.value = data.total || 0
  } catch {
    products.value = []
    total.value = 0
  }
}

watch(page, () => {
  if (activeCategory.value) {
    loadProducts(activeCategory.value)
  }
})

loadCategories()
</script>
