<template>
  <view class="page-distribution">
    <!-- Not a distributor yet -->
    <template v-if="!center?.isDistributor">
      <view class="apply-card">
        <view class="apply-card__header">
          <RemixIcon name="user-star-line" :size="64" color="#F97316" />
          <text class="apply-card__title">成为分销员</text>
          <text class="apply-card__desc">推广商品赚取佣金，轻松实现副业收入</text>
        </view>
        <view class="apply-card__form">
          <view class="form-item">
            <text class="form-item__label">真实姓名</text>
            <input
              v-model="applyForm.realName"
              class="form-item__input"
              placeholder="请输入真实姓名"
            />
          </view>
          <view class="form-item">
            <text class="form-item__label">手机号码</text>
            <input
              v-model="applyForm.phone"
              class="form-item__input"
              placeholder="请输入手机号码"
              type="number"
              maxlength="11"
            />
          </view>
        </view>
        <view class="apply-card__btn" @tap="handleApply">
          <text class="apply-card__btn-text">申请成为分销员</text>
        </view>
      </view>
    </template>

    <!-- Distributor center -->
    <template v-else>
      <!-- Commission overview -->
      <view class="overview-card">
        <view class="overview-card__top">
          <view class="overview-card__amount">
            <text class="overview-card__label">可提现佣金(元)</text>
            <text class="overview-card__value">{{ center?.availableCommission || '0.00' }}</text>
          </view>
          <view class="overview-card__withdraw-btn" @tap="showWithdrawPopup = true">
            <text class="overview-card__withdraw-text">提现</text>
          </view>
        </view>
        <view class="overview-card__row">
          <view class="overview-card__stat">
            <text class="overview-card__stat-value">{{ center?.totalCommission || '0.00' }}</text>
            <text class="overview-card__stat-label">累计佣金</text>
          </view>
          <view class="overview-card__stat">
            <text class="overview-card__stat-value">{{ center?.frozenCommission || '0.00' }}</text>
            <text class="overview-card__stat-label">冻结中</text>
          </view>
          <view class="overview-card__stat">
            <text class="overview-card__stat-value">{{ center?.withdrawnCommission || '0.00' }}</text>
            <text class="overview-card__stat-label">已提现</text>
          </view>
        </view>
      </view>

      <!-- Quick stats -->
      <view class="quick-stats">
        <view class="quick-stats__item" @tap="activeTab = 'commission'">
          <RemixIcon name="money-cny-circle-line" :size="40" color="#F97316" />
          <text class="quick-stats__value">{{ stat?.todayCommission || '0.00' }}</text>
          <text class="quick-stats__label">今日佣金</text>
        </view>
        <view class="quick-stats__item" @tap="activeTab = 'orders'">
          <RemixIcon name="file-list-3-line" :size="40" color="#2563EB" />
          <text class="quick-stats__value">{{ stat?.todayOrders || 0 }}</text>
          <text class="quick-stats__label">今日订单</text>
        </view>
        <view class="quick-stats__item" @tap="activeTab = 'team'">
          <RemixIcon name="team-line" :size="40" color="#7C3AED" />
          <text class="quick-stats__value">{{ center?.teamCount || 0 }}</text>
          <text class="quick-stats__label">我的团队</text>
        </view>
        <view class="quick-stats__item" @tap="activeTab = 'products'">
          <RemixIcon name="shopping-bag-line" :size="40" color="#16A34A" />
          <text class="quick-stats__value">{{ stat?.weekOrders || 0 }}</text>
          <text class="quick-stats__label">本周订单</text>
        </view>
      </view>

      <!-- Tab bar -->
      <view class="tab-bar">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-bar__item"
          :class="{ 'tab-bar__item--active': activeTab === tab.key }"
          @tap="activeTab = tab.key"
        >
          <text class="tab-bar__text">{{ tab.label }}</text>
        </view>
      </view>

      <!-- Tab content -->
      <scroll-view scroll-y class="content-scroll">
        <!-- Commission tab -->
        <template v-if="activeTab === 'commission'">
          <view v-if="commissionList.length === 0 && !loadingCommission" class="empty-state">
            <text class="empty-state__text">暂无佣金记录</text>
          </view>
          <view v-else class="commission-list">
            <view v-for="item in commissionList" :key="item.id" class="commission-item">
              <view class="commission-item__left">
                <text class="commission-item__desc">{{ item.type === 1 ? '直推佣金' : '间推佣金' }}</text>
                <text class="commission-item__nickname">来自 {{ item.memberNickname }}</text>
                <text class="commission-item__time">{{ item.createTime }}</text>
              </view>
              <view class="commission-item__right">
                <text class="commission-item__amount">+{{ item.amount }}</text>
                <text class="commission-item__status">{{ commissionStatusText(item.status) }}</text>
              </view>
            </view>
          </view>
          <view v-if="loadingCommission" class="load-more">
            <text class="load-more__text">加载中...</text>
          </view>
        </template>

        <!-- Orders tab -->
        <template v-if="activeTab === 'orders'">
          <view v-if="orderList.length === 0 && !loadingOrders" class="empty-state">
            <text class="empty-state__text">暂无分销订单</text>
          </view>
          <view v-else class="order-list">
            <view v-for="item in orderList" :key="item.id" class="order-item">
              <image class="order-item__img" :src="item.mainImage" mode="aspectFill" />
              <view class="order-item__info">
                <text class="order-item__name">{{ item.productName }}</text>
                <text class="order-item__no">订单号: {{ item.orderNo }}</text>
                <view class="order-item__bottom">
                  <text class="order-item__amount">佣金 &yen;{{ item.commissionAmount }}</text>
                  <text class="order-item__time">{{ item.createTime }}</text>
                </view>
              </view>
            </view>
          </view>
          <view v-if="loadingOrders" class="load-more">
            <text class="load-more__text">加载中...</text>
          </view>
        </template>

        <!-- Products tab -->
        <template v-if="activeTab === 'products'">
          <view v-if="distProducts.length === 0 && !loadingProducts" class="empty-state">
            <text class="empty-state__text">暂无分销商品</text>
          </view>
          <view v-else class="dist-product-list">
            <view v-for="item in distProducts" :key="item.id" class="dist-product-item">
              <image class="dist-product-item__img" :src="item.mainImage" mode="aspectFill" />
              <view class="dist-product-item__info">
                <text class="dist-product-item__name">{{ item.productName }}</text>
                <text class="dist-product-item__price">售价 &yen;{{ item.price }}</text>
                <view class="dist-product-item__commission">
                  <text class="dist-product-item__commission-text">佣金 {{ item.commissionRate }}% 约&yen;{{ item.commissionAmount }}</text>
                </view>
              </view>
              <view class="dist-product-item__share" @tap="handleShare(item)">
                <RemixIcon name="share-forward-line" :size="32" color="#F97316" />
                <text class="dist-product-item__share-text">分享</text>
              </view>
            </view>
          </view>
          <view v-if="loadingProducts" class="load-more">
            <text class="load-more__text">加载中...</text>
          </view>
        </template>

        <!-- Team tab -->
        <template v-if="activeTab === 'team'">
          <view class="team-filter">
            <view
              class="team-filter__item"
              :class="{ 'team-filter__item--active': teamLevel === 0 }"
              @tap="teamLevel = 0; fetchTeam()"
            >
              <text class="team-filter__text">全部</text>
            </view>
            <view
              class="team-filter__item"
              :class="{ 'team-filter__item--active': teamLevel === 1 }"
              @tap="teamLevel = 1; fetchTeam()"
            >
              <text class="team-filter__text">直推</text>
            </view>
            <view
              class="team-filter__item"
              :class="{ 'team-filter__item--active': teamLevel === 2 }"
              @tap="teamLevel = 2; fetchTeam()"
            >
              <text class="team-filter__text">间推</text>
            </view>
          </view>
          <view v-if="teamList.length === 0 && !loadingTeam" class="empty-state">
            <text class="empty-state__text">暂无团队成员</text>
          </view>
          <view v-else class="team-list">
            <view v-for="item in teamList" :key="item.id" class="team-item">
              <image v-if="item.avatar" class="team-item__avatar" :src="item.avatar" mode="aspectFill" />
              <view v-else class="team-item__avatar team-item__avatar--placeholder">
                <text class="team-item__avatar-text">{{ (item.nickname || '用户').charAt(0) }}</text>
              </view>
              <view class="team-item__info">
                <view class="team-item__name-row">
                  <text class="team-item__nickname">{{ item.nickname || '匿名用户' }}</text>
                  <view class="team-item__level-tag">
                    <text class="team-item__level-text">{{ item.level === 1 ? '直推' : '间推' }}</text>
                  </view>
                </view>
                <text class="team-item__meta">订单{{ item.orderCount }}单 | 佣金&yen;{{ item.commissionAmount }}</text>
              </view>
              <text class="team-item__time">{{ item.joinTime }}</text>
            </view>
          </view>
          <view v-if="loadingTeam" class="load-more">
            <text class="load-more__text">加载中...</text>
          </view>
        </template>

        <!-- Withdraw records tab -->
        <template v-if="activeTab === 'withdraw'">
          <view v-if="withdrawList.length === 0 && !loadingWithdraw" class="empty-state">
            <text class="empty-state__text">暂无提现记录</text>
          </view>
          <view v-else class="withdraw-list">
            <view v-for="item in withdrawList" :key="item.id" class="withdraw-item">
              <view class="withdraw-item__left">
                <text class="withdraw-item__amount">&yen;{{ item.amount }}</text>
                <text class="withdraw-item__time">{{ item.applyTime }}</text>
              </view>
              <text class="withdraw-item__status" :class="`withdraw-item__status--${item.status}`">
                {{ withdrawStatusText(item.status) }}
              </text>
            </view>
          </view>
          <view v-if="loadingWithdraw" class="load-more">
            <text class="load-more__text">加载中...</text>
          </view>
        </template>
      </scroll-view>
    </template>

    <!-- Withdraw popup -->
    <view v-if="showWithdrawPopup" class="popup-overlay" @tap="showWithdrawPopup = false">
      <view class="popup-panel" @tap.stop>
        <view class="popup-panel__header">
          <text class="popup-panel__title">申请提现</text>
          <view class="popup-panel__close" @tap="showWithdrawPopup = false">
            <RemixIcon name="close-line" :size="40" color="#94A3B8" />
          </view>
        </view>
        <view class="popup-panel__body">
          <view class="form-item">
            <text class="form-item__label">提现金额</text>
            <input
              v-model="withdrawForm.amount"
              class="form-item__input"
              placeholder="请输入提现金额"
              type="digit"
            />
          </view>
          <text class="popup-panel__hint">可提现金额: &yen;{{ center?.availableCommission || '0.00' }}</text>
        </view>
        <view class="popup-panel__footer">
          <view class="popup-panel__btn" @tap="handleWithdraw">
            <text class="popup-panel__btn-text">确认提现</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  applyDistributor,
  getDistributionCenter,
  getDistributionProducts,
  getCommissionList,
  getDistributionOrders,
  getDistributionStat,
  applyWithdraw,
  getWithdrawList,
  getTeamList,
  type DistributionCenterVO,
  type DistributionStatVO,
  type CommissionVO,
  type DistributionOrderVO,
  type DistributionProductVO,
  type WithdrawVO,
  type TeamMemberVO,
} from '@/api/distribution'

const userStore = useUserStore()

const center = ref<DistributionCenterVO | null>(null)
const stat = ref<DistributionStatVO | null>(null)

// Apply form
const applyForm = ref({ realName: '', phone: '' })

// Tabs
const tabs = [
  { key: 'commission', label: '佣金明细' },
  { key: 'orders', label: '分销订单' },
  { key: 'products', label: '分销商品' },
  { key: 'team', label: '我的团队' },
  { key: 'withdraw', label: '提现记录' },
]
const activeTab = ref('commission')

// Commission
const commissionList = ref<CommissionVO[]>([])
const loadingCommission = ref(false)

// Orders
const orderList = ref<DistributionOrderVO[]>([])
const loadingOrders = ref(false)

// Products
const distProducts = ref<DistributionProductVO[]>([])
const loadingProducts = ref(false)

// Team
const teamList = ref<TeamMemberVO[]>([])
const loadingTeam = ref(false)
const teamLevel = ref(0)

// Withdraw
const withdrawList = ref<WithdrawVO[]>([])
const loadingWithdraw = ref(false)
const showWithdrawPopup = ref(false)
const withdrawForm = ref({ amount: '' })

onShow(() => {
  if (userStore.isLoggedIn) {
    fetchCenter()
  }
})

watch(activeTab, (tab) => {
  switch (tab) {
    case 'commission':
      if (commissionList.value.length === 0) fetchCommission()
      break
    case 'orders':
      if (orderList.value.length === 0) fetchOrders()
      break
    case 'products':
      if (distProducts.value.length === 0) fetchDistProducts()
      break
    case 'team':
      if (teamList.value.length === 0) fetchTeam()
      break
    case 'withdraw':
      if (withdrawList.value.length === 0) fetchWithdrawList()
      break
  }
})

async function fetchCenter() {
  try {
    const data = await getDistributionCenter()
    center.value = data
    if (data?.isDistributor) {
      fetchStat()
      fetchCommission()
    }
  } catch {
    // handled
  }
}

async function fetchStat() {
  try {
    stat.value = await getDistributionStat()
  } catch {
    // handled
  }
}

async function fetchCommission() {
  loadingCommission.value = true
  try {
    const data = await getCommissionList({ page: 1, limit: 20 })
    commissionList.value = data.list || []
  } catch {
    // handled
  } finally {
    loadingCommission.value = false
  }
}

async function fetchOrders() {
  loadingOrders.value = true
  try {
    const data = await getDistributionOrders({ page: 1, limit: 20 })
    orderList.value = data.list || []
  } catch {
    // handled
  } finally {
    loadingOrders.value = false
  }
}

async function fetchDistProducts() {
  loadingProducts.value = true
  try {
    const data = await getDistributionProducts({ page: 1, limit: 20 })
    distProducts.value = data.list || []
  } catch {
    // handled
  } finally {
    loadingProducts.value = false
  }
}

async function fetchTeam() {
  loadingTeam.value = true
  try {
    const params: any = { page: 1, limit: 20 }
    if (teamLevel.value > 0) params.level = teamLevel.value
    const data = await getTeamList(params)
    teamList.value = data.list || []
  } catch {
    // handled
  } finally {
    loadingTeam.value = false
  }
}

async function fetchWithdrawList() {
  loadingWithdraw.value = true
  try {
    const data = await getWithdrawList({ page: 1, limit: 20 })
    withdrawList.value = data.list || []
  } catch {
    // handled
  } finally {
    loadingWithdraw.value = false
  }
}

function commissionStatusText(status: number): string {
  const map: Record<number, string> = { 0: '待结算', 1: '已结算', 2: '已冻结' }
  return map[status] || '未知'
}

function withdrawStatusText(status: number): string {
  const map: Record<number, string> = { 0: '审核中', 1: '已通过', 2: '已拒绝', 3: '已打款' }
  return map[status] || '未知'
}

async function handleApply() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!applyForm.value.realName.trim()) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' })
    return
  }
  if (!applyForm.value.phone.trim() || applyForm.value.phone.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '提交中...' })
    await applyDistributor(applyForm.value)
    uni.hideLoading()
    uni.showToast({ title: '申请已提交', icon: 'success' })
    fetchCenter()
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '申请失败', icon: 'none' })
  }
}

function handleShare(item: DistributionProductVO) {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

async function handleWithdraw() {
  const amount = parseFloat(withdrawForm.value.amount)
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入提现金额', icon: 'none' })
    return
  }
  const available = parseFloat(center.value?.availableCommission || '0')
  if (amount > available) {
    uni.showToast({ title: '超出可提现金额', icon: 'none' })
    return
  }
  try {
    uni.showLoading({ title: '提交中...' })
    await applyWithdraw({ amount, bankCardId: 0 })
    uni.hideLoading()
    uni.showToast({ title: '提现申请已提交', icon: 'success' })
    showWithdrawPopup.value = false
    withdrawForm.value.amount = ''
    fetchCenter()
    if (activeTab.value === 'withdraw') fetchWithdrawList()
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '提现失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.page-distribution {
  min-height: 100vh;
  background: #F9FAFB;
}

/* Apply card */
.apply-card {
  margin: 32rpx 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
}

.apply-card__header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

.apply-card__title {
  font-size: 36rpx;
  color: #1E293B;
  font-weight: 700;
  margin-top: 16rpx;
}

.apply-card__desc {
  font-size: 26rpx;
  color: #64748B;
  margin-top: 8rpx;
  text-align: center;
}

.apply-card__form {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item__label {
  font-size: 26rpx;
  color: #64748B;
  display: block;
  margin-bottom: 8rpx;
}

.form-item__input {
  height: 80rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1E293B;
}

.apply-card__btn {
  background: linear-gradient(135deg, #F97316, #EA580C);
  border-radius: 44rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.apply-card__btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* Overview card */
.overview-card {
  background: linear-gradient(135deg, #F97316, #EA580C);
  padding: 32rpx;
  color: #FFFFFF;
}

.overview-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.overview-card__label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.overview-card__value {
  font-size: 56rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-top: 8rpx;
}

.overview-card__withdraw-btn {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 28rpx;
  padding: 12rpx 32rpx;
}

.overview-card__withdraw-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.overview-card__row {
  display: flex;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.overview-card__stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-card__stat-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #FFFFFF;
}

.overview-card__stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4rpx;
}

/* Quick stats */
.quick-stats {
  display: flex;
  background: #FFFFFF;
  padding: 24rpx 16rpx;
  margin-top: 16rpx;
}

.quick-stats__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.quick-stats__value {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 600;
}

.quick-stats__label {
  font-size: 22rpx;
  color: #64748B;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 0 12rpx;
  overflow-x: auto;
}

.tab-bar__item {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 20rpx;
  position: relative;
}

.tab-bar__item--active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background: #F97316;
  border-radius: 3rpx;
}

.tab-bar__text {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 500;
  white-space: nowrap;
}

.tab-bar__item--active .tab-bar__text {
  color: #F97316;
  font-weight: 600;
}

/* Content scroll */
.content-scroll {
  height: 600rpx;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.empty-state__text {
  font-size: 28rpx;
  color: #94A3B8;
}

/* Commission list */
.commission-list {
  padding: 0 24rpx;
}

.commission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.commission-item:last-child {
  border-bottom: none;
}

.commission-item__left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  flex: 1;
  min-width: 0;
}

.commission-item__desc {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.commission-item__nickname {
  font-size: 24rpx;
  color: #64748B;
}

.commission-item__time {
  font-size: 22rpx;
  color: #94A3B8;
}

.commission-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-left: 20rpx;
}

.commission-item__amount {
  font-size: 32rpx;
  color: #16A34A;
  font-weight: 600;
}

.commission-item__status {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

/* Order list */
.order-list {
  padding: 0 24rpx;
}

.order-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item__img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.order-item__info {
  flex: 1;
  margin-left: 16rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.order-item__name {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-item__no {
  font-size: 22rpx;
  color: #94A3B8;
}

.order-item__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-item__amount {
  font-size: 26rpx;
  color: #F97316;
  font-weight: 600;
}

.order-item__time {
  font-size: 22rpx;
  color: #94A3B8;
}

/* Distribution product list */
.dist-product-list {
  padding: 0 24rpx;
}

.dist-product-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.dist-product-item:last-child {
  border-bottom: none;
}

.dist-product-item__img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dist-product-item__info {
  flex: 1;
  margin-left: 16rpx;
  min-width: 0;
}

.dist-product-item__name {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.dist-product-item__price {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 6rpx;
  display: block;
}

.dist-product-item__commission {
  margin-top: 6rpx;
}

.dist-product-item__commission-text {
  font-size: 22rpx;
  color: #F97316;
  font-weight: 500;
}

.dist-product-item__share {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.dist-product-item__share-text {
  font-size: 20rpx;
  color: #F97316;
}

/* Team filter */
.team-filter {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx;
}

.team-filter__item {
  padding: 8rpx 24rpx;
  background: #F3F4F6;
  border-radius: 20rpx;
}

.team-filter__item--active {
  background: #FFF7ED;
  border: 1rpx solid #F97316;
}

.team-filter__text {
  font-size: 24rpx;
  color: #64748B;
}

.team-filter__item--active .team-filter__text {
  color: #F97316;
  font-weight: 500;
}

/* Team list */
.team-list {
  padding: 0 24rpx;
}

.team-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.team-item:last-child {
  border-bottom: none;
}

.team-item__avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.team-item__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #DBEAFE;
}

.team-item__avatar-text {
  font-size: 28rpx;
  color: #2563EB;
  font-weight: 600;
}

.team-item__info {
  flex: 1;
  margin-left: 16rpx;
  min-width: 0;
}

.team-item__name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.team-item__nickname {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
}

.team-item__level-tag {
  background: #FFF7ED;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
}

.team-item__level-text {
  font-size: 20rpx;
  color: #F97316;
  font-weight: 500;
}

.team-item__meta {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.team-item__time {
  font-size: 22rpx;
  color: #94A3B8;
  flex-shrink: 0;
  margin-left: 12rpx;
}

/* Withdraw list */
.withdraw-list {
  padding: 0 24rpx;
}

.withdraw-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.withdraw-item:last-child {
  border-bottom: none;
}

.withdraw-item__left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.withdraw-item__amount {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 600;
}

.withdraw-item__time {
  font-size: 22rpx;
  color: #94A3B8;
}

.withdraw-item__status {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.withdraw-item__status--1,
.withdraw-item__status--3 {
  color: #16A34A;
}

.withdraw-item__status--2 {
  color: #EF4444;
}

/* Load more */
.load-more {
  padding: 24rpx 0;
  text-align: center;
}

.load-more__text {
  font-size: 24rpx;
  color: #94A3B8;
}

/* Popup overlay */
.popup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.popup-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.popup-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.popup-panel__title {
  font-size: 32rpx;
  color: #1E293B;
  font-weight: 600;
}

.popup-panel__close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popup-panel__body {
  margin-bottom: 32rpx;
}

.popup-panel__hint {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 12rpx;
  display: block;
}

.popup-panel__footer {
  margin-top: 24rpx;
}

.popup-panel__btn {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F97316, #EA580C);
  border-radius: 44rpx;
}

.popup-panel__btn-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
