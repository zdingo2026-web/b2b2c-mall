<template>
  <view class="page-seckill-detail">
    <!-- Loading -->
    <view v-if="loading" class="loading-state">
      <text class="loading-state__text">加载中...</text>
    </view>

    <template v-else-if="detail">
      <scroll-view scroll-y class="detail-scroll">
        <!-- Product images -->
        <swiper class="product-swiper" circular>
          <swiper-item v-for="(img, idx) in productImages" :key="idx">
            <image class="product-swiper__img" :src="img" mode="aspectFill" />
          </swiper-item>
        </swiper>

        <!-- Price & countdown -->
        <view class="price-card">
          <view class="price-card__row">
            <view class="price-card__left">
              <text class="price-card__symbol">&yen;</text>
              <text class="price-card__value">{{ detail.seckillPrice }}</text>
              <text class="price-card__original">&yen;{{ detail.originalPrice }}</text>
            </view>
            <view class="price-card__tag">
              <text class="price-card__tag-text">秒杀</text>
            </view>
          </view>
          <text class="price-card__name">{{ detail.productName }}</text>
          <view class="price-card__meta">
            <text class="price-card__meta-item">限购{{ detail.limitPerUser }}件</text>
            <text class="price-card__meta-item">已抢{{ detail.salesCount }}件</text>
          </view>
        </view>

        <!-- Countdown -->
        <view class="countdown-card">
          <RemixIcon name="time-line" :size="28" color="#E11148" />
          <text class="countdown-card__label">{{ countdownLabel }}</text>
          <view class="countdown-card__blocks">
            <view class="countdown-card__block">
              <text class="countdown-card__block-text">{{ countdownH }}</text>
            </view>
            <text class="countdown-card__colon">:</text>
            <view class="countdown-card__block">
              <text class="countdown-card__block-text">{{ countdownM }}</text>
            </view>
            <text class="countdown-card__colon">:</text>
            <view class="countdown-card__block">
              <text class="countdown-card__block-text">{{ countdownS }}</text>
            </view>
          </view>
        </view>

        <!-- Progress -->
        <view class="progress-card">
          <view class="progress-card__bar">
            <view class="progress-card__fill" :style="{ width: progressPercent + '%' }" />
          </view>
          <text class="progress-card__text">已抢{{ progressPercent }}%</text>
        </view>

        <!-- SKU selection -->
        <view class="card" @tap="showSkuPicker = true">
          <view class="spec-row">
            <text class="spec-row__label">选择</text>
            <text class="spec-row__value">{{ selectedSku ? selectedSku.specValues : '请选择规格' }}</text>
            <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
          </view>
        </view>

        <!-- Product description -->
        <view class="card">
          <text class="card__title">商品详情</text>
          <rich-text v-if="detail.description" :nodes="detail.description" class="product-desc" />
          <text v-else class="empty-desc">暂无详情</text>
        </view>

        <!-- Bottom spacer -->
        <view style="height: 140rpx" />
      </scroll-view>

      <!-- Bottom action bar -->
      <view class="bottom-bar">
        <view class="bottom-bar__info">
          <text class="bottom-bar__price">&yen;{{ detail.seckillPrice }}</text>
          <text class="bottom-bar__stock">剩余{{ detail.availableStock }}件</text>
        </view>
        <view
          class="bottom-bar__btn"
          :class="{ 'bottom-bar__btn--disabled': detail.availableStock <= 0 }"
          @tap="handleSeckill"
        >
          <text class="bottom-bar__btn-text">{{ detail.availableStock <= 0 ? '已抢光' : '立即抢购' }}</text>
        </view>
      </view>

      <!-- SKU picker popup -->
      <view v-if="showSkuPicker" class="sku-overlay" @tap="showSkuPicker = false">
        <view class="sku-panel" @tap.stop>
          <view class="sku-panel__header">
            <image
              v-if="detail.mainImage"
              class="sku-panel__img"
              :src="detail.mainImage"
              mode="aspectFill"
            />
            <view class="sku-panel__info">
              <text class="sku-panel__price">&yen;{{ selectedSku?.seckillPrice || detail.seckillPrice }}</text>
              <text class="sku-panel__stock">库存: {{ selectedSku?.stock || detail.availableStock }}</text>
              <text v-if="selectedSku?.specValues" class="sku-panel__spec">已选: {{ selectedSku.specValues }}</text>
            </view>
            <view class="sku-panel__close" @tap="showSkuPicker = false">
              <RemixIcon name="close-line" :size="40" color="#94A3B8" />
            </view>
          </view>
          <scroll-view scroll-y class="sku-panel__body">
            <view
              v-for="sku in detail.skuList"
              :key="sku.id"
              class="sku-panel__item"
              :class="{
                'sku-panel__item--active': selectedSku?.id === sku.id,
                'sku-panel__item--disabled': sku.stock <= 0,
              }"
              @tap="selectSku(sku)"
            >
              <text>{{ sku.specValues || '默认' }}</text>
            </view>
          </scroll-view>
          <view class="sku-panel__footer">
            <view class="sku-panel__confirm" @tap="handleSkuConfirm">
              <text class="sku-panel__confirm-text">确定</text>
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- Error state -->
    <view v-else class="empty-state">
      <text class="empty-state__text">活动不存在或已结束</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getSeckillProducts,
  doSeckill,
  type SeckillProductVO,
} from '@/api/seckill'

const userStore = useUserStore()

const detail = ref<SeckillProductVO | null>(null)
const loading = ref(true)
const selectedSku = ref<any>(null)
const showSkuPicker = ref(false)

const countdownH = ref('00')
const countdownM = ref('00')
const countdownS = ref('00')
const countdownLabel = ref('距结束')
let countdownTimer: ReturnType<typeof setInterval> | null = null

let activityId = 0
let skuId = 0

const productImages = computed(() => {
  if (!detail.value) return []
  return [detail.value.mainImage]
})

const progressPercent = computed(() => {
  if (!detail.value?.totalStock) return 0
  return Math.min(100, Math.round((detail.value.salesCount / detail.value.totalStock) * 100))
})

onLoad((query) => {
  activityId = Number(query?.activityId) || 0
  skuId = Number(query?.skuId) || 0
  if (activityId) {
    fetchDetail()
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

async function fetchDetail() {
  loading.value = true
  try {
    // Use the product list API to find the matching product
    // In a real app, there would be a detail API
    const data = await getSeckillProducts({ slotId: 0, page: 1, limit: 1 })
    // For now, construct detail from the product data passed via event channel
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1] as any
    const eventChannel = currentPage.getOpenerEventChannel?.()
    if (eventChannel) {
      eventChannel.on('productData', (data: SeckillProductVO) => {
        detail.value = data
        startCountdown()
        loading.value = false
      })
    } else {
      // Fallback: use query params
      detail.value = {
        id: 0,
        activityId,
        spuId: Number(query?.spuId) || 0,
        skuId,
        productName: query?.name || '秒杀商品',
        mainImage: query?.image || '',
        seckillPrice: query?.price || '0',
        originalPrice: query?.originalPrice || '0',
        totalStock: Number(query?.totalStock) || 0,
        availableStock: Number(query?.stock) || 0,
        limitPerUser: Number(query?.limit) || 1,
        salesCount: Number(query?.sales) || 0,
      } as SeckillProductVO
      startCountdown()
      loading.value = false
    }
  } catch {
    detail.value = null
    loading.value = false
  }
}

function startCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  const update = () => {
    const now = Date.now()
    // Assume 24h seckill window for demo
    const endTime = now + 3600000
    const diff = endTime - now
    if (diff <= 0) {
      countdownH.value = '00'
      countdownM.value = '00'
      countdownS.value = '00'
      countdownLabel.value = '已结束'
      if (countdownTimer) clearInterval(countdownTimer)
      return
    }
    const totalSec = Math.floor(diff / 1000)
    countdownH.value = String(Math.floor(totalSec / 3600)).padStart(2, '0')
    countdownM.value = String(Math.floor((totalSec % 3600) / 60)).padStart(2, '0')
    countdownS.value = String(totalSec % 60).padStart(2, '0')
  }
  update()
  countdownTimer = setInterval(update, 1000)
}

function selectSku(sku: any) {
  if (sku.stock <= 0) return
  selectedSku.value = sku
}

function handleSkuConfirm() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  showSkuPicker.value = false
}

async function handleSeckill() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!detail.value || detail.value.availableStock <= 0) return

  const { confirm } = await uni.showModal({
    title: '确认抢购',
    content: `确认以 ¥${detail.value.seckillPrice} 抢购该商品？`,
  })
  if (!confirm) return

  try {
    uni.showLoading({ title: '抢购中...' })
    const result = await doSeckill(activityId, selectedSku.value?.skuId || skuId)
    uni.hideLoading()
    // Navigate to seckill result page
    uni.redirectTo({
      url: `/pages/seckill/result?activityId=${activityId}&skuId=${selectedSku.value?.skuId || skuId}`,
    })
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '抢购失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-seckill-detail {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
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

.detail-scroll {
  flex: 1;
  height: 0;
}

/* Swiper */
.product-swiper {
  height: 600rpx;
}

.product-swiper__img {
  width: 100%;
  height: 600rpx;
}

/* Price card */
.price-card {
  background: #FFFFFF;
  padding: 28rpx 32rpx;
}

.price-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-card__left {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.price-card__symbol {
  font-size: 28rpx;
  color: #E11148;
  font-weight: 700;
}

.price-card__value {
  font-size: 48rpx;
  color: #E11148;
  font-weight: 700;
  line-height: 1;
}

.price-card__original {
  font-size: 24rpx;
  color: #94A3B8;
  text-decoration: line-through;
  margin-left: 16rpx;
}

.price-card__tag {
  background: linear-gradient(135deg, #E11148, #DC2626);
  border-radius: 8rpx;
  padding: 6rpx 16rpx;
}

.price-card__tag-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.price-card__name {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 600;
  display: block;
  margin-top: 16rpx;
  line-height: 1.5;
}

.price-card__meta {
  display: flex;
  gap: 24rpx;
  margin-top: 12rpx;
}

.price-card__meta-item {
  font-size: 24rpx;
  color: #64748B;
}

/* Countdown card */
.countdown-card {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #FEF2F2;
  padding: 20rpx 32rpx;
}

.countdown-card__label {
  font-size: 26rpx;
  color: #E11148;
  font-weight: 500;
}

.countdown-card__blocks {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-left: auto;
}

.countdown-card__block {
  background: #E11148;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  min-width: 48rpx;
  text-align: center;
}

.countdown-card__block-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.countdown-card__colon {
  font-size: 26rpx;
  color: #E11148;
  font-weight: 700;
}

/* Progress card */
.progress-card {
  background: #FFFFFF;
  padding: 20rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-card__bar {
  flex: 1;
  height: 16rpx;
  background: #FEE2E2;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-card__fill {
  height: 100%;
  background: linear-gradient(90deg, #E11148, #F97316);
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

.progress-card__text {
  font-size: 24rpx;
  color: #E11148;
  font-weight: 500;
  flex-shrink: 0;
}

/* Card */
.card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 28rpx 32rpx;
}

.card__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 16rpx;
}

/* Spec row */
.spec-row {
  display: flex;
  align-items: center;
}

.spec-row__label {
  font-size: 28rpx;
  color: #64748B;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.spec-row__value {
  font-size: 28rpx;
  color: #1E293B;
  flex: 1;
}

.product-desc {
  font-size: 28rpx;
  color: #334155;
  line-height: 1.8;
}

.empty-desc {
  font-size: 26rpx;
  color: #94A3B8;
}

/* Bottom bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.bottom-bar__info {
  display: flex;
  flex-direction: column;
}

.bottom-bar__price {
  font-size: 36rpx;
  color: #E11148;
  font-weight: 700;
}

.bottom-bar__stock {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 4rpx;
}

.bottom-bar__btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E11148, #DC2626);
  border-radius: 44rpx;
}

.bottom-bar__btn--disabled {
  background: #CBD5E1;
}

.bottom-bar__btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* SKU overlay */
.sku-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.sku-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.sku-panel__header {
  display: flex;
  padding: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
  position: relative;
}

.sku-panel__img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.sku-panel__info {
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.sku-panel__price {
  font-size: 40rpx;
  color: #E11148;
  font-weight: 700;
}

.sku-panel__stock {
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 8rpx;
}

.sku-panel__spec {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.sku-panel__close {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sku-panel__body {
  flex: 1;
  padding: 24rpx 32rpx;
  max-height: 400rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  align-content: flex-start;
}

.sku-panel__item {
  padding: 14rpx 28rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #334155;
  border: 2rpx solid transparent;
}

.sku-panel__item--active {
  background: #FEF2F2;
  color: #E11148;
  border-color: #E11148;
}

.sku-panel__item--disabled {
  color: #CBD5E1;
  text-decoration: line-through;
}

.sku-panel__footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F0F0F0;
}

.sku-panel__confirm {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E11148, #DC2626);
  border-radius: 44rpx;
}

.sku-panel__confirm-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
