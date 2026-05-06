<template>
  <div>
    <!-- Banner Carousel -->
    <section class="relative bg-gray-100 overflow-hidden">
      <div class="max-w-7xl mx-auto relative">
        <div class="relative h-[420px]">
          <transition-group name="fade" tag="div" class="relative w-full h-full">
            <div
              v-for="(banner, idx) in banners"
              :key="banner.id"
              v-show="idx === currentBanner"
              class="absolute inset-0 cursor-pointer"
              @click="banner.linkUrl && navigateTo(banner.linkUrl)"
            >
              <img
                v-if="banner.imageUrl"
                :src="banner.imageUrl"
                :alt="banner.title"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full bg-gradient-to-r from-primary-500 to-primary-700 flex items-center">
                <div class="max-w-7xl mx-auto px-8 text-white">
                  <h2 class="text-4xl font-bold mb-4">{{ banner.title || '品质生活 尽在掌握' }}</h2>
                  <p class="text-lg text-primary-100">多商户入驻，海量精选商品，一站式购物体验</p>
                </div>
              </div>
            </div>
          </transition-group>
          <!-- Dots -->
          <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
            <button
              v-for="(_, idx) in banners"
              :key="idx"
              class="w-3 h-3 rounded-full transition-all"
              :class="idx === currentBanner ? 'bg-primary-500 w-6' : 'bg-white/60 hover:bg-white'"
              @click="currentBanner = idx"
            />
          </div>
          <!-- Arrows -->
          <button
            class="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-black/20 hover:bg-black/40 rounded-full flex items-center justify-center text-white"
            @click="prevBanner"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
          </button>
          <button
            class="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-black/20 hover:bg-black/40 rounded-full flex items-center justify-center text-white"
            @click="nextBanner"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
          </button>
        </div>
      </div>
    </section>

    <!-- Quick Entries (2 rows x 5 columns) -->
    <section class="max-w-7xl mx-auto px-4 -mt-8 relative z-10">
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="grid grid-cols-5 gap-4">
          <div
            v-for="entry in quickEntries"
            :key="entry.name"
            class="flex flex-col items-center gap-2 py-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
            @click="handleQuickEntry(entry)"
          >
            <div class="w-12 h-12 rounded-full flex items-center justify-center text-xl" :style="{ background: entry.bgColor, color: entry.iconColor }">
              {{ entry.icon }}
            </div>
            <span class="text-xs text-gray-700 font-medium">{{ entry.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Notice Bar -->
    <section v-if="notices.length" class="max-w-7xl mx-auto px-4 mt-4">
      <div class="bg-white rounded-lg shadow-sm px-6 py-3 flex items-center gap-3">
        <span class="text-orange-500 text-lg">📢</span>
        <div class="flex-1 overflow-hidden">
          <div class="notice-scroll">
            <span class="text-sm text-gray-500">{{ currentNotice?.title }}</span>
          </div>
        </div>
        <span class="text-gray-400 text-sm cursor-pointer hover:text-primary-500">›</span>
      </div>
    </section>

    <!-- Floor Sections -->
    <section v-for="floor in floors" :key="floor.id" class="max-w-7xl mx-auto px-4 py-10">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-gray-800">{{ floor.title }}</h2>
        <NuxtLink :to="`/category/${floor.categoryId}`" class="text-sm text-primary-500 hover:text-primary-600">
          查看更多 &rarr;
        </NuxtLink>
      </div>
      <div class="grid grid-cols-5 gap-4">
        <ProductCard
          v-for="product in floor.products"
          :key="product.id"
          :product="product"
        />
      </div>
    </section>

    <!-- New Products -->
    <section class="max-w-7xl mx-auto px-4 py-10">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-gray-800">新品上架</h2>
        <NuxtLink to="/product?sort=new" class="text-sm text-primary-500 hover:text-primary-600">
          查看更多 &rarr;
        </NuxtLink>
      </div>
      <div class="grid grid-cols-5 gap-4">
        <ProductCard
          v-for="product in newProducts"
          :key="product.id"
          :product="product"
        />
      </div>
      <div v-if="newProducts.length === 0" class="text-center py-10 text-gray-400">暂无新品</div>
    </section>

    <!-- Recommend Products -->
    <section class="bg-gray-50 py-10">
      <div class="max-w-7xl mx-auto px-4">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">为你推荐</h2>
        <div class="grid grid-cols-5 gap-4">
          <ProductCard
            v-for="product in recommendProducts"
            :key="product.id"
            :product="product"
          />
        </div>
        <div v-if="recommendProducts.length === 0" class="text-center py-10 text-gray-400">暂无推荐</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { Banner, Floor, Category, ProductListItem, NoticeVO, QuickEntryVO } from '~/composables/types'

const { get } = useRequest()

const banners = ref<Banner[]>([])
const categories = ref<Category[]>([])
const floors = ref<Floor[]>([])
const newProducts = ref<ProductListItem[]>([])
const recommendProducts = ref<ProductListItem[]>([])
const notices = ref<NoticeVO[]>([])
const quickEntries = ref<QuickEntryVO[]>([])
const currentBanner = ref(0)
const currentNoticeIdx = ref(0)
let noticeTimer: ReturnType<typeof setInterval> | null = null

const currentNotice = computed(() => notices.value[currentNoticeIdx.value] || null)

const QUICK_ENTRY_DEFAULTS: QuickEntryVO[] = [
  { name: '全部分类', icon: '📋', bgColor: '#DBEAFE', iconColor: '#2563EB', linkType: 1, linkUrl: '/category' },
  { name: '限时秒杀', icon: '⚡', bgColor: '#FFF7ED', iconColor: '#F97316', linkType: 0, linkUrl: '' },
  { name: '领券中心', icon: '🎫', bgColor: '#FEF2F2', iconColor: '#E11148', linkType: 0, linkUrl: '' },
  { name: '多人拼团', icon: '👥', bgColor: '#F0FDF4', iconColor: '#16A34A', linkType: 0, linkUrl: '' },
  { name: '积分商城', icon: '🪙', bgColor: '#FAF5FF', iconColor: '#9333EA', linkType: 0, linkUrl: '' },
  { name: '新品首发', icon: '✨', bgColor: '#ECFEFF', iconColor: '#0891B2', linkType: 0, linkUrl: '' },
  { name: '品牌馆', icon: '🏪', bgColor: '#EEF2FF', iconColor: '#4F46E5', linkType: 0, linkUrl: '' },
  { name: '会员中心', icon: '👑', bgColor: '#FEFCE8', iconColor: '#CA8A04', linkType: 0, linkUrl: '' },
  { name: '预售专区', icon: '⏰', bgColor: '#FDF2F8', iconColor: '#DB2777', linkType: 0, linkUrl: '' },
  { name: '更多服务', icon: '⋯', bgColor: '#F3F4F6', iconColor: '#6B7280', linkType: 0, linkUrl: '' },
]

function handleQuickEntry(entry: QuickEntryVO) {
  if (entry.linkType === 1 && entry.linkUrl) {
    navigateTo(entry.linkUrl)
  }
}

let bannerTimer: ReturnType<typeof setInterval> | null = null

const nextBanner = () => {
  currentBanner.value = (currentBanner.value + 1) % banners.value.length
}
const prevBanner = () => {
  currentBanner.value = (currentBanner.value - 1 + banners.value.length) % banners.value.length
}

const startBannerAutoplay = () => {
  stopBannerAutoplay()
  if (banners.value.length > 1) {
    bannerTimer = setInterval(nextBanner, 4000)
  }
}
const stopBannerAutoplay = () => {
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = null
  }
}

onMounted(() => {
  startBannerAutoplay()
  startNoticeRotation()
})

onUnmounted(() => {
  stopBannerAutoplay()
  stopNoticeRotation()
})

watch(banners, () => {
  startBannerAutoplay()
})

// Fetch home data
const loadHomeData = async () => {
  try {
    const [bannerData, catData, floorData, newData, recData, noticeData, homeData] = await Promise.allSettled([
      get<Banner[]>('/home/banners'),
      get<Category[]>('/category/tree'),
      get<Floor[]>('/home/floors'),
      get<ProductListItem[]>('/home/new-products'),
      get<ProductListItem[]>('/home/recommend-products'),
      get<NoticeVO[]>('/content/notice/list'),
      get<{ quickEntries: QuickEntryVO[] }>('/home'),
    ])
    if (bannerData.status === 'fulfilled') banners.value = bannerData.value
    if (catData.status === 'fulfilled') {
      categories.value = catData.value.slice(0, 5)
    }
    if (floorData.status === 'fulfilled') floors.value = floorData.value
    if (newData.status === 'fulfilled') newProducts.value = newData.value
    if (recData.status === 'fulfilled') recommendProducts.value = recData.value
    if (noticeData.status === 'fulfilled') notices.value = noticeData.value
    if (homeData.status === 'fulfilled' && homeData.value.quickEntries?.length) {
      quickEntries.value = homeData.value.quickEntries
    } else {
      quickEntries.value = QUICK_ENTRY_DEFAULTS
    }
  } catch {
    // Data will remain empty, pages show fallback states
    quickEntries.value = QUICK_ENTRY_DEFAULTS
  }
}

loadHomeData()

function startNoticeRotation() {
  stopNoticeRotation()
  if (notices.value.length > 1) {
    noticeTimer = setInterval(() => {
      currentNoticeIdx.value = (currentNoticeIdx.value + 1) % notices.value.length
    }, 3000)
  }
}

function stopNoticeRotation() {
  if (noticeTimer) {
    clearInterval(noticeTimer)
    noticeTimer = null
  }
}

watch(notices, () => {
  startNoticeRotation()
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
