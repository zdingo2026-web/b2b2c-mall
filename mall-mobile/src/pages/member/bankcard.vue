<template>
  <view class="page-bankcard">
    <scroll-view scroll-y class="bankcard-scroll">
      <!-- Card list -->
      <view v-if="cards.length" class="bankcard-list">
        <view
          v-for="card in cards"
          :key="card.id"
          class="bankcard-card"
          :style="{ background: getCardGradient(card.cardColor) }"
          @tap="handleCardTap(card)"
        >
          <!-- Top row: bank logo + name + type -->
          <view class="bankcard-card__top">
            <view class="bankcard-card__logo">
              <image
                v-if="card.bankLogo"
                class="bankcard-card__logo-img"
                :src="card.bankLogo"
                mode="aspectFit"
              />
              <text v-else class="bankcard-card__logo-letter">
                {{ card.bankName.charAt(0) }}
              </text>
            </view>
            <view class="bankcard-card__meta">
              <text class="bankcard-card__bank">{{ card.bankName }}</text>
              <text class="bankcard-card__type">{{ cardTypeMap[card.cardType] || '银行卡' }}</text>
            </view>
          </view>
          <!-- Card number -->
          <text class="bankcard-card__number">{{ card.cardNoMask }}</text>
          <!-- Bottom row: expiry + default tag -->
          <view class="bankcard-card__bottom">
            <text v-if="card.expiryDate" class="bankcard-card__expiry">{{ card.expiryDate }}</text>
            <view v-if="card.isDefault === 1" class="bankcard-card__default-tag">
              <text class="bankcard-card__default-text">默认</text>
            </view>
          </view>
        </view>
      </view>

      <Empty v-if="!loading && !cards.length" text="暂无银行卡" />

      <!-- Security tip -->
      <view class="bankcard-tip">
        <RemixIcon name="shield-check-line" :size="32" color="#D97706" />
        <text class="bankcard-tip__text">您的银行卡信息已加密保护</text>
      </view>

      <!-- Add card button -->
      <view class="bankcard-add" @tap="handleAddCard">
        <RemixIcon name="add-line" :size="44" color="#94A3B8" />
        <text class="bankcard-add__text">添加银行卡</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'
import { getBankCardList, deleteBankCard, setDefaultBankCard, type BankCardVO } from '@/api/bankcard'

const cardTypeMap: Record<number, string> = {
  1: '借记卡',
  2: '信用卡',
}

const cards = ref<BankCardVO[]>([])
const loading = ref(false)

onLoad(() => {
  fetchCards()
})

onShow(() => {
  fetchCards()
})

async function fetchCards() {
  loading.value = true
  try {
    const data = await getBankCardList()
    cards.value = data || []
  } catch {
    cards.value = []
  } finally {
    loading.value = false
  }
}

function getCardGradient(cardColor: string): string {
  const colorMap: Record<string, string> = {
    blue: 'linear-gradient(135deg, #2563EB, #1D4ED8)',
    red: 'linear-gradient(135deg, #DC2626, #B91C1C)',
    green: 'linear-gradient(135deg, #059669, #047857)',
  }
  return colorMap[cardColor] || colorMap.blue
}

function handleCardTap(card: BankCardVO) {
  const itemList = ['设为默认', '删除']
  uni.showActionSheet({
    itemList,
    success: async (res) => {
      if (res.tapIndex === 0) {
        await handleSetDefault(card)
      } else if (res.tapIndex === 1) {
        await handleDelete(card)
      }
    },
  })
}

async function handleSetDefault(card: BankCardVO) {
  try {
    await setDefaultBankCard(card.id)
    uni.showToast({ title: '已设为默认', icon: 'success' })
    cards.value = cards.value.map((c) => ({
      ...c,
      isDefault: c.id === card.id ? 1 : 0,
    }))
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleDelete(card: BankCardVO) {
  const [err] = await uni.showModal({
    title: '提示',
    content: `确定删除${card.bankName}尾号${card.cardNoMask.slice(-4)}的银行卡？`,
  }) as any
  if (!(err?.confirm || err === true)) return

  try {
    await deleteBankCard(card.id)
    uni.showToast({ title: '已删除', icon: 'success' })
    cards.value = cards.value.filter((c) => c.id !== card.id)
  } catch {
    uni.showToast({ title: '删除失败', icon: 'none' })
  }
}

function handleAddCard() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page-bankcard {
  min-height: 100vh;
  background: #F9FAFB;
}

.bankcard-scroll {
  height: 100vh;
  padding: 24rpx;
}

/* Card list */
.bankcard-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* Bank card visual */
.bankcard-card {
  border-radius: 24rpx;
  padding: 32rpx;
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.bankcard-card__top {
  display: flex;
  align-items: center;
}

.bankcard-card__logo {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.bankcard-card__logo-img {
  width: 48rpx;
  height: 48rpx;
}

.bankcard-card__logo-letter {
  font-size: 32rpx;
  color: #fff;
  font-weight: 700;
}

.bankcard-card__meta {
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.bankcard-card__bank {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}

.bankcard-card__type {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.75);
}

.bankcard-card__number {
  display: block;
  margin-top: 32rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4rpx;
}

.bankcard-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
}

.bankcard-card__expiry {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.bankcard-card__default-tag {
  background: linear-gradient(135deg, #FBBF24, #D97706);
  border-radius: 12rpx;
  padding: 4rpx 16rpx;
}

.bankcard-card__default-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
}

/* Security tip */
.bankcard-tip {
  display: flex;
  align-items: center;
  background: #FEF3C7;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-top: 32rpx;
  gap: 12rpx;
}

.bankcard-tip__text {
  font-size: 24rpx;
  color: #92400E;
}

/* Add card */
.bankcard-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed #CBD5E1;
  border-radius: 24rpx;
  padding: 48rpx 0;
  margin-top: 24rpx;
  background: #fff;
  gap: 12rpx;
}

.bankcard-add__text {
  font-size: 26rpx;
  color: #64748B;
}
</style>
