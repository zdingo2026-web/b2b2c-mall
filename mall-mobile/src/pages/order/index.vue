<template>
  <view class="page-order">
    <!-- Top nav bar -->
    <view class="order-navbar">
      <view class="order-navbar__back" @tap="handleBack">
        <RemixIcon name="arrow-left-line" :size="40" color="#1E293B" />
      </view>
      <text class="order-navbar__title">订单列表</text>
      <view class="order-navbar__placeholder" />
    </view>

    <!-- Order status tabs -->
    <view class="order-tabs">
      <view
        v-for="tab in ORDER_TABS"
        :key="tab.value"
        class="order-tabs__item"
        :class="{ 'order-tabs__item--active': activeTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text class="order-tabs__label">{{ tab.label }}</text>
        <view v-if="activeTab === tab.value" class="order-tabs__indicator" />
      </view>
    </view>

    <!-- Order list with pull-down refresh and scroll-to-load -->
    <scroll-view
      scroll-y
      class="order-list"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @tap="navigateTo(`/pages/order/detail?orderNo=${order.orderNo}`)"
      >
        <!-- Card header: store info + status -->
        <view class="order-card__header">
          <view class="order-card__store">
            <RemixIcon name="store-2-line" :size="28" color="#64748B" />
            <text class="order-card__store-name">{{ order.tenantName || '平台自营' }}</text>
          </view>
          <text
            class="order-card__status"
            :style="{ color: ORDER_STATUS_COLOR[order.orderStatus] || '#9CA3AF' }"
          >
            {{ ORDER_STATUS_MAP[order.orderStatus] || '未知' }}
          </text>
        </view>

        <!-- Product items -->
        <view
          v-for="item in order.items"
          :key="item.id"
          class="order-card__item"
        >
          <image class="order-card__img" :src="item.productImage" mode="aspectFill" />
          <view class="order-card__info">
            <text class="order-card__name">{{ item.productName }}</text>
            <text class="order-card__spec">{{ item.specValues }}</text>
          </view>
          <view class="order-card__right">
            <text class="order-card__price">&yen;{{ item.price }}</text>
            <text class="order-card__qty">x{{ item.quantity }}</text>
          </view>
        </view>

        <!-- Logistics info -->
        <view
          v-if="order.orderStatus >= 2 && order.logisticsNo"
          class="order-card__logistics"
        >
          <RemixIcon name="truck-line" :size="24" color="#64748B" />
          <text class="order-card__logistics-text">
            物流: {{ order.logisticsCompany || '' }} {{ order.logisticsNo }}
          </text>
        </view>

        <!-- Order summary -->
        <view class="order-card__summary">
          <text class="order-card__summary-text">
            共{{ totalItems(order) }}件商品 实付
            <text class="order-card__summary-amount">&yen;{{ order.payAmount }}</text>
          </text>
        </view>

        <!-- Action buttons -->
        <view class="order-card__actions">
          <!-- 待付款 -->
          <template v-if="order.orderStatus === 0">
            <view class="order-card__btn" @tap.stop="handleCancel(order.orderNo)">取消订单</view>
            <view class="order-card__btn order-card__btn--primary" @tap.stop="navigateTo(`/pages/order/payment?orderNo=${order.orderNo}`)">付款</view>
          </template>
          <!-- 待发货 -->
          <template v-if="order.orderStatus === 1">
            <view class="order-card__btn" @tap.stop="handleRemindShip">提醒发货</view>
          </template>
          <!-- 待收货 -->
          <template v-if="order.orderStatus === 2">
            <view class="order-card__btn" @tap.stop="navigateTo(`/pages/order/detail?orderNo=${order.orderNo}`)">查看物流</view>
            <view class="order-card__btn order-card__btn--primary" @tap.stop="handleConfirm(order.id)">确认收货</view>
          </template>
          <!-- 待评价 -->
          <template v-if="order.orderStatus === 7">
            <view class="order-card__btn" @tap.stop="handleAfterSale(order)">申请售后</view>
            <view class="order-card__btn order-card__btn--primary" @tap.stop="handleReview(order)">评价</view>
          </template>
          <!-- 已完成 -->
          <template v-if="order.orderStatus === 3">
            <view class="order-card__btn" @tap.stop="handleAfterSale(order)">申请售后</view>
            <view class="order-card__btn" @tap.stop="handleRebuy(order)">再次购买</view>
          </template>
          <!-- 已取消 -->
          <template v-if="order.orderStatus === 4">
            <view class="order-card__btn" @tap.stop="handleRebuy(order)">再次购买</view>
          </template>
        </view>
      </view>

      <Empty v-if="!loading && !orders.length" text="暂无订单" />
      <view v-if="loading" class="order-loading">
        <text>加载中...</text>
      </view>
      <view v-if="noMore && orders.length" class="order-nomore">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import Empty from '@/components/Empty.vue'
import RemixIcon from '@/components/RemixIcon.vue'
import {
  getOrderList,
  cancelOrder,
  confirmReceive,
  ORDER_TABS,
  ORDER_STATUS_MAP,
  ORDER_STATUS_COLOR,
  type OrderMainVO,
} from '@/api/order'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref<'all' | number>('all')
const orders = ref<OrderMainVO[]>([])
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const noMore = ref(false)

/** Compute total item count in an order */
function totalItems(order: OrderMainVO): number {
  return (order.items || []).reduce((sum, item) => sum + item.quantity, 0)
}

/** Fetch orders with current tab filter */
async function fetchOrders(reset = false) {
  if (!userStore.isLoggedIn) return
  if (reset) {
    page.value = 1
    noMore.value = false
    orders.value = []
  }
  loading.value = true
  try {
    const params: { page: number; limit: number; status?: number } = { page: page.value, limit: 10 }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value as number
    }
    const data = await getOrderList(params)
    if (reset) {
      orders.value = data.list || []
    } else {
      orders.value.push(...(data.list || []))
    }
    noMore.value = (data.list || []).length < 10
  } finally {
    loading.value = false
  }
}

/** Pull-down refresh */
async function onRefresh() {
  refreshing.value = true
  await fetchOrders(true)
  refreshing.value = false
}

/** Switch tab and reload */
function switchTab(value: 'all' | number) {
  activeTab.value = value
  fetchOrders(true)
}

/** Load next page */
function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchOrders()
}

/** Go back */
function handleBack() {
  uni.navigateBack({ delta: 1 })
}

/** Cancel order */
async function handleCancel(orderNo: string) {
  const { confirm } = await uni.showModal({ title: '提示', content: '确定取消此订单？' })
  if (confirm) {
    await cancelOrder(orderNo)
    uni.showToast({ title: '已取消', icon: 'success' })
    fetchOrders(true)
  }
}

/** Confirm receipt */
async function handleConfirm(orderId: string) {
  const { confirm } = await uni.showModal({ title: '提示', content: '确认已收到商品？' })
  if (confirm) {
    await confirmReceive(orderId)
    uni.showToast({ title: '已确认收货', icon: 'success' })
    fetchOrders(true)
  }
}

/** Remind seller to ship */
function handleRemindShip() {
  uni.showToast({ title: '已提醒发货', icon: 'none' })
}

/** Navigate to review page */
function handleReview(_order: OrderMainVO) {
  uni.showToast({ title: '评价功能开发中', icon: 'none' })
}

/** After-sale request */
function handleAfterSale(_order: OrderMainVO) {
  uni.showToast({ title: '售后功能开发中', icon: 'none' })
}

/** Re-buy order items */
function handleRebuy(_order: OrderMainVO) {
  uni.showToast({ title: '再次购买功能开发中', icon: 'none' })
}

/** Simple navigation helper */
function navigateTo(url: string) {
  uni.navigateTo({ url })
}

/** Accept tab query param when navigated from Mine page */
onShow(() => {
  const storedTab = uni.getStorageSync('order_list_tab')
  if (storedTab !== '' && storedTab !== undefined) {
    const parsed = Number(storedTab)
    if (!isNaN(parsed)) {
      activeTab.value = parsed
    }
    uni.removeStorageSync('order_list_tab')
  }
  fetchOrders(true)
})
</script>

<style lang="scss" scoped>
.page-order {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Navbar */
.order-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  padding-top: var(--status-bar-height, 44rpx);
  background: #fff;
  border-bottom: 1rpx solid #F0F0F0;
  flex-shrink: 0;
}

.order-navbar__back {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.order-navbar__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
}

.order-navbar__placeholder {
  width: 72rpx;
}

/* Tabs */
.order-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #F0F0F0;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.order-tabs__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0 20rpx;
  position: relative;
}

.order-tabs__label {
  font-size: 28rpx;
  color: #64748B;
}

.order-tabs__item--active .order-tabs__label {
  color: #2563EB;
  font-weight: 600;
}

.order-tabs__indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 8rpx;
  background: #2563EB;
  border-radius: 4rpx;
}

/* Order list */
.order-list {
  flex: 1;
  padding: 20rpx 24rpx;
}

/* Order card */
.order-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.order-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.order-card__store {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.order-card__store-name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.order-card__status {
  font-size: 24rpx;
  font-weight: 500;
}

/* Product item */
.order-card__item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-card__img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.order-card__info {
  flex: 1;
  min-width: 0;
}

.order-card__name {
  font-size: 26rpx;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.order-card__spec {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 8rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-card__right {
  text-align: right;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.order-card__price {
  font-size: 28rpx;
  color: #1E293B;
  display: block;
}

.order-card__qty {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 4rpx;
  display: block;
}

/* Logistics */
.order-card__logistics {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #F3F4F6;
  padding: 16rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.order-card__logistics-text {
  font-size: 24rpx;
  color: #64748B;
}

/* Summary */
.order-card__summary {
  border-top: 1rpx solid #F0F0F0;
  padding-top: 16rpx;
  margin-bottom: 16rpx;
}

.order-card__summary-text {
  font-size: 28rpx;
  color: #334155;
  text-align: right;
  display: block;
}

.order-card__summary-amount {
  font-weight: 600;
  color: #1E293B;
}

/* Action buttons */
.order-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.order-card__btn {
  padding: 8rpx 28rpx;
  border: 1rpx solid #E2E8F0;
  border-radius: 32rpx;
  font-size: 24rpx;
  color: #64748B;
  background: #fff;
}

.order-card__btn--primary {
  border-color: #2563EB;
  color: #2563EB;
}

/* Loading states */
.order-loading,
.order-nomore {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
