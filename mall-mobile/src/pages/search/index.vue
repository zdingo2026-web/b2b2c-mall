<template>
  <view class="page-search">
    <!-- Search bar -->
    <view class="search-bar">
      <view class="search-bar__input">
        <text class="search-bar__icon">🔍</text>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索商品"
          confirm-type="search"
          @confirm="handleSearch"
        />
      </view>
      <text class="search-bar__btn" @tap="handleSearch">搜索</text>
    </view>

    <!-- Search results -->
    <view v-if="hasSearched" class="search-results">
      <view v-if="products.length" class="search-results__grid">
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          @click="(p: any) => navigateTo(`/pages/product/index?id=${p.id}`)"
        />
      </view>
      <Empty v-if="!loading && !products.length" text="没有找到相关商品" />
      <view v-if="loading" class="search-loading">
        <text>搜索中...</text>
      </view>
      <view v-if="noMore && products.length" class="search-nomore">
        <text>没有更多了</text>
      </view>
    </view>

    <!-- Default: Hot search + history -->
    <template v-if="!hasSearched">
      <view class="search-section">
        <text class="search-section__title">热门搜索</text>
        <view class="search-tags">
          <view
            v-for="tag in hotSearches"
            :key="tag"
            class="search-tag"
            @tap="keyword = tag; handleSearch()"
          >
            {{ tag }}
          </view>
        </view>
      </view>

      <view class="search-section">
        <view class="search-section__header">
          <text class="search-section__title">搜索历史</text>
          <text class="search-section__clear" @tap="clearHistory">清空</text>
        </view>
        <view v-if="searchHistory.length" class="search-tags">
          <view
            v-for="tag in searchHistory"
            :key="tag"
            class="search-tag"
            @tap="keyword = tag; handleSearch()"
          >
            {{ tag }}
          </view>
        </view>
        <Empty v-else text="暂无搜索历史" />
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ProductCard from '@/components/ProductCard.vue'
import Empty from '@/components/Empty.vue'
import { getSpuList, type SpuVO } from '@/api/product'

const keyword = ref('')
const hotSearches = ['手机', '笔记本', '空调', '连衣裙', '口红']
const searchHistory = ref<string[]>(JSON.parse(uni.getStorageSync('search_history') || '[]'))

const hasSearched = ref(false)
const products = ref<SpuVO[]>([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)

async function handleSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }

  // Save to history
  const history = searchHistory.value.filter((h) => h !== kw)
  history.unshift(kw)
  searchHistory.value = history.slice(0, 10)
  uni.setStorageSync('search_history', JSON.stringify(searchHistory.value))

  // Search
  hasSearched.value = true
  page.value = 1
  noMore.value = false
  loading.value = true
  try {
    const data = await getSpuList({ keyword: kw, page: 1, limit: 20 })
    products.value = data.list || []
    noMore.value = (data.list || []).length < 20
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

function clearHistory() {
  searchHistory.value = []
  uni.removeStorageSync('search_history')
}
</script>

<style lang="scss" scoped>
.page-search { min-height: 100vh; background: #f5f5f5; }
.search-bar { background: #fff; display: flex; align-items: center; padding: 16rpx 24rpx; gap: 16rpx; }
.search-bar__input { flex: 1; display: flex; align-items: center; background: #f5f5f5; border-radius: 40rpx; padding: 12rpx 24rpx; }
.search-bar__icon { font-size: 28rpx; margin-right: 12rpx; }
.search-bar__input input { flex: 1; font-size: 28rpx; }
.search-bar__btn { font-size: 28rpx; color: #f97316; white-space: nowrap; }
.search-section { background: #fff; margin: 20rpx 24rpx; border-radius: 16rpx; padding: 24rpx; }
.search-section__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.search-section__title { font-size: 28rpx; font-weight: 600; color: #333; display: block; margin-bottom: 20rpx; }
.search-section__clear { font-size: 24rpx; color: #999; }
.search-tags { display: flex; flex-wrap: wrap; gap: 16rpx; }
.search-tag { background: #f5f5f5; padding: 12rpx 28rpx; border-radius: 32rpx; font-size: 26rpx; color: #666; }
.search-results { padding: 20rpx 24rpx; }
.search-results__grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20rpx; }
.search-loading, .search-nomore { text-align: center; padding: 32rpx; font-size: 24rpx; color: #999; }
</style>
