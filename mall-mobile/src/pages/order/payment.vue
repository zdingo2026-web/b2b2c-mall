<template>
  <view class="page-payment">
    <view class="payment-info">
      <text class="payment-info__label">支付金额</text>
      <text class="payment-info__amount">&yen;{{ orderAmount }}</text>
    </view>

    <view class="payment-methods">
      <text class="payment-methods__title">选择支付方式</text>
      <view
        v-for="method in payMethods"
        :key="method.value"
        class="payment-method"
        :class="{ 'payment-method--active': selectedMethod === method.value }"
        @tap="selectedMethod = method.value"
      >
        <text class="payment-method__icon">{{ method.icon }}</text>
        <text class="payment-method__name">{{ method.label }}</text>
        <view class="payment-method__radio" :class="{ 'payment-method__radio--checked': selectedMethod === method.value }">
          <text v-if="selectedMethod === method.value" class="payment-method__check">✓</text>
        </view>
      </view>
    </view>

    <view class="payment-btn" @tap="handlePay">
      <text>确认支付</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { payOrder } from '@/api/payment'
import { getOrderDetail } from '@/api/order'

const orderNo = ref('')
const orderAmount = ref('0.00')
const selectedMethod = ref(3) // Default to balance payment (3)
const paying = ref(false)

const payMethods = [
  { value: 1, label: '微信支付', icon: '💬' },
  { value: 2, label: '支付宝', icon: '🔵' },
  { value: 3, label: '余额支付', icon: '💰' },
]

onLoad(async (query) => {
  orderNo.value = query?.orderNo || ''
  if (orderNo.value) {
    try {
      const order = await getOrderDetail(orderNo.value)
      orderAmount.value = order.payAmount
    } catch { /* handled */ }
  }
})

async function handlePay() {
  if (paying.value) return
  paying.value = true
  try {
    await payOrder(orderNo.value, selectedMethod.value)
    uni.showToast({ title: '支付成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/order/detail?orderNo=${orderNo.value}` })
    }, 1500)
  } catch {
    // handled by request wrapper
  } finally {
    paying.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-payment { min-height: 100vh; background: #f5f5f5; }
.payment-info { background: #fff; text-align: center; padding: 64rpx 32rpx; }
.payment-info__label { font-size: 28rpx; color: #999; display: block; margin-bottom: 16rpx; }
.payment-info__amount { font-size: 56rpx; color: #f97316; font-weight: 700; }
.payment-methods { background: #fff; margin: 20rpx 24rpx; border-radius: 16rpx; padding: 24rpx; }
.payment-methods__title { font-size: 28rpx; font-weight: 600; color: #333; display: block; margin-bottom: 20rpx; }
.payment-method { display: flex; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.payment-method:last-child { border-bottom: none; }
.payment-method__icon { font-size: 40rpx; margin-right: 20rpx; }
.payment-method__name { flex: 1; font-size: 28rpx; color: #333; }
.payment-method__radio { width: 40rpx; height: 40rpx; border-radius: 50%; border: 2rpx solid #ddd; display: flex; align-items: center; justify-content: center; }
.payment-method__radio--checked { background: #f97316; border-color: #f97316; }
.payment-method__check { color: #fff; font-size: 22rpx; }
.payment-btn { background: #f97316; color: #fff; text-align: center; padding: 28rpx; border-radius: 40rpx; font-size: 32rpx; font-weight: 500; margin: 40rpx 24rpx; }
</style>
