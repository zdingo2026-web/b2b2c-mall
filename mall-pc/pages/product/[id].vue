<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- Breadcrumb -->
    <div class="text-sm text-gray-400 mb-4">
      <NuxtLink to="/" class="hover:text-primary-500">首页</NuxtLink>
      <span class="mx-2">/</span>
      <NuxtLink v-if="product" to="/product" class="hover:text-primary-500">全部商品</NuxtLink>
      <span v-if="product" class="mx-2">/</span>
      <span v-if="product" class="text-gray-700">{{ product.name }}</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <template v-if="product && !loading">
      <div class="flex gap-8">
        <!-- Product Images -->
        <div class="w-[480px] flex-shrink-0">
          <!-- Main image -->
          <div class="w-[480px] h-[480px] bg-white rounded-lg overflow-hidden border">
            <img
              v-if="currentImage"
              :src="currentImage"
              :alt="product.name"
              class="w-full h-full object-contain"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
              <svg class="w-24 h-24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          <!-- Thumbnail list -->
          <div class="flex gap-2 mt-4">
            <div
              v-for="(img, idx) in product.images"
              :key="idx"
              class="w-20 h-20 bg-white rounded cursor-pointer border-2 overflow-hidden"
              :class="currentImageIndex === idx ? 'border-primary-500' : 'border-gray-200 hover:border-primary-300'"
              @click="currentImageIndex = idx"
            >
              <img :src="img" :alt="`${product.name} - ${idx + 1}`" class="w-full h-full object-contain" />
            </div>
          </div>
        </div>

        <!-- Product Info -->
        <div class="flex-1 min-w-0">
          <h1 class="text-xl font-bold text-gray-800 mb-2">{{ product.name }}</h1>
          <p class="text-sm text-gray-400 mb-4">{{ product.subTitle }}</p>

          <!-- Price -->
          <div class="bg-gradient-to-r from-primary-50 to-blue-50 p-4 rounded mb-6">
            <div class="flex items-baseline gap-3">
              <span class="text-sm text-gray-500">价格</span>
              <span class="text-3xl font-bold text-promo">&yen;{{ currentSkuPrice }}</span>
              <span v-if="product.originalPrice > currentSkuPrice" class="text-sm text-gray-400 line-through">&yen;{{ product.originalPrice }}</span>
              <span v-if="product.originalPrice > currentSkuPrice" class="text-xs bg-red-500 text-white px-2 py-0.5 rounded ml-1">
                {{ discount }}折
              </span>
            </div>
            <div class="flex items-center gap-4 mt-2 text-sm text-gray-500">
              <span>销量 {{ product.sales || 0 }}</span>
              <span>店铺: {{ product.storeName }}</span>
            </div>
          </div>

          <!-- SKU Selection -->
          <div v-if="product.attrs?.length" class="space-y-4 mb-6">
            <div v-for="(attr, attrIdx) in product.attrs" :key="attrIdx">
              <span class="text-sm text-gray-500 mr-4 inline-block w-12">{{ attr.name }}</span>
              <div class="inline-flex gap-2 flex-wrap">
                <button
                  v-for="(val, valIdx) in attr.values"
                  :key="valIdx"
                  class="px-4 py-1.5 border rounded text-sm transition-colors"
                  :class="selectedAttrs[attrIdx] === valIdx
                    ? 'border-primary-500 text-primary-500 bg-primary-50'
                    : 'hover:border-primary-300'"
                  @click="selectAttr(attrIdx, valIdx)"
                >
                  {{ val }}
                </button>
              </div>
            </div>
          </div>

          <!-- Stock display -->
          <div v-if="currentSku" class="mb-4 text-sm">
            <span class="text-gray-500">库存:</span>
            <span class="ml-2" :class="currentSku.stock > 0 ? 'text-green-600' : 'text-red-500'">
              {{ currentSku.stock > 0 ? `${currentSku.stock}件` : '已售罄' }}
            </span>
          </div>

          <!-- Quantity -->
          <div class="mb-6">
            <span class="text-sm text-gray-500 mr-4 inline-block w-12">数量</span>
            <div class="inline-flex items-center border rounded">
              <button
                class="px-3 py-1.5 text-gray-500 hover:text-primary-500 disabled:opacity-30"
                :disabled="quantity <= 1"
                @click="quantity = Math.max(1, quantity - 1)"
              >
                -
              </button>
              <input
                v-model.number="quantity"
                type="number"
                class="w-16 text-center border-x py-1.5 text-sm"
                min="1"
                :max="currentSku?.stock || 9999"
                @change="clampQuantity"
              />
              <button
                class="px-3 py-1.5 text-gray-500 hover:text-primary-500 disabled:opacity-30"
                :disabled="!!currentSku && quantity >= currentSku.stock"
                @click="quantity++"
              >
                +
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-4">
            <button
              class="btn-primary px-12 py-3 text-lg"
              :disabled="!canAddToCart"
              @click="handleAddCart"
            >
              加入购物车
            </button>
            <button
              class="bg-red-500 text-white px-12 py-3 text-lg rounded hover:bg-red-600 transition-colors disabled:opacity-50"
              :disabled="!canAddToCart"
              @click="handleBuyNow"
            >
              立即购买
            </button>
            <button
              class="border border-gray-300 px-4 py-3 rounded text-gray-500 hover:text-primary-500 hover:border-primary-500 transition-colors"
              @click="toggleFavorite"
            >
              <svg class="w-5 h-5" :class="{ 'text-red-500 fill-red-500': isFavorited }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Store info -->
      <div class="mt-8 bg-white rounded-lg p-6">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded flex items-center justify-center text-primary-500 font-bold">
            {{ product.storeName?.charAt(0) || '店' }}
          </div>
          <div>
            <h3 class="font-bold text-gray-800">{{ product.storeName }}</h3>
            <p class="text-xs text-gray-400">B2B2C认证商家</p>
          </div>
          <NuxtLink :to="`/product?storeId=${product.storeId}`" class="ml-auto text-sm text-primary-500 hover:text-primary-600">
            进入店铺 &rarr;
          </NuxtLink>
        </div>
      </div>

      <!-- Product Detail Tabs -->
      <div class="mt-8">
        <div class="flex border-b">
          <button
            v-for="tab in detailTabs"
            :key="tab.key"
            class="px-8 py-3 text-sm font-medium border-b-2 -mb-px transition-colors"
            :class="activeDetailTab === tab.key ? 'border-primary-500 text-primary-500' : 'border-transparent text-gray-500 hover:text-primary-500'"
            @click="activeDetailTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
        <div class="bg-white rounded-b-lg p-6">
          <!-- Detail content -->
          <div v-if="activeDetailTab === 'detail'" class="prose max-w-none" v-html="sanitizeHtml(product.detail || '<p class=&quot;text-gray-400&quot;>暂无详情</p>')"></div>
          <!-- Specs -->
          <div v-if="activeDetailTab === 'spec'" class="text-gray-500">
            <p v-if="!product.attrs?.length">暂无规格信息</p>
            <table v-else class="w-full text-sm">
              <tr v-for="(attr, idx) in product.attrs" :key="idx" class="border-b">
                <td class="py-2 w-24 text-gray-500 bg-gray-50 px-4">{{ attr.name }}</td>
                <td class="py-2 px-4">{{ attr.values.join(' / ') }}</td>
              </tr>
            </table>
          </div>
          <!-- Reviews -->
          <div v-if="activeDetailTab === 'review'" class="text-gray-500">
            <p class="text-center py-8">暂无评价</p>
          </div>
        </div>
      </div>
    </template>

    <!-- Not found -->
    <div v-if="!product && !loading" class="text-center py-20">
      <p class="text-gray-400 mb-4">商品不存在或已下架</p>
      <NuxtLink to="/" class="btn-primary inline-block">返回首页</NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Product, ProductSku } from '~/composables/types'
import { sanitizeHtml } from '~/utils/sanitize'

const route = useRoute()
const { get, post, delete: del } = useRequest()
const cartStore = useCartStore()
const userStore = useUserStore()

const productId = computed(() => Number(route.params.id))
const product = ref<Product | null>(null)
const loading = ref(true)
const currentImageIndex = ref(0)
const selectedAttrs = ref<number[]>([])
const quantity = ref(1)
const isFavorited = ref(false)
const activeDetailTab = ref('detail')

const detailTabs = [
  { key: 'detail', label: '商品详情' },
  { key: 'spec', label: '规格参数' },
  { key: 'review', label: '商品评价' },
]

const currentImage = computed(() => {
  if (!product.value) return ''
  return product.value.images?.[currentImageIndex.value] || product.value.mainImage || ''
})

const currentSku = computed((): ProductSku | null => {
  if (!product.value?.skus?.length) return null
  // Find matching SKU by selected attribute combination
  const selectedAttrStr = selectedAttrs.value
    .map((valIdx, attrIdx) => product.value!.attrs?.[attrIdx]?.values?.[valIdx])
    .filter(Boolean)
    .join(';')
  return product.value.skus.find((s) => s.attrs === selectedAttrStr) || product.value.skus[0] || null
})

const currentSkuPrice = computed(() => {
  if (currentSku.value) return currentSku.value.price
  return product.value?.price || 0
})

const discount = computed(() => {
  if (!product.value?.originalPrice || !currentSkuPrice.value) return 10
  return (currentSkuPrice.value / product.value.originalPrice * 10).toFixed(1)
})

const canAddToCart = computed(() => {
  if (!currentSku.value) return false
  return currentSku.value.stock > 0
})

const selectAttr = (attrIdx: number, valIdx: number) => {
  selectedAttrs.value = [...selectedAttrs.value.slice(0, attrIdx), valIdx, ...selectedAttrs.value.slice(attrIdx + 1)]
}

const clampQuantity = () => {
  quantity.value = Math.max(1, Math.min(quantity.value, currentSku.value?.stock || 9999))
}

const handleAddCart = async () => {
  if (!currentSku.value) return
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  try {
    await cartStore.addItem({
      productId: productId.value,
      skuId: currentSku.value.id,
      quantity: quantity.value,
    })
    alert('已加入购物车')
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '加入购物车失败'
    alert(msg)
  }
}

const handleBuyNow = async () => {
  if (!currentSku.value) return
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  // Add to cart then go to checkout
  try {
    await cartStore.addItem({
      productId: productId.value,
      skuId: currentSku.value.id,
      quantity: quantity.value,
    })
    navigateTo('/order/confirm')
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '操作失败'
    alert(msg)
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await del(`/favorite/remove/${productId.value}`)
      isFavorited.value = false
    } else {
      await post('/favorite/add', { productId: productId.value })
      isFavorited.value = true
    }
  } catch {
    // ignore
  }
}

const checkFavorite = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const data = await get<{ favorited: boolean }>(`/favorite/check/${productId.value}`)
    isFavorited.value = data.favorited
  } catch {
    // ignore
  }
}

const loadProduct = async () => {
  loading.value = true
  try {
    product.value = await get<Product>(`/product/${productId.value}`)
    // Initialize selected attrs
    if (product.value.attrs?.length) {
      selectedAttrs.value = product.value.attrs.map(() => 0)
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

watch(productId, () => {
  currentImageIndex.value = 0
  quantity.value = 1
  activeDetailTab.value = 'detail'
  loadProduct()
  checkFavorite()
}, { immediate: true })
</script>
