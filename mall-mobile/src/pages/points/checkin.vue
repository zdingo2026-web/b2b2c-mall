<template>
  <view class="page-checkin">
    <!-- Header -->
    <view class="checkin-header">
      <text class="checkin-header__title">每日签到</text>
      <text class="checkin-header__desc">签到领积分，连续签到奖励更多</text>
    </view>

    <!-- Checkin button -->
    <view class="checkin-main">
      <view
        class="checkin-circle"
        :class="{ 'checkin-circle--done': checkinStatus?.checkedToday }"
        @tap="handleCheckin"
      >
        <template v-if="checkinStatus?.checkedToday">
          <RemixIcon name="check-line" :size="64" color="#FFFFFF" />
          <text class="checkin-circle__text">已签到</text>
        </template>
        <template v-else>
          <text class="checkin-circle__points">+{{ checkinStatus?.todayPoints || 10 }}</text>
          <text class="checkin-circle__text">签到</text>
        </template>
      </view>

      <!-- Success animation overlay -->
      <view v-if="showSuccessAnim" class="success-anim">
        <view class="success-anim__ring" />
        <text class="success-anim__text">+{{ earnedPoints }}积分</text>
      </view>

      <!-- Continuous days -->
      <view class="continuous-info">
        <text class="continuous-info__days">{{ checkinStatus?.continuousDays || 0 }}</text>
        <text class="continuous-info__label">天连续签到</text>
      </view>
    </view>

    <!-- 7-day calendar -->
    <view class="calendar-card">
      <text class="calendar-card__title">本周签到</text>
      <view class="calendar-card__week">
        <view
          v-for="(day, idx) in weekDays"
          :key="idx"
          class="calendar-day"
          :class="{
            'calendar-day--checked': day.checked,
            'calendar-day--today': day.isToday,
          }"
        >
          <text class="calendar-day__label">{{ day.label }}</text>
          <view class="calendar-day__icon">
            <template v-if="day.checked">
              <RemixIcon name="check-line" :size="24" color="#FFFFFF" />
            </template>
            <template v-else-if="day.isFuture">
              <text class="calendar-day__dot" />
            </template>
            <template v-else>
              <text class="calendar-day__miss">-</text>
            </template>
          </view>
        </view>
      </view>
    </view>

    <!-- Checkin rules -->
    <view class="rules-card">
      <text class="rules-card__title">签到规则</text>
      <view class="rules-card__list">
        <text class="rules-card__item">1. 每日签到可获得积分奖励</text>
        <text class="rules-card__item">2. 连续签到天数越多，奖励越丰富</text>
        <text class="rules-card__item">3. 中途断签将重新计算连续天数</text>
        <text class="rules-card__item">4. 积分可在积分商城兑换商品</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  doCheckin,
  getCheckinStatus,
  type CheckinStatusVO,
} from '@/api/points'

const userStore = useUserStore()

const checkinStatus = ref<CheckinStatusVO | null>(null)
const showSuccessAnim = ref(false)
const earnedPoints = ref(0)

onShow(() => {
  if (userStore.isLoggedIn) {
    fetchCheckinStatus()
  }
})

async function fetchCheckinStatus() {
  try {
    checkinStatus.value = await getCheckinStatus()
  } catch {
    // handled
  }
}

const weekDays = computed(() => {
  const labels = ['一', '二', '三', '四', '五', '六', '日']
  const now = new Date()
  const dayOfWeek = now.getDay() || 7 // Monday=1, Sunday=7
  const continuousDays = checkinStatus.value?.continuousDays || 0
  const checkedToday = checkinStatus.value?.checkedToday || false

  return labels.map((label, idx) => {
    const dayIndex = idx + 1 // 1-7
    const isToday = dayIndex === dayOfWeek
    const isFuture = dayIndex > dayOfWeek

    // Determine if this day was checked
    let checked = false
    if (isToday && checkedToday) {
      checked = true
    } else if (dayIndex < dayOfWeek) {
      // Past days: check based on continuous days
      const daysAgo = dayOfWeek - dayIndex
      checked = daysAgo < continuousDays || (checkedToday && daysAgo <= continuousDays)
    }

    return { label, checked, isToday, isFuture }
  })
})

async function handleCheckin() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (checkinStatus.value?.checkedToday) return

  try {
    const result = await doCheckin()
    earnedPoints.value = result?.points || 0
    showSuccessAnim.value = true
    setTimeout(() => {
      showSuccessAnim.value = false
    }, 2000)
    fetchCheckinStatus()
  } catch (e: any) {
    uni.showToast({ title: e.message || '签到失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-checkin {
  min-height: 100vh;
  background: #F9FAFB;
}

/* Header */
.checkin-header {
  background: linear-gradient(135deg, #16A34A, #15803D);
  padding: 40rpx 32rpx 60rpx;
  text-align: center;
}

.checkin-header__title {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: 700;
  display: block;
}

.checkin-header__desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

/* Main */
.checkin-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -40rpx;
  position: relative;
}

.checkin-circle {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #16A34A, #15803D);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(22, 163, 74, 0.4);
  position: relative;
  z-index: 2;
}

.checkin-circle--done {
  background: #CBD5E1;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.checkin-circle__points {
  font-size: 44rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.checkin-circle__text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 500;
  margin-top: 4rpx;
}

/* Success animation */
.success-anim {
  position: absolute;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 3;
  animation: animScale 0.6s ease-out;
}

.success-anim__ring {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(22, 163, 74, 0.3);
  animation: animRing 1s ease-out forwards;
}

.success-anim__text {
  font-size: 32rpx;
  color: #16A34A;
  font-weight: 700;
  margin-top: 16rpx;
  animation: animFadeUp 0.8s ease-out;
}

@keyframes animScale {
  0% { transform: scale(0.8); opacity: 0; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes animRing {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

@keyframes animFadeUp {
  0% { transform: translateY(20rpx); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

/* Continuous info */
.continuous-info {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-top: 24rpx;
}

.continuous-info__days {
  font-size: 48rpx;
  color: #16A34A;
  font-weight: 700;
}

.continuous-info__label {
  font-size: 26rpx;
  color: #64748B;
}

/* Calendar card */
.calendar-card {
  background: #FFFFFF;
  margin: 32rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 24rpx;
}

.calendar-card__title {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
  display: block;
  margin-bottom: 20rpx;
}

.calendar-card__week {
  display: flex;
  justify-content: space-between;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.calendar-day__label {
  font-size: 22rpx;
  color: #64748B;
}

.calendar-day__icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-day--checked .calendar-day__icon {
  background: #16A34A;
}

.calendar-day--today .calendar-day__icon {
  border: 2rpx solid #16A34A;
}

.calendar-day__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #CBD5E1;
}

.calendar-day__miss {
  font-size: 24rpx;
  color: #CBD5E1;
}

/* Rules card */
.rules-card {
  background: #FFFFFF;
  margin: 16rpx 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
}

.rules-card__title {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
  display: block;
  margin-bottom: 16rpx;
}

.rules-card__list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.rules-card__item {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.6;
}
</style>
