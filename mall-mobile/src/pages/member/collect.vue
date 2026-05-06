<template>
  <view class="page-collect">
    <!-- Tabs -->
    <view class="collect-tabs">
      <view
        v-for="(tab, idx) in tabs"
        :key="tab.key"
        class="collect-tabs__item"
        :class="{ 'collect-tabs__item--active': activeTab === idx }"
        @tap="switchTab(idx)"
      >
        <text class="collect-tabs__text" :class="{ 'collect-tabs__text--active': activeTab === idx }">
          {{ tab.label }}
        </text>
        <view v-if="activeTab === idx" class="collect-tabs__indicator" />
      </view>
    </view>

    <!-- Product tab -->
    <scroll-view v-show="activeTab === 0" scroll-y class="collect-scroll" @scrolltolower="loadMore">
      <view v-if="products.length" class="collect-grid">
        <view
          v-for="item in products"
          :key="item.id"
          class="collect-card"
        >
          <!-- Heart icon top-right -->
          <view class="collect-card__heart" @tap.stop="handleUncollect(item)">
            <RemixIcon name="heart-fill" :size="40" color="#EF4444" />
          </view>
          <image
            class="collect-card__img"
            :src="item.mainImage"
            mode="aspectFill"
            @tap="navigateTo(`/pages/product/index?id=${item.spuId}`)"
          />
          <view class="collect-card__info">
            <text class="collect-card__name">{{ item.productName }}</text>
            <view class="collect-card__bottom">
              <text class="collect-card__price">&yen;{{ item.minPrice }}</text>
              <view class="collect-card__similar" @tap.stop="handleFindSimilar(item)">
                <text class="collect-card__similar-text">找相似</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <Empty v-if="!loading && !products.length" text="暂无商品收藏" />
      <view v-if="loading" class="collect-loading"><text>加载中...</text></view>
      <view v-if="noMore && products.length" class="collect-nomore"><text>没有更多了</text></view>
    </scroll-view>

    <!-- Store tab -->
    <view v-show="activeTab === 1" class="collect-empty-tab">
      <Empty text="暂无店铺收藏" />
    </view>

    <!-- Content tab -->
    <view v-show="activeTab === 2" class="collect-empty-tab">
      <Empty text="暂无内容收藏" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'
import { getCollectList, toggleCollect, type CollectVO } from '@/api/collect'

const tabs = [
  { key: 'product', label: '商品' },
  { key: 'store', label: '店铺' },
  { key: 'content', label: '内容' },
]

const activeTab = ref(0)
const products = ref<CollectVO[]>([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)

const PAGE_SIZE = 20

onLoad(() => {
  fetchList(true)
})

onShow(() => {
  if (products.value.length > 0) {
    fetchList(true)
  }
})

function switchTab(idx: number) {
  if (activeTab.value === idx) return
  activeTab.value = idx
  if (idx === 0) {
    fetchList(true)
  }
}

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    noMore.value = false
    products.value = []
  }
  loading.value = true
  try {
    const data = await getCollectList({
      page: page.value,
      limit: PAGE_SIZE,
      collectType: 1,
    })
    const list = data.list || []
    if (reset) {
      products.value = list
    } else {
      products.value = [...products.value, ...list]
    }
    noMore.value = list.length < PAGE_SIZE
  } catch {
    // Silently fail — list will show empty or existing items
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchList()
}

async function handleUncollect(item: CollectVO) {
  const [err] = await uni.showModal({
    title: '提示',
    content: '确定取消收藏该商品？',
  }) as any
  if (!(err?.confirm || err === true)) return

  try {
    await toggleCollect(item.spuId, 1)
    uni.showToast({ title: '已取消收藏', icon: 'success' })
    products.value = products.value.filter((p) => p.id !== item.id)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function handleFindSimilar(item: CollectVO) {
  uni.navigateTo({
    url: `/pages/product/list?keyword=${encodeURIComponent(item.productName)}`,
  })
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.page-collect {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Tabs */
.collect-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #F3F4F6;
  flex-shrink: 0;
}

.collect-tabs__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0 16rpx;
  position: relative;
}

.collect-tabs__text {
  font-size: 28rpx;
  color: #64748B;
}

.collect-tabs__text--active {
  color: #2563EB;
  font-weight: 600;
}

.collect-tabs__indicator {
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #2563EB;
  margin-top: 8rpx;
}

/* Scroll area */
.collect-scroll {
  flex: 1;
  padding: 20rpx 24rpx;
}

/* Product grid */
.collect-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

/* Card */
.collect-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
}

.collect-card__heart {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  z-index: 10;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 50%;
}

.collect-card__img {
  width: 100%;
  height: 340rpx;
}

.collect-card__info {
  padding: 16rpx;
}

.collect-card__name {
  font-size: 26rpx;
  color: #1E293B;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 72rpx;
}

.collect-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.collect-card__price {
  font-size: 32rpx;
  color: #E11148;
  font-weight: 600;
}

.collect-card__similar {
  background: #FEF2F2;
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}

.collect-card__similar-text {
  font-size: 20rpx;
  color: #E11148;
}

/* Empty tab content */
.collect-empty-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Loading / No more */
.collect-loading,
.collect-nomore {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
