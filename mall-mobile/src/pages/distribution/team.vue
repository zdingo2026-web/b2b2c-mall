<template>
  <view class="page-team">
    <!-- Team stats -->
    <view class="stats-card">
      <view class="stats-card__item">
        <text class="stats-card__value">{{ center?.teamCount || 0 }}</text>
        <text class="stats-card__label">团队总人数</text>
      </view>
      <view class="stats-card__divider" />
      <view class="stats-card__item">
        <text class="stats-card__value">{{ levelCounts[1] || 0 }}</text>
        <text class="stats-card__label">一级成员</text>
      </view>
      <view class="stats-card__divider" />
      <view class="stats-card__item">
        <text class="stats-card__value">{{ levelCounts[2] || 0 }}</text>
        <text class="stats-card__label">二级成员</text>
      </view>
      <view class="stats-card__divider" />
      <view class="stats-card__item">
        <text class="stats-card__value">{{ levelCounts[3] || 0 }}</text>
        <text class="stats-card__label">三级成员</text>
      </view>
    </view>

    <!-- Level tabs -->
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

    <!-- Team list -->
    <scroll-view
      scroll-y
      class="list-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && list.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-state__text">暂无团队成员</text>
      </view>

      <view v-else class="team-list">
        <view v-for="item in list" :key="item.id" class="team-item">
          <image v-if="item.avatar" class="team-item__avatar" :src="item.avatar" mode="aspectFill" />
          <view v-else class="team-item__avatar team-item__avatar--placeholder">
            <text class="team-item__avatar-text">{{ (item.nickname || '用户').charAt(0) }}</text>
          </view>
          <view class="team-item__info">
            <view class="team-item__name-row">
              <text class="team-item__nickname">{{ item.nickname || '匿名用户' }}</text>
              <view class="team-item__level-tag">
                <text class="team-item__level-text">{{ levelText(item.level) }}</text>
              </view>
            </view>
            <text class="team-item__meta">订单{{ item.orderCount }}单 | 佣金&yen;{{ item.commissionAmount }}</text>
          </view>
          <text class="team-item__time">{{ item.joinTime }}</text>
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
import { ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getDistributionCenter,
  getTeamList,
  type DistributionCenterVO,
  type TeamMemberVO,
} from '@/api/distribution'

const center = ref<DistributionCenterVO | null>(null)
const activeTab = ref(0) // 0-all, 1/2/3-level
const list = ref<TeamMemberVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const levelCounts = reactive<Record<number, number>>({ 1: 0, 2: 0, 3: 0 })

const tabs = [
  { value: 0, label: '全部' },
  { value: 1, label: '一级' },
  { value: 2, label: '二级' },
  { value: 3, label: '三级' },
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
    if (activeTab.value > 0) params.level = activeTab.value
    const data = await getTeamList(params)
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

function levelText(level: number): string {
  const map: Record<number, string> = { 1: '一级', 2: '二级', 3: '三级' }
  return map[level] || '未知'
}
</script>

<style lang="scss" scoped>
.page-team {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Stats card */
.stats-card {
  background: #FFFFFF;
  padding: 32rpx;
  display: flex;
  align-items: center;
}

.stats-card__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-card__value {
  font-size: 36rpx;
  color: #1E293B;
  font-weight: 700;
}

.stats-card__label {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.stats-card__divider {
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

/* Team list */
.team-list {
  padding: 0 24rpx;
}

.team-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.team-item:last-child {
  border-bottom: none;
}

.team-item__avatar {
  width: 80rpx;
  height: 80rpx;
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
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.team-item__level-tag {
  background: #FFF7ED;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
  flex-shrink: 0;
}

.team-item__level-text {
  font-size: 20rpx;
  color: #F97316;
  font-weight: 500;
}

.team-item__meta {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 6rpx;
  display: block;
}

.team-item__time {
  font-size: 22rpx;
  color: #94A3B8;
  flex-shrink: 0;
  margin-left: 12rpx;
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
