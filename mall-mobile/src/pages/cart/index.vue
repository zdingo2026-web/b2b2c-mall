<template>
  <view class="page-cart">
    <!-- Empty state -->
    <view v-if="!cartGroups.length" class="cart-empty">
      <text class="cart-empty__emoji">🛒</text>
      <text class="cart-empty__text">购物车是空的</text>
      <view class="cart-empty__btn" @tap="uni.switchTab({ url: '/pages/index/index' })">去逛逛</view>
    </view>

    <!-- Cart list grouped by tenant -->
    <view v-else>
      <view v-for="group in cartGroups" :key="group.tenantId" class="cart-group">
        <view class="cart-group__header">
          <text class="cart-group__store">{{ group.tenantName }}</text>
        </view>
        <view v-for="item in group.items" :key="item.id" class="cart-item">
          <view class="cart-item__check" @tap="toggleItemCheck(item)">
            <view class="cart-item__checkbox" :class="{ 'cart-item__checkbox--checked': item.isChecked }">
              <text v-if="item.isChecked" class="cart-item__checkmark">✓</text>
            </view>
          </view>
          <image class="cart-item__img" :src="item.productImage" mode="aspectFill" @tap="navigateTo(`/pages/product/index?id=${item.spuId}`)" />
          <view class="cart-item__info">
            <text class="cart-item__name">{{ item.productName }}</text>
            <text class="cart-item__spec">{{ item.specValues }}</text>
            <view class="cart-item__bottom">
              <text class="cart-item__price">&yen;{{ item.price }}</text>
              <view class="cart-item__stepper">
                <text class="cart-item__btn" @tap="handleDecrease(item)">-</text>
                <text class="cart-item__num">{{ item.quantity }}</text>
                <text class="cart-item__btn" @tap="handleIncrease(item)">+</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Bottom bar -->
      <view class="cart-bottom">
        <view class="cart-bottom__all" @tap="toggleSelectAll">
          <view class="cart-item__checkbox" :class="{ 'cart-item__checkbox--checked': allSelected }">
            <text v-if="allSelected" class="cart-item__checkmark">✓</text>
          </view>
          <text class="cart-bottom__all-text">全选</text>
        </view>
        <view class="cart-bottom__right">
          <text class="cart-bottom__label">合计：</text>
          <text class="cart-bottom__total">&yen;{{ selectedTotal }}</text>
          <view class="cart-bottom__checkout" @tap="handleCheckout">结算({{ selectedCount }})</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCartList, updateCartItem, deleteCartItem, type CartGroupVO, type CartItemVO } from '@/api/cart'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const cartGroups = ref<CartGroupVO[]>([])

const allItems = computed<CartItemVO[]>(() => cartGroups.value.flatMap((g) => g.items))
const checkedItems = computed(() => allItems.value.filter((i) => i.isChecked))
const selectedCount = computed(() => checkedItems.value.reduce((sum, i) => sum + i.quantity, 0))
const selectedTotal = computed(() => checkedItems.value.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2))
const allSelected = computed(() => allItems.value.length > 0 && allItems.value.every((i) => i.isChecked))

async function loadCart() {
  if (!userStore.isLoggedIn) {
    cartGroups.value = []
    return
  }
  try {
    cartGroups.value = await getCartList()
  } catch {
    cartGroups.value = []
  }
}

function toggleItemCheck(item: CartItemVO) {
  item.isChecked = item.isChecked ? 0 : 1
}

function toggleSelectAll() {
  const newVal = !allSelected.value ? 1 : 0
  allItems.value.forEach((item) => {
    item.isChecked = newVal
  })
}

async function handleDecrease(item: CartItemVO) {
  if (item.quantity <= 1) {
    await deleteCartItem(item.id)
    loadCart()
  } else {
    await updateCartItem(item.id, item.quantity - 1)
    loadCart()
  }
}

async function handleIncrease(item: CartItemVO) {
  await updateCartItem(item.id, item.quantity + 1)
  loadCart()
}

function handleCheckout() {
  if (selectedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  const items = checkedItems.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity, spuId: i.spuId }))
  uni.navigateTo({ url: `/pages/order/confirm?items=${encodeURIComponent(JSON.stringify(items))}` })
}

onShow(() => {
  loadCart()
})
</script>

<style lang="scss" scoped>
.page-cart { min-height: 100vh; background: #f5f5f5; padding-bottom: 140rpx; }
.cart-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 200rpx 0; }
.cart-empty__emoji { font-size: 120rpx; margin-bottom: 24rpx; }
.cart-empty__text { font-size: 28rpx; color: #999; margin-bottom: 40rpx; }
.cart-empty__btn { background: #f97316; color: #fff; padding: 16rpx 64rpx; border-radius: 40rpx; font-size: 28rpx; }
.cart-group { background: #fff; border-radius: 16rpx; margin: 20rpx 24rpx; overflow: hidden; }
.cart-group__header { padding: 24rpx 24rpx 12rpx; border-bottom: 1rpx solid #f5f5f5; }
.cart-group__store { font-size: 28rpx; font-weight: 600; color: #333; }
.cart-item { display: flex; align-items: center; padding: 20rpx 24rpx; }
.cart-item__check { margin-right: 16rpx; }
.cart-item__checkbox { width: 40rpx; height: 40rpx; border-radius: 50%; border: 2rpx solid #ddd; display: flex; align-items: center; justify-content: center; }
.cart-item__checkbox--checked { background: #f97316; border-color: #f97316; }
.cart-item__checkmark { color: #fff; font-size: 22rpx; }
.cart-item__img { width: 160rpx; height: 160rpx; border-radius: 12rpx; margin-right: 20rpx; flex-shrink: 0; }
.cart-item__info { flex: 1; min-width: 0; }
.cart-item__name { font-size: 26rpx; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.cart-item__spec { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
.cart-item__bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 12rpx; }
.cart-item__price { font-size: 30rpx; color: #f97316; font-weight: 600; }
.cart-item__stepper { display: flex; align-items: center; border: 1rpx solid #e5e5e5; border-radius: 6rpx; }
.cart-item__btn { width: 52rpx; height: 48rpx; line-height: 48rpx; text-align: center; font-size: 28rpx; color: #666; }
.cart-item__num { min-width: 56rpx; text-align: center; font-size: 26rpx; border-left: 1rpx solid #e5e5e5; border-right: 1rpx solid #e5e5e5; line-height: 48rpx; }
.cart-bottom { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 20rpx 32rpx; box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06); z-index: 100; }
.cart-bottom__all { display: flex; align-items: center; }
.cart-bottom__all-text { font-size: 28rpx; color: #333; margin-left: 12rpx; }
.cart-bottom__right { display: flex; align-items: center; }
.cart-bottom__label { font-size: 26rpx; color: #666; }
.cart-bottom__total { font-size: 36rpx; color: #f97316; font-weight: 700; }
.cart-bottom__checkout { background: #f97316; color: #fff; padding: 16rpx 40rpx; border-radius: 40rpx; font-size: 28rpx; margin-left: 20rpx; }
</style>
