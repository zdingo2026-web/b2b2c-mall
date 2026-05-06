<template>
  <view class="notice-bar" v-if="currentNotice">
    <RemixIcon name="notification-line" :size="36" color="#F97316" />
    <view class="notice-bar__content" @tap="handleTap">
      <view class="notice-bar__scroll-wrapper">
        <text class="notice-bar__text" :class="{ 'notice-bar__text--animate': notices.length > 1 }">
          {{ currentNotice.title }}
        </text>
      </view>
    </view>
    <RemixIcon name="arrow-right-s-line" :size="32" color="#9CA3AF" />
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import RemixIcon from './RemixIcon.vue'

export interface NoticeItem {
  id: string
  title: string
}

const props = withDefaults(defineProps<{
  notices: NoticeItem[]
}>(), {
  notices: () => []
})

const emit = defineEmits<{
  tap: [notice: NoticeItem]
}>()

const currentIndex = ref(0)
let rotateTimer: ReturnType<typeof setInterval> | null = null

const currentNotice = ref<NoticeItem | null>(null)

function updateCurrent() {
  if (props.notices.length === 0) {
    currentNotice.value = null
    return
  }
  currentNotice.value = props.notices[currentIndex.value]
}

function startRotation() {
  stopRotation()
  if (props.notices.length <= 1) return
  rotateTimer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % props.notices.length
    updateCurrent()
  }, 3000)
}

function stopRotation() {
  if (rotateTimer) {
    clearInterval(rotateTimer)
    rotateTimer = null
  }
}

function handleTap() {
  if (currentNotice.value) {
    emit('tap', currentNotice.value)
  }
}

onMounted(() => {
  updateCurrent()
  startRotation()
})

onUnmounted(() => {
  stopRotation()
})

watch(() => props.notices, () => {
  currentIndex.value = 0
  updateCurrent()
  startRotation()
})
</script>

<style lang="scss" scoped>
.notice-bar {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  margin: 0 32rpx;
  gap: 16rpx;
}

.notice-bar__content {
  flex: 1;
  overflow: hidden;
}

.notice-bar__scroll-wrapper {
  overflow: hidden;
}

.notice-bar__text {
  font-size: 26rpx;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.notice-bar__text--animate {
  animation: noticeSlide 0.3s ease-in-out;
}

@keyframes noticeSlide {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
