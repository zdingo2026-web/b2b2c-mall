<template>
  <view class="countdown" v-if="!finished">
    <view class="countdown__block">{{ hours }}</view>
    <text class="countdown__sep">:</text>
    <view class="countdown__block">{{ minutes }}</view>
    <text class="countdown__sep">:</text>
    <view class="countdown__block">{{ seconds }}</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  endTime: string | number
}>()

const emit = defineEmits<{
  finished: []
}>()

const hours = ref('00')
const minutes = ref('00')
const seconds = ref('00')
const finished = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

function getEndTimestamp(): number {
  if (typeof props.endTime === 'number') {
    return props.endTime > 1e12 ? props.endTime : props.endTime * 1000
  }
  return new Date(props.endTime).getTime()
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`
}

function update() {
  const now = Date.now()
  const end = getEndTimestamp()
  let diff = Math.max(0, Math.floor((end - now) / 1000))

  if (diff <= 0) {
    finished.value = true
    hours.value = '00'
    minutes.value = '00'
    seconds.value = '00'
    stopTimer()
    emit('finished')
    return
  }

  const h = Math.floor(diff / 3600)
  diff %= 3600
  const m = Math.floor(diff / 60)
  const s = diff % 60

  hours.value = pad(h)
  minutes.value = pad(m)
  seconds.value = pad(s)
}

function startTimer() {
  update()
  timer = setInterval(update, 1000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(startTimer)
onUnmounted(stopTimer)
</script>

<style lang="scss" scoped>
.countdown {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.countdown__block {
  background: #000000;
  color: #FFFFFF;
  font-size: 22rpx;
  font-weight: 600;
  border-radius: 8rpx;
  padding: 4rpx 8rpx;
  min-width: 40rpx;
  text-align: center;
  line-height: 1.2;
}

.countdown__sep {
  font-size: 24rpx;
  font-weight: 600;
  color: #1E293B;
}
</style>
