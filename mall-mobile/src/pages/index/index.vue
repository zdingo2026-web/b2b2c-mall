<template>
  <view class="page-index">
    <!-- Custom Navigation Bar -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-bar__content">
        <view class="nav-bar__location" @tap="handleLocationTap">
          <RemixIcon name="map-pin-line" :size="36" color="#2563EB" />
          <text class="nav-bar__city">广州</text>
          <RemixIcon name="arrow-down-s-line" :size="28" color="#1E293B" />
        </view>
        <view class="nav-bar__search" @tap="navigateTo('/pages/search/index')">
          <RemixIcon name="search-line" :size="32" color="#94A3B8" />
          <text class="nav-bar__search-placeholder">搜索商品、店铺</text>
        </view>
        <view class="nav-bar__notify" @tap="handleNotifyTap">
          <RemixIcon name="notification-3-line" :size="44" color="#4B5563" />
          <view v-if="hasUnread" class="nav-bar__badge" />
        </view>
      </view>
    </view>

    <!-- Scrollable content area -->
    <scroll-view
      scroll-y
      class="page-index__scroll"
      :style="{ paddingTop: (statusBarHeight + navBarHeight) + 'px' }"
      @scrolltolower="loadMoreProducts"
      refresher-enabled
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- Banner Swiper -->
      <view class="section-banner">
        <swiper
          v-if="banners.length"
          class="banner-swiper"
          :indicator-dots="false"
          autoplay
          circular
          :interval="4000"
          :current="bannerCurrent"
          @change="onBannerChange"
        >
          <swiper-item v-for="banner in banners" :key="banner.id">
            <image
              class="banner-swiper__img"
              :src="banner.imageUrl"
              mode="aspectFill"
              @tap="handleBannerTap(banner)"
            />
          </swiper-item>
        </swiper>
        <view v-else class="banner-swiper banner-placeholder">
          <text class="banner-placeholder__text">B2B2C商城</text>
        </view>
        <!-- Custom indicator: capsule style -->
        <view v-if="banners.length > 1" class="banner-dots">
          <view
            v-for="(_, idx) in banners"
            :key="idx"
            class="banner-dot"
            :class="{ 'banner-dot--active': idx === bannerCurrent }"
          />
        </view>
      </view>

      <!-- Quick Entry Area: white card, 2 rows x 5 columns -->
      <view class="section-entry">
        <view class="entry-grid">
          <view
            v-for="entry in quickEntryList"
            :key="entry.name"
            class="entry-item"
            @tap="handleEntryTap(entry.name)"
          >
            <view class="entry-icon" :style="{ background: entry.bgColor }">
              <RemixIcon :name="entry.icon" :size="44" :color="entry.iconColor" />
            </view>
            <text class="entry-name">{{ entry.name }}</text>
          </view>
        </view>
      </view>

      <!-- Notice Bar -->
      <NoticeBar
        v-if="notices.length"
        :notices="notices"
        class="section-notice"
        @tap="handleNoticeTap"
      />

      <!-- Flash Sale Section (static placeholder, no API call) -->
      <view class="section-flash">
        <view class="flash-header">
          <view class="flash-header__left">
            <text class="flash-header__title">限时秒杀</text>
            <Countdown :end-time="flashSaleEndTime" @finished="onCountdownFinished" />
          </view>
          <view class="flash-header__right" @tap="handleEntryTap('限时秒杀')">
            <text class="flash-header__more">更多秒杀</text>
            <RemixIcon name="arrow-right-s-line" :size="28" color="#64748B" />
          </view>
        </view>
        <scroll-view scroll-x class="flash-scroll">
          <view class="flash-list">
            <view
              v-for="product in flashSaleProducts"
              :key="product.id"
              class="flash-item"
              @click="navigateTo(`/pages/product/index?id=${product.id}`)"
            >
              <image
                v-if="product.mainImage"
                class="flash-item__img"
                :src="product.mainImage"
                mode="aspectFill"
              />
              <view v-else class="flash-item__img flash-item__img--placeholder">
                <text class="flash-item__placeholder">暂无图片</text>
              </view>
              <text class="flash-item__price">&yen;{{ product.flashPrice }}</text>
              <text class="flash-item__original">&yen;{{ product.originalPrice }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Recommend Section -->
      <view class="section-recommend">
        <view class="recommend-header">
          <text class="recommend-header__title">为你推荐</text>
          <view class="recommend-header__refresh" @tap="handleRefreshProducts">
            <RemixIcon name="refresh-line" :size="28" color="#64748B" />
            <text class="recommend-header__refresh-text">换一批</text>
          </view>
        </view>
        <view class="recommend-grid">
          <ProductCard
            v-for="product in recommendProducts"
            :key="product.id"
            :product="product"
            :tag-type="getProductTagType(product)"
            :original-price="product.originalPrice || ''"
            :total-sales="product.totalSales"
            @tap="navigateTo(`/pages/product/index?id=${product.id}`)"
          />
        </view>
        <view v-if="loadingMore" class="recommend-loading">
          <text class="recommend-loading__text">加载中...</text>
        </view>
        <view v-if="noMoreProducts && recommendProducts.length > 0" class="recommend-loading">
          <text class="recommend-loading__text">没有更多了</text>
        </view>
      </view>

      <!-- Bottom padding for tab bar -->
      <view class="bottom-safe" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemixIcon from '@/components/RemixIcon.vue'
import NoticeBar from '@/components/NoticeBar.vue'
import Countdown from '@/components/Countdown.vue'
import TagBadge from '@/components/TagBadge.vue'
import ProductCard from '@/components/ProductCard.vue'
import { getHomeData, type BannerVO, type NoticeVO } from '@/api/home'
import { getSpuList, type SpuVO } from '@/api/product'
import { getUnreadCount } from '@/api/message'

// --- Quick entry configuration (hardcoded per design spec) ---
interface QuickEntry {
  name: string
  icon: string
  bgColor: string
  iconColor: string
}

const quickEntryList: QuickEntry[] = [
  // Row 1
  { name: '全部分类', icon: 'apps-line', bgColor: '#DBEAFE', iconColor: '#2563EB' },
  { name: '限时秒杀', icon: 'flashlight-line', bgColor: '#FFF7ED', iconColor: '#F97316' },
  { name: '领券中心', icon: 'coupon-3-line', bgColor: '#FEF2F2', iconColor: '#E11148' },
  { name: '多人拼团', icon: 'group-line', bgColor: '#F0FDF4', iconColor: '#16A34A' },
  { name: '积分商城', icon: 'coin-line', bgColor: '#FAF5FF', iconColor: '#9333EA' },
  // Row 2
  { name: '新品首发', icon: 'sparkling-line', bgColor: '#ECFEFF', iconColor: '#0891B2' },
  { name: '品牌馆', icon: 'store-2-line', bgColor: '#EEF2FF', iconColor: '#4F46E5' },
  { name: '会员中心', icon: 'vip-crown-line', bgColor: '#FEFCE8', iconColor: '#CA8A04' },
  { name: '预售专区', icon: 'time-line', bgColor: '#FDF2F8', iconColor: '#DB2777' },
  { name: '更多服务', icon: 'more-line', bgColor: '#F3F4F6', iconColor: '#6B7280' },
]

// Marketing entries that show toast until implemented
const MARKETING_ENTRIES = new Set([
  '限时秒杀', '领券中心', '多人拼团', '积分商城',
  '新品首发', '品牌馆', '会员中心', '预售专区', '更多服务',
])

// --- Static flash sale placeholder data (no API call) ---
interface FlashSalePlaceholder {
  id: number
  mainImage: string
  flashPrice: string
  originalPrice: string
}

const flashSaleProducts = ref<FlashSalePlaceholder[]>([
  { id: 9001, mainImage: '', flashPrice: '29.9', originalPrice: '99.00' },
  { id: 9002, mainImage: '', flashPrice: '49.9', originalPrice: '159.00' },
  { id: 9003, mainImage: '', flashPrice: '19.9', originalPrice: '69.00' },
])

// --- Status bar height ---
const statusBarHeight = ref(0)
const navBarHeight = 44

// --- Data ---
const banners = ref<BannerVO[]>([])
const notices = ref<NoticeVO[]>([])
const recommendProducts = ref<SpuVO[]>([])
const bannerCurrent = ref(0)
const isRefreshing = ref(false)
const loadingMore = ref(false)
const noMoreProducts = ref(false)
const recommendPage = ref(1)
const hasUnread = ref(false)

// Flash sale end time: 24 hours from now
const flashSaleEndTime = ref(Date.now() + 24 * 60 * 60 * 1000)

// --- Methods ---
function getStatusBarHeight() {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 0
}

async function loadHomeData() {
  try {
    const token = uni.getStorageSync('mall_mobile_token')
    const requests: Promise<any>[] = [
      getHomeData(),
      getSpuList({ page: 1, limit: 10 }),
    ]
    if (token) {
      requests.push(getUnreadCount())
    }

    const results = await Promise.allSettled(requests)

    if (results[0].status === 'fulfilled') {
      banners.value = (results[0].value as any).banners || []
      notices.value = (results[0].value as any).notices || []
    }

    if (results[1].status === 'fulfilled') {
      recommendProducts.value = (results[1].value as any).list || []
    }

    if (token && results.length > 2 && results[2].status === 'fulfilled') {
      hasUnread.value = (results[2].value as number) > 0
    } else {
      hasUnread.value = false
    }
  } catch {
    // Errors handled by request wrapper
  }
}

async function loadMoreProducts() {
  if (loadingMore.value || noMoreProducts.value) return
  loadingMore.value = true
  recommendPage.value += 1
  try {
    const result = await getSpuList({ page: recommendPage.value, limit: 10 })
    const list = result.list || []
    if (list.length === 0) {
      noMoreProducts.value = true
      recommendPage.value -= 1
    } else {
      recommendProducts.value = [...recommendProducts.value, ...list]
    }
  } catch {
    recommendPage.value -= 1
  } finally {
    loadingMore.value = false
  }
}

async function handleRefreshProducts() {
  recommendPage.value += 1
  try {
    const result = await getSpuList({ page: recommendPage.value, limit: 10 })
    const list = result.list || []
    if (list.length > 0) {
      recommendProducts.value = list
      noMoreProducts.value = false
    } else {
      recommendPage.value = 1
      const firstPage = await getSpuList({ page: 1, limit: 10 })
      recommendProducts.value = firstPage.list || []
      noMoreProducts.value = false
    }
  } catch {
    recommendPage.value -= 1
  }
}

function onBannerChange(e: any) {
  bannerCurrent.value = e.detail.current
}

function handleBannerTap(banner: BannerVO) {
  if (banner.linkUrl) {
    navigateTo(banner.linkUrl)
  }
}

function handleLocationTap() {
  uni.showToast({ title: '切换城市', icon: 'none' })
}

function handleNotifyTap() {
  const token = uni.getStorageSync('mall_mobile_token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  uni.navigateTo({ url: '/pages/message/index' })
}

function handleNoticeTap(notice: NoticeVO) {
  uni.showToast({ title: notice.title, icon: 'none' })
}

function handleEntryTap(name: string) {
  if (name === '全部分类') {
    uni.switchTab({ url: '/pages/category/index' })
    return
  }
  if (MARKETING_ENTRIES.has(name)) {
    uni.showToast({ title: '敬请期待', icon: 'none' })
    return
  }
}

function onCountdownFinished() {
  // Flash sale ended - could refresh or show ended state
}

function getProductTagType(_product: SpuVO): number {
  // Placeholder: return 0 (no tag) by default
  return 0
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

async function onRefresh() {
  isRefreshing.value = true
  recommendPage.value = 1
  noMoreProducts.value = false
  await loadHomeData()
  isRefreshing.value = false
}

// --- Lifecycle ---
onMounted(() => {
  getStatusBarHeight()
  loadHomeData()
})
</script>

<style lang="scss" scoped>
.page-index {
  min-height: 100vh;
  background: #F9FAFB;
}

.page-index__scroll {
  height: 100vh;
  box-sizing: border-box;
}

/* ===== Custom Nav Bar ===== */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: #FFFFFF;
}

.nav-bar__content {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 32rpx;
  gap: 16rpx;
}

.nav-bar__location {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
}

.nav-bar__city {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
}

.nav-bar__search {
  flex: 1;
  display: flex;
  align-items: center;
  height: 64rpx;
  background: #F3F4F6;
  border-radius: 32rpx;
  padding: 0 24rpx;
  gap: 12rpx;
}

.nav-bar__search-placeholder {
  font-size: 26rpx;
  color: #94A3B8;
}

.nav-bar__notify {
  position: relative;
  flex-shrink: 0;
  width: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-bar__badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 16rpx;
  height: 16rpx;
  background: #EF4444;
  border-radius: 50%;
  border: 2rpx solid #FFFFFF;
}

/* ===== Banner Swiper ===== */
.section-banner {
  padding: 24rpx 32rpx 0;
  position: relative;
}

.banner-swiper {
  height: 320rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.banner-swiper__img {
  width: 100%;
  height: 320rpx;
}

.banner-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
}

.banner-placeholder__text {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.banner-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
}

.banner-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #D1D5DB;
  transition: all 0.3s ease;
}

.banner-dot--active {
  width: 50rpx;
  height: 12rpx;
  border-radius: 6rpx;
  background: #2563EB;
}

/* ===== Quick Entry ===== */
.section-entry {
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 24rpx 32rpx 0;
  padding: 32rpx 16rpx 24rpx;
}

.entry-grid {
  display: flex;
  flex-wrap: wrap;
}

.entry-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
}

.entry-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.entry-name {
  font-size: 24rpx;
  color: #334155;
  text-align: center;
}

/* ===== Notice Bar ===== */
.section-notice {
  margin-top: 24rpx;
}

/* ===== Flash Sale ===== */
.section-flash {
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 24rpx 32rpx 0;
  padding: 32rpx;
}

.flash-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.flash-header__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.flash-header__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #E11148;
}

.flash-header__right {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.flash-header__more {
  font-size: 24rpx;
  color: #64748B;
}

.flash-scroll {
  white-space: nowrap;
}

.flash-list {
  display: flex;
  gap: 20rpx;
}

.flash-item {
  width: 200rpx;
  flex-shrink: 0;
}

.flash-item__img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
}

.flash-item__img--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F3F4F6;
}

.flash-item__placeholder {
  font-size: 20rpx;
  color: #94A3B8;
}

.flash-item__price {
  display: block;
  font-size: 28rpx;
  color: #E11148;
  font-weight: 600;
  margin-top: 8rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.flash-item__original {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
  text-decoration: line-through;
  margin-top: 4rpx;
  white-space: nowrap;
}

/* ===== Recommend Section ===== */
.section-recommend {
  padding: 24rpx 32rpx 0;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.recommend-header__title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
}

.recommend-header__refresh {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.recommend-header__refresh-text {
  font-size: 26rpx;
  color: #64748B;
}

.recommend-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.recommend-loading {
  text-align: center;
  padding: 32rpx 0;
}

.recommend-loading__text {
  font-size: 24rpx;
  color: #94A3B8;
}

/* ===== Bottom Safe Area ===== */
.bottom-safe {
  height: 120rpx;
}
</style>
