<template>
  <view class="page-category">
    <!-- Top search bar -->
    <view class="search-bar">
      <view class="search-bar__input-wrap" @tap="navigateTo('/pages/search/index')">
        <RemixIcon name="search-line" :size="28" color="#9CA3AF" />
        <text class="search-bar__placeholder">搜索商品</text>
      </view>
      <view class="search-bar__btn" @tap="navigateTo('/pages/search/index')">
        <text class="search-bar__btn-text">搜索</text>
      </view>
    </view>

    <!-- Category body -->
    <view class="category-layout">
      <!-- Left sidebar -->
      <scroll-view scroll-y class="category-sidebar">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="category-sidebar__item"
          :class="{ 'category-sidebar__item--active': activeCategory === cat.id }"
          @tap="activeCategory = cat.id"
        >
          <text class="category-sidebar__text">{{ cat.categoryName }}</text>
        </view>
      </scroll-view>

      <!-- Right content area -->
      <scroll-view scroll-y class="category-content" :key="activeCategory">
        <!-- Category banner (轮播) -->
        <image
          v-if="currentCategory?.image"
          class="category-banner"
          :src="currentCategory.image"
          mode="aspectFill"
        />

        <!-- Sub-category section -->
        <view v-if="currentCategory?.children?.length" class="section">
          <text class="section__title">热门分类</text>
          <view class="sub-grid">
            <view
              v-for="child in currentCategory.children"
              :key="child.id"
              class="sub-grid__item"
              @tap="navigateTo(`/pages/product/list?categoryId=${child.id}`)"
            >
              <image v-if="child.image" class="sub-grid__img" :src="child.image" mode="aspectFill" />
              <view v-else class="sub-grid__placeholder">
                <text class="sub-grid__placeholder-text">{{ child.categoryName.charAt(0) }}</text>
              </view>
              <text class="sub-grid__name">{{ child.categoryName }}</text>
            </view>
          </view>
        </view>

        <!-- Hot products section -->
        <view v-if="products.length" class="section">
          <text class="section__title">热门推荐</text>
          <view class="product-grid">
            <ProductCard
              v-for="product in products"
              :key="product.id"
              :product="product"
              @click="(p: SpuVO) => navigateTo(`/pages/product/index?id=${p.id}`)"
            />
          </view>
        </view>

        <Empty v-if="!currentCategory?.children?.length && !products.length" text="暂无分类数据" />
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import RemixIcon from '@/components/RemixIcon.vue'
import ProductCard from '@/components/ProductCard.vue'
import Empty from '@/components/Empty.vue'
import { getCategoryTree, getSpuList, type CategoryTreeVO, type SpuVO } from '@/api/product'

const activeCategory = ref<string>('0')
const categories = ref<CategoryTreeVO[]>([])
const products = ref<SpuVO[]>([])

const currentCategory = computed(() =>
  categories.value.find((c) => c.id === activeCategory.value),
)

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

async function loadCategories() {
  try {
    const data = await getCategoryTree()
    categories.value = data
    if (data.length && activeCategory.value === '0') {
      activeCategory.value = data[0].id
    }
  } catch {
    // handled
  }
}

async function loadProducts(categoryId: string) {
  try {
    const data = await getSpuList({ categoryId, page: 1, limit: 20 })
    products.value = data.list || []
  } catch {
    products.value = []
  }
}

watch(activeCategory, (val) => {
  if (val) {
    loadProducts(val)
  }
})

onMounted(loadCategories)
</script>

<style lang="scss" scoped>
.page-category {
  height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* Search bar */
.search-bar {
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  height: 96rpx;
  background: #FFFFFF;
}

.search-bar__input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  height: 64rpx;
  background: #F3F4F6;
  border-radius: 16rpx;
  padding: 0 24rpx;
  gap: 12rpx;
}

.search-bar__placeholder {
  font-size: 26rpx;
  color: #94A3B8;
}

.search-bar__btn {
  margin-left: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.search-bar__btn-text {
  font-size: 28rpx;
  color: #2563EB;
  font-weight: 500;
}

/* Category layout */
.category-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* Left sidebar */
.category-sidebar {
  width: 180rpx;
  background: #FFFFFF;
  height: 100%;
}

.category-sidebar__item {
  position: relative;
  padding: 24rpx 16rpx;
  text-align: center;
  font-size: 26rpx;
  color: #334155;
  font-weight: 400;
  border-right: 6rpx solid transparent;
  transition: all 0.2s;
}

.category-sidebar__item--active {
  color: #2563EB;
  font-weight: 500;
  background: #EFF6FF;
  border-right-color: #2563EB;
}

.category-sidebar__text {
  line-height: 1.4;
}

/* Right content */
.category-content {
  flex: 1;
  padding: 24rpx;
  height: 100%;
  background: #F9FAFB;
}

/* Category banner */
.category-banner {
  width: 100%;
  height: 200rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

/* Section */
.section {
  margin-bottom: 32rpx;
}

.section__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 24rpx;
  display: block;
}

/* Sub-category grid */
.sub-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.sub-grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 16rpx;
}

.sub-grid__img {
  width: 122rpx;
  height: 120rpx;
  border-radius: 16rpx;
}

.sub-grid__placeholder {
  width: 122rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: #EFF6FF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sub-grid__placeholder-text {
  font-size: 36rpx;
  color: #2563EB;
  font-weight: 600;
}

.sub-grid__name {
  font-size: 24rpx;
  color: #334155;
  text-align: center;
  margin-top: 16rpx;
  line-height: 1.3;
}

/* Product grid */
.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}
</style>
