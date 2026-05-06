<template>
  <text class="remix-icon" :class="'ri-' + name" :style="iconStyle" @tap="$emit('tap')" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  name: string
  size?: number
  color?: string
}>(), {
  size: 32,
  color: 'inherit',
})

defineEmits<{ tap: [] }>()

const iconStyle = computed(() => ({
  fontSize: props.size + 'rpx',
  color: props.color,
}))
</script>

<style>
@import url('@/static/fonts/remixicon.css');

.remix-icon {
  display: inline-block;
  vertical-align: middle;
}
</style>
