<template>
  <view class="page-product">
    <!-- Loading state -->
    <view v-if="loading" class="page-product__loading">
      <text class="page-product__loading-text">加载中...</text>
    </view>

    <!-- Error / empty state -->
    <view v-else-if="!product" class="page-product__empty">
      <Empty text="商品不存在或已下架" />
      <view class="page-product__back" @tap="goBack">
        <text class="page-product__back-text">返回</text>
      </view>
    </view>

    <!-- Product content -->
    <template v-else>
    <!-- Top nav bar -->
    <view class="nav-bar">
      <view class="nav-bar__left" @tap="goBack">
        <RemixIcon name="arrow-left-line" :size="40" color="#1E293B" />
      </view>
      <text class="nav-bar__title">商品详情</text>
      <view class="nav-bar__right">
        <RemixIcon name="share-line" :size="40" color="#334155" @tap="handleShare" />
      </view>
    </view>

    <scroll-view scroll-y class="product-scroll">
      <!-- Product swiper -->
      <view class="swiper-wrap">
        <swiper
          class="product-swiper"
          :current="swiperCurrent"
          circular
          @change="onSwiperChange"
        >
          <swiper-item v-for="(img, idx) in productImages" :key="idx">
            <image class="product-swiper__img" :src="img" mode="aspectFill" @tap="previewImage(idx)" />
          </swiper-item>
        </swiper>
        <!-- Custom capsule/circle indicators -->
        <view class="swiper-indicator">
          <view
            v-for="(img, idx) in productImages"
            :key="idx"
            class="swiper-indicator__dot"
            :class="{ 'swiper-indicator__dot--active': swiperCurrent === idx }"
          />
        </view>
      </view>

      <!-- Basic info area -->
      <view class="info-card">
        <!-- Price area -->
        <view class="price-row">
          <text class="price-row__symbol">&yen;</text>
          <text class="price-row__value">{{ displayPrice }}</text>
          <text v-if="product.maxPrice !== product.minPrice" class="price-row__range">-{{ product.maxPrice }}</text>
          <text v-if="originalPriceDisplay" class="price-row__original">&yen;{{ originalPriceDisplay }}</text>
        </view>

        <!-- Coupon tag -->
        <view v-if="couponAmount" class="coupon-tag" @tap="handleCouponTap">
          <RemixIcon name="coupon-3-line" :size="24" color="#E11148" />
          <text class="coupon-tag__text">领券立减{{ couponAmount }}元</text>
          <RemixIcon name="arrow-right-s-line" :size="24" color="#E11148" />
        </view>

        <!-- Product title -->
        <text class="product-title">{{ product.productName }}</text>
        <text v-if="product.subTitle" class="product-subtitle">{{ product.subTitle }}</text>

        <!-- Promo info -->
        <view v-if="promoTags.length" class="promo-row">
          <view v-for="(tag, idx) in promoTags" :key="idx" class="promo-tag">
            <text class="promo-tag__text">{{ tag }}</text>
          </view>
        </view>

        <!-- Service info -->
        <view class="service-row">
          <view v-for="(svc, idx) in serviceItems" :key="idx" class="service-item">
            <RemixIcon name="shield-check-line" :size="24" color="#16A34A" />
            <text class="service-item__text">{{ svc }}</text>
          </view>
        </view>

        <!-- Sales info -->
        <text class="sales-text">已售{{ product.totalSales || 0 }}件</text>
      </view>

      <!-- SKU selection -->
      <view class="info-card" @tap="openSkuPicker('cart')">
        <view class="spec-row">
          <text class="spec-row__label">选择</text>
          <text class="spec-row__value">{{ selectedSku ? selectedSku.specValues : '请选择规格' }}</text>
          <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
        </view>
      </view>

      <!-- Store info -->
      <view v-if="product.tenantName" class="info-card store-card">
        <view class="store-row">
          <image v-if="storeAvatar" class="store-avatar" :src="storeAvatar" mode="aspectFill" />
          <view v-else class="store-avatar store-avatar--placeholder">
            <RemixIcon name="store-2-line" :size="36" color="#94A3B8" />
          </view>
          <view class="store-info">
            <view class="store-name-row">
              <text class="store-name">{{ product.tenantName }}</text>
              <RemixIcon name="shield-check-line" :size="24" color="#2563EB" />
            </view>
            <view class="store-scores">
              <view class="store-score">
                <text class="store-score__label">商品</text>
                <text class="store-score__value">{{ storeScores.product }}</text>
              </view>
              <view class="store-score">
                <text class="store-score__label">服务</text>
                <text class="store-score__value">{{ storeScores.service }}</text>
              </view>
              <view class="store-score">
                <text class="store-score__label">物流</text>
                <text class="store-score__value">{{ storeScores.logistics }}</text>
              </view>
            </view>
          </view>
          <view class="store-enter" @tap="navigateTo(`/pages/category/index?tenantId=${product.tenantId}`)">
            <text class="store-enter__text">进入店铺</text>
            <RemixIcon name="arrow-right-s-line" :size="24" color="#2563EB" />
          </view>
        </view>
      </view>

      <!-- Product params -->
      <view v-if="product.attributeList?.length" class="info-card">
        <text class="card-title">商品参数</text>
        <view v-for="(attr, idx) in product.attributeList" :key="idx" class="param-row">
          <text class="param-row__label">{{ attr.attributeName }}</text>
          <text class="param-row__value">{{ attr.attributeValue }}</text>
        </view>
      </view>

      <!-- Delivery area -->
      <view class="info-card">
        <text class="card-title">配送信息</text>
        <view class="delivery-row">
          <RemixIcon name="truck-line" :size="32" color="#2563EB" />
          <view class="delivery-info">
            <text class="delivery-info__text">预计3-5个工作日送达</text>
            <text class="delivery-info__sub">满99元免运费</text>
          </view>
        </view>
      </view>

      <!-- Comment area -->
      <view class="info-card">
        <!-- Comment header -->
        <view class="comment-header">
          <view class="comment-header__left">
            <text class="card-title" style="margin-bottom: 0">商品评价</text>
            <text v-if="commentSummary" class="comment-count">({{ commentSummary.totalCount }}条)</text>
          </view>
          <view class="comment-header__right">
            <view v-if="commentSummary" class="comment-rate">
              <RemixIcon v-for="n in 5" :key="n" :name="n <= goodRateStars ? 'star-fill' : 'star-line'" :size="24" :color="n <= goodRateStars ? '#F97316' : '#E2E8F0'" />
            </view>
            <view class="comment-more" @tap="navigateTo(`/pages/product/list?commentId=${productId}`)">
              <text class="comment-more__text">更多评论</text>
              <RemixIcon name="arrow-right-s-line" :size="24" color="#64748B" />
            </view>
          </view>
        </view>

        <!-- Comment tags -->
        <view v-if="commentSummary?.tags?.length" class="comment-tags">
          <view
            v-for="(tag, idx) in commentSummary.tags"
            :key="idx"
            class="comment-tag"
            :class="{ 'comment-tag--active': activeCommentTag === idx }"
            @tap="activeCommentTag = idx"
          >
            <text class="comment-tag__text">{{ tag.name }}({{ tag.count }})</text>
          </view>
        </view>

        <!-- Comment items -->
        <view v-if="comments.length" class="comment-list">
          <view v-for="comment in comments" :key="comment.id" class="comment-item">
            <view class="comment-item__header">
              <image v-if="comment.memberAvatar" class="comment-avatar" :src="comment.memberAvatar" mode="aspectFill" />
              <view v-else class="comment-avatar comment-avatar--placeholder">
                <text class="comment-avatar__text">{{ (comment.memberNickname || '用户').charAt(0) }}</text>
              </view>
              <view class="comment-user-info">
                <text class="comment-nickname">{{ comment.memberNickname || '匿名用户' }}</text>
                <text class="comment-time">{{ formatTime(comment.createTime) }}</text>
              </view>
            </view>
            <text class="comment-content">{{ comment.content }}</text>
            <!-- Comment images -->
            <view v-if="comment.images?.length" class="comment-images">
              <image
                v-for="(img, imgIdx) in comment.images"
                :key="imgIdx"
                class="comment-image"
                :src="img"
                mode="aspectFill"
                @tap="previewCommentImage(img, comment.images)"
              />
            </view>
            <!-- Merchant reply -->
            <view v-if="comment.replyContent" class="comment-reply">
              <text class="comment-reply__label">商家回复：</text>
              <text class="comment-reply__content">{{ comment.replyContent }}</text>
            </view>
            <!-- Like -->
            <view class="comment-like" @tap="handleLikeComment(comment)">
              <RemixIcon name="thumb-up-line" :size="24" :color="comment.isLiked ? '#2563EB' : '#94A3B8'" />
              <text class="comment-like__count">{{ comment.likeCount || '' }}</text>
            </view>
          </view>
        </view>
        <Empty v-else text="暂无评价" />
      </view>

      <!-- Product detail intro -->
      <view class="info-card">
        <text class="card-title">商品详情</text>
        <rich-text v-if="product.description" :nodes="sanitizeHtml(product.description)" class="product-desc" />
        <Empty v-else text="暂无详情" />
      </view>

      <!-- Bottom spacer -->
      <view style="height: 140rpx" />
    </scroll-view>

    <!-- Bottom action bar -->
    <view class="bottom-bar">
      <view class="bottom-bar__icons">
        <view class="bottom-bar__icon-item" @tap="handleService">
          <RemixIcon name="customer-service-2-line" :size="44" color="#334155" />
          <text class="bottom-bar__icon-text">客服</text>
        </view>
        <view class="bottom-bar__icon-item" @tap="toggleCollect">
          <RemixIcon :name="isCollected ? 'heart-fill' : 'heart-line'" :size="44" :color="isCollected ? '#E11148' : '#334155'" />
          <text class="bottom-bar__icon-text" :style="{ color: isCollected ? '#E11148' : '#334155' }">收藏</text>
        </view>
        <view class="bottom-bar__icon-item" @tap="navigateTo('/pages/cart/index')">
          <view class="bottom-bar__cart-wrap">
            <RemixIcon name="shopping-cart-line" :size="44" color="#334155" />
            <view v-if="cartCount > 0" class="bottom-bar__cart-dot">
              <text class="bottom-bar__cart-dot-text">{{ cartCount > 99 ? '99+' : cartCount }}</text>
            </view>
          </view>
          <text class="bottom-bar__icon-text">购物车</text>
        </view>
      </view>
      <view class="bottom-bar__actions">
        <view class="bottom-bar__cart-btn" @tap="openSkuPicker('cart')">
          <text class="bottom-bar__cart-btn-text">加入购物车</text>
        </view>
        <view class="bottom-bar__buy-btn" @tap="openSkuPicker('buy')">
          <text class="bottom-bar__buy-btn-text">立即购买</text>
        </view>
      </view>
    </view>

    <!-- SKU picker popup -->
    <view v-if="showSkuPicker" class="sku-overlay" @tap="showSkuPicker = false">
      <view class="sku-panel" @tap.stop>
        <view class="sku-panel__header">
          <image
            v-if="selectedSku?.image || product.mainImage"
            class="sku-panel__img"
            :src="selectedSku?.image || product.mainImage"
            mode="aspectFill"
          />
          <view class="sku-panel__info">
            <text class="sku-panel__price">&yen;{{ selectedSku?.price || product.minPrice }}</text>
            <text class="sku-panel__stock">库存: {{ selectedSku?.stock || product.totalStock }}</text>
            <text v-if="selectedSku?.specValues" class="sku-panel__spec">已选: {{ selectedSku.specValues }}</text>
          </view>
          <view class="sku-panel__close" @tap="showSkuPicker = false">
            <RemixIcon name="close-line" :size="40" color="#94A3B8" />
          </view>
        </view>

        <scroll-view scroll-y class="sku-panel__body">
          <view v-for="sku in product.skuList" :key="sku.id"
            class="sku-panel__item"
            :class="{
              'sku-panel__item--active': selectedSku?.id === sku.id,
              'sku-panel__item--disabled': sku.stock <= 0
            }"
            @tap="selectSku(sku)"
          >
            <text>{{ sku.specValues || sku.skuName }}</text>
          </view>
        </scroll-view>

        <view class="sku-panel__footer">
          <view class="sku-panel__quantity">
            <text class="sku-panel__quantity-label">数量</text>
            <view class="sku-panel__stepper">
              <view class="sku-panel__step-btn" :class="{ 'sku-panel__step-btn--disabled': quantity <= 1 }" @tap="decrementQty">
                <text>-</text>
              </view>
              <text class="sku-panel__step-num">{{ quantity }}</text>
              <view class="sku-panel__step-btn" @tap="incrementQty">
                <text>+</text>
              </view>
            </view>
          </view>
          <view class="sku-panel__confirm" @tap="handleSkuConfirm">
            <text class="sku-panel__confirm-text">确定</text>
          </view>
        </view>
      </view>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sanitizeHtml } from '@/utils/sanitize'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'
import {
  getSpuDetail,
  getCommentSummary,
  getCommentList,
  likeComment,
  type SpuDetailVO,
  type SkuVO,
  type CommentSummaryVO,
  type ProductCommentVO,
} from '@/api/product'
import { addCartItem } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const product = ref<SpuDetailVO | null>(null)
const selectedSku = ref<SkuVO | null>(null)
const showSkuPicker = ref(false)
const quantity = ref(1)
const skuAction = ref<'cart' | 'buy'>('cart')
const isCollected = ref(false)
const swiperCurrent = ref(0)
const commentSummary = ref<CommentSummaryVO | null>(null)
const comments = ref<ProductCommentVO[]>([])
const activeCommentTag = ref(0)
const loading = ref(true)

const cartStore = useCartStore()
const userStore = useUserStore()

let productId = ''

const productImages = computed(() => {
  if (!product.value) return []
  return product.value.images?.length ? product.value.images : [product.value.mainImage]
})

const displayPrice = computed(() => {
  if (selectedSku.value) return selectedSku.value.price
  return product.value?.minPrice || '0'
})

const originalPriceDisplay = computed(() => {
  if (selectedSku.value && selectedSku.value.originalPrice && selectedSku.value.originalPrice !== selectedSku.value.price) {
    return selectedSku.value.originalPrice
  }
  return ''
})

const couponAmount = computed(() => {
  if (!selectedSku.value || !selectedSku.value.originalPrice) return ''
  const diff = parseFloat(selectedSku.value.originalPrice) - parseFloat(selectedSku.value.price)
  if (diff > 0) return diff.toFixed(0)
  return ''
})

const promoTags = computed(() => {
  const tags: string[] = []
  if (product.value?.brandName) tags.push('品牌直供')
  if (product.value?.totalStock > 100) tags.push('库存充足')
  return tags
})

const serviceItems = ['7天无理由', '极速退款', '正品保障']

const storeAvatar = computed(() => '')

const storeScores = computed(() => ({
  product: '4.8',
  service: '4.9',
  logistics: '4.7',
}))

const goodRateStars = computed(() => {
  if (!commentSummary.value) return 5
  return Math.round(commentSummary.value.goodRate / 20)
})

const cartCount = computed(() => cartStore.totalCount || 0)

onLoad((query) => {
  productId = query?.id || ''
  if (productId) {
    loadProduct(productId)
    loadCommentSummary(productId)
    loadComments(productId)
  }
})

async function loadProduct(id: string) {
  loading.value = true
  try {
    const data = await getSpuDetail(id)
    product.value = {
      ...data,
      minPrice: String(data.minPrice ?? '0'),
      maxPrice: String(data.maxPrice ?? '0'),
      originalPrice: String((data as any).originalPrice ?? ''),
      skuList: (data.skuList || []).map((sku: any) => ({
        ...sku,
        price: String(sku.price ?? '0'),
        originalPrice: String(sku.originalPrice ?? ''),
      })),
    } as any
    if (product.value.skuList?.length === 1) {
      selectedSku.value = product.value.skuList[0]
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function loadCommentSummary(spuId: string) {
  try {
    commentSummary.value = await getCommentSummary(spuId)
  } catch {
    commentSummary.value = null
  }
}

async function loadComments(spuId: string) {
  try {
    const data = await getCommentList({ spuId, page: 1, limit: 3 })
    comments.value = (data.list || []).map((c: any) => ({
      ...c,
      memberNickname: c.memberNickname || c.nickname || '匿名用户',
      memberAvatar: c.memberAvatar || c.avatar || '',
      images: typeof c.images === 'string' && c.images ? c.images.split(',').filter(Boolean) : (c.images || []),
      isLiked: false,
    }))
  } catch {
    comments.value = []
  }
}

function onSwiperChange(e: any) {
  swiperCurrent.value = e.detail.current
}

function selectSku(sku: SkuVO) {
  if (sku.stock <= 0) return
  selectedSku.value = sku
}

function previewImage(idx: number) {
  uni.previewImage({ urls: productImages.value, current: idx })
}

function previewCommentImage(current: string, urls: string[]) {
  uni.previewImage({ urls, current })
}

function goBack() {
  uni.navigateBack({ delta: 1 })
}

function handleShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

function handleCouponTap() {
  uni.showToast({ title: '领券功能开发中', icon: 'none' })
}

function handleService() {
  uni.showToast({ title: '客服功能开发中', icon: 'none' })
}

function toggleCollect() {
  isCollected.value = !isCollected.value
  uni.showToast({ title: isCollected.value ? '已收藏' : '已取消收藏', icon: 'none' })
}

async function handleLikeComment(comment: ProductCommentVO & { isLiked?: boolean }) {
  if (comment.isLiked) return
  try {
    await likeComment(comment.id)
    const idx = comments.value.findIndex((c) => c.id === comment.id)
    if (idx !== -1) {
      comments.value[idx] = {
        ...comments.value[idx],
        likeCount: comments.value[idx].likeCount + 1,
        isLiked: true,
      } as any
    }
  } catch {
    // handled
  }
}

function checkLogin(): boolean {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return false
  }
  return true
}

function openSkuPicker(action: 'cart' | 'buy') {
  if (!checkLogin()) return
  skuAction.value = action
  if (product.value?.skuList?.length === 1) {
    selectedSku.value = product.value.skuList[0]
  }
  showSkuPicker.value = true
}

function decrementQty() {
  if (quantity.value > 1) quantity.value--
}

function incrementQty() {
  const max = selectedSku.value?.stock || product.value?.totalStock || 999
  if (quantity.value < max) quantity.value++
}

async function doAddCart() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  try {
    await addCartItem({ spuId: product.value!.id, skuId: selectedSku.value.id, quantity: quantity.value })
    cartStore.fetchCart()
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch {
    // handled
  }
}

function doBuyNow() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  const items = JSON.stringify([{ skuId: selectedSku.value.id, quantity: quantity.value, spuId: product.value!.id }])
  uni.navigateTo({ url: `/pages/order/confirm?items=${encodeURIComponent(items)}` })
}

function handleSkuConfirm() {
  if (!selectedSku.value) {
    uni.showToast({ title: '请选择规格', icon: 'none' })
    return
  }
  showSkuPicker.value = false
  if (skuAction.value === 'cart') doAddCart()
  else doBuyNow()
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
</script>

<style lang="scss" scoped>
.page-product {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

.page-product__loading,
.page-product__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.page-product__loading-text {
  font-size: 28rpx;
  color: #94A3B8;
}

.page-product__back {
  margin-top: 32rpx;
  padding: 16rpx 48rpx;
  background: #2563EB;
  border-radius: 40rpx;
}

.page-product__back-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* Nav bar */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  height: 88rpx;
  background: #FFFFFF;
  position: relative;
  z-index: 10;
}

.nav-bar__left {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-bar__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
}

.nav-bar__right {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Scroll area */
.product-scroll {
  flex: 1;
  height: 0;
}

/* Swiper */
.swiper-wrap {
  position: relative;
}

.product-swiper {
  height: 750rpx;
}

.product-swiper__img {
  width: 100%;
  height: 750rpx;
}

.swiper-indicator {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
  min-width: 64rpx;
  text-align: center;
}

.swiper-indicator__text {
  font-size: 22rpx;
  color: #FFFFFF;
}

/* Info card */
.info-card {
  background: #FFFFFF;
  margin-top: 16rpx;
  padding: 32rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  display: block;
  margin-bottom: 20rpx;
}

/* Price row */
.price-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.price-row__symbol {
  font-size: 28rpx;
  color: #E11148;
  font-weight: 700;
}

.price-row__value {
  font-size: 48rpx;
  color: #E11148;
  font-weight: 700;
  line-height: 1;
}

.price-row__range {
  font-size: 28rpx;
  color: #E11148;
  font-weight: 700;
}

.price-row__original {
  font-size: 24rpx;
  color: #94A3B8;
  text-decoration: line-through;
  margin-left: 16rpx;
}

/* Coupon tag */
.coupon-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: #FEF2F2;
  border: 1rpx solid #E11148;
  border-radius: 20rpx;
  padding: 6rpx 16rpx;
  margin-top: 16rpx;
}

.coupon-tag__text {
  font-size: 22rpx;
  color: #E11148;
  font-weight: 500;
}

/* Title */
.product-title {
  font-size: 32rpx;
  color: #1E293B;
  font-weight: 600;
  display: block;
  margin-top: 20rpx;
  line-height: 1.5;
}

.product-subtitle {
  font-size: 26rpx;
  color: #64748B;
  display: block;
  margin-top: 8rpx;
  line-height: 1.4;
}

/* Promo tags */
.promo-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.promo-tag {
  background: #EFF6FF;
  border: 1rpx solid #DBEAFE;
  border-radius: 6rpx;
  padding: 4rpx 12rpx;
}

.promo-tag__text {
  font-size: 22rpx;
  color: #2563EB;
}

/* Service row */
.service-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F0F0F0;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.service-item__text {
  font-size: 22rpx;
  color: #16A34A;
}

/* Sales text */
.sales-text {
  font-size: 24rpx;
  color: #94A3B8;
  display: block;
  margin-top: 16rpx;
}

/* Spec row */
.spec-row {
  display: flex;
  align-items: center;
}

.spec-row__label {
  font-size: 28rpx;
  color: #64748B;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.spec-row__value {
  font-size: 28rpx;
  color: #1E293B;
  flex: 1;
}

/* Store card */
.store-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.store-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.store-avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F3F4F6;
}

.store-info {
  flex: 1;
  min-width: 0;
}

.store-name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.store-name {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.store-scores {
  display: flex;
  gap: 24rpx;
  margin-top: 8rpx;
}

.store-score {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.store-score__label {
  font-size: 22rpx;
  color: #64748B;
}

.store-score__value {
  font-size: 22rpx;
  color: #F97316;
  font-weight: 500;
}

.store-enter {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border: 1rpx solid #2563EB;
  border-radius: 24rpx;
}

.store-enter__text {
  font-size: 22rpx;
  color: #2563EB;
  font-weight: 500;
}

/* Params */
.param-row {
  display: flex;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.param-row:last-child {
  border-bottom: none;
}

.param-row__label {
  font-size: 26rpx;
  color: #64748B;
  width: 160rpx;
  flex-shrink: 0;
}

.param-row__value {
  font-size: 26rpx;
  color: #1E293B;
  flex: 1;
}

/* Delivery */
.delivery-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.delivery-info {
  display: flex;
  flex-direction: column;
}

.delivery-info__text {
  font-size: 26rpx;
  color: #1E293B;
}

.delivery-info__sub {
  font-size: 22rpx;
  color: #64748B;
  margin-top: 4rpx;
}

/* Comment area */
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.comment-header__left {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.comment-count {
  font-size: 24rpx;
  color: #64748B;
}

.comment-header__right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.comment-rate {
  display: flex;
  gap: 2rpx;
}

.comment-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.comment-more__text {
  font-size: 24rpx;
  color: #64748B;
}

/* Comment tags */
.comment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.comment-tag {
  padding: 8rpx 20rpx;
  background: #F3F4F6;
  border-radius: 20rpx;
}

.comment-tag--active {
  background: #EFF6FF;
  border: 1rpx solid #2563EB;
}

.comment-tag__text {
  font-size: 22rpx;
  color: #334155;
}

.comment-tag--active .comment-tag__text {
  color: #2563EB;
}

/* Comment list */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.comment-item {
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-item__header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.comment-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #DBEAFE;
}

.comment-avatar__text {
  font-size: 24rpx;
  color: #2563EB;
  font-weight: 600;
}

.comment-user-info {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.comment-nickname {
  font-size: 24rpx;
  color: #1E293B;
  font-weight: 500;
}

.comment-time {
  font-size: 20rpx;
  color: #94A3B8;
}

.comment-content {
  font-size: 26rpx;
  color: #334155;
  line-height: 1.6;
  display: block;
  margin-bottom: 12rpx;
}

.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.comment-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
}

.comment-reply {
  background: #F9FAFB;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 12rpx;
}

.comment-reply__label {
  font-size: 24rpx;
  color: #2563EB;
  font-weight: 500;
}

.comment-reply__content {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.5;
}

.comment-like {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6rpx;
}

.comment-like__count {
  font-size: 22rpx;
  color: #94A3B8;
}

/* Product desc */
.product-desc {
  font-size: 28rpx;
  color: #334155;
  line-height: 1.8;
}

/* Bottom bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.bottom-bar__icons {
  display: flex;
  gap: 24rpx;
}

.bottom-bar__icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rpx;
  position: relative;
}

.bottom-bar__icon-text {
  font-size: 18rpx;
  color: #334155;
}

.bottom-bar__cart-wrap {
  position: relative;
}

.bottom-bar__cart-dot {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  background: #E11148;
  border-radius: 16rpx;
  min-width: 28rpx;
  height: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
}

.bottom-bar__cart-dot-text {
  font-size: 18rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.bottom-bar__actions {
  flex: 1;
  display: flex;
  margin-left: 24rpx;
  gap: 0;
  overflow: hidden;
  border-radius: 40rpx;
}

.bottom-bar__cart-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F97316;
}

.bottom-bar__cart-btn-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.bottom-bar__buy-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E11148;
}

.bottom-bar__buy-btn-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* SKU overlay */
.sku-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.sku-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.sku-panel__header {
  display: flex;
  padding: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
  position: relative;
}

.sku-panel__img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.sku-panel__info {
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.sku-panel__price {
  font-size: 40rpx;
  color: #E11148;
  font-weight: 700;
}

.sku-panel__stock {
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 8rpx;
}

.sku-panel__spec {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 4rpx;
}

.sku-panel__close {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sku-panel__body {
  flex: 1;
  padding: 24rpx 32rpx;
  max-height: 400rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  align-content: flex-start;
}

.sku-panel__item {
  padding: 14rpx 28rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #334155;
  border: 2rpx solid transparent;
}

.sku-panel__item--active {
  background: #EFF6FF;
  color: #2563EB;
  border-color: #2563EB;
}

.sku-panel__item--disabled {
  color: #CBD5E1;
  text-decoration: line-through;
}

.sku-panel__footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F0F0F0;
}

.sku-panel__quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.sku-panel__quantity-label {
  font-size: 28rpx;
  color: #1E293B;
}

.sku-panel__stepper {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.sku-panel__step-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #E2E8F0;
  border-radius: 12rpx;
  font-size: 32rpx;
  color: #334155;
}

.sku-panel__step-btn--disabled {
  color: #CBD5E1;
  border-color: #F0F0F0;
}

.sku-panel__step-num {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 500;
  min-width: 60rpx;
  text-align: center;
}

.sku-panel__confirm {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2563EB;
  border-radius: 44rpx;
}

.sku-panel__confirm-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
