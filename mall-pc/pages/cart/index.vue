<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">购物车 <span v-if="cartStore.totalCount" class="text-base font-normal text-gray-400">({{ cartStore.totalCount }}件)</span></h1>

    <!-- Loading -->
    <div v-if="!cartStore.loaded" class="text-center py-20">
      <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Empty cart -->
    <div v-else-if="cartStore.items.length === 0" class="text-center py-20 bg-white rounded-lg">
      <svg class="w-24 h-24 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
      </svg>
      <p class="text-gray-400 mb-4">购物车是空的</p>
      <NuxtLink to="/" class="btn-primary inline-block">去逛逛</NuxtLink>
    </div>

    <!-- Cart with items -->
    <div v-else>
      <!-- Cart header -->
      <div class="bg-white rounded-t-lg px-6 py-3 flex items-center text-sm text-gray-500 border-b">
        <label class="w-12 flex items-center cursor-pointer">
          <input type="checkbox" :checked="cartStore.allSelected" class="mr-2" @change="cartStore.toggleSelectAll(!cartStore.allSelected)" />
          全选
        </label>
        <span class="flex-1 ml-4">商品信息</span>
        <span class="w-24 text-center">单价</span>
        <span class="w-32 text-center">数量</span>
        <span class="w-24 text-center">小计</span>
        <span class="w-20 text-center">操作</span>
      </div>

      <!-- Items grouped by store -->
      <div v-for="[storeId, group] in cartStore.storeGroups" :key="storeId" class="bg-white border-b">
        <!-- Store header -->
        <div class="px-6 py-3 bg-gray-50 flex items-center">
          <input
            type="checkbox"
            :checked="group.items.every((i) => i.selected)"
            class="mr-3"
            @change="cartStore.toggleStoreSelect(storeId, !group.items.every((i) => i.selected))"
          />
          <span class="text-sm font-medium text-gray-700">{{ group.storeName }}</span>
        </div>
        <!-- Items -->
        <div
          v-for="item in group.items"
          :key="item.id"
          class="px-6 py-4 flex items-center border-t border-gray-100"
        >
          <div class="w-12">
            <input type="checkbox" :checked="item.selected" class="cursor-pointer" @change="cartStore.toggleSelect(item.id)" />
          </div>
          <div class="flex-1 ml-4 flex gap-4">
            <NuxtLink :to="`/product/${item.productId}`" class="w-20 h-20 bg-gray-100 rounded flex-shrink-0 overflow-hidden">
              <img v-if="item.image" :src="item.image" :alt="item.name" class="w-full h-full object-cover" />
            </NuxtLink>
            <div class="min-w-0">
              <NuxtLink :to="`/product/${item.productId}`" class="text-sm text-gray-700 line-clamp-2 hover:text-primary-500">{{ item.name }}</NuxtLink>
              <p class="text-xs text-gray-400 mt-1">{{ item.spec }}</p>
            </div>
          </div>
          <div class="w-24 text-center text-promo font-bold">&yen;{{ item.price.toFixed(2) }}</div>
          <div class="w-32 flex justify-center">
            <div class="inline-flex items-center border rounded">
              <button
                class="px-2 py-1 text-gray-500 hover:text-primary-500 disabled:opacity-30"
                :disabled="item.quantity <= 1"
                @click="cartStore.decreaseQuantity(item.id)"
              >
                -
              </button>
              <span class="px-3 py-1 border-x text-sm w-10 text-center">{{ item.quantity }}</span>
              <button
                class="px-2 py-1 text-gray-500 hover:text-primary-500 disabled:opacity-30"
                :disabled="item.stock > 0 && item.quantity >= item.stock"
                @click="cartStore.increaseQuantity(item.id)"
              >
                +
              </button>
            </div>
          </div>
          <div class="w-24 text-center text-promo font-bold">&yen;{{ (item.price * item.quantity).toFixed(2) }}</div>
          <div class="w-20 text-center">
            <button class="text-gray-400 hover:text-red-500 text-sm" @click="handleRemove(item.id)">删除</button>
          </div>
        </div>
      </div>

      <!-- Cart footer -->
      <div class="bg-white rounded-b-lg px-6 py-4 flex items-center justify-between border-t-2 border-primary-500">
        <div class="flex items-center gap-6">
          <label class="flex items-center cursor-pointer text-sm">
            <input type="checkbox" :checked="cartStore.allSelected" class="mr-2" @change="cartStore.toggleSelectAll(!cartStore.allSelected)" />
            全选
          </label>
          <button class="text-sm text-gray-400 hover:text-red-500" @click="handleClearInvalid">清理失效商品</button>
        </div>
        <div class="flex items-center gap-6">
          <div>
            <span class="text-gray-500">已选 <span class="text-primary-500 font-bold">{{ cartStore.selectedCount }}</span> 件商品</span>
            <span class="ml-4 text-gray-500">合计：</span>
            <span class="text-2xl font-bold text-promo">&yen;{{ cartStore.selectedTotal }}</span>
          </div>
          <button
            class="btn-primary px-12 py-3 text-lg"
            :class="{ 'opacity-50 cursor-not-allowed': cartStore.selectedCount === 0 }"
            @click="handleCheckout"
          >
            去结算
          </button>
        </div>
      </div>
    </div>

    <!-- Recommend section -->
    <div class="mt-10">
      <h2 class="text-lg font-bold mb-4">猜你喜欢</h2>
      <div class="grid grid-cols-5 gap-4">
        <ProductCard v-for="p in recommendProducts" :key="p.id" :product="p" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProductListItem } from '~/composables/types'

const { get } = useRequest()
const cartStore = useCartStore()
const userStore = useUserStore()

const recommendProducts = ref<ProductListItem[]>([])

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await cartStore.fetchCart()
  }
  try {
    recommendProducts.value = await get<ProductListItem[]>('/home/recommend-products')
  } catch {
    // ignore
  }
})

const handleRemove = async (itemId: number) => {
  if (confirm('确定要删除该商品吗？')) {
    await cartStore.removeItem(itemId)
  }
}

const handleClearInvalid = () => {
  // Remove items with 0 stock
  const invalid = cartStore.items.filter((i) => i.stock === 0)
  invalid.forEach((item) => cartStore.removeItem(item.id))
}

const handleCheckout = () => {
  if (cartStore.selectedCount === 0) {
    alert('请选择要结算的商品')
    return
  }
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  navigateTo('/order/confirm')
}
</script>
