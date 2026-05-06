<template>
  <view class="product-card" @tap="handleTap">
    <image
      v-if="product.mainImage && !imgError"
      class="product-card__img"
      :src="product.mainImage"
      mode="aspectFill"
      @error="imgError = true"
    />
    <view v-else class="product-card__img product-card__img--placeholder">
      <text class="product-card__placeholder-text">暂无图片</text>
    </view>
    <view class="product-card__info">
      <text class="product-card__name">{{ product.productName }}</text>
      <TagBadge v-if="displayTagType > 0" :type="displayTagType" class="product-card__tag" />
      <view class="product-card__bottom">
        <view class="product-card__price-row">
          <text class="product-card__price">&yen;{{ product.minPrice }}</text>
          <text v-if="displayOriginalPrice && displayOriginalPrice !== product.minPrice" class="product-card__original-price">&yen;{{ displayOriginalPrice }}</text>
        </view>
        <text v-if="displaySales != null" class="product-card__sales">已售{{ displaySales }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import TagBadge from './TagBadge.vue'
import type { SpuVO } from '@/api/product'

const props = withDefaults(defineProps<{
  product: SpuVO
  tagType?: number
  originalPrice?: string
  totalSales?: number
}>(), {
  tagType: 0,
  originalPrice: '',
  totalSales: undefined,
})

const emit = defineEmits<{
  click: [product: SpuVO]
}>()

const imgError = ref(false)

const displayTagType = computed(() => {
  return props.tagType || (props.product as any).tagType || 0
})

const displayOriginalPrice = computed(() => {
  return props.originalPrice || (props.product as any).originalPrice || ''
})

const displaySales = computed(() => {
  if (props.totalSales !== undefined) return props.totalSales
  return props.product.totalSales ?? null
})

function handleTap() {
  emit('click', props.product)
}
</script>

<style lang="scss" scoped>
.product-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.product-card__img {
  width: 100%;
  height: 360rpx;
}

.product-card__img--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F3F4F6;
}

.product-card__placeholder-text {
  font-size: 24rpx;
  color: #94A3B8;
}

.product-card__info {
  padding: 16rpx;
}

.product-card__name {
  font-size: 26rpx;
  color: #1E293B;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 72rpx;
}

.product-card__tag {
  margin-top: 8rpx;
}

.product-card__bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12rpx;
}

.product-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.product-card__price {
  font-size: 32rpx;
  color: #E11148;
  font-weight: 600;
}

.product-card__original-price {
  font-size: 22rpx;
  color: #94A3B8;
  text-decoration: line-through;
}

.product-card__sales {
  font-size: 22rpx;
  color: #94A3B8;
}
</style>
