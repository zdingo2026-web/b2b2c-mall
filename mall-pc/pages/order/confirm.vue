<template>
  <div class="max-w-7xl mx-auto px-4 py-6 pb-28">
    <h1 class="text-2xl font-bold mb-6">确认订单</h1>

    <!-- Address Section -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <div class="flex items-center justify-between mb-4">
        <h2 class="font-bold flex items-center gap-2">
          <svg class="w-5 h-5 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          收货地址
        </h2>
        <button class="text-sm text-primary-500 hover:text-primary-600 flex items-center gap-1" @click="showAddressModal = true">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          新增地址
        </button>
      </div>

      <!-- No address -->
      <div v-if="addresses.length === 0" class="text-center py-8 text-gray-400">
        <svg class="w-12 h-12 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
        </svg>
        <p class="mb-2">暂无收货地址</p>
        <button class="text-primary-500 text-sm" @click="showAddressModal = true">添加收货地址</button>
      </div>

      <!-- Selected address card -->
      <div v-else-if="selectedAddress" class="border-2 border-primary-500 bg-primary-50/40 rounded-lg p-5">
        <div class="flex items-center justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <span class="font-bold text-gray-800">{{ selectedAddress.name }}</span>
              <span class="text-gray-500">{{ selectedAddress.phone }}</span>
              <span v-if="selectedAddress.isDefault" class="text-xs bg-primary-500 text-white px-2 py-0.5 rounded">默认</span>
              <span v-if="selectedAddress.tag" class="text-xs px-2 py-0.5 rounded" :class="tagClass(selectedAddress.tag)">{{ selectedAddress.tag }}</span>
            </div>
            <p class="text-sm text-gray-600">{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detail }}</p>
          </div>
          <div class="flex items-center gap-3 ml-4">
            <button class="text-sm text-gray-400 hover:text-primary-500 flex items-center gap-1" @click="editAddress(selectedAddress)">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              编辑
            </button>
            <button v-if="addresses.length > 1" class="text-sm text-primary-500 hover:text-primary-600" @click="showAddressList = !showAddressList">
              切换地址
              <svg class="w-4 h-4 inline-block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Address list (expandable) -->
      <div v-if="showAddressList" class="mt-3 space-y-2">
        <div
          v-for="addr in otherAddresses"
          :key="addr.id"
          class="flex items-center gap-4 p-4 border rounded-lg cursor-pointer transition-colors hover:border-primary-300 hover:bg-primary-50/20"
          @click="selectedAddressId = addr.id; showAddressList = false"
        >
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-1">
              <span class="font-medium">{{ addr.name }}</span>
              <span class="text-gray-500">{{ addr.phone }}</span>
              <span v-if="addr.isDefault" class="text-xs bg-primary-500 text-white px-2 py-0.5 rounded">默认</span>
              <span v-if="addr.tag" class="text-xs px-2 py-0.5 rounded" :class="tagClass(addr.tag)">{{ addr.tag }}</span>
            </div>
            <p class="text-sm text-gray-500">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Products by store -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <h2 class="font-bold mb-4">商品清单</h2>
      <div v-for="storeGroup in storeGroups" :key="storeGroup.storeId" class="mb-6 last:mb-0">
        <div class="flex items-center gap-2 mb-3 pb-2 border-b">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          <span class="text-sm font-medium text-gray-700">{{ storeGroup.storeName }}</span>
        </div>
        <div v-for="item in storeGroup.items" :key="item.id" class="flex items-center gap-4 py-3">
          <div class="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
            <img v-if="item.image" :src="item.image" :alt="item.name" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm text-gray-700 line-clamp-2">{{ item.name }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ item.spec }}</p>
          </div>
          <div class="text-right">
            <p class="text-promo font-bold">&yen;{{ item.price.toFixed(2) }}</p>
            <p class="text-xs text-gray-400">x{{ item.quantity }}</p>
          </div>
        </div>
      </div>
      <div v-if="selectedItems.length === 0" class="text-center py-8 text-gray-400">
        <p>没有选中的商品</p>
        <NuxtLink to="/cart" class="text-primary-500 text-sm mt-2 inline-block">返回购物车选择</NuxtLink>
      </div>
    </div>

    <!-- Coupon Row -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-red-50 rounded flex items-center justify-center">
            <svg class="w-5 h-5 text-promo" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          </div>
          <div>
            <span class="font-medium text-gray-800">优惠券</span>
            <span v-if="selectedCoupon" class="ml-2 text-xs bg-red-50 text-promo px-2 py-0.5 rounded">{{ selectedCoupon.name }}</span>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-500">{{ availableCoupons.length }}张可用</span>
          <select
            v-model="selectedCouponId"
            class="text-sm border rounded px-3 py-1.5 bg-white text-gray-700 focus:border-primary-500 focus:outline-none"
          >
            <option :value="null">不使用优惠券</option>
            <option v-for="c in availableCoupons" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Delivery Method -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-blue-50 rounded flex items-center justify-center">
            <svg class="w-5 h-5 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0" />
            </svg>
          </div>
          <span class="font-medium text-gray-800">配送方式</span>
          <span class="text-sm text-gray-600">快递配送</span>
        </div>
        <span class="text-sm text-gray-600">运费：<span :class="freight > 0 ? 'text-gray-800' : 'text-green-500'">{{ freight > 0 ? `¥${freight.toFixed(2)}` : '免运费' }}</span></span>
      </div>
    </div>

    <!-- Invoice Info -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-green-50 rounded flex items-center justify-center">
            <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <span class="font-medium text-gray-800">发票信息</span>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-500">{{ invoiceLabel }}</span>
          <select
            v-model="invoiceType"
            class="text-sm border rounded px-3 py-1.5 bg-white text-gray-700 focus:border-primary-500 focus:outline-none"
          >
            <option :value="0">不开发票</option>
            <option :value="1">个人</option>
            <option :value="2">企业</option>
          </select>
        </div>
      </div>
      <div v-if="invoiceType === 2" class="mt-3 pl-11">
        <input v-model="invoiceTitle" class="w-full h-10 px-3 border rounded text-sm focus:border-primary-500 focus:outline-none" placeholder="请输入企业名称" />
      </div>
    </div>

    <!-- Order note -->
    <div class="bg-white rounded-lg p-6 mb-4">
      <h2 class="font-bold mb-4">订单备注</h2>
      <textarea v-model="orderNote" class="w-full h-20 border rounded-lg p-3 text-sm focus:border-primary-500 focus:outline-none resize-none" placeholder="选填，对本次交易的说明"></textarea>
    </div>

    <!-- Fee breakdown -->
    <div class="bg-white rounded-lg p-6">
      <div class="space-y-3">
        <div class="flex justify-between text-sm">
          <span class="text-gray-500">商品金额</span>
          <span class="text-gray-800">&yen;{{ goodsTotal }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-gray-500">运费</span>
          <span :class="freight > 0 ? 'text-gray-800' : 'text-green-500'">{{ freight > 0 ? `¥${freight.toFixed(2)}` : '免运费' }}</span>
        </div>
        <div v-if="discountAmount > 0" class="flex justify-between text-sm">
          <span class="text-gray-500">优惠</span>
          <span class="text-green-500">-&yen;{{ discountAmount.toFixed(2) }}</span>
        </div>
        <div class="flex justify-between font-bold text-lg pt-3 border-t">
          <span>应付总额</span>
          <span class="text-promo">&yen;{{ payTotal }}</span>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg z-40">
      <div class="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
        <NuxtLink to="/cart" class="text-sm text-gray-500 hover:text-primary-500 flex items-center gap-1">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回购物车
        </NuxtLink>
        <div class="flex items-center gap-6">
          <div class="text-right">
            <span class="text-gray-500">合计：</span>
            <span class="text-2xl font-bold text-promo">&yen;{{ payTotal }}</span>
          </div>
          <button
            class="bg-primary-500 text-white px-16 py-3 rounded-lg text-lg font-medium hover:bg-primary-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="submitting || !selectedAddressId || selectedItems.length === 0"
            @click="handleSubmitOrder"
          >
            {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Address Modal -->
    <AddressModal
      :visible="showAddressModal"
      :address="editingAddress"
      @close="showAddressModal = false; editingAddress = null"
      @saved="onAddressSaved"
    />
  </div>
</template>

<script setup lang="ts">
import type { Address, CartItem, Coupon } from '~/composables/types'

const { get, post } = useRequest()
const cartStore = useCartStore()
const userStore = useUserStore()

const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const orderNote = ref('')
const submitting = ref(false)
const showAddressModal = ref(false)
const showAddressList = ref(false)
const editingAddress = ref<Address | null>(null)

// Coupon state
const coupons = ref<Coupon[]>([])
const selectedCouponId = ref<number | null>(null)

// Invoice state
const invoiceType = ref(0)
const invoiceTitle = ref('')

const invoiceLabel = computed(() => {
  if (invoiceType.value === 1) return '个人'
  if (invoiceType.value === 2) return '企业'
  return '不开发票'
})

const selectedItems = computed(() => cartStore.selectedItems)

const selectedAddress = computed(() => addresses.value.find((a) => a.id === selectedAddressId.value) || null)

const otherAddresses = computed(() => addresses.value.filter((a) => a.id !== selectedAddressId.value))

const availableCoupons = computed(() => coupons.value.filter((c) => c.available))

const selectedCoupon = computed(() => coupons.value.find((c) => c.id === selectedCouponId.value) || null)

const goodsTotal = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

const freight = computed(() => {
  const total = Number(goodsTotal.value)
  if (total >= 99) return 0
  return 10
})

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0
  return selectedCoupon.value.discount
})

const payTotal = computed(() => {
  return (Number(goodsTotal.value) + freight.value - discountAmount.value).toFixed(2)
})

const storeGroups = computed(() => {
  const map = new Map<number, { storeId: number; storeName: string; items: CartItem[] }>()
  for (const item of selectedItems.value) {
    const group = map.get(item.storeId)
    if (group) {
      group.items.push(item)
    } else {
      map.set(item.storeId, { storeId: item.storeId, storeName: item.storeName, items: [item] })
    }
  }
  return Array.from(map.values())
})

const tagClass = (tag?: string) => {
  if (tag === '家') return 'bg-green-50 text-green-600'
  if (tag === '公司') return 'bg-orange-50 text-orange-600'
  if (tag === '学校') return 'bg-blue-50 text-blue-600'
  return 'bg-gray-50 text-gray-500'
}

const editAddress = (addr: Address) => {
  editingAddress.value = addr
  showAddressModal.value = true
}

const onAddressSaved = () => {
  loadAddresses()
}

const loadAddresses = async () => {
  try {
    addresses.value = await get<Address[]>('/address/list')
    const defaultAddr = addresses.value.find((a) => a.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0 && !selectedAddressId.value) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch {
    addresses.value = []
  }
}

const loadCoupons = async () => {
  try {
    coupons.value = await get<Coupon[]>('/coupon/available')
  } catch {
    coupons.value = []
  }
}

const handleSubmitOrder = async () => {
  if (!selectedAddressId.value) {
    alert('请选择收货地址')
    return
  }
  if (selectedItems.value.length === 0) {
    alert('请选择要结算的商品')
    return
  }
  submitting.value = true
  try {
    const items = selectedItems.value.map((item) => ({
      skuId: item.skuId,
      quantity: item.quantity,
    }))
    const payload: Record<string, unknown> = {
      addressId: selectedAddressId.value,
      note: orderNote.value,
      items,
      couponId: selectedCouponId.value,
      invoiceType: invoiceType.value,
      invoiceTitle: invoiceType.value === 2 ? invoiceTitle.value : '',
    }
    const data = await post<{ orderNo: string; orderId: number }>('/order/create', payload)
    navigateTo(`/order/payment?orderNo=${data.orderNo}`)
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '订单提交失败'
    alert(msg)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  await cartStore.fetchCart()
  await loadAddresses()
  await loadCoupons()
})
</script>
