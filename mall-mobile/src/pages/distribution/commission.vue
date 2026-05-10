<template>
  <view class="page-commission">
    <!-- Summary -->
    <view class="summary-card">
      <view class="summary-card__item">
        <text class="summary-card__value">{{ center?.totalCommission || '0.00' }}</text>
        <text class="summary-card__label">累计佣金</text>
      </view>
      <view class="summary-card__divider" />
      <view class="summary-card__item">
        <text class="summary-card__value summary-card__value--accent">{{ center?.availableCommission || '0.00' }}</text>
        <text class="summary-card__label">可提现</text>
      </view>
    </view>

    <!-- Tab filter -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === tab.value }"
        @tap="switchTab(tab.value)"
      >
        <text class="tab-bar__text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- Commission list -->
    <scroll-view
      scroll-y
      class="list-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-state__text">暂无佣金记录</text>
      </view>

      <view v-else class="commission-list">
        <view v-for="item in list" :key="item.id" class="commission-item">
          <view class="commission-item__left">
            <view class="commission-item__type-tag">
              <text class="commission-item__type-text">{{ item.type === 1 ? '直推' : '间推' }}</text>
            </view>
            <view class="commission-item__info">
              <text class="commission-item__desc">来自 {{ item.memberNickname }}</text>
              <text class="commission-item__time">{{ item.createTime }}</text>
            </view>
          </view>
          <view class="commission-item__right">
            <text class="commission-item__amount">+{{ item.amount }}</text>
            <text
              class="commission-item__status"
              :class="`commission-item__status--${item.status}`"
            >
              {{ statusText(item.status) }}
            </text>
          </view>
        </view>
      </view>

      <view v-if="loading && list.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMore && list.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getDistributionCenter,
  getCommissionList,
  type DistributionCenterVO,
  type CommissionVO,
} from '@/api/distribution'

const center = ref<DistributionCenterVO | null>(null)
const activeTab = ref(0) // 0-all, 1-frozen, 2-available, 3-withdrawn
const list = ref<CommissionVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)

const tabs = [
  { value: 0, label: '全部' },
  { value: 1, label: '冻结' },
  { value: 2, label: '可提现' },
  { value: 3, label: '已提现' },
]

onShow(() => {
  fetchCenter()
  fetchList()
})

async function fetchCenter() {
  try {
    center.value = await getDistributionCenter()
  } catch {
    // handled
  }
}

function switchTab(tab: number) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  page.value = 1
  list.value = []
  noMore.value = false
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: page.value, limit: 10 }
    if (activeTab.value === 1) params.status = 2
    if (activeTab.value === 2) params.status = 1
    if (activeTab.value === 3) params.status = 3
    const data = await getCommissionList(params)
    const items = data.list || []
    if (page.value === 1) {
      list.value = items
    } else {
      list.value.push(...items)
    }
    noMore.value = items.length < 10
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchList()
}

function statusText(status: number): string {
  const map: Record<number, string> = { 0: '待结算', 1: '已结算', 2: '已冻结', 3: '已提现' }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
.page-commission {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Summary card */
.summary-card {
  background: #FFFFFF;
  padding: 32rpx;
  display: flex;
  align-items: center;
}

.summary-card__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-card__value {
  font-size: 36rpx;
  color: #1E293B;
  font-weight: 700;
}

.summary-card__value--accent {
  color: #F97316;
}

.summary-card__label {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.summary-card__divider {
  width: 2rpx;
  height: 60rpx;
  background: #E5E7EB;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 0 12rpx;
}

.tab-bar__item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 0;
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
}

.tab-bar__item--active .tab-bar__text {
  color: #F97316;
  font-weight: 600;
}

/* List scroll */
.list-scroll {
  flex: 1;
  height: 0;
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

/* Commission list */
.commission-list {
  padding: 0 24rpx;
}

.commission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.commission-item:last-child {
  border-bottom: none;
}

.commission-item__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.commission-item__type-tag {
  background: #FFF7ED;
  border-radius: 8rpx;
  padding: 6rpx 14rpx;
  flex-shrink: 0;
}

.commission-item__type-text {
  font-size: 22rpx;
  color: #F97316;
  font-weight: 500;
}

.commission-item__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.commission-item__desc {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.commission-item__time {
  font-size: 22rpx;
  color: #94A3B8;
}

.commission-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.commission-item__amount {
  font-size: 30rpx;
  color: #16A34A;
  font-weight: 600;
}

.commission-item__status {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.commission-item__status--1,
.commission-item__status--3 {
  color: #16A34A;
}

.commission-item__status--2 {
  color: #F97316;
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
</style>
