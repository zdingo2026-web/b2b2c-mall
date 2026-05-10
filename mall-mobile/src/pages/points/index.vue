<template>
  <view class="page-points">
    <!-- Points header -->
    <view class="points-header">
      <view class="points-header__balance">
        <text class="points-header__label">可用积分</text>
        <text class="points-header__value">{{ account?.availablePoints || 0 }}</text>
      </view>
      <view class="points-header__row">
        <view class="points-header__stat">
          <text class="points-header__stat-value">{{ account?.totalPoints || 0 }}</text>
          <text class="points-header__stat-label">累计积分</text>
        </view>
        <view class="points-header__stat">
          <text class="points-header__stat-value">{{ account?.usedPoints || 0 }}</text>
          <text class="points-header__stat-label">已使用</text>
        </view>
        <view class="points-header__stat">
          <text class="points-header__stat-value">{{ account?.frozenPoints || 0 }}</text>
          <text class="points-header__stat-label">冻结中</text>
        </view>
      </view>
    </view>

    <!-- Checkin card -->
    <view class="checkin-card">
      <view class="checkin-card__left">
        <RemixIcon name="calendar-check-line" :size="36" color="#16A34A" />
        <view class="checkin-card__info">
          <text class="checkin-card__title">每日签到</text>
          <text v-if="checkinStatus" class="checkin-card__desc">
            {{ checkinStatus.checkedToday ? `已连续签到${checkinStatus.continuousDays}天` : '今日未签到，签到领积分' }}
          </text>
        </view>
      </view>
      <view
        class="checkin-card__btn"
        :class="{ 'checkin-card__btn--done': checkinStatus?.checkedToday }"
        @tap="handleCheckin"
      >
        <text class="checkin-card__btn-text">{{ checkinStatus?.checkedToday ? '已签到' : '签到' }}</text>
      </view>
    </view>

    <!-- Tab switcher -->
    <view class="tab-bar">
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 'products' }"
        @tap="activeTab = 'products'"
      >
        <text class="tab-bar__text">积分商品</text>
      </view>
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 'details' }"
        @tap="activeTab = 'details'"
      >
        <text class="tab-bar__text">积分明细</text>
      </view>
    </view>

    <!-- Products tab -->
    <scroll-view
      v-if="activeTab === 'products'"
      scroll-y
      class="content-scroll"
      @scrolltolower="loadMoreProducts"
    >
      <view v-if="loadingProducts && products.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="products.length === 0" class="empty-state">
        <text class="empty-state__text">暂无积分商品</text>
      </view>

      <view v-else class="product-grid">
        <view
          v-for="item in products"
          :key="item.id"
          class="product-card"
          @tap="goExchange(item)"
        >
          <image class="product-card__img" :src="item.mainImage" mode="aspectFill" />
          <view class="product-card__info">
            <text class="product-card__name">{{ item.productName }}</text>
            <view class="product-card__bottom">
              <view class="product-card__points">
                <text class="product-card__points-value">{{ item.pointsPrice }}</text>
                <text class="product-card__points-unit">积分</text>
              </view>
              <view class="product-card__exchange-btn">
                <text class="product-card__exchange-btn-text">兑换</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loadingProducts && products.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMoreProducts && products.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- Details tab -->
    <scroll-view
      v-if="activeTab === 'details'"
      scroll-y
      class="content-scroll"
      @scrolltolower="loadMoreDetails"
    >
      <view v-if="loadingDetails && details.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="details.length === 0" class="empty-state">
        <text class="empty-state__text">暂无积分记录</text>
      </view>

      <view v-else class="detail-list">
        <view v-for="item in details" :key="item.id" class="detail-item">
          <view class="detail-item__left">
            <text class="detail-item__desc">{{ item.description || item.source }}</text>
            <text class="detail-item__time">{{ item.createTime }}</text>
          </view>
          <text
            class="detail-item__points"
            :class="{ 'detail-item__points--plus': item.type === 1 }"
          >
            {{ item.type === 1 ? '+' : '-' }}{{ item.points }}
          </text>
        </view>
      </view>

      <view v-if="loadingDetails && details.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMoreDetails && details.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import {
  getPointsAccount,
  getPointsDetails,
  getPointsProductList,
  doCheckin,
  getCheckinStatus,
  type PointsAccountVO,
  type PointsDetailVO,
  type PointsProductVO,
  type CheckinStatusVO,
} from '@/api/points'

const account = ref<PointsAccountVO | null>(null)
const checkinStatus = ref<CheckinStatusVO | null>(null)
const activeTab = ref<'products' | 'details'>('products')

// Products
const products = ref<PointsProductVO[]>([])
const loadingProducts = ref(false)
const noMoreProducts = ref(false)
const productPage = ref(1)

// Details
const details = ref<PointsDetailVO[]>([])
const loadingDetails = ref(false)
const noMoreDetails = ref(false)
const detailPage = ref(1)

onShow(() => {
  fetchAccount()
  fetchCheckinStatus()
  fetchProducts()
})

watch(activeTab, (tab) => {
  if (tab === 'details' && details.value.length === 0) {
    fetchDetails()
  }
})

async function fetchAccount() {
  try {
    account.value = await getPointsAccount()
  } catch {
    // handled
  }
}

async function fetchCheckinStatus() {
  try {
    checkinStatus.value = await getCheckinStatus()
  } catch {
    // handled
  }
}

async function fetchProducts() {
  loadingProducts.value = true
  try {
    const data = await getPointsProductList({ page: productPage.value, limit: 10 })
    const list = data.list || []
    if (productPage.value === 1) {
      products.value = list
    } else {
      products.value.push(...list)
    }
    noMoreProducts.value = list.length < 10
  } catch {
    // handled
  } finally {
    loadingProducts.value = false
  }
}

function loadMoreProducts() {
  if (loadingProducts.value || noMoreProducts.value) return
  productPage.value++
  fetchProducts()
}

async function fetchDetails() {
  loadingDetails.value = true
  try {
    const data = await getPointsDetails({ page: detailPage.value, limit: 10 })
    const list = data.list || []
    if (detailPage.value === 1) {
      details.value = list
    } else {
      details.value.push(...list)
    }
    noMoreDetails.value = list.length < 10
  } catch {
    // handled
  } finally {
    loadingDetails.value = false
  }
}

function loadMoreDetails() {
  if (loadingDetails.value || noMoreDetails.value) return
  detailPage.value++
  fetchDetails()
}

async function handleCheckin() {
  if (checkinStatus.value?.checkedToday) return
  try {
    const result = await doCheckin()
    uni.showToast({ title: `签到成功，获得${result?.points || 0}积分`, icon: 'none' })
    fetchCheckinStatus()
    fetchAccount()
  } catch (e: any) {
    uni.showToast({ title: e.message || '签到失败', icon: 'none' })
  }
}

function goExchange(item: PointsProductVO) {
  uni.navigateTo({ url: `/pages/points/exchange?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.page-points {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Header */
.points-header {
  background: linear-gradient(135deg, #16A34A, #15803D);
  padding: 40rpx 32rpx 32rpx;
  color: #FFFFFF;
}

.points-header__balance {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-header__label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.points-header__value {
  font-size: 72rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
  margin-top: 8rpx;
}

.points-header__row {
  display: flex;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.points-header__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-header__stat-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.points-header__stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4rpx;
}

/* Checkin card */
.checkin-card {
  background: #FFFFFF;
  margin: 20rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkin-card__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.checkin-card__info {
  display: flex;
  flex-direction: column;
}

.checkin-card__title {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
}

.checkin-card__desc {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.checkin-card__btn {
  background: #16A34A;
  border-radius: 28rpx;
  padding: 12rpx 32rpx;
}

.checkin-card__btn--done {
  background: #E2E8F0;
}

.checkin-card__btn-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.checkin-card__btn--done .checkin-card__btn-text {
  color: #94A3B8;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 0 24rpx;
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
  width: 48rpx;
  height: 6rpx;
  background: #16A34A;
  border-radius: 3rpx;
}

.tab-bar__text {
  font-size: 28rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-bar__item--active .tab-bar__text {
  color: #16A34A;
  font-weight: 600;
}

/* Content scroll */
.content-scroll {
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

/* Product grid */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx 16rpx 0;
  gap: 16rpx;
}

.product-card {
  width: calc(50% - 8rpx);
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
}

.product-card__img {
  width: 100%;
  height: 300rpx;
}

.product-card__info {
  padding: 16rpx;
}

.product-card__name {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.product-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.product-card__points {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.product-card__points-value {
  font-size: 32rpx;
  color: #16A34A;
  font-weight: 700;
}

.product-card__points-unit {
  font-size: 20rpx;
  color: #16A34A;
}

.product-card__exchange-btn {
  background: #16A34A;
  border-radius: 20rpx;
  padding: 6rpx 20rpx;
}

.product-card__exchange-btn-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* Detail list */
.detail-list {
  padding: 0 24rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item__left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
  min-width: 0;
}

.detail-item__desc {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-item__time {
  font-size: 22rpx;
  color: #94A3B8;
}

.detail-item__points {
  font-size: 32rpx;
  color: #EF4444;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.detail-item__points--plus {
  color: #16A34A;
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
