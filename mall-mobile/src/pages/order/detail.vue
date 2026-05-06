<template>
  <view class="page-order-detail" v-if="order">
    <!-- Status area -->
    <view class="detail-status" :style="{ background: statusGradient(order.orderStatus) }">
      <view class="detail-status__content">
        <RemixIcon :name="statusIcon(order.orderStatus)" :size="48" color="#fff" />
        <view class="detail-status__info">
          <text class="detail-status__text">{{ ORDER_STATUS_MAP[order.orderStatus] || '未知' }}</text>
          <text v-if="order.orderStatus === 0 && order.expireTime" class="detail-status__sub">
            请在{{ order.expireTime }}前完成支付
          </text>
        </view>
      </view>
    </view>

    <!-- Logistics card (status >= 2 with deliveryNo) -->
    <view v-if="order.orderStatus >= 2 && logistics" class="detail-logistics" @tap="navigateToLogistics">
      <view class="detail-logistics__header">
        <RemixIcon name="truck-line" :size="20" color="#2563EB" />
        <text class="detail-logistics__company">{{ logistics.deliveryCompany }}</text>
        <text class="detail-logistics__no">{{ logistics.deliveryNo }}</text>
        <RemixIcon name="arrow-right-s-line" :size="18" color="#9CA3AF" class="detail-logistics__arrow" />
      </view>
      <view v-if="logistics.traces && logistics.traces.length > 0" class="detail-logistics__trace">
        <text class="detail-logistics__trace-dot" />
        <text class="detail-logistics__trace-text">{{ logistics.traces[0].description }}</text>
      </view>
      <view v-else class="detail-logistics__trace">
        <text class="detail-logistics__trace-dot" />
        <text class="detail-logistics__trace-text">暂无物流信息</text>
      </view>
    </view>

    <!-- Address -->
    <view v-if="order.address" class="detail-card">
      <view class="detail-address">
        <view class="detail-address__icon">
          <RemixIcon name="map-pin-2-fill" :size="22" color="#E11148" />
        </view>
        <view class="detail-address__info">
          <view class="detail-address__row">
            <text class="detail-address__name">{{ order.address.receiverName }}</text>
            <text class="detail-address__phone">{{ order.address.receiverPhone }}</text>
            <view v-if="order.address.isDefault" class="detail-address__tag">
              <text class="detail-address__tag-text">默认</text>
            </view>
          </view>
          <text class="detail-address__addr">{{ order.address.fullAddress }}</text>
        </view>
      </view>
    </view>

    <!-- Store info -->
    <view class="detail-card">
      <view class="detail-store">
        <RemixIcon name="store-3-line" :size="20" color="#2563EB" />
        <text class="detail-store__name">{{ order.tenantName || '官方旗舰店' }}</text>
        <view class="detail-store__btn" @tap="navigateToStore">
          <text class="detail-store__btn-text">进店逛逛</text>
          <RemixIcon name="arrow-right-s-line" :size="14" color="#2563EB" />
        </view>
      </view>
    </view>

    <!-- Product items -->
    <view class="detail-card">
      <view v-for="item in order.items" :key="item.id" class="detail-item">
        <image
          class="detail-item__img"
          :src="item.productImage"
          mode="aspectFill"
          @tap="navigateTo(`/pages/product/index?id=${item.spuId}`)"
        />
        <view class="detail-item__info">
          <text class="detail-item__name">{{ item.productName }}</text>
          <text v-if="item.specValues" class="detail-item__spec">{{ item.specValues }}</text>
          <view class="detail-item__bottom">
            <text class="detail-item__price">&yen;{{ item.price }}</text>
            <text class="detail-item__qty">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Order info -->
    <view class="detail-card">
      <text class="detail-card__title">订单信息</text>
      <view class="detail-info-row">
        <text class="detail-info-row__label">订单编号</text>
        <view class="detail-info-row__right">
          <text class="detail-info-row__value">{{ order.orderNo }}</text>
          <view class="detail-info-row__copy" @tap="copyText(order.orderNo)">
            <RemixIcon name="file-copy-line" :size="14" color="#2563EB" />
            <text class="detail-info-row__copy-text">复制</text>
          </view>
        </view>
      </view>
      <view class="detail-info-row">
        <text class="detail-info-row__label">下单时间</text>
        <text class="detail-info-row__value">{{ order.createTime }}</text>
      </view>
      <view v-if="order.payTime" class="detail-info-row">
        <text class="detail-info-row__label">支付时间</text>
        <text class="detail-info-row__value">{{ order.payTime }}</text>
      </view>
      <view v-if="order.payType" class="detail-info-row">
        <text class="detail-info-row__label">支付方式</text>
        <text class="detail-info-row__value">{{ payTypeMap[order.payType] || '其他' }}</text>
      </view>
      <view v-if="order.invoiceType !== undefined && order.invoiceType !== null" class="detail-info-row">
        <text class="detail-info-row__label">发票信息</text>
        <text class="detail-info-row__value">
          {{ invoiceTypeMap[order.invoiceType] || '无' }}
          <text v-if="order.invoiceTitle && order.invoiceType !== 0">（{{ order.invoiceTitle }}）</text>
        </text>
      </view>
      <view v-if="order.remark" class="detail-info-row">
        <text class="detail-info-row__label">订单备注</text>
        <text class="detail-info-row__value">{{ order.remark }}</text>
      </view>
    </view>

    <!-- Fee breakdown -->
    <view class="detail-card">
      <text class="detail-card__title">费用明细</text>
      <view class="detail-info-row">
        <text class="detail-info-row__label">商品总额</text>
        <text class="detail-info-row__value">&yen;{{ order.totalAmount }}</text>
      </view>
      <view class="detail-info-row">
        <text class="detail-info-row__label">运费</text>
        <text class="detail-info-row__value">&yen;{{ order.freightAmount }}</text>
      </view>
      <view v-if="order.discountAmount && order.discountAmount !== '0.00'" class="detail-info-row">
        <text class="detail-info-row__label">优惠</text>
        <text class="detail-info-row__value detail-info-row__value--red">-&yen;{{ order.discountAmount }}</text>
      </view>
      <view class="detail-info-row detail-info-row--total">
        <text class="detail-info-row__label">实付金额</text>
        <text class="detail-info-row__value detail-info-row__value--accent">&yen;{{ order.payAmount }}</text>
      </view>
    </view>

    <!-- Empty state -->
    <Empty v-if="!order" text="订单不存在" />

    <!-- Bottom action bar -->
    <view class="detail-bottom">
      <!-- Status 0: 待付款 -->
      <template v-if="order.orderStatus === 0">
        <view class="detail-bottom__btn" @tap="handleCancel">取消订单</view>
        <view class="detail-bottom__btn detail-bottom__btn--primary" @tap="navigateTo(`/pages/order/payment?orderNo=${order.orderNo}`)">去支付</view>
      </template>
      <!-- Status 1: 待发货 -->
      <template v-if="order.orderStatus === 1">
        <view class="detail-bottom__btn detail-bottom__btn--primary" @tap="handleRemindShip">提醒发货</view>
      </template>
      <!-- Status 2: 待收货 -->
      <template v-if="order.orderStatus === 2">
        <view class="detail-bottom__btn" @tap="navigateToLogistics">查看物流</view>
        <view class="detail-bottom__btn detail-bottom__btn--primary" @tap="handleConfirm">确认收货</view>
      </template>
      <!-- Status 3: 已完成 -->
      <template v-if="order.orderStatus === 3">
        <view class="detail-bottom__btn" @tap="handleRebuy">再次购买</view>
        <view v-if="!order.isReviewed" class="detail-bottom__btn detail-bottom__btn--primary" @tap="navigateTo(`/pages/order/review?orderNo=${order.orderNo}`)">去评价</view>
      </template>
      <!-- Status 7: 待评价 -->
      <template v-if="order.orderStatus === 7">
        <view class="detail-bottom__btn" @tap="handleRebuy">再次购买</view>
        <view class="detail-bottom__btn detail-bottom__btn--primary" @tap="navigateTo(`/pages/order/review?orderNo=${order.orderNo}`)">去评价</view>
      </template>
    </view>
  </view>

  <!-- Loading / empty fallback -->
  <view v-else class="page-order-detail page-order-detail--empty">
    <Empty text="订单不存在" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getOrderDetail,
  cancelOrder,
  confirmReceive,
  getOrderLogistics,
  ORDER_STATUS_MAP,
  ORDER_STATUS_COLOR,
  type OrderMainVO,
  type OrderLogisticsVO,
} from '@/api/order'
import RemixIcon from '@/components/RemixIcon.vue'
import Empty from '@/components/Empty.vue'

const order = ref<OrderMainVO | null>(null)
const logistics = ref<OrderLogisticsVO | null>(null)
const orderNo = ref('')

/** Pay type label map */
const payTypeMap: Record<number, string> = {
  1: '微信支付',
  2: '支付宝',
  3: '余额支付',
}

/** Invoice type label map */
const invoiceTypeMap: Record<number, string> = {
  0: '不开发票',
  1: '电子发票',
  2: '纸质发票',
}

/** Gradient per status */
const statusGradients: Record<number, string> = {
  0: 'linear-gradient(135deg, #F97316, #EA580C)',
  1: 'linear-gradient(135deg, #3B82F6, #2563EB)',
  2: 'linear-gradient(135deg, #8B5CF6, #7C3AED)',
  3: 'linear-gradient(135deg, #10B981, #059669)',
  4: 'linear-gradient(135deg, #9CA3AF, #6B7280)',
  7: 'linear-gradient(135deg, #3B82F6, #2563EB)',
}

function statusGradient(status: number): string {
  return statusGradients[status] || statusGradients[4]
}

/** Icon per status */
const statusIcons: Record<number, string> = {
  0: 'time-line',
  1: 'archive-line',
  2: 'truck-line',
  3: 'checkbox-circle-line',
  4: 'close-circle-line',
  7: 'star-line',
}

function statusIcon(status: number): string {
  return statusIcons[status] || 'question-line'
}

/** Fetch order detail and logistics */
async function fetchOrder(): Promise<void> {
  if (!orderNo.value) return
  try {
    order.value = await getOrderDetail(orderNo.value)
    // Fetch logistics for status >= 2
    if (order.value.orderStatus >= 2 && order.value.deliveryNo) {
      try {
        logistics.value = await getOrderLogistics(orderNo.value)
      } catch {
        logistics.value = null
      }
    }
  } catch {
    order.value = null
    uni.showToast({ title: '订单不存在', icon: 'none' })
  }
}

onLoad((options) => {
  orderNo.value = options?.orderNo || ''
  fetchOrder()
})

/** Copy text to clipboard */
function copyText(text: string): void {
  uni.setClipboardData({ data: text, showToast: true })
}

/** Navigate helper */
function navigateTo(url: string): void {
  uni.navigateTo({ url })
}

/** Navigate to store page */
function navigateToStore(): void {
  if (order.value?.tenantId) {
    uni.navigateTo({ url: `/pages/store/index?tenantId=${order.value.tenantId}` })
  }
}

/** Navigate to logistics detail */
function navigateToLogistics(): void {
  if (orderNo.value) {
    uni.navigateTo({ url: `/pages/order/logistics?orderNo=${orderNo.value}` })
  }
}

/** Cancel order */
async function handleCancel(): Promise<void> {
  const res = await uni.showModal({ title: '提示', content: '确定取消此订单？' })
  if (!res.confirm) return
  try {
    await cancelOrder(orderNo.value)
    uni.showToast({ title: '已取消', icon: 'success' })
    await fetchOrder()
  } catch {
    // Error handled by request util
  }
}

/** Confirm receipt */
async function handleConfirm(): Promise<void> {
  const res = await uni.showModal({ title: '提示', content: '确认已收到商品？' })
  if (!res.confirm) return
  const firstItem = order.value?.items?.[0]
  if (!firstItem) return
  try {
    await confirmReceive(firstItem.id)
    uni.showToast({ title: '已确认收货', icon: 'success' })
    await fetchOrder()
  } catch {
    // Error handled by request util
  }
}

/** Remind seller to ship */
function handleRemindShip(): void {
  uni.showToast({ title: '已提醒卖家发货', icon: 'success' })
}

/** Rebuy - navigate back to product */
function handleRebuy(): void {
  const firstItem = order.value?.items?.[0]
  if (firstItem) {
    uni.navigateTo({ url: `/pages/product/index?id=${firstItem.spuId}` })
  }
}
</script>

<style lang="scss" scoped>
.page-order-detail {
  min-height: 100vh;
  background: #F9FAFB;
  padding-bottom: 140rpx;
}

.page-order-detail--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 0;
}

/* ---------- Status area ---------- */
.detail-status {
  padding: 48rpx 32rpx 56rpx;
  border-radius: 0 0 32rpx 32rpx;

  &__content {
    display: flex;
    align-items: center;
    gap: 24rpx;
  }

  &__info {
    flex: 1;
  }

  &__text {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
    display: block;
  }

  &__sub {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
    display: block;
    margin-top: 8rpx;
  }
}

/* ---------- Logistics card ---------- */
.detail-logistics {
  margin: -24rpx 24rpx 0;
  background: #EFF6FF;
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
  position: relative;
  z-index: 2;

  &__header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 16rpx;
  }

  &__company {
    font-size: 26rpx;
    color: #1E40AF;
    font-weight: 600;
  }

  &__no {
    font-size: 24rpx;
    color: #3B82F6;
    flex: 1;
  }

  &__arrow {
    flex-shrink: 0;
  }

  &__trace {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;
  }

  &__trace-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    background: #3B82F6;
    flex-shrink: 0;
    margin-top: 10rpx;
  }

  &__trace-text {
    font-size: 24rpx;
    color: #6B7280;
    line-height: 1.5;
    flex: 1;
  }
}

/* ---------- Card ---------- */
.detail-card {
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 16rpx;
  padding: 28rpx;

  &__title {
    font-size: 28rpx;
    font-weight: 600;
    color: #1F2937;
    display: block;
    margin-bottom: 20rpx;
  }
}

/* ---------- Address ---------- */
.detail-address {
  display: flex;
  align-items: flex-start;

  &__icon {
    margin-right: 16rpx;
    padding-top: 4rpx;
  }

  &__info {
    flex: 1;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 8rpx;
  }

  &__name {
    font-size: 30rpx;
    font-weight: 600;
    color: #1F2937;
  }

  &__phone {
    font-size: 28rpx;
    color: #6B7280;
  }

  &__tag {
    height: 32rpx;
    padding: 0 12rpx;
    border-radius: 6rpx;
    background: #EFF6FF;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__tag-text {
    font-size: 20rpx;
    color: #2563EB;
    font-weight: 500;
  }

  &__addr {
    font-size: 26rpx;
    color: #6B7280;
    line-height: 1.6;
  }
}

/* ---------- Store ---------- */
.detail-store {
  display: flex;
  align-items: center;

  &__name {
    font-size: 28rpx;
    font-weight: 600;
    color: #1F2937;
    margin-left: 12rpx;
    flex: 1;
  }

  &__btn {
    display: flex;
    align-items: center;
    gap: 2rpx;
    padding: 8rpx 16rpx;
    border: 1rpx solid #2563EB;
    border-radius: 24rpx;
  }

  &__btn-text {
    font-size: 22rpx;
    color: #2563EB;
  }
}

/* ---------- Product item ---------- */
.detail-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F3F4F6;

  &:first-child {
    padding-top: 0;
  }

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &__img {
    width: 168rpx;
    height: 168rpx;
    border-radius: 12rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
    background: #F3F4F6;
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 168rpx;
  }

  &__name {
    font-size: 28rpx;
    color: #1F2937;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.4;
  }

  &__spec {
    font-size: 22rpx;
    color: #9CA3AF;
    margin-top: 8rpx;
    background: #F3F4F6;
    display: inline-block;
    padding: 4rpx 12rpx;
    border-radius: 6rpx;
    align-self: flex-start;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-top: auto;
  }

  &__price {
    font-size: 30rpx;
    color: #E11148;
    font-weight: 600;
  }

  &__qty {
    font-size: 24rpx;
    color: #9CA3AF;
  }
}

/* ---------- Info row ---------- */
.detail-info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10rpx 0;

  &--total {
    border-top: 1rpx solid #F3F4F6;
    margin-top: 8rpx;
    padding-top: 20rpx;
  }

  &__label {
    font-size: 26rpx;
    color: #9CA3AF;
    flex-shrink: 0;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__value {
    font-size: 26rpx;
    color: #1F2937;
    text-align: right;
    word-break: break-all;

    &--accent {
      color: #E11148;
      font-weight: 700;
      font-size: 32rpx;
    }

    &--red {
      color: #E11148;
    }
  }

  &__copy {
    display: flex;
    align-items: center;
    gap: 4rpx;
    flex-shrink: 0;
  }

  &__copy-text {
    font-size: 22rpx;
    color: #2563EB;
  }
}

/* ---------- Bottom action bar ---------- */
.detail-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  &__btn {
    height: 72rpx;
    padding: 0 40rpx;
    border: 1rpx solid #D1D5DB;
    border-radius: 36rpx;
    font-size: 26rpx;
    color: #6B7280;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      opacity: 0.7;
    }

    &--primary {
      border-color: #2563EB;
      color: #2563EB;
      font-weight: 600;
    }
  }
}
</style>
