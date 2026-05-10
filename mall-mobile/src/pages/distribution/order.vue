<template>
  <view class="page-dist-order">
    <!-- Tab filter -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text class="tab-bar__text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- Order list -->
    <scroll-view
      scroll-y
      class="list-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-state__text">暂无分销订单</text>
      </view>

      <view v-else class="order-list">
        <view v-for="item in list" :key="item.id" class="order-item">
          <image class="order-item__img" :src="item.mainImage" mode="aspectFill" />
          <view class="order-item__info">
            <text class="order-item__name">{{ item.productName }}</text>
            <text class="order-item__no">订单号: {{ item.orderNo }}</text>
            <view class="order-item__bottom">
              <view class="order-item__amounts">
                <text class="order-item__order-amount">金额 &yen;{{ item.orderAmount }}</text>
                <text class="order-item__commission">佣金 &yen;{{ item.commissionAmount }}</text>
              </view>
              <text class="order-item__time">{{ item.createTime }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && list.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMore && list.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getDistributionOrders,
  type DistributionOrderVO,
} from '@/api/distribution'

const activeTab = ref(0) // 0-all, 1-pending, 2-completed, 3-refunded
const list = ref<DistributionOrderVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)

const tabs = [
  { value: 0, label: '全部' },
  { value: 1, label: '待结算' },
  { value: 2, label: '已结算' },
  { value: 3, label: '已退款' },
]

onShow(() => {
  fetchList()
})

function switchTab(tab: number) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  page.value = 1
  list.value = []
  noMore.value = false
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: page.value, limit: 10 }
    if (activeTab.value > 0) params.status = activeTab.value
    const data = await getDistributionOrders(params)
    const items = data.list || []
    if (page.value === 1) {
      list.value = items
    } else {
      list.value.push(...items)
    }
    noMore.value = items.length < 10
  } catch {
    // handled
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
.page-dist-order {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  padding: 0 12rpx;
}

.tab-bar__item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
  position: relative;
}

.tab-bar__item--active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background: #F97316;
  border-radius: 3rpx;
}

.tab-bar__text {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-bar__item--active .tab-bar__text {
  color: #F97316;
  font-weight: 600;
}

/* List scroll */
.list-scroll {
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

/* Order list */
.order-list {
  padding: 16rpx 24rpx;
}

.order-item {
  display: flex;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.order-item__img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.order-item__info {
  flex: 1;
  margin-left: 16rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.order-item__name {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-item__no {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 6rpx;
  display: block;
}

.order-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}

.order-item__amounts {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.order-item__order-amount {
  font-size: 24rpx;
  color: #64748B;
}

.order-item__commission {
  font-size: 26rpx;
  color: #F97316;
  font-weight: 600;
}

.order-item__time {
  font-size: 22rpx;
  color: #94A3B8;
  flex-shrink: 0;
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
