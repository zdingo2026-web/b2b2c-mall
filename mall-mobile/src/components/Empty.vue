<template>
  <view class="empty">
    <image class="empty__img" src="/static/empty.png" mode="aspectFit" />
    <text class="empty__text">{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
}>(), {
  text: '暂无数据',
})
</script>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty__img {
  width: 240rpx;
  height: 240rpx;
  margin-bottom: 24rpx;
}

.empty__text {
  font-size: 28rpx;
  color: #999;
}
</style>
