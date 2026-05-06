<template>
  <view class="page-product-list">
    <!-- Filter bar -->
    <view class="list-filter">
      <text class="list-filter__item" :class="{ 'list-filter__item--active': sortField === 'default' }" @tap="sortField = 'default'; fetchList()">综合</text>
      <text class="list-filter__item" :class="{ 'list-filter__item--active': sortField === 'sales' }" @tap="sortField = 'sales'; fetchList()">销量</text>
      <text class="list-filter__item" :class="{ 'list-filter__item--active': sortField === 'price' }" @tap="sortField = 'price'; fetchList()">价格</text>
    </view>

    <!-- Product grid -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view class="list-grid">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          @click="(p: any) => navigateTo(`/pages/product/index?id=${p.id}`)"
        />
      </view>
      <Empty v-if="!loading && !products.length" text="暂无商品" />
      <view v-if="loading" class="list-loading"><text>加载中...</text></view>
      <view v-if="noMore && products.length" class="list-nomore"><text>没有更多了</text></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import ProductCard from '@/components/ProductCard.vue'
import Empty from '@/components/Empty.vue'
import { getSpuList, type SpuVO } from '@/api/product'

const products = ref<SpuVO[]>([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)
const sortField = ref('default')

let categoryId: string | undefined
let keyword: string | undefined

onLoad((query) => {
  categoryId = query?.categoryId || undefined
  keyword = query?.keyword || undefined
  fetchList()
})

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    noMore.value = false
    products.value = []
  }
  loading.value = true
  try {
    const params: any = { page: page.value, limit: 20 }
    if (categoryId) params.categoryId = categoryId
    if (keyword) params.keyword = keyword
    if (sortField.value === 'sales') { params.sortField = 'totalSales'; params.sortOrder = 'desc' }
    if (sortField.value === 'price') { params.sortField = 'minPrice'; params.sortOrder = 'asc' }
    const data = await getSpuList(params)
    if (reset) {
      products.value = data.list || []
    } else {
      products.value.push(...(data.list || []))
    }
    noMore.value = (data.list || []).length < 20
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchList()
}
</script>

<style lang="scss" scoped>
.page-product-list { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; }
.list-filter { display: flex; background: #fff; border-bottom: 1rpx solid #f0f0f0; flex-shrink: 0; }
.list-filter__item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; }
.list-filter__item--active { color: #f97316; font-weight: 600; }
.list-scroll { flex: 1; padding: 20rpx 24rpx; }
.list-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20rpx; }
.list-loading, .list-nomore { text-align: center; padding: 32rpx; font-size: 24rpx; color: #999; }
</style>
