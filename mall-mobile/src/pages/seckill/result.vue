<template>
  <view class="page-seckill-result">
    <!-- Queuing state -->
    <view v-if="status === 'pending'" class="result-state">
      <view class="result-state__icon result-state__icon--pending">
        <RemixIcon name="loader-4-line" :size="64" color="#F97316" />
      </view>
      <text class="result-state__title">排队中</text>
      <text class="result-state__desc">正在为您排队抢购，请稍候...</text>
      <view class="result-state__actions">
        <view class="result-state__btn result-state__btn--primary" @tap="checkResult">
          <text class="result-state__btn-text">查看结果</text>
        </view>
        <view class="result-state__btn result-state__btn--secondary" @tap="goHome">
          <text class="result-state__btn-text result-state__btn-text--secondary">返回首页</text>
        </view>
      </view>
    </view>

    <!-- Success state -->
    <view v-else-if="status === 'success'" class="result-state">
      <view class="result-state__icon result-state__icon--success">
        <RemixIcon name="check-line" :size="64" color="#16A34A" />
      </view>
      <text class="result-state__title">抢购成功</text>
      <text class="result-state__desc">恭喜您，秒杀商品抢购成功！</text>
      <view class="result-state__order-info">
        <text class="result-state__order-label">订单号</text>
        <text class="result-state__order-no">{{ orderNo }}</text>
      </view>
      <view class="result-state__actions">
        <view class="result-state__btn result-state__btn--primary" @tap="goOrderDetail">
          <text class="result-state__btn-text">查看订单</text>
        </view>
        <view class="result-state__btn result-state__btn--secondary" @tap="goHome">
          <text class="result-state__btn-text result-state__btn-text--secondary">返回首页</text>
        </view>
      </view>
    </view>

    <!-- Failed state -->
    <view v-else-if="status === 'failed'" class="result-state">
      <view class="result-state__icon result-state__icon--failed">
        <RemixIcon name="close-line" :size="64" color="#EF4444" />
      </view>
      <text class="result-state__title">抢购失败</text>
      <text class="result-state__desc">{{ failReason || '很遗憾，商品已被抢光，下次再来吧' }}</text>
      <view class="result-state__actions">
        <view class="result-state__btn result-state__btn--primary" @tap="goSeckill">
          <text class="result-state__btn-text">继续抢购</text>
        </view>
        <view class="result-state__btn result-state__btn--secondary" @tap="goHome">
          <text class="result-state__btn-text result-state__btn-text--secondary">返回首页</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { getSeckillResult } from '@/api/seckill'

const status = ref<'pending' | 'success' | 'failed'>('pending')
const orderNo = ref('')
const failReason = ref('')

let activityId = 0
let skuId = 0
let pollTimer: ReturnType<typeof setInterval> | null = null
let pollCount = 0

onLoad((query) => {
  activityId = Number(query?.activityId) || 0
  skuId = Number(query?.skuId) || 0

  // Check if orderNo is already available (from successful seckill redirect)
  if (query?.orderNo) {
    orderNo.value = query.orderNo
    status.value = 'success'
    return
  }

  if (activityId && skuId) {
    startPolling()
  } else {
    status.value = 'failed'
    failReason.value = '参数错误'
  }
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})

function startPolling() {
  checkResult()
  pollTimer = setInterval(() => {
    pollCount++
    if (pollCount >= 10) {
      // Max 10 polls (~30 seconds)
      if (pollTimer) clearInterval(pollTimer)
      if (status.value === 'pending') {
        status.value = 'failed'
        failReason.value = '排队超时，请稍后查看订单'
      }
      return
    }
    checkResult()
  }, 3000)
}

async function checkResult() {
  try {
    const data = await getSeckillResult({ activityId, skuId })
    if (data.status === 1) {
      // Success
      status.value = 'success'
      orderNo.value = data.orderNo || ''
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    } else if (data.status === 2) {
      // Failed
      status.value = 'failed'
      failReason.value = ''
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }
    // status === 0 means still pending, continue polling
  } catch {
    // Keep pending state, will retry
  }
}

function goOrderDetail() {
  uni.redirectTo({ url: `/pages/order/detail?orderNo=${orderNo.value}` })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

function goSeckill() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page-seckill-result {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 48rpx;
}

.result-state__icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.result-state__icon--pending {
  background: #FFF7ED;
}

.result-state__icon--success {
  background: #F0FDF4;
}

.result-state__icon--failed {
  background: #FEF2F2;
}

.result-state__title {
  font-size: 36rpx;
  color: #1E293B;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.result-state__desc {
  font-size: 26rpx;
  color: #64748B;
  text-align: center;
  line-height: 1.6;
}

.result-state__order-info {
  margin-top: 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
}

.result-state__order-label {
  font-size: 26rpx;
  color: #64748B;
  flex-shrink: 0;
}

.result-state__order-no {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-state__actions {
  margin-top: 48rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  width: 100%;
}

.result-state__btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
}

.result-state__btn--primary {
  background: linear-gradient(135deg, #E11148, #DC2626);
}

.result-state__btn--secondary {
  background: #FFFFFF;
  border: 2rpx solid #E5E7EB;
}

.result-state__btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.result-state__btn-text--secondary {
  color: #64748B;
}
</style>
