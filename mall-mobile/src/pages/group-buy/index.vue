<template>
  <view class="page-group-buy">
    <!-- Banner -->
    <view class="gb-banner">
      <view class="gb-banner__content">
        <text class="gb-banner__title">超值拼团</text>
        <text class="gb-banner__desc">邀请好友一起拼，价格更优惠</text>
      </view>
    </view>

    <!-- Product list -->
    <scroll-view
      scroll-y
      class="product-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && products.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="products.length === 0" class="empty-state">
        <text class="empty-state__text">暂无拼团活动</text>
      </view>

      <view v-else class="product-list">
        <view
          v-for="item in products"
          :key="item.id"
          class="product-item"
          @tap="goDetail(item)"
        >
          <image class="product-item__img" :src="item.mainImage" mode="aspectFill" />
          <view class="product-item__info">
            <text class="product-item__name">{{ item.productName }}</text>
            <view class="product-item__price-row">
              <text class="product-item__group-price">&yen;{{ item.groupPrice }}</text>
              <text class="product-item__original-price">&yen;{{ item.originalPrice }}</text>
            </view>
            <view class="product-item__meta">
              <view class="product-item__group-tag">
                <RemixIcon name="group-line" :size="22" color="#FFFFFF" />
                <text class="product-item__group-tag-text">{{ item.groupSize }}人团</text>
              </view>
              <text class="product-item__joined">已拼{{ item.joinedCount }}件</text>
            </view>
            <view class="product-item__bottom">
              <text class="product-item__countdown">{{ formatRemainTime(item.endTime) }}</text>
              <view class="product-item__btn" @tap.stop="goDetail(item)">
                <text class="product-item__btn-text">去拼团</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && products.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMore && products.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { getGroupBuyProducts, type GroupBuyProductVO } from '@/api/group-buy'

const products = ref<GroupBuyProductVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)

onShow(() => {
  page.value = 1
  products.value = []
  noMore.value = false
  fetchProducts()
})

async function fetchProducts() {
  loading.value = true
  try {
    const data = await getGroupBuyProducts({ page: page.value, limit: 10 })
    const list = data.list || []
    if (page.value === 1) {
      products.value = list
    } else {
      products.value.push(...list)
    }
    noMore.value = list.length < 10
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchProducts()
}

function formatRemainTime(endTime: string): string {
  if (!endTime) return ''
  const diff = new Date(endTime).getTime() - Date.now()
  if (diff <= 0) return '已结束'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  if (h > 24) return `剩余${Math.floor(h / 24)}天`
  return `剩余${h}时${m}分`
}

function goDetail(item: GroupBuyProductVO) {
  uni.navigateTo({ url: `/pages/group-buy/detail?activityId=${item.activityId}` })
}
</script>

<style lang="scss" scoped>
.page-group-buy {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Banner */
.gb-banner {
  background: linear-gradient(135deg, #7C3AED, #6D28D9);
  padding: 40rpx 32rpx;
}

.gb-banner__title {
  font-size: 40rpx;
  color: #FFFFFF;
  font-weight: 700;
  display: block;
}

.gb-banner__desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

/* Product scroll */
.product-scroll {
  flex: 1;
  height: 0;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-state__text,
.empty-state__text {
  font-size: 28rpx;
  color: #94A3B8;
}

/* Product list */
.product-list {
  padding: 16rpx 24rpx;
}

.product-item {
  display: flex;
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
  padding: 20rpx;
}

.product-item__img {
  width: 220rpx;
  height: 220rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.product-item__info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.product-item__name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-item__price-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 10rpx;
}

.product-item__group-price {
  font-size: 36rpx;
  color: #7C3AED;
  font-weight: 700;
}

.product-item__original-price {
  font-size: 22rpx;
  color: #94A3B8;
  text-decoration: line-through;
}

/* Meta */
.product-item__meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 10rpx;
}

.product-item__group-tag {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #7C3AED;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
}

.product-item__group-tag-text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.product-item__joined {
  font-size: 22rpx;
  color: #94A3B8;
}

/* Bottom */
.product-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10rpx;
}

.product-item__countdown {
  font-size: 22rpx;
  color: #F97316;
}

.product-item__btn {
  background: linear-gradient(135deg, #7C3AED, #6D28D9);
  border-radius: 28rpx;
  padding: 10rpx 28rpx;
}

.product-item__btn-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* Load more */
.load-more {
  padding: 24rpx 0;
  text-align: center;
}

.load-more__text {
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
