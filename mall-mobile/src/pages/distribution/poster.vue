<template>
  <view class="page-poster">
    <!-- Poster preview -->
    <view class="poster-area">
      <view class="poster-card" id="posterCard">
        <!-- Product image -->
        <image class="poster-card__img" :src="productInfo.mainImage" mode="aspectFill" />

        <!-- Product info overlay -->
        <view class="poster-card__overlay">
          <view class="poster-card__info">
            <text class="poster-card__name">{{ productInfo.productName }}</text>
            <view class="poster-card__price-row">
              <text class="poster-card__price">&yen;{{ productInfo.price }}</text>
              <text class="poster-card__commission">佣金{{ productInfo.commissionRate }}%</text>
            </view>
          </view>

          <!-- QR code area -->
          <view class="poster-card__qrcode">
            <view class="poster-card__qrcode-placeholder">
              <RemixIcon name="qr-code-line" :size="80" color="#64748B" />
            </view>
            <text class="poster-card__qrcode-text">扫码购买</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Actions -->
    <view class="action-area">
      <view class="action-area__btn action-area__btn--save" @tap="handleSave">
        <RemixIcon name="download-line" :size="32" color="#FFFFFF" />
        <text class="action-area__btn-text">保存海报</text>
      </view>
      <view class="action-area__btn action-area__btn--share" @tap="handleShare">
        <RemixIcon name="share-forward-line" :size="32" color="#F97316" />
        <text class="action-area__btn-text action-area__btn-text--share">分享给好友</text>
      </view>
    </view>

    <!-- Tips -->
    <view class="tips">
      <text class="tips__text">分享海报给好友，好友扫码购买后您可获得佣金奖励</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getDistributionProducts,
  type DistributionProductVO,
} from '@/api/distribution'

const userStore = useUserStore()

const productInfo = reactive<Partial<DistributionProductVO>>({})
let spuId = 0

onLoad((query) => {
  spuId = Number(query?.spuId) || 0
  if (spuId) {
    fetchProduct()
  }
})

async function fetchProduct() {
  try {
    const data = await getDistributionProducts({ page: 1, limit: 20 })
    const product = (data.list || []).find((p) => p.spuId === spuId)
    if (product) {
      Object.assign(productInfo, product)
    }
  } catch {
    // handled
  }
}

function handleSave() {
  uni.showToast({ title: '长按海报图片可保存到相册', icon: 'none' })
}

function handleShare() {
  // #ifdef H5
  if (navigator.share) {
    navigator.share({
      title: productInfo.productName || '好物推荐',
      text: `我在商城发现了一个好物，快来看看吧！`,
      url: window.location.href,
    }).catch(() => {})
    return
  }
  // #endif

  uni.showToast({ title: '请截图分享给好友', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page-poster {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Poster area */
.poster-area {
  padding: 32rpx 24rpx;
  width: 100%;
}

.poster-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.poster-card__img {
  width: 100%;
  height: 600rpx;
}

.poster-card__overlay {
  padding: 24rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.poster-card__info {
  flex: 1;
  min-width: 0;
}

.poster-card__name {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.poster-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 12rpx;
}

.poster-card__price {
  font-size: 40rpx;
  color: #E11148;
  font-weight: 700;
}

.poster-card__commission {
  font-size: 24rpx;
  color: #F97316;
  font-weight: 500;
  background: #FFF7ED;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.poster-card__qrcode {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: 24rpx;
  flex-shrink: 0;
}

.poster-card__qrcode-placeholder {
  width: 160rpx;
  height: 160rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.poster-card__qrcode-text {
  font-size: 20rpx;
  color: #64748B;
  margin-top: 8rpx;
}

/* Action area */
.action-area {
  display: flex;
  gap: 20rpx;
  padding: 0 24rpx;
  width: 100%;
}

.action-area__btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 44rpx;
}

.action-area__btn--save {
  background: linear-gradient(135deg, #F97316, #EA580C);
}

.action-area__btn--share {
  background: #FFFFFF;
  border: 2rpx solid #F97316;
}

.action-area__btn-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.action-area__btn-text--share {
  color: #F97316;
}

/* Tips */
.tips {
  padding: 24rpx 32rpx;
  width: 100%;
}

.tips__text {
  font-size: 22rpx;
  color: #94A3B8;
  text-align: center;
  display: block;
}
</style>
