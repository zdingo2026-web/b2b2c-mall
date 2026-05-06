<template>
  <view class="page-footprint">
    <!-- Navigation bar right action -->
    <view class="footprint-header">
      <view class="footprint-header__clear" @tap="handleClear">
        <text class="footprint-header__clear-text">清空</text>
      </view>
    </view>

    <!-- Grouped content -->
    <scroll-view scroll-y class="footprint-scroll">
      <view v-if="groups.length" class="footprint-groups">
        <view v-for="group in groups" :key="group.label" class="footprint-group">
          <!-- Date label -->
          <view class="footprint-group__label">
            <text class="footprint-group__label-text">{{ group.label }}</text>
          </view>
          <!-- Items -->
          <view v-for="item in group.items" :key="item.id" class="footprint-item" @tap="navigateTo(`/pages/product/index?id=${item.spuId}`)">
            <image class="footprint-item__img" :src="item.mainImage" mode="aspectFill" />
            <view class="footprint-item__info">
              <text class="footprint-item__name">{{ item.productName }}</text>
              <text class="footprint-item__price">&yen;{{ item.minPrice }}</text>
            </view>
            <view class="footprint-item__cart" @tap.stop="handleAddCart(item)">
              <RemixIcon name="add-circle-fill" :size="48" color="#2563EB" />
            </view>
          </view>
        </view>
      </view>
      <Empty v-if="!loading && !groups.length" text="暂无浏览记录" />
      <view v-if="loading" class="footprint-loading"><text>加载中...</text></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'
import { getFootprintGrouped, clearFootprint, type FootprintGroupVO, type FootprintVO } from '@/api/footprint'

const groups = ref<FootprintGroupVO[]>([])
const loading = ref(false)

onLoad(() => {
  fetchGroups()
})

onShow(() => {
  fetchGroups()
})

async function fetchGroups() {
  loading.value = true
  try {
    const data = await getFootprintGrouped()
    groups.value = data || []
  } catch {
    groups.value = []
  } finally {
    loading.value = false
  }
}

async function handleClear() {
  if (!groups.value.length) return

  const [err] = await uni.showModal({
    title: '提示',
    content: '确定清空所有浏览记录？',
  }) as any
  if (!(err?.confirm || err === true)) return

  try {
    await clearFootprint()
    groups.value = []
    uni.showToast({ title: '已清空', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function handleAddCart(_item: FootprintVO) {
  uni.showToast({ title: '请从商品详情页加入购物车', icon: 'none' })
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.page-footprint {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Header clear button */
.footprint-header {
  display: flex;
  justify-content: flex-end;
  padding: 16rpx 32rpx;
  background: #fff;
  flex-shrink: 0;
}

.footprint-header__clear {
  padding: 8rpx 20rpx;
}

.footprint-header__clear-text {
  font-size: 28rpx;
  color: #64748B;
}

/* Scroll area */
.footprint-scroll {
  flex: 1;
}

/* Groups */
.footprint-groups {
  padding: 0 24rpx 32rpx;
}

.footprint-group {
  margin-top: 24rpx;
}

.footprint-group__label {
  padding: 8rpx 0 16rpx;
}

.footprint-group__label-text {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 600;
}

/* Item */
.footprint-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.footprint-item__img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.footprint-item__info {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.footprint-item__name {
  font-size: 28rpx;
  color: #1E293B;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.footprint-item__price {
  font-size: 30rpx;
  color: #E11148;
  font-weight: 600;
  margin-top: 12rpx;
  display: block;
}

.footprint-item__cart {
  flex-shrink: 0;
  margin-left: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Loading */
.footprint-loading {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #94A3B8;
}
</style>
