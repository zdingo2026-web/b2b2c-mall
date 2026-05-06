<template>
  <view class="page-order-confirm">
    <!-- Address section -->
    <view class="confirm-section" @tap="navigateTo('/pages/order/address')">
      <view v-if="selectedAddress" class="address-card">
        <view class="address-card__left">
          <RemixIcon name="map-pin-line" :size="40" color="#2563EB" />
        </view>
        <view class="address-card__detail">
          <view class="address-card__row">
            <text class="address-card__name">{{ selectedAddress.receiverName }}</text>
            <text class="address-card__phone">{{ selectedAddress.receiverPhone }}</text>
          </view>
          <text class="address-card__addr">
            {{ selectedAddress.fullAddress || `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detailAddress}` }}
          </text>
        </view>
        <RemixIcon name="arrow-right-s-line" :size="36" color="#94A3B8" />
      </view>
      <view v-else class="address-empty">
        <RemixIcon name="add-circle-line" :size="32" color="#94A3B8" />
        <text class="address-empty__text">添加收货地址</text>
      </view>
      <!-- Decorative bottom border -->
      <view class="address-border" />
    </view>

    <!-- Store + Products -->
    <view v-for="group in productGroups" :key="group.tenantId" class="confirm-section">
      <view class="store-header">
        <RemixIcon name="store-2-line" :size="32" color="#2563EB" />
        <text class="store-header__name">{{ group.tenantName || '店铺' }}</text>
      </view>
      <view v-for="item in group.items" :key="item.skuId" class="confirm-product">
        <image
          class="confirm-product__img"
          :src="item.productImage || '/static/empty.png'"
          mode="aspectFill"
        />
        <view class="confirm-product__info">
          <text class="confirm-product__name">{{ item.productName }}</text>
          <text v-if="item.specValues" class="confirm-product__spec">{{ item.specValues }}</text>
          <view class="confirm-product__bottom">
            <text class="confirm-product__price">&yen;{{ item.price }}</text>
            <text class="confirm-product__qty">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Coupon row -->
    <view class="confirm-section confirm-row-item" @tap="navigateTo('/pages/mine/index?tab=coupon')">
      <view class="row-item__left">
        <RemixIcon name="coupon-3-line" :size="36" color="#E11148" />
        <text class="row-item__label">优惠券</text>
        <view v-if="bestCoupon" class="coupon-tag">
          <text class="coupon-tag__text">{{ bestCoupon.couponName }}</text>
        </view>
      </view>
      <view class="row-item__right">
        <text v-if="availableCouponCount > 0" class="row-item__extra">{{ availableCouponCount }}张可用</text>
        <text v-else class="row-item__extra row-item__extra--dim">暂无可用</text>
        <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
      </view>
    </view>

    <!-- Delivery method -->
    <view class="confirm-section confirm-row-item">
      <view class="row-item__left">
        <RemixIcon name="truck-line" :size="36" color="#2563EB" />
        <text class="row-item__label">配送方式</text>
      </view>
      <view class="row-item__right">
        <text class="row-item__value">快递配送</text>
        <text v-if="freightAmount !== '0.00'" class="row-item__freight">&yen;{{ freightAmount }}</text>
        <text v-else class="row-item__freight row-item__freight--free">免运费</text>
      </view>
    </view>

    <!-- Invoice info -->
    <view class="confirm-section confirm-row-item" @tap="handleInvoiceTap">
      <view class="row-item__left">
        <RemixIcon name="file-text-line" :size="36" color="#16A34A" />
        <text class="row-item__label">发票</text>
      </view>
      <view class="row-item__right">
        <text class="row-item__value">{{ invoiceType === 0 ? '不开发票' : invoiceType === 1 ? '个人' : '企业' }}</text>
        <RemixIcon name="arrow-right-s-line" :size="32" color="#94A3B8" />
      </view>
    </view>

    <!-- Order remarks -->
    <view class="confirm-section">
      <text class="confirm-section__title">订单备注</text>
      <textarea
        v-model="orderNote"
        placeholder="选填：对本次交易的说明"
        placeholder-class="confirm-note__placeholder"
        class="confirm-note"
        :maxlength="200"
      />
    </view>

    <!-- Fee breakdown -->
    <view class="confirm-section">
      <view class="confirm-row">
        <text class="confirm-row__label">商品金额</text>
        <text class="confirm-row__value">&yen;{{ totalAmount }}</text>
      </view>
      <view class="confirm-row">
        <text class="confirm-row__label">运费</text>
        <text class="confirm-row__value">+&yen;{{ freightAmount }}</text>
      </view>
      <view v-if="discountAmount !== '0.00'" class="confirm-row">
        <text class="confirm-row__label">优惠</text>
        <text class="confirm-row__value confirm-row__value--discount">-&yen;{{ discountAmount }}</text>
      </view>
      <view class="confirm-row confirm-row--total">
        <text class="confirm-row__label">应付总额</text>
        <text class="confirm-row__value confirm-row__value--accent">&yen;{{ payAmount }}</text>
      </view>
    </view>

    <!-- Bottom bar -->
    <view class="confirm-bottom">
      <view class="confirm-bottom__left">
        <text class="confirm-bottom__label">合计:</text>
        <text class="confirm-bottom__total">&yen;{{ payAmount }}</text>
      </view>
      <view
        class="confirm-bottom__submit"
        :class="{ 'confirm-bottom__submit--disabled': submitting }"
        @tap="handleSubmitOrder"
      >
        <text class="confirm-bottom__submit-text">提交订单</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import RemixIcon from '@/components/RemixIcon.vue'
import { getAddressList, type MemberAddressVO } from '@/api/address'
import { getSpuDetail, type SpuDetailVO, type SkuVO } from '@/api/product'
import { createOrder } from '@/api/order'
import { getCouponList, type CouponVO } from '@/api/coupon'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const selectedAddress = ref<MemberAddressVO | null>(null)
const addresses = ref<MemberAddressVO[]>([])
const orderNote = ref('')
const submitting = ref(false)
const invoiceType = ref(0) // 0: no invoice, 1: personal, 2: enterprise
const invoiceTitle = ref('')
const selectedCouponId = ref<string | null>(null)
const coupons = ref<CouponVO[]>([])

interface OrderItemDisplay {
  spuId: string
  skuId: string
  tenantId: string
  tenantName: string
  productName: string
  specValues: string
  productImage: string
  price: string
  quantity: number
}

const orderItems = ref<OrderItemDisplay[]>([])
const freightAmount = ref('0.00')

let orderItemsRaw: { skuId: string; quantity: number; spuId: string }[] = []

// Group products by tenant
const productGroups = computed(() => {
  const map = new Map<string, { tenantId: string; tenantName: string; items: OrderItemDisplay[] }>()
  for (const item of orderItems.value) {
    const tid = item.tenantId || '0'
    if (!map.has(tid)) {
      map.set(tid, { tenantId: tid, tenantName: item.tenantName || '店铺', items: [] })
    }
    map.get(tid)!.items.push(item)
  }
  return Array.from(map.values())
})

// Available coupons for this order
const availableCouponCount = computed(() => {
  return coupons.value.filter((c) => c.status === 0).length
})

// Best coupon display
const bestCoupon = computed<CouponVO | null>(() => {
  const available = coupons.value.filter((c) => c.status === 0)
  if (available.length === 0) return null
  // Pick the one with the greatest discount
  return available.reduce((best, c) => {
    const bestVal = parseFloat(best.couponValue)
    const cVal = parseFloat(c.couponValue)
    return cVal > bestVal ? c : best
  })
})

// Totals
const totalAmount = computed(() =>
  orderItems.value.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2),
)

const discountAmount = computed(() => {
  if (selectedCouponId.value) {
    const coupon = coupons.value.find((c) => c.id === selectedCouponId.value)
    if (coupon) return coupon.couponValue
  }
  return '0.00'
})

const payAmount = computed(() => {
  const total = Number(totalAmount.value) + Number(freightAmount.value) - Number(discountAmount.value)
  return Math.max(0, total).toFixed(2)
})

onLoad(async (query) => {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }

  // Load addresses
  try {
    addresses.value = await getAddressList()
    selectedAddress.value = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0] || null
  } catch {
    /* no addresses */
  }

  // Load coupons
  try {
    const data = await getCouponList({ status: 0 })
    coupons.value = data.list || []
  } catch {
    coupons.value = []
  }

  // Parse order items from query
  if (query?.items) {
    try {
      orderItemsRaw = JSON.parse(decodeURIComponent(query.items))
    } catch {
      /* invalid */
    }
  }

  // Load product details for each item
  const spuCache = new Map<string, SpuDetailVO>()
  const displayItems: OrderItemDisplay[] = []

  for (const item of orderItemsRaw) {
    let spu = spuCache.get(item.spuId)
    if (!spu) {
      try {
        spu = await getSpuDetail(item.spuId)
        spuCache.set(item.spuId, spu)
      } catch {
        continue
      }
    }
    const sku = spu.skuList?.find((s: SkuVO) => s.id === item.skuId)
    displayItems.push({
      spuId: item.spuId,
      skuId: item.skuId,
      tenantId: spu.tenantId || '0',
      tenantName: spu.tenantName || '',
      productName: spu.productName,
      specValues: sku?.specValues || '',
      productImage: sku?.image || spu.mainImage,
      price: sku?.price || spu.minPrice,
      quantity: item.quantity,
    })
  }

  orderItems.value = displayItems
})

// Re-select address when returning from address page
onShow(() => {
  if (addresses.value.length > 0 && !selectedAddress.value) {
    selectedAddress.value = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0] || null
  }
})

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

function handleInvoiceTap() {
  const types = ['不开发票', '个人', '企业']
  uni.showActionSheet({
    itemList: types,
    success: (res) => {
      invoiceType.value = res.tapIndex
      if (res.tapIndex === 2) {
        // Enterprise invoice: ask for title
        uni.showModal({
          title: '发票抬头',
          editable: true,
          placeholderText: '请输入企业名称',
          success: (modalRes) => {
            if (modalRes.confirm && modalRes.content) {
              invoiceTitle.value = modalRes.content.trim()
            }
          },
        })
      } else {
        invoiceTitle.value = ''
      }
    },
  })
}

async function handleSubmitOrder() {
  if (!selectedAddress.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }
  if (orderItems.value.length === 0) {
    uni.showToast({ title: '暂无商品', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true

  try {
    const order = await createOrder({
      addressId: selectedAddress.value.id!,
      items: orderItemsRaw.map((i) => ({ skuId: i.skuId, quantity: i.quantity })),
      remark: orderNote.value || undefined,
      couponId: selectedCouponId.value || undefined,
      invoiceType: invoiceType.value || undefined,
      invoiceTitle: invoiceTitle.value || undefined,
    })

    uni.showToast({ title: '订单提交成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/order/payment?orderNo=${order.orderNo}` })
    }, 1500)
  } catch {
    // handled by request util
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-order-confirm {
  min-height: 100vh;
  background: #F9FAFB;
  padding-bottom: 140rpx;
}

.confirm-section {
  background: #FFFFFF;
  margin: 20rpx 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
}

.confirm-section__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
  display: block;
  margin-bottom: 20rpx;
}

/* Address card */
.address-card {
  display: flex;
  align-items: center;
}

.address-card__left {
  margin-right: 16rpx;
  flex-shrink: 0;
}

.address-card__detail {
  flex: 1;
  min-width: 0;
}

.address-card__row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.address-card__name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.address-card__phone {
  font-size: 28rpx;
  color: #64748B;
}

.address-card__addr {
  font-size: 26rpx;
  color: #64748B;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.address-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  gap: 12rpx;
}

.address-empty__text {
  font-size: 28rpx;
  color: #94A3B8;
}

/* Decorative address bottom border */
.address-border {
  height: 6rpx;
  margin-top: 24rpx;
  background: repeating-linear-gradient(
    135deg,
    #2563EB 0,
    #2563EB 10rpx,
    #E11148 10rpx,
    #E11148 20rpx,
    #F97316 20rpx,
    #F97316 30rpx,
    #16A34A 30rpx,
    #16A34A 40rpx
  );
  border-radius: 3rpx;
}

/* Store header */
.store-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F5F5F5;
}

.store-header__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

/* Product item */
.confirm-product {
  display: flex;
  margin-bottom: 24rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.confirm-product__img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  background: #F3F4F6;
}

.confirm-product__info {
  flex: 1;
  min-width: 0;
}

.confirm-product__name {
  font-size: 26rpx;
  color: #1E293B;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.confirm-product__spec {
  font-size: 22rpx;
  color: #94A3B8;
  margin-top: 8rpx;
  display: block;
}

.confirm-product__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.confirm-product__price {
  font-size: 30rpx;
  color: #E11148;
  font-weight: 600;
}

.confirm-product__qty {
  font-size: 24rpx;
  color: #94A3B8;
}

/* Row items (coupon, delivery, invoice) */
.confirm-row-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.row-item__left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}

.row-item__label {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  flex-shrink: 0;
}

.row-item__right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.row-item__value {
  font-size: 26rpx;
  color: #64748B;
}

.row-item__extra {
  font-size: 26rpx;
  color: #E11148;
  font-weight: 500;

  &--dim {
    color: #94A3B8;
    font-weight: 400;
  }
}

.row-item__freight {
  font-size: 26rpx;
  color: #1E293B;
  font-weight: 500;
  margin-left: 8rpx;

  &--free {
    color: #16A34A;
  }
}

/* Coupon tag */
.coupon-tag {
  display: inline-flex;
  align-items: center;
  background: #FEF2F2;
  border: 1rpx solid #FECACA;
  border-radius: 6rpx;
  padding: 4rpx 12rpx;
  max-width: 240rpx;
  overflow: hidden;
}

.coupon-tag__text {
  font-size: 22rpx;
  color: #E11148;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Order note */
.confirm-note {
  width: 100%;
  height: 160rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #1E293B;
  box-sizing: border-box;
  line-height: 1.5;
}

.confirm-note__placeholder {
  color: #94A3B8;
  font-size: 26rpx;
}

/* Fee breakdown rows */
.confirm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.confirm-row__label {
  font-size: 26rpx;
  color: #64748B;
}

.confirm-row__value {
  font-size: 26rpx;
  color: #1E293B;

  &--discount {
    color: #E11148;
  }

  &--accent {
    color: #E11148;
    font-weight: 700;
    font-size: 32rpx;
  }
}

.confirm-row--total {
  border-top: 1rpx solid #F0F0F0;
  padding-top: 20rpx;
  margin-top: 8rpx;
}

/* Bottom bar */
.confirm-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.confirm-bottom__left {
  display: flex;
  align-items: baseline;
}

.confirm-bottom__label {
  font-size: 26rpx;
  color: #64748B;
}

.confirm-bottom__total {
  font-size: 36rpx;
  color: #E11148;
  font-weight: 700;
}

.confirm-bottom__submit {
  background: #2563EB;
  padding: 18rpx 64rpx;
  border-radius: 40rpx;

  &--disabled {
    background: #93C5FD;
  }
}

.confirm-bottom__submit-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
