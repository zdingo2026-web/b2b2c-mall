<template>
  <view class="page-message">
    <!-- Tabs -->
    <view class="msg-tabs">
      <view
        v-for="(tab, idx) in tabs"
        :key="tab.key"
        class="msg-tabs__item"
        :class="{ 'msg-tabs__item--active': activeTab === idx }"
        @tap="switchTab(idx)"
      >
        <text class="msg-tabs__text" :class="{ 'msg-tabs__text--active': activeTab === idx }">
          {{ tab.label }}
        </text>
        <view v-if="activeTab === idx" class="msg-tabs__indicator" />
      </view>
    </view>

    <!-- Message list -->
    <scroll-view scroll-y class="msg-scroll" @scrolltolower="loadMore">
      <view v-if="messages.length" class="msg-list">
        <view
          v-for="msg in messages"
          :key="msg.id"
          class="msg-item"
          :class="{ 'msg-item--unread': msg.isRead === 0 }"
          @tap="handleMessageTap(msg)"
        >
          <view class="msg-item__icon">
            <RemixIcon :name="getMsgIcon(msg.msgType)" :size="40" :color="getMsgIconColor(msg.msgType)" />
          </view>
          <view class="msg-item__content">
            <view class="msg-item__header">
              <text class="msg-item__title">{{ msg.title }}</text>
              <view v-if="msg.isRead === 0" class="msg-item__dot" />
            </view>
            <text class="msg-item__text">{{ msg.content }}</text>
            <text class="msg-item__time">{{ msg.createTime }}</text>
          </view>
        </view>
      </view>
      <Empty v-if="!loading && !messages.length" text="暂无消息" />
      <view v-if="loading" class="msg-loading"><text>加载中...</text></view>
      <view v-if="noMore && messages.length" class="msg-nomore"><text>没有更多了</text></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'
import { getMessageList, markRead, type MessageVO } from '@/api/message'

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'system', label: '系统' },
  { key: 'order', label: '订单' },
  { key: 'activity', label: '活动' },
]

const tabMsgTypeMap: Record<number, number | undefined> = {
  0: undefined,
  1: 1,
  2: 2,
  3: 3,
}

const activeTab = ref(0)
const messages = ref<MessageVO[]>([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)

const PAGE_SIZE = 20

onLoad(() => {
  fetchList(true)
})

function switchTab(idx: number) {
  if (activeTab.value === idx) return
  activeTab.value = idx
  fetchList(true)
}

async function fetchList(reset = false) {
  if (reset) {
    page.value = 1
    noMore.value = false
    messages.value = []
  }
  loading.value = true
  try {
    const msgType = tabMsgTypeMap[activeTab.value]
    const data = await getMessageList({
      page: page.value,
      limit: PAGE_SIZE,
      ...(msgType !== undefined ? { msgType } : {}),
    })
    const list = data.list || []
    if (reset) {
      messages.value = list
    } else {
      messages.value = [...messages.value, ...list]
    }
    noMore.value = list.length < PAGE_SIZE
  } catch {
    // Silently fail
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || noMore.value) return
  page.value++
  fetchList()
}

async function handleMessageTap(msg: MessageVO) {
  if (msg.isRead === 0) {
    try {
      await markRead(msg.id)
      messages.value = messages.value.map((m) =>
        m.id === msg.id ? { ...m, isRead: 1 } : m,
      )
    } catch {
      // Ignore mark-read error
    }
  }
  uni.showToast({ title: msg.title, icon: 'none' })
}

function getMsgIcon(msgType: number): string {
  const icons: Record<number, string> = {
    1: 'notification-3-line',
    2: 'file-list-3-line',
    3: 'gift-line',
  }
  return icons[msgType] || 'notification-3-line'
}

function getMsgIconColor(msgType: number): string {
  const colors: Record<number, string> = {
    1: '#2563EB',
    2: '#F97316',
    3: '#E11148',
  }
  return colors[msgType] || '#2563EB'
}
</script>

<style lang="scss" scoped>
.page-message {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Tabs */
.msg-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #F3F4F6;
  flex-shrink: 0;
}

.msg-tabs__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0 16rpx;
  position: relative;
}

.msg-tabs__text {
  font-size: 28rpx;
  color: #64748B;
}

.msg-tabs__text--active {
  color: #2563EB;
  font-weight: 600;
}

.msg-tabs__indicator {
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #2563EB;
  margin-top: 8rpx;
}

/* Scroll area */
.msg-scroll {
  flex: 1;
  padding: 20rpx 24rpx;
}

/* Message list */
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.msg-item {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  gap: 20rpx;
}

.msg-item--unread {
  background: #FAFBFF;
}

.msg-item__icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #EFF6FF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.msg-item__content {
  flex: 1;
  min-width: 0;
}

.msg-item__header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.msg-item__title {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-item__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #EF4444;
  flex-shrink: 0;
}

.msg-item__text {
  display: block;
  font-size: 24rpx;
  color: #64748B;
  margin-top: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-item__time {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 8rpx;
}

/* Loading / No more */
.msg-loading,
.msg-nomore {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
