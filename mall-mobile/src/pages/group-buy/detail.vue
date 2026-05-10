<template>
  <view class="page-group-buy-detail">
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

        <!-- Price info -->
        <view class="price-card">
          <view class="price-card__row">
            <view class="price-card__left">
              <text class="price-card__symbol">&yen;</text>
              <text class="price-card__value">{{ detail.groupPrice }}</text>
              <text class="price-card__original">&yen;{{ detail.originalPrice }}</text>
            </view>
            <view class="price-card__group-tag">
              <text class="price-card__group-tag-text">{{ detail.groupSize }}人团</text>
            </view>
          </view>
          <text class="price-card__name">{{ detail.productName }}</text>
          <view class="price-card__meta">
            <text class="price-card__meta-item">已拼{{ detail.joinedCount }}件</text>
            <text class="price-card__meta-item">限购{{ detail.limitPerUser }}件</text>
            <text class="price-card__meta-item">库存{{ detail.totalStock }}件</text>
          </view>
        </view>

        <!-- SKU selection -->
        <view class="card" @tap="showSkuPicker = true">
          <view class="spec-row">
            <text class="spec-row__label">选择</text>
            <text class="spec-row__value">{{ selectedSku ? selectedSku.specValues : '请选择规格' }}</text>
            <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
          </view>
        </view>

        <!-- Existing groups -->
        <view v-if="detail.groups?.length" class="card">
          <view class="card__header">
            <text class="card__title">正在拼团</text>
            <text class="card__count">{{ detail.groups.length }}个团</text>
          </view>
          <view v-for="group in detail.groups" :key="group.groupId" class="group-item">
            <view class="group-item__left">
              <image v-if="group.leaderAvatar" class="group-item__avatar" :src="group.leaderAvatar" mode="aspectFill" />
              <view v-else class="group-item__avatar group-item__avatar--placeholder">
                <text class="group-item__avatar-text">{{ (group.leaderNickname || '用户').charAt(0) }}</text>
              </view>
              <view class="group-item__info">
                <text class="group-item__nickname">{{ group.leaderNickname || '匿名用户' }}</text>
                <text class="group-item__remain">还差{{ group.remainCount }}人成团</text>
              </view>
            </view>
            <view class="group-item__btn" @tap="handleJoinGroup(group)">
              <text class="group-item__btn-text">去拼团</text>
            </view>
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
        <view class="bottom-bar__btn bottom-bar__btn--solo" @tap="handleStartGroup">
          <text class="bottom-bar__btn-text">发起拼团</text>
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
              <text class="sku-panel__price">&yen;{{ selectedSku?.groupPrice || detail.groupPrice }}</text>
              <text class="sku-panel__stock">库存: {{ selectedSku?.stock || detail.totalStock }}</text>
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getGroupBuyDetail,
  joinGroupBuy,
  type GroupBuyDetailVO,
  type GroupBuySkuVO,
  type GroupBuyGroupVO,
} from '@/api/group-buy'

const userStore = useUserStore()

const detail = ref<GroupBuyDetailVO | null>(null)
const loading = ref(true)
const selectedSku = ref<GroupBuySkuVO | null>(null)
const showSkuPicker = ref(false)
const pendingGroupId = ref<number | null>(null)

let activityId = 0

const productImages = computed(() => {
  if (!detail.value) return []
  return detail.value.images?.length ? detail.value.images : [detail.value.mainImage]
})

onLoad((query) => {
  activityId = Number(query?.activityId) || 0
  if (activityId) {
    fetchDetail()
  }
})

async function fetchDetail() {
  loading.value = true
  try {
    detail.value = await getGroupBuyDetail(activityId)
    if (detail.value?.skuList?.length === 1) {
      selectedSku.value = detail.value.skuList[0]
    }
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function selectSku(sku: GroupBuySkuVO) {
  if (sku.stock <= 0) return
  selectedSku.value = sku
}

function checkLogin(): boolean {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return false
  }
  return true
}

function handleStartGroup() {
  if (!checkLogin()) return
  pendingGroupId.value = null
  if (detail.value?.skuList?.length > 1) {
    showSkuPicker.value = true
  } else {
    if (!selectedSku.value && detail.value?.skuList?.length === 1) {
      selectedSku.value = detail.value.skuList[0]
    }
    doJoinGroup()
  }
}

function handleJoinGroup(group: GroupBuyGroupVO) {
  if (!checkLogin()) return
  pendingGroupId.value = group.groupId
  if (detail.value?.skuList?.length > 1) {
    showSkuPicker.value = true
  } else {
    if (!selectedSku.value && detail.value?.skuList?.length === 1) {
      selectedSku.value = detail.value.skuList[0]
    }
    doJoinGroup()
  }
}

function handleSkuConfirm() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  showSkuPicker.value = false
  doJoinGroup()
}

async function doJoinGroup() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '提交中...' })
    const data = {
      skuId: selectedSku.value.skuId,
      groupId: pendingGroupId.value || undefined,
    }
    const result = await joinGroupBuy(activityId, data)
    uni.hideLoading()
    uni.showToast({ title: '拼团成功', icon: 'success' })
    if (result?.orderNo) {
      setTimeout(() => {
        uni.navigateTo({ url: `/pages/order/detail?orderNo=${result.orderNo}` })
      }, 1500)
    }
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '拼团失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-group-buy-detail {
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
  color: #7C3AED;
  font-weight: 700;
}

.price-card__value {
  font-size: 48rpx;
  color: #7C3AED;
  font-weight: 700;
  line-height: 1;
}

.price-card__original {
  font-size: 24rpx;
  color: #94A3B8;
  text-decoration: line-through;
  margin-left: 16rpx;
}

.price-card__group-tag {
  background: #7C3AED;
  border-radius: 8rpx;
  padding: 6rpx 16rpx;
}

.price-card__group-tag-text {
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

/* Card */
.card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 28rpx 32rpx;
}

.card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.card__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.card__count {
  font-size: 24rpx;
  color: #64748B;
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

/* Group item */
.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.group-item:last-child {
  border-bottom: none;
}

.group-item__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.group-item__avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.group-item__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #DBEAFE;
}

.group-item__avatar-text {
  font-size: 28rpx;
  color: #2563EB;
  font-weight: 600;
}

.group-item__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.group-item__nickname {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
}

.group-item__remain {
  font-size: 22rpx;
  color: #F97316;
}

.group-item__btn {
  background: #7C3AED;
  border-radius: 28rpx;
  padding: 10rpx 28rpx;
  flex-shrink: 0;
}

.group-item__btn-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 600;
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
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-bar__btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
}

.bottom-bar__btn--solo {
  background: linear-gradient(135deg, #7C3AED, #6D28D9);
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
  color: #7C3AED;
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
  background: #F5F3FF;
  color: #7C3AED;
  border-color: #7C3AED;
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
  background: #7C3AED;
  border-radius: 44rpx;
}

.sku-panel__confirm-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
