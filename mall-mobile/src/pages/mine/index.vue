<template>
  <view class="page-mine">
    <!-- Top gradient header with user info -->
    <view class="mine-header">
      <!-- Settings icon top right -->
      <view v-if="userStore.isLoggedIn" class="mine-header__settings" @tap="handleSettings">
        <RemixIcon name="settings-3-line" :size="40" color="rgba(255,255,255,0.8)" />
      </view>

      <template v-if="userStore.isLoggedIn">
        <view class="mine-header__user">
          <view class="mine-header__avatar">
            <text v-if="!userStore.userInfo?.avatar" class="mine-header__avatar-letter">
              {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
            </text>
            <image
              v-else
              class="mine-header__avatar-img"
              :src="userStore.userInfo.avatar"
              mode="aspectFill"
            />
          </view>
          <view class="mine-header__info">
            <view class="mine-header__name-row">
              <text class="mine-header__name">{{ userStore.userInfo?.nickname || '用户' }}</text>
              <view class="mine-header__level-badge">
                <text class="mine-header__level-text">V{{ memberLevel }}会员</text>
              </view>
            </view>
            <view class="mine-header__id-row">
              <text class="mine-header__id">会员ID: {{ userStore.userInfo?.memberNo || userStore.userInfo?.id || '' }}</text>
              <view class="mine-header__copy" @tap.stop="copyMemberId">
                <RemixIcon name="file-copy-line" :size="24" color="rgba(255,255,255,0.8)" />
              </view>
            </view>
          </view>
        </view>
      </template>
      <template v-else>
        <view class="mine-header__login" @tap="navigateTo('/pages/login/index')">
          <view class="mine-header__avatar">
            <text class="mine-header__avatar-letter">U</text>
          </view>
          <view class="mine-header__info">
            <text class="mine-header__name">点击登录</text>
            <text class="mine-header__login-hint">登录后享受更多服务</text>
          </view>
        </view>
      </template>
    </view>

    <!-- Asset info card -->
    <view v-if="userStore.isLoggedIn" class="mine-assets">
      <view class="mine-assets__row">
        <view class="mine-assets__item" @tap="handleAssetTap('balance')">
          <RemixIcon name="wallet-3-line" :size="44" color="#2563EB" />
          <text class="mine-assets__value">{{ memberAssets?.balance || '0.00' }}</text>
          <text class="mine-assets__label">余额</text>
        </view>
        <view class="mine-assets__item" @tap="handleAssetTap('coupon')">
          <RemixIcon name="coupon-3-line" :size="44" color="#F97316" />
          <text class="mine-assets__value">{{ memberAssets?.couponCount || 0 }}</text>
          <text class="mine-assets__label">优惠券</text>
        </view>
        <view class="mine-assets__item" @tap="handleAssetTap('points')">
          <RemixIcon name="coin-line" :size="44" color="#16A34A" />
          <text class="mine-assets__value">{{ memberAssets?.points || 0 }}</text>
          <text class="mine-assets__label">积分</text>
        </view>
        <view class="mine-assets__item" @tap="handleAssetTap('redPacket')">
          <RemixIcon name="gift-line" :size="44" color="#EF4444" />
          <text class="mine-assets__value">{{ memberAssets?.redPacketBalance || '0.00' }}</text>
          <text class="mine-assets__label">红包</text>
        </view>
      </view>
      <!-- Upgrade hint -->
      <view class="mine-assets__upgrade">
        <text class="mine-assets__upgrade-text">升级至V{{ memberLevel + 1 }} 解锁更多权益</text>
        <view class="mine-assets__progress">
          <view class="mine-assets__progress-fill" :style="{ width: upgradeProgress + '%' }" />
        </view>
      </view>
    </view>

    <!-- Order function card -->
    <view class="mine-orders">
      <view class="mine-orders__header" @tap="goToOrders()">
        <text class="mine-orders__title">我的订单</text>
        <view class="mine-orders__all">
          <text class="mine-orders__all-text">全部订单</text>
          <RemixIcon name="arrow-right-s-line" :size="28" color="#64748B" />
        </view>
      </view>
      <view class="mine-orders__grid">
        <view
          v-for="entry in orderEntries"
          :key="entry.label"
          class="mine-orders__item"
          @tap="goToOrders(entry.tab)"
        >
          <view class="mine-orders__icon-wrap">
            <RemixIcon :name="entry.icon" :size="44" :color="entry.color" />
            <view v-if="entry.badge > 0" class="mine-orders__badge">
              <text class="mine-orders__badge-text">{{ entry.badge > 99 ? '99+' : entry.badge }}</text>
            </view>
          </view>
          <text class="mine-orders__label">{{ entry.label }}</text>
        </view>
      </view>
    </view>

    <!-- Function menu row 1 -->
    <view class="mine-menu">
      <view
        v-for="item in menuItems"
        :key="item.label"
        class="mine-menu__item"
        @tap="item.action"
      >
        <view class="mine-menu__left">
          <RemixIcon :name="item.icon" :size="36" :color="item.color" />
          <text class="mine-menu__label">{{ item.label }}</text>
        </view>
        <RemixIcon name="arrow-right-s-line" :size="28" color="#CCC" />
      </view>
    </view>

    <!-- Function menu row 2 -->
    <view class="mine-menu mine-menu--extra">
      <view
        v-for="item in extraMenuItems"
        :key="item.label"
        class="mine-menu__item"
        @tap="handleComingSoon"
      >
        <view class="mine-menu__left">
          <RemixIcon :name="item.icon" :size="36" :color="item.color" />
          <text class="mine-menu__label">{{ item.label }}</text>
        </view>
        <RemixIcon name="arrow-right-s-line" :size="28" color="#CCC" />
      </view>
    </view>

    <!-- Logout button -->
    <view v-if="userStore.isLoggedIn" class="mine-logout" @tap="handleLogout">
      <text class="mine-logout__text">退出登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import { http } from '@/utils/request'

interface MemberAssetVO {
  balance: string
  couponCount: number
  points: number
  redPacketBalance: string
}

const userStore = useUserStore()
const memberAssets = ref<MemberAssetVO | null>(null)

/** Computed member level */
const memberLevel = computed(() => userStore.userInfo?.memberLevel || 0)

/** Upgrade progress (placeholder) */
const upgradeProgress = computed(() => {
  const level = memberLevel.value
  if (level === 0) return 0
  return Math.min(75, level * 25)
})

/** Order badge counts from API */
const orderBadgeCounts = ref<Record<number, number>>({})

/** Order status entries with badge counts */
const orderEntries = computed(() => [
  { icon: 'wallet-line', color: '#F97316', label: '待付款', tab: 0, badge: orderBadgeCounts.value[0] || 0 },
  { icon: 'truck-line', color: '#3B82F6', label: '待发货', tab: 1, badge: orderBadgeCounts.value[1] || 0 },
  { icon: 'package-line', color: '#8B5CF6', label: '待收货', tab: 2, badge: orderBadgeCounts.value[2] || 0 },
  { icon: 'edit-line', color: '#2563EB', label: '待评价', tab: 7, badge: orderBadgeCounts.value[7] || 0 },
  { icon: 'refund-line', color: '#EF4444', label: '退款售后', tab: -1, badge: 0 },
])

/** Main menu items row 1 */
const menuItems = [
  { icon: 'heart-line', color: '#EF4444', label: '我的收藏', action: () => navigateTo('/pages/member/collect') },
  { icon: 'history-line', color: '#64748B', label: '浏览历史', action: () => navigateTo('/pages/member/footprint') },
  { icon: 'map-pin-line', color: '#2563EB', label: '地址管理', action: () => navigateTo('/pages/order/address') },
  { icon: 'bank-card-line', color: '#F97316', label: '银行卡', action: () => navigateTo('/pages/member/bankcard') },
  { icon: 'customer-service-2-line', color: '#16A34A', label: '客服中心', action: () => handleComingSoon() },
]

/** Extra menu items row 2 */
const extraMenuItems = [
  { icon: 'vip-crown-line', color: '#CA8A04', label: '会员中心' },
  { icon: 'user-add-line', color: '#8B5CF6', label: '邀请好友' },
  { icon: 'question-line', color: '#64748B', label: '帮助中心' },
  { icon: 'chat-3-line', color: '#0891B2', label: '意见反馈' },
]

/** Fetch member assets from API */
async function fetchMemberAssets() {
  try {
    const data = await http.get<MemberAssetVO>('/v1/member/member/assets')
    memberAssets.value = data
  } catch {
    // Silently fail - assets will show defaults
  }
}

/** Fetch order badge counts */
async function fetchOrderBadgeCounts() {
  try {
    const statuses = [0, 1, 2, 7]
    const counts: Record<number, number> = {}
    const results = await Promise.allSettled(
      statuses.map(async (status) => {
        const data = await http.get<{ list: unknown[]; total: number }>('/v1/member/order/list', { status, page: 1, limit: 1 })
        counts[status] = data.total || 0
      })
    )
    // Only update counts for fulfilled requests
    statuses.forEach((status, index) => {
      if (results[index].status === 'fulfilled') {
        counts[status] = counts[status] || 0
      }
    })
    orderBadgeCounts.value = counts
  } catch {
    // Silently fail - badges will show 0
  }
}

/** Copy member ID to clipboard */
function copyMemberId() {
  const id = String(userStore.userInfo?.memberNo || userStore.userInfo?.id || '')
  if (!id) return
  uni.setClipboardData({
    data: id,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    },
  })
}

/** Navigate to a page */
function navigateTo(url: string) {
  uni.navigateTo({ url })
}

/** Navigate to orders tab page */
function goToOrders(tab?: number) {
  if (tab !== undefined) {
    uni.setStorageSync('order_list_tab', tab)
  } else {
    uni.removeStorageSync('order_list_tab')
  }
  uni.switchTab({ url: '/pages/order/index' })
}

/** Handle asset item tap */
function handleAssetTap(_type: string) {
  uni.showToast({ title: '敬请期待', icon: 'none' })
}

/** Handle settings tap */
function handleSettings() {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}

/** Handle coming soon */
function handleComingSoon() {
  uni.showToast({ title: '敬请期待', icon: 'none' })
}

/** Logout */
async function handleLogout() {
  const { confirm } = await uni.showModal({ title: '提示', content: '确定退出登录？' })
  if (confirm) {
    await userStore.logout()
    memberAssets.value = null
    orderBadgeCounts.value = {}
    uni.showToast({ title: '已退出', icon: 'success' })
  }
}

/** Refresh user info and assets on show */
onShow(() => {
  if (userStore.isLoggedIn) {
    if (!userStore.userInfo) {
      userStore.fetchUserInfo()
    }
    fetchMemberAssets()
    fetchOrderBadgeCounts()
  }
})
</script>

<style lang="scss" scoped>
.page-mine {
  min-height: 100vh;
  background: #F9FAFB;
  padding-bottom: 120rpx;
}

/* Header */
.mine-header {
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  padding: 64rpx 32rpx 48rpx;
  color: #fff;
  position: relative;
}

.mine-header__settings {
  position: absolute;
  top: 64rpx;
  right: 32rpx;
  padding: 8rpx;
  z-index: 2;
}

.mine-header__user,
.mine-header__login {
  display: flex;
  align-items: center;
}

.mine-header__avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.mine-header__avatar-letter {
  font-size: 48rpx;
  color: #fff;
  font-weight: 700;
}

.mine-header__avatar-img {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
}

.mine-header__info {
  margin-left: 24rpx;
  flex: 1;
}

.mine-header__name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.mine-header__name {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.mine-header__level-badge {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  padding: 4rpx 16rpx;
}

.mine-header__level-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
}

.mine-header__id-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
}

.mine-header__id {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
}

.mine-header__copy {
  display: flex;
  align-items: center;
}

.mine-header__login-hint {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

/* Assets card */
.mine-assets {
  background: #fff;
  margin: -24rpx 24rpx 0;
  border-radius: 24rpx;
  position: relative;
  z-index: 10;
  padding: 28rpx 16rpx 24rpx;
}

.mine-assets__row {
  display: flex;
}

.mine-assets__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.mine-assets__value {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
  margin-top: 8rpx;
}

.mine-assets__label {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.mine-assets__upgrade {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F3F4F6;
}

.mine-assets__upgrade-text {
  font-size: 22rpx;
  color: #64748B;
  display: block;
  margin-bottom: 12rpx;
}

.mine-assets__progress {
  height: 8rpx;
  background: #F3F4F6;
  border-radius: 4rpx;
  overflow: hidden;
}

.mine-assets__progress-fill {
  height: 100%;
  background: #2563EB;
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

/* Orders card */
.mine-orders {
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 24rpx;
  padding: 24rpx;
}

.mine-orders__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.mine-orders__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.mine-orders__all {
  display: flex;
  align-items: center;
}

.mine-orders__all-text {
  font-size: 24rpx;
  color: #64748B;
}

.mine-orders__grid {
  display: flex;
}

.mine-orders__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.mine-orders__icon-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
}

.mine-orders__badge {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  background: #EF4444;
  border-radius: 16rpx;
  min-width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.mine-orders__badge-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: 600;
}

.mine-orders__label {
  font-size: 24rpx;
  color: #334155;
  margin-top: 4rpx;
}

/* Menu */
.mine-menu {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.mine-menu--extra {
  margin-top: 20rpx;
}

.mine-menu__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.mine-menu__item:last-child {
  border-bottom: none;
}

.mine-menu__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.mine-menu__label {
  font-size: 28rpx;
  color: #1E293B;
}

/* Logout */
.mine-logout {
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 24rpx;
  padding: 28rpx;
  text-align: center;
}

.mine-logout__text {
  font-size: 28rpx;
  color: #2563EB;
}
</style>
