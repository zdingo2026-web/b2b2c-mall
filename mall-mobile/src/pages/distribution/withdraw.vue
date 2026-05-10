<template>
  <view class="page-withdraw">
    <!-- Balance info -->
    <view class="balance-card">
      <text class="balance-card__label">可提现佣金(元)</text>
      <text class="balance-card__value">{{ center?.availableCommission || '0.00' }}</text>
      <view class="balance-card__row">
        <view class="balance-card__stat">
          <text class="balance-card__stat-value">{{ center?.totalCommission || '0.00' }}</text>
          <text class="balance-card__stat-label">累计佣金</text>
        </view>
        <view class="balance-card__stat">
          <text class="balance-card__stat-value">{{ center?.frozenCommission || '0.00' }}</text>
          <text class="balance-card__stat-label">冻结中</text>
        </view>
        <view class="balance-card__stat">
          <text class="balance-card__stat-value">{{ center?.withdrawnCommission || '0.00' }}</text>
          <text class="balance-card__stat-label">已提现</text>
        </view>
      </view>
    </view>

    <!-- Withdraw form -->
    <view class="form-card">
      <view class="form-item">
        <text class="form-item__label">提现金额</text>
        <view class="form-item__input-wrap">
          <text class="form-item__prefix">&yen;</text>
          <input
            v-model="form.amount"
            class="form-item__input"
            placeholder="请输入提现金额"
            type="digit"
          />
        </view>
        <view class="form-item__actions">
          <text class="form-item__link" @tap="withdrawAll">全部提现</text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-item__label">提现方式</text>
        <view class="method-list">
          <view
            v-for="method in methods"
            :key="method.value"
            class="method-item"
            :class="{ 'method-item--active': form.method === method.value }"
            @tap="form.method = method.value"
          >
            <RemixIcon :name="method.icon" :size="32" :color="form.method === method.value ? '#F97316' : '#64748B'" />
            <text class="method-item__text">{{ method.label }}</text>
          </view>
        </view>
      </view>

      <!-- Bank card info (when method is bank) -->
      <view v-if="form.method === 'bank'" class="form-item">
        <text class="form-item__label">银行卡信息</text>
        <view class="bankcard-select" @tap="goBankCard">
          <template v-if="bankCard">
            <view class="bankcard-info">
              <text class="bankcard-info__bank">{{ bankCard.bankName }}</text>
              <text class="bankcard-info__no">{{ bankCard.cardNo }}</text>
            </view>
          </template>
          <template v-else>
            <text class="bankcard-select__placeholder">请选择银行卡</text>
          </template>
          <RemixIcon name="arrow-right-s-line" :size="28" color="#94A3B8" />
        </view>
      </view>

      <!-- Alipay account -->
      <view v-if="form.method === 'alipay'" class="form-item">
        <text class="form-item__label">支付宝账号</text>
        <input
          v-model="form.alipayAccount"
          class="form-item__input form-item__input--full"
          placeholder="请输入支付宝账号"
        />
      </view>

      <!-- WeChat account -->
      <view v-if="form.method === 'wechat'" class="form-item">
        <text class="form-item__label">微信账号</text>
        <input
          v-model="form.wechatAccount"
          class="form-item__input form-item__input--full"
          placeholder="请输入微信号"
        />
      </view>
    </view>

    <!-- Submit -->
    <view class="submit-area">
      <view class="submit-btn" @tap="handleSubmit">
        <text class="submit-btn__text">提交申请</text>
      </view>
      <text class="submit-area__hint">提现申请提交后，将在1-3个工作日内审核</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getDistributionCenter,
  applyWithdraw,
  type DistributionCenterVO,
} from '@/api/distribution'

const userStore = useUserStore()

const center = ref<DistributionCenterVO | null>(null)
const bankCard = ref<any>(null)

const form = reactive({
  amount: '',
  method: 'bank',
  alipayAccount: '',
  wechatAccount: '',
})

const methods = [
  { value: 'bank', label: '银行卡', icon: 'bank-card-line' },
  { value: 'alipay', label: '支付宝', icon: 'alipay-line' },
  { value: 'wechat', label: '微信', icon: 'wechat-line' },
]

onShow(() => {
  fetchCenter()
})

async function fetchCenter() {
  try {
    center.value = await getDistributionCenter()
  } catch {
    // handled
  }
}

function withdrawAll() {
  if (center.value?.availableCommission) {
    form.amount = center.value.availableCommission
  }
}

function goBankCard() {
  uni.navigateTo({
    url: '/pages/member/bankcard?select=1',
    events: {
      selectCard: (data: any) => {
        bankCard.value = data
      },
    },
  })
}

async function handleSubmit() {
  const amount = parseFloat(form.amount)
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入提现金额', icon: 'none' })
    return
  }
  const available = parseFloat(center.value?.availableCommission || '0')
  if (amount > available) {
    uni.showToast({ title: '超出可提现金额', icon: 'none' })
    return
  }
  if (form.method === 'bank' && !bankCard.value) {
    uni.showToast({ title: '请选择银行卡', icon: 'none' })
    return
  }
  if (form.method === 'alipay' && !form.alipayAccount.trim()) {
    uni.showToast({ title: '请输入支付宝账号', icon: 'none' })
    return
  }
  if (form.method === 'wechat' && !form.wechatAccount.trim()) {
    uni.showToast({ title: '请输入微信账号', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })
    await applyWithdraw({
      amount,
      bankCardId: form.method === 'bank' ? bankCard.value?.id : 0,
    })
    uni.hideLoading()
    uni.showToast({ title: '提现申请已提交', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-withdraw {
  min-height: 100vh;
  background: #F9FAFB;
}

/* Balance card */
.balance-card {
  background: linear-gradient(135deg, #F97316, #EA580C);
  padding: 32rpx;
  color: #FFFFFF;
}

.balance-card__label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.balance-card__value {
  font-size: 56rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-top: 8rpx;
  display: block;
}

.balance-card__row {
  display: flex;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.balance-card__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-card__stat-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.balance-card__stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4rpx;
}

/* Form card */
.form-card {
  background: #FFFFFF;
  margin: 20rpx 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
}

.form-item {
  margin-bottom: 28rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item__label {
  font-size: 26rpx;
  color: #64748B;
  display: block;
  margin-bottom: 12rpx;
}

.form-item__input-wrap {
  display: flex;
  align-items: center;
  background: #F3F4F6;
  border-radius: 12rpx;
  padding: 0 24rpx;
  height: 80rpx;
}

.form-item__prefix {
  font-size: 36rpx;
  color: #1E293B;
  font-weight: 700;
  margin-right: 12rpx;
}

.form-item__input {
  flex: 1;
  height: 80rpx;
  font-size: 32rpx;
  color: #1E293B;
  font-weight: 600;
}

.form-item__input--full {
  background: #F3F4F6;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-weight: 400;
  font-size: 28rpx;
}

.form-item__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8rpx;
}

.form-item__link {
  font-size: 24rpx;
  color: #F97316;
  font-weight: 500;
}

/* Method list */
.method-list {
  display: flex;
  gap: 16rpx;
}

.method-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 20rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
}

.method-item--active {
  background: #FFF7ED;
  border-color: #F97316;
}

.method-item__text {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 500;
}

.method-item--active .method-item__text {
  color: #F97316;
}

/* Bank card select */
.bankcard-select {
  display: flex;
  align-items: center;
  background: #F3F4F6;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
}

.bankcard-info {
  flex: 1;
}

.bankcard-info__bank {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  display: block;
}

.bankcard-info__no {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 4rpx;
  display: block;
}

.bankcard-select__placeholder {
  flex: 1;
  font-size: 28rpx;
  color: #94A3B8;
}

/* Submit area */
.submit-area {
  padding: 32rpx 24rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.submit-btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F97316, #EA580C);
  border-radius: 44rpx;
}

.submit-btn__text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.submit-area__hint {
  font-size: 22rpx;
  color: #94A3B8;
  text-align: center;
  margin-top: 16rpx;
  display: block;
}
</style>
