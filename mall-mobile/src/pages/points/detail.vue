<template>
  <view class="page-points-detail">
    <!-- Tab filter -->
    <view class="tab-bar">
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 0 }"
        @tap="switchTab(0)"
      >
        <text class="tab-bar__text">全部</text>
      </view>
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 1 }"
        @tap="switchTab(1)"
      >
        <text class="tab-bar__text">收入</text>
      </view>
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 2 }"
        @tap="switchTab(2)"
      >
        <text class="tab-bar__text">支出</text>
      </view>
    </view>

    <!-- Timeline list -->
    <scroll-view
      scroll-y
      class="detail-scroll"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && details.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="details.length === 0" class="empty-state">
        <text class="empty-state__text">暂无积分记录</text>
      </view>

      <view v-else class="timeline">
        <view v-for="(item, idx) in details" :key="item.id" class="timeline-item">
          <!-- Timeline connector -->
          <view class="timeline-item__connector">
            <view
              class="timeline-item__dot"
              :class="{ 'timeline-item__dot--plus': item.type === 1 }"
            />
            <view v-if="idx < details.length - 1" class="timeline-item__line" />
          </view>
          <!-- Content -->
          <view class="timeline-item__content">
            <view class="timeline-item__header">
              <text class="timeline-item__desc">{{ item.description || item.source }}</text>
              <text
                class="timeline-item__points"
                :class="{ 'timeline-item__points--plus': item.type === 1 }"
              >
                {{ item.type === 1 ? '+' : '-' }}{{ item.points }}
              </text>
            </view>
            <text class="timeline-item__time">{{ item.createTime }}</text>
            <view class="timeline-item__tag">
              <text class="timeline-item__tag-text">{{ sourceText(item.source) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading && details.length > 0" class="load-more">
        <text class="load-more__text">加载中...</text>
      </view>
      <view v-if="noMore && details.length > 0" class="load-more">
        <text class="load-more__text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getPointsDetails,
  type PointsDetailVO,
} from '@/api/points'

const activeTab = ref(0) // 0-all, 1-income, 2-expense
const details = ref<PointsDetailVO[]>([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)

onShow(() => {
  fetchDetails()
})

function switchTab(tab: number) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  page.value = 1
  details.value = []
  noMore.value = false
  fetchDetails()
}

async function fetchDetails() {
  loading.value = true
  try {
    const params: any = { page: page.value, limit: 10 }
    if (activeTab.value === 1) params.type = 1
    if (activeTab.value === 2) params.type = 2
    const data = await getPointsDetails(params)
    const list = data.list || []
    if (page.value === 1) {
      details.value = list
    } else {
      details.value.push(...list)
    }
    noMore.value = list.length < 10
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchDetails()
}

function sourceText(source: string): string {
  const map: Record<string, string> = {
    checkin: '签到',
    order: '购物',
    exchange: '兑换',
    refund: '退款',
    invite: '邀请',
    activity: '活动',
  }
  return map[source] || source
}
</script>

<style lang="scss" scoped>
.page-points-detail {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Tab bar */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  padding: 0 24rpx;
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
  width: 48rpx;
  height: 6rpx;
  background: #16A34A;
  border-radius: 3rpx;
}

.tab-bar__text {
  font-size: 28rpx;
  color: #64748B;
  font-weight: 500;
}

.tab-bar__item--active .tab-bar__text {
  color: #16A34A;
  font-weight: 600;
}

/* Detail scroll */
.detail-scroll {
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

/* Timeline */
.timeline {
  padding: 24rpx;
}

.timeline-item {
  display: flex;
  min-height: 120rpx;
}

.timeline-item__connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40rpx;
  flex-shrink: 0;
}

.timeline-item__dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #EF4444;
  flex-shrink: 0;
  margin-top: 8rpx;
}

.timeline-item__dot--plus {
  background: #16A34A;
}

.timeline-item__line {
  flex: 1;
  width: 2rpx;
  background: #E5E7EB;
  margin-top: 8rpx;
}

.timeline-item__content {
  flex: 1;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-left: 16rpx;
  margin-bottom: 16rpx;
}

.timeline-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.timeline-item__desc {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-item__points {
  font-size: 32rpx;
  color: #EF4444;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.timeline-item__points--plus {
  color: #16A34A;
}

.timeline-item__time {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 8rpx;
  display: block;
}

.timeline-item__tag {
  margin-top: 8rpx;
  display: inline-flex;
}

.timeline-item__tag-text {
  font-size: 20rpx;
  color: #64748B;
  background: #F3F4F6;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
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
