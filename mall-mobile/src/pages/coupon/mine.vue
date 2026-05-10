<template>
  <view class="page-coupon-mine">
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

    <!-- Coupon list -->
    <scroll-view
      scroll-y
      class="list-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-state__text">暂无优惠券</text>
      </view>

      <view v-else class="coupon-list">
        <view
          v-for="item in list"
          :key="item.id"
          class="coupon-card"
          :class="{ 'coupon-card--disabled': item.status !== 0 }"
          @tap="handleUse(item)"
        >
          <!-- Left: value -->
          <view
            class="coupon-card__value"
            :class="[
              `coupon-card__value--type${item.couponType}`,
              { 'coupon-card__value--disabled': item.status !== 0 },
            ]"
          >
            <template v-if="item.couponType === 1 || item.couponType === 3">
              <text class="coupon-card__symbol">&yen;</text>
              <text class="coupon-card__amount">{{ item.couponValue }}</text>
            </template>
            <template v-else-if="item.couponType === 2">
              <text class="coupon-card__amount">{{ item.couponValue }}</text>
              <text class="coupon-card__symbol">折</text>
            </template>
            <text v-if="item.couponType === 1" class="coupon-card__condition">满{{ item.minAmount }}可用</text>
            <text v-else-if="item.couponType === 3" class="coupon-card__condition">无门槛</text>
          </view>

          <!-- Divider -->
          <view class="coupon-card__divider">
            <view class="coupon-card__circle coupon-card__circle--top" />
            <view class="coupon-card__dashed" />
            <view class="coupon-card__circle coupon-card__circle--bottom" />
          </view>

          <!-- Right: info -->
          <view class="coupon-card__info">
            <text class="coupon-card__name">{{ item.couponName }}</text>
            <text class="coupon-card__expire">有效期至 {{ item.expireTime }}</text>
            <view class="coupon-card__footer">
              <template v-if="item.status === 0">
                <view class="coupon-card__btn">
                  <text class="coupon-card__btn-text">去使用</text>
                </view>
              </template>
              <template v-else-if="item.status === 1">
                <view class="coupon-card__status-tag coupon-card__status-tag--used">
                  <text class="coupon-card__status-text">已使用</text>
                </view>
              </template>
              <template v-else>
                <view class="coupon-card__status-tag coupon-card__status-tag--expired">
                  <text class="coupon-card__status-text">已过期</text>
                </view>
              </template>
            </view>
          </view>

          <!-- Used/expired watermark -->
          <view v-if="item.status !== 0" class="coupon-card__watermark">
            <text class="coupon-card__watermark-text">{{ item.status === 1 ? '已使用' : '已过期' }}</text>
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

    <!-- Bottom: go to coupon center -->
    <view class="bottom-area">
      <view class="bottom-area__btn" @tap="goCouponCenter">
        <text class="bottom-area__btn-text">领券中心</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getCouponList,
  type CouponVO,
} from '@/api/coupon'

const activeTab = ref(0) // 0-unused, 1-used, 2-expired
const list = ref<CouponVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)

const tabs = [
  { value: 0, label: '未使用' },
  { value: 1, label: '已使用' },
  { value: 2, label: '已过期' },
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
    // Map tab to status
    if (activeTab.value === 0) params.status = 0 // unused
    if (activeTab.value === 1) params.status = 1 // used
    if (activeTab.value === 2) params.status = 2 // expired
    const data = await getCouponList(params)
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

function handleUse(item: CouponVO) {
  if (item.status !== 0) return
  uni.switchTab({ url: '/pages/index/index' })
}

function goCouponCenter() {
  uni.navigateTo({ url: '/pages/coupon/list' })
}
</script>

<style lang="scss" scoped>
.page-coupon-mine {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
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
  background: #E11148;
  border-radius: 3rpx;
}

.tab-bar__text {
  font-size: 28rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-bar__item--active .tab-bar__text {
  color: #E11148;
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

/* Coupon list */
.coupon-list {
  padding: 16rpx 24rpx;
}

.coupon-card {
  display: flex;
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
  min-height: 180rpx;
  position: relative;
}

.coupon-card--disabled {
  opacity: 0.6;
}

/* Value section */
.coupon-card__value {
  width: 220rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx 16rpx;
  flex-shrink: 0;
}

.coupon-card__value--type1 {
  background: linear-gradient(135deg, #E11148, #DC2626);
  color: #FFFFFF;
}

.coupon-card__value--type2 {
  background: linear-gradient(135deg, #7C3AED, #6D28D9);
  color: #FFFFFF;
}

.coupon-card__value--type3 {
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: #FFFFFF;
}

.coupon-card__value--disabled {
  background: #CBD5E1 !important;
}

.coupon-card__symbol {
  font-size: 24rpx;
  font-weight: 600;
}

.coupon-card__amount {
  font-size: 52rpx;
  font-weight: 700;
  line-height: 1;
}

.coupon-card__condition {
  font-size: 20rpx;
  opacity: 0.8;
  margin-top: 8rpx;
}

/* Divider */
.coupon-card__divider {
  width: 2rpx;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.coupon-card__circle {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #F9FAFB;
  position: absolute;
}

.coupon-card__circle--top {
  top: -10rpx;
}

.coupon-card__circle--bottom {
  bottom: -10rpx;
}

.coupon-card__dashed {
  flex: 1;
  width: 0;
  border-left: 2rpx dashed #E5E7EB;
}

/* Info section */
.coupon-card__info {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.coupon-card__name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-card__expire {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 8rpx;
  display: block;
}

.coupon-card__footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8rpx;
}

.coupon-card__btn {
  background: linear-gradient(135deg, #E11148, #DC2626);
  border-radius: 24rpx;
  padding: 8rpx 28rpx;
}

.coupon-card__btn-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.coupon-card__status-tag {
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
}

.coupon-card__status-tag--used {
  background: #F3F4F6;
}

.coupon-card__status-tag--expired {
  background: #FEF2F2;
}

.coupon-card__status-text {
  font-size: 22rpx;
  color: #94A3B8;
}

.coupon-card__status-tag--expired .coupon-card__status-text {
  color: #EF4444;
}

/* Watermark */
.coupon-card__watermark {
  position: absolute;
  top: 50%;
  right: 24rpx;
  transform: translateY(-50%) rotate(-15deg);
}

.coupon-card__watermark-text {
  font-size: 28rpx;
  color: rgba(0, 0, 0, 0.1);
  font-weight: 700;
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

/* Bottom area */
.bottom-area {
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
}

.bottom-area__btn {
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #E11148;
  border-radius: 40rpx;
}

.bottom-area__btn-text {
  font-size: 28rpx;
  color: #E11148;
  font-weight: 600;
}
</style>
