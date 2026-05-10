<template>
  <view class="page-points-exchange">
    <!-- Loading -->
    <view v-if="loading" class="loading-state">
      <text class="loading-state__text">加载中...</text>
    </view>

    <template v-else-if="product">
      <scroll-view scroll-y class="exchange-scroll">
        <!-- Product images -->
        <swiper class="product-swiper" circular>
          <swiper-item v-for="(img, idx) in productImages" :key="idx">
            <image class="product-swiper__img" :src="img" mode="aspectFill" />
          </swiper-item>
        </swiper>

        <!-- Price info -->
        <view class="price-card">
          <view class="price-card__points">
            <text class="price-card__points-value">{{ product.pointsPrice }}</text>
            <text class="price-card__points-unit">积分</text>
          </view>
          <text class="price-card__original">参考价 &yen;{{ product.originalPrice }}</text>
          <text class="price-card__name">{{ product.productName }}</text>
          <view class="price-card__meta">
            <text class="price-card__meta-item">库存{{ product.availableStock }}件</text>
          </view>
        </view>

        <!-- SKU selection -->
        <view v-if="product.skuList?.length > 1" class="card" @tap="showSkuPicker = true">
          <view class="spec-row">
            <text class="spec-row__label">选择</text>
            <text class="spec-row__value">{{ selectedSku ? selectedSku.specValues : '请选择规格' }}</text>
            <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
          </view>
        </view>

        <!-- Product description -->
        <view class="card">
          <text class="card__title">商品详情</text>
          <rich-text v-if="product.description" :nodes="product.description" class="product-desc" />
          <text v-else class="empty-desc">暂无详情</text>
        </view>

        <!-- Bottom spacer -->
        <view style="height: 140rpx" />
      </scroll-view>

      <!-- Bottom action bar -->
      <view class="bottom-bar">
        <view class="bottom-bar__info">
          <text class="bottom-bar__points">需{{ selectedSku?.pointsPrice || product.pointsPrice }}积分</text>
          <text class="bottom-bar__balance">可用{{ availablePoints }}积分</text>
        </view>
        <view
          class="bottom-bar__btn"
          :class="{ 'bottom-bar__btn--disabled': !canExchange }"
          @tap="handleExchange"
        >
          <text class="bottom-bar__btn-text">{{ canExchange ? '立即兑换' : '积分不足' }}</text>
        </view>
      </view>

      <!-- SKU picker popup -->
      <view v-if="showSkuPicker" class="sku-overlay" @tap="showSkuPicker = false">
        <view class="sku-panel" @tap.stop>
          <view class="sku-panel__header">
            <image
              v-if="product.mainImage"
              class="sku-panel__img"
              :src="product.mainImage"
              mode="aspectFill"
            />
            <view class="sku-panel__info">
              <text class="sku-panel__price">{{ selectedSku?.pointsPrice || product.pointsPrice }} 积分</text>
              <text class="sku-panel__stock">库存: {{ selectedSku?.stock || product.availableStock }}</text>
              <text v-if="selectedSku?.specValues" class="sku-panel__spec">已选: {{ selectedSku.specValues }}</text>
            </view>
            <view class="sku-panel__close" @tap="showSkuPicker = false">
              <RemixIcon name="close-line" :size="40" color="#94A3B8" />
            </view>
          </view>
          <scroll-view scroll-y class="sku-panel__body">
            <view
              v-for="sku in product.skuList"
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
            <view class="sku-panel__confirm" @tap="showSkuPicker = false">
              <text class="sku-panel__confirm-text">确定</text>
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- Error state -->
    <view v-else class="empty-state">
      <text class="empty-state__text">商品不存在或已下架</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getPointsProductDetail,
  getPointsAccount,
  createPointsOrder,
  type PointsProductDetailVO,
  type PointsProductSkuVO,
} from '@/api/points'

const userStore = useUserStore()

const product = ref<PointsProductDetailVO | null>(null)
const loading = ref(true)
const selectedSku = ref<PointsProductSkuVO | null>(null)
const showSkuPicker = ref(false)
const availablePoints = ref(0)

let productId = 0

const productImages = computed(() => {
  if (!product.value) return []
  return product.value.images?.length ? product.value.images : [product.value.mainImage]
})

const canExchange = computed(() => {
  const needed = selectedSku.value?.pointsPrice || product.value?.pointsPrice || 0
  return needed <= availablePoints.value
})

onLoad((query) => {
  productId = Number(query?.id) || 0
  if (productId) {
    fetchProduct()
    fetchAccount()
  }
})

async function fetchProduct() {
  loading.value = true
  try {
    product.value = await getPointsProductDetail(productId)
    if (product.value?.skuList?.length === 1) {
      selectedSku.value = product.value.skuList[0]
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function fetchAccount() {
  try {
    const data = await getPointsAccount()
    availablePoints.value = data?.availablePoints || 0
  } catch {
    // handled
  }
}

function selectSku(sku: PointsProductSkuVO) {
  if (sku.stock <= 0) return
  selectedSku.value = sku
}

async function handleExchange() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!canExchange.value) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  if (!selectedSku.value && product.value?.skuList?.length !== 1) {
    showSkuPicker.value = true
    return
  }

  const skuId = selectedSku.value?.skuId || product.value?.skuList?.[0]?.skuId
  if (!skuId) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }

  const { confirm } = await uni.showModal({
    title: '确认兑换',
    content: `确认使用${selectedSku.value?.pointsPrice || product.value?.pointsPrice}积分兑换该商品？`,
  })
  if (!confirm) return

  try {
    uni.showLoading({ title: '兑换中...' })
    const result = await createPointsOrder({
      productId: productId,
      skuId,
      quantity: 1,
      addressId: 0, // Will be selected in order page
    })
    uni.hideLoading()
    uni.showToast({ title: '兑换成功', icon: 'success' })
    if (result?.orderNo) {
      setTimeout(() => {
        uni.navigateTo({ url: `/pages/order/detail?orderNo=${result.orderNo}` })
      }, 1500)
    }
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '兑换失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-points-exchange {
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

/* Scroll */
.exchange-scroll {
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

.price-card__points {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.price-card__points-value {
  font-size: 52rpx;
  color: #16A34A;
  font-weight: 700;
  line-height: 1;
}

.price-card__points-unit {
  font-size: 26rpx;
  color: #16A34A;
  font-weight: 500;
}

.price-card__original {
  font-size: 24rpx;
  color: #94A3B8;
  text-decoration: line-through;
  margin-left: 16rpx;
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
  display: block;
  margin-bottom: 20rpx;
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

/* Product desc */
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
  display: flex;
  align-items: center;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-bar__info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bottom-bar__points {
  font-size: 28rpx;
  color: #16A34A;
  font-weight: 600;
}

.bottom-bar__balance {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.bottom-bar__btn {
  background: #16A34A;
  border-radius: 40rpx;
  padding: 20rpx 48rpx;
  margin-left: 24rpx;
}

.bottom-bar__btn--disabled {
  background: #CBD5E1;
}

.bottom-bar__btn-text {
  font-size: 28rpx;
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
  font-size: 36rpx;
  color: #16A34A;
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
  background: #F0FDF4;
  color: #16A34A;
  border-color: #16A34A;
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
  background: #16A34A;
  border-radius: 44rpx;
}

.sku-panel__confirm-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
