<template>
  <view v-if="type > 0" class="tag-badge" :class="`tag-badge--${type}`">
    {{ label }}
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const TAG_MAP: Record<number, { label: string }> = {
  1: { label: '百亿补贴' },
  2: { label: '品牌特卖' },
  3: { label: '新品首发' },
  4: { label: '以旧换新' },
}

const props = defineProps<{
  type: number
}>()

const label = computed(() => TAG_MAP[props.type]?.label ?? '')
</script>

<style lang="scss" scoped>
.tag-badge {
  display: inline-block;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  line-height: 1.2;
  white-space: nowrap;
}

.tag-badge--1 {
  background: #FEF2F2;
  color: #E11148;
}

.tag-badge--2 {
  background: #EFF6FF;
  color: #2563EB;
}

.tag-badge--3 {
  background: #FFF7ED;
  color: #F97316;
}

.tag-badge--4 {
  background: #F0FDF4;
  color: #16A34A;
}
</style>
