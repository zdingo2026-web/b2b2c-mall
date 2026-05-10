<template>
  <view class="page-group-buy-confirm">
    <scroll-view scroll-y class="confirm-scroll">
      <!-- Address section -->
      <view class="address-card" @tap="chooseAddress">
        <template v-if="address">
          <view class="address-card__left">
            <RemixIcon name="map-pin-line" :size="36" color="#7C3AED" />
          </view>
          <view class="address-card__info">
            <view class="address-card__row">
              <text class="address-card__name">{{ address.name }}</text>
              <text class="address-card__phone">{{ address.phone }}</text>
            </view>
            <text class="address-card__detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</text>
          </view>
        </template>
        <template v-else>
          <view class="address-card__empty">
            <RemixIcon name="add-circle-line" :size="36" color="#94A3B8" />
            <text class="address-card__empty-text">添加收货地址</text>
          </view>
        </template>
        <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
      </view>

      <!-- Product info -->
      <view class="product-card">
        <image class="product-card__img" :src="productInfo.mainImage" mode="aspectFill" />
        <view class="product-card__info">
          <text class="product-card__name">{{ productInfo.productName }}</text>
          <text v-if="selectedSku" class="product-card__spec">规格: {{ selectedSku.specValues }}</text>
          <view class="product-card__bottom">
            <view class="product-card__price-row">
              <text class="product-card__group-price">&yen;{{ selectedSku?.groupPrice || productInfo.groupPrice }}</text>
              <text class="product-card__original-price">&yen;{{ productInfo.originalPrice }}</text>
            </view>
            <text class="product-card__qty">x1</text>
          </view>
        </view>
      </view>

      <!-- Group info -->
      <view v-if="groupId" class="group-info-card">
        <view class="group-info-card__header">
          <text class="group-info-card__title">拼团信息</text>
        </view>
        <view class="group-info-card__row">
          <text class="group-info-card__label">拼团类型</text>
          <text class="group-info-card__value">{{ productInfo.groupSize }}人团</text>
        </view>
        <view class="group-info-card__row">
          <text class="group-info-card__label">参团方式</text>
          <text class="group-info-card__value">加入已有团</text>
        </view>
      </view>
      <view v-else class="group-info-card">
        <view class="group-info-card__header">
          <text class="group-info-card__title">拼团信息</text>
        </view>
        <view class="group-info-card__row">
          <text class="group-info-card__label">拼团类型</text>
          <text class="group-info-card__value">{{ productInfo.groupSize }}人团</text>
        </view>
        <view class="group-info-card__row">
          <text class="group-info-card__label">参团方式</text>
          <text class="group-info-card__value">发起拼团</text>
        </view>
      </view>

      <!-- Remark -->
      <view class="remark-card">
        <text class="remark-card__label">订单备注</text>
        <input
          v-model="remark"
          class="remark-card__input"
          placeholder="选填，请先和商家协商一致"
        />
      </view>

      <!-- Bottom spacer -->
      <view style="height: 140rpx" />
    </scroll-view>

    <!-- Bottom action bar -->
    <view class="bottom-bar">
      <view class="bottom-bar__left">
        <text class="bottom-bar__label">合计</text>
        <text class="bottom-bar__price">&yen;{{ selectedSku?.groupPrice || productInfo.groupPrice }}</text>
      </view>
      <view class="bottom-bar__btn" @tap="handleConfirm">
        <text class="bottom-bar__btn-text">{{ groupId ? '确认参团' : '确认开团' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getGroupBuyDetail,
  joinGroupBuy,
  type GroupBuyDetailVO,
  type GroupBuySkuVO,
} from '@/api/group-buy'

const userStore = useUserStore()

const address = ref<any>(null)
const remark = ref('')
const productInfo = reactive<Partial<GroupBuyDetailVO>>({})
const selectedSku = ref<GroupBuySkuVO | null>(null)

let activityId = 0
let skuId = 0
let groupId: number | null = null

onLoad((query) => {
  activityId = Number(query?.activityId) || 0
  skuId = Number(query?.skuId) || 0
  groupId = query?.groupId ? Number(query.groupId) : null

  if (activityId) {
    fetchDetail()
  }
})

async function fetchDetail() {
  try {
    const data = await getGroupBuyDetail(activityId)
    Object.assign(productInfo, data)
    // Auto select the matching SKU
    if (skuId && data.skuList?.length) {
      const match = data.skuList.find((s) => s.skuId === skuId)
      if (match) selectedSku.value = match
    } else if (data.skuList?.length === 1) {
      selectedSku.value = data.skuList[0]
    }
  } catch {
    // handled
  }
}

function chooseAddress() {
  uni.navigateTo({
    url: '/pages/order/address?select=1',
    events: {
      selectAddress: (data: any) => {
        address.value = data
      },
    },
  })
}

async function handleConfirm() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!address.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择商品规格', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })
    const data = {
      skuId: selectedSku.value.skuId,
      groupId: groupId || undefined,
    }
    const result = await joinGroupBuy(activityId, data)
    uni.hideLoading()
    uni.showToast({ title: '拼团成功', icon: 'success' })
    if (result?.orderNo) {
      setTimeout(() => {
        uni.redirectTo({ url: `/pages/order/detail?orderNo=${result.orderNo}` })
      }, 1500)
    }
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '参团失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-group-buy-confirm {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

.confirm-scroll {
  flex: 1;
  height: 0;
}

/* Address card */
.address-card {
  background: #FFFFFF;
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.address-card__left {
  flex-shrink: 0;
}

.address-card__info {
  flex: 1;
  min-width: 0;
}

.address-card__row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.address-card__name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
}

.address-card__phone {
  font-size: 26rpx;
  color: #64748B;
}

.address-card__detail {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.address-card__empty {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.address-card__empty-text {
  font-size: 28rpx;
  color: #94A3B8;
}

/* Product card */
.product-card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 24rpx 32rpx;
  display: flex;
  gap: 20rpx;
}

.product-card__img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.product-card__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.product-card__name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-card__spec {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 8rpx;
}

.product-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.product-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.product-card__group-price {
  font-size: 32rpx;
  color: #7C3AED;
  font-weight: 700;
}

.product-card__original-price {
  font-size: 22rpx;
  color: #94A3B8;
  text-decoration: line-through;
}

.product-card__qty {
  font-size: 26rpx;
  color: #64748B;
}

/* Group info card */
.group-info-card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 24rpx 32rpx;
}

.group-info-card__header {
  margin-bottom: 16rpx;
}

.group-info-card__title {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
}

.group-info-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
}

.group-info-card__label {
  font-size: 26rpx;
  color: #64748B;
}

.group-info-card__value {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
}

/* Remark card */
.remark-card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.remark-card__label {
  font-size: 26rpx;
  color: #64748B;
  flex-shrink: 0;
}

.remark-card__input {
  flex: 1;
  font-size: 26rpx;
  color: #1E293B;
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
  justify-content: space-between;
}

.bottom-bar__left {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.bottom-bar__label {
  font-size: 26rpx;
  color: #64748B;
}

.bottom-bar__price {
  font-size: 36rpx;
  color: #7C3AED;
  font-weight: 700;
}

.bottom-bar__btn {
  background: linear-gradient(135deg, #7C3AED, #6D28D9);
  border-radius: 44rpx;
  height: 88rpx;
  padding: 0 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-bar__btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
