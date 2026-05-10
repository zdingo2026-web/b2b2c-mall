<template>
  <view class="page-seckill">
    <!-- Time slot selector -->
    <scroll-view scroll-x class="time-slots">
      <view
        v-for="slot in timeSlots"
        :key="slot.slotId"
        class="time-slot"
        :class="{
          'time-slot--active': activeSlotId === slot.slotId,
          'time-slot--past': slot.status === 2,
        }"
        @tap="selectSlot(slot)"
      >
        <text class="time-slot__label">{{ slot.label }}</text>
        <text class="time-slot__range">{{ slot.startTime }}-{{ slot.endTime }}</text>
        <view v-if="slot.status === 1" class="time-slot__badge">
          <text class="time-slot__badge-text">抢购中</text>
        </view>
      </view>
    </scroll-view>

    <!-- Countdown -->
    <view v-if="countdownText" class="countdown-bar">
      <RemixIcon name="time-line" :size="28" color="#E11148" />
      <text class="countdown-bar__text">{{ countdownText }}</text>
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
        <text class="empty-state__text">暂无秒杀商品</text>
      </view>

      <view v-else class="product-list">
        <view
          v-for="item in products"
          :key="item.id"
          class="product-item"
          @tap="goProductDetail(item)"
        >
          <image class="product-item__img" :src="item.mainImage" mode="aspectFill" />
          <view class="product-item__info">
            <text class="product-item__name">{{ item.productName }}</text>
            <view class="product-item__price-row">
              <text class="product-item__seckill-price">&yen;{{ item.seckillPrice }}</text>
              <text class="product-item__original-price">&yen;{{ item.originalPrice }}</text>
            </view>
            <!-- Progress bar -->
            <view class="product-item__progress">
              <view class="product-item__progress-bar">
                <view
                  class="product-item__progress-fill"
                  :style="{ width: progressPercent(item) + '%' }"
                />
              </view>
              <text class="product-item__progress-text">已抢{{ progressPercent(item) }}%</text>
            </view>
            <view class="product-item__bottom">
              <text class="product-item__limit">限购{{ item.limitPerUser }}件</text>
              <view
                class="product-item__btn"
                :class="{ 'product-item__btn--disabled': item.availableStock <= 0 }"
                @tap.stop="handleSeckill(item)"
              >
                <text class="product-item__btn-text">{{ item.availableStock <= 0 ? '已抢光' : '立即抢购' }}</text>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getSeckillTimeSlots,
  getSeckillProducts,
  doSeckill,
  type SeckillTimeSlotVO,
  type SeckillProductVO,
} from '@/api/seckill'

const userStore = useUserStore()

const timeSlots = ref<SeckillTimeSlotVO[]>([])
const activeSlotId = ref<number>(0)
const products = ref<SeckillProductVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const countdownText = ref('')
let countdownTimer: ReturnType<typeof setInterval> | null = null

onShow(() => {
  fetchTimeSlots()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

async function fetchTimeSlots() {
  try {
    const data = await getSeckillTimeSlots()
    timeSlots.value = data || []
    // Auto-select the ongoing slot, or the first upcoming one
    const ongoing = timeSlots.value.find((s) => s.status === 1)
    const upcoming = timeSlots.value.find((s) => s.status === 0)
    const target = ongoing || upcoming || timeSlots.value[0]
    if (target) {
      selectSlot(target)
    }
  } catch {
    timeSlots.value = []
  }
}

function selectSlot(slot: SeckillTimeSlotVO) {
  if (slot.status === 2) return
  activeSlotId.value = slot.slotId
  page.value = 1
  products.value = []
  noMore.value = false
  fetchProducts()
  startCountdown(slot)
}

async function fetchProducts() {
  if (!activeSlotId.value) return
  loading.value = true
  try {
    const data = await getSeckillProducts({ slotId: activeSlotId.value, page: page.value, limit: 10 })
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

function progressPercent(item: SeckillProductVO): number {
  if (!item.totalStock) return 0
  return Math.min(100, Math.round((item.salesCount / item.totalStock) * 100))
}

function startCountdown(slot: SeckillTimeSlotVO) {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  const updateCountdown = () => {
    const now = Date.now()
    let target: number
    if (slot.status === 1) {
      // Ongoing - countdown to end
      target = new Date(slot.endTime).getTime()
      const diff = target - now
      if (diff <= 0) {
        countdownText.value = '本场已结束'
        if (countdownTimer) clearInterval(countdownTimer)
        return
      }
      countdownText.value = `距结束 ${formatCountdown(diff)}`
    } else if (slot.status === 0) {
      // Upcoming - countdown to start
      target = new Date(slot.startTime).getTime()
      const diff = target - now
      if (diff <= 0) {
        countdownText.value = '即将开始'
        return
      }
      countdownText.value = `距开始 ${formatCountdown(diff)}`
    } else {
      countdownText.value = ''
    }
  }
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

function formatCountdown(ms: number): string {
  const totalSec = Math.floor(ms / 1000)
  const h = Math.floor(totalSec / 3600)
  const m = Math.floor((totalSec % 3600) / 60)
  const s = totalSec % 60
  return `${pad(h)}:${pad(m)}:${pad(s)}`
}

function pad(n: number): string {
  return String(n).padStart(2, '0')
}

function goProductDetail(item: SeckillProductVO) {
  uni.navigateTo({ url: `/pages/product/index?id=${item.spuId}` })
}

async function handleSeckill(item: SeckillProductVO) {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (item.availableStock <= 0) return

  const { confirm } = await uni.showModal({
    title: '确认抢购',
    content: `确认以 ¥${item.seckillPrice} 抢购该商品？`,
  })
  if (!confirm) return

  try {
    uni.showLoading({ title: '抢购中...' })
    const result = await doSeckill(item.activityId, item.skuId)
    uni.hideLoading()
    uni.showToast({ title: '抢购成功', icon: 'success' })
    // Navigate to order confirmation
    if (result?.orderNo) {
      setTimeout(() => {
        uni.navigateTo({ url: `/pages/order/detail?orderNo=${result.orderNo}` })
      }, 1500)
    }
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '抢购失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-seckill {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Time slots */
.time-slots {
  white-space: nowrap;
  background: #FFFFFF;
  padding: 16rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.time-slot {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 28rpx;
  margin-right: 16rpx;
  border-radius: 16rpx;
  background: #F3F4F6;
  position: relative;
}

.time-slot--active {
  background: linear-gradient(135deg, #E11148, #DC2626);
}

.time-slot--active .time-slot__label,
.time-slot--active .time-slot__range {
  color: #FFFFFF;
}

.time-slot--past {
  opacity: 0.5;
}

.time-slot__label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.time-slot__range {
  font-size: 20rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.time-slot__badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background: #F97316;
  border-radius: 12rpx;
  padding: 2rpx 10rpx;
}

.time-slot__badge-text {
  font-size: 18rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* Countdown */
.countdown-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: #FEF2F2;
  padding: 12rpx 0;
}

.countdown-bar__text {
  font-size: 26rpx;
  color: #E11148;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
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
  margin-top: 12rpx;
}

.product-item__seckill-price {
  font-size: 36rpx;
  color: #E11148;
  font-weight: 700;
}

.product-item__original-price {
  font-size: 22rpx;
  color: #94A3B8;
  text-decoration: line-through;
}

/* Progress */
.product-item__progress {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}

.product-item__progress-bar {
  flex: 1;
  height: 12rpx;
  background: #FEE2E2;
  border-radius: 6rpx;
  overflow: hidden;
}

.product-item__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #E11148, #F97316);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.product-item__progress-text {
  font-size: 20rpx;
  color: #E11148;
  font-weight: 500;
  flex-shrink: 0;
}

/* Bottom */
.product-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.product-item__limit {
  font-size: 22rpx;
  color: #94A3B8;
}

.product-item__btn {
  background: linear-gradient(135deg, #E11148, #DC2626);
  border-radius: 28rpx;
  padding: 10rpx 28rpx;
}

.product-item__btn--disabled {
  background: #CBD5E1;
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
