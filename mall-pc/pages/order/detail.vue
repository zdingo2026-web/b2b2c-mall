<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- Breadcrumb -->
    <div class="text-sm text-gray-400 mb-4">
      <NuxtLink to="/" class="hover:text-primary-500">首页</NuxtLink>
      <span class="mx-2">/</span>
      <NuxtLink to="/order/list" class="hover:text-primary-500">我的订单</NuxtLink>
      <span class="mx-2">/</span>
      <span class="text-gray-700">订单详情</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <template v-if="order && !loading">
      <!-- ========== Status bar ========== -->
      <div
        class="rounded-xl p-6 mb-4 text-white"
        :style="{ background: `linear-gradient(135deg, ${statusGradient.from}, ${statusGradient.to})` }"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <span class="text-3xl">{{ statusIcon }}</span>
            <div>
              <h1 class="text-xl font-bold">{{ order.statusText || ORDER_STATUS_MAP[order.status] }}</h1>
              <p v-if="order.status === 0 && countdownText" class="text-sm opacity-90 mt-1">
                剩余 {{ countdownText }}，请尽快付款
              </p>
              <p v-if="order.status === 2" class="text-sm opacity-90 mt-1">商品正在配送中，请耐心等待</p>
            </div>
          </div>
          <div class="flex gap-3">
            <button
              v-if="order.status === 0"
              class="px-6 py-2.5 bg-white/20 backdrop-blur text-white rounded-lg text-sm font-medium hover:bg-white/30 transition"
              @click="navigateTo(`/order/payment?orderNo=${order.orderNo}`)"
            >
              去支付
            </button>
            <button
              v-if="order.status === 0"
              class="px-6 py-2.5 bg-white/10 backdrop-blur text-white rounded-lg text-sm font-medium hover:bg-white/20 transition"
              @click="handleCancel"
            >
              取消订单
            </button>
            <button
              v-if="order.status === 2"
              class="px-6 py-2.5 bg-white/20 backdrop-blur text-white rounded-lg text-sm font-medium hover:bg-white/30 transition"
              @click="handleConfirm"
            >
              确认收货
            </button>
            <button
              v-if="order.status === 7 || (order.status === 3 && !order.isReviewed)"
              class="px-6 py-2.5 bg-white/20 backdrop-blur text-white rounded-lg text-sm font-medium hover:bg-white/30 transition"
              @click="navigateTo(`/order/review?orderNo=${order.orderNo}`)"
            >
              去评价
            </button>
            <button
              v-if="order.status === 3 || order.status === 4 || order.status === 6"
              class="px-6 py-2.5 bg-white/20 backdrop-blur text-white rounded-lg text-sm font-medium hover:bg-white/30 transition"
              @click="handleDelete"
            >
              删除订单
            </button>
          </div>
        </div>
      </div>

      <!-- ========== Logistics card ========== -->
      <div
        v-if="order.deliveryNo"
        class="rounded-xl p-5 mb-4 bg-[#EFF6FF] border border-blue-100"
      >
        <div class="flex items-center gap-3">
          <span class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
            <svg class="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
            </svg>
          </span>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 text-sm">
              <span class="font-medium text-gray-700">{{ order.deliveryCompany }}</span>
              <span class="text-gray-400">|</span>
              <span class="text-gray-500 font-mono">{{ order.deliveryNo }}</span>
            </div>
            <p v-if="order.deliveryStatus" class="text-xs text-blue-500 mt-1">{{ order.deliveryStatus }}</p>
          </div>
        </div>
      </div>

      <!-- ========== Address section ========== -->
      <div class="bg-white rounded-xl p-5 mb-4">
        <div class="flex items-start gap-3">
          <span class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center flex-shrink-0 mt-0.5">
            <svg class="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
            </svg>
          </span>
          <div class="flex-1">
            <div class="flex items-center gap-2">
              <span class="font-medium text-gray-800">{{ order.addressName }}</span>
              <span class="text-gray-500 text-sm">{{ order.addressPhone }}</span>
              <span
                v-if="order.addressIsDefault"
                class="inline-block px-1.5 py-0.5 text-xs bg-red-50 text-promo rounded"
              >
                默认
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ order.addressDetail }}</p>
          </div>
        </div>
      </div>

      <!-- ========== Store & products ========== -->
      <div class="bg-white rounded-xl p-5 mb-4">
        <!-- Store info -->
        <div v-if="order.tenantName || order.storeId" class="flex items-center justify-between mb-4 pb-4 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <svg class="w-4 h-4 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 21v-7.5a.75.75 0 01.75-.75h3a.75.75 0 01.75.75V21m-4.5 0H2.36m11.14 0H18m0 0h3.64m-1.39 0V9.349m-16.5 11.65V9.35m0 0a3.001 3.001 0 003.75-.615A2.993 2.993 0 009.75 9.75c.896 0 1.7-.393 2.25-1.016a2.993 2.993 0 002.25 1.016c.896 0 1.7-.393 2.25-1.016A3.001 3.001 0 0021 9.349m-18 0a2.998 2.998 0 002.25 1.016 2.993 2.993 0 002.25-1.016A2.993 2.993 0 009.75 9.75" />
            </svg>
            <span class="font-medium text-gray-800">{{ order.tenantName || '店铺' }}</span>
          </div>
          <NuxtLink
            v-if="order.storeId"
            :to="`/store/${order.storeId}`"
            class="text-xs text-primary-500 hover:text-primary-600 font-medium transition"
          >
            进店逛逛 &gt;
          </NuxtLink>
        </div>

        <!-- Product items -->
        <div
          v-for="item in (order.items || [])"
          :key="item.id"
          class="flex items-center gap-4 py-4 border-b border-gray-50 last:border-0"
        >
          <NuxtLink :to="`/product/${item.productId}`" class="w-20 h-20 bg-gray-50 rounded-lg overflow-hidden flex-shrink-0">
            <img v-if="item.image" :src="item.image" :alt="item.productName" class="w-full h-full object-cover" />
          </NuxtLink>
          <div class="flex-1 min-w-0">
            <NuxtLink :to="`/product/${item.productId}`" class="text-sm text-gray-800 line-clamp-2 hover:text-primary-500 transition">
              {{ item.productName }}
            </NuxtLink>
            <p class="text-xs text-gray-400 mt-1.5">{{ item.spec }}</p>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-sm font-medium text-gray-800">&yen;{{ item.price.toFixed(2) }}</p>
            <p class="text-xs text-gray-400 mt-1">x{{ item.quantity }}</p>
          </div>
        </div>
      </div>

      <!-- ========== Order info ========== -->
      <div class="bg-white rounded-xl p-5 mb-4">
        <h2 class="font-bold text-gray-800 mb-4">订单信息</h2>
        <div class="space-y-3 text-sm">
          <div class="flex items-center">
            <span class="text-gray-500 w-20 flex-shrink-0">订单编号</span>
            <span class="text-gray-800 font-mono mr-3">{{ order.orderNo }}</span>
            <button
              class="px-2 py-0.5 text-xs border border-primary-500 text-primary-500 rounded hover:bg-primary-50 transition"
              @click="handleCopy(order.orderNo)"
            >
              复制
            </button>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-20 flex-shrink-0">下单时间</span>
            <span class="text-gray-800">{{ order.createTime }}</span>
          </div>
          <div v-if="order.payTime" class="flex">
            <span class="text-gray-500 w-20 flex-shrink-0">支付时间</span>
            <span class="text-gray-800">{{ order.payTime }}</span>
          </div>
          <div v-if="order.payType != null" class="flex">
            <span class="text-gray-500 w-20 flex-shrink-0">支付方式</span>
            <span class="text-gray-800">{{ PAY_TYPE_MAP[order.payType] || '未知' }}</span>
          </div>
          <div v-if="order.invoiceType != null" class="flex">
            <span class="text-gray-500 w-20 flex-shrink-0">发票信息</span>
            <span class="text-gray-800">
              {{ INVOICE_TYPE_MAP[order.invoiceType] || '未知' }}
              <template v-if="order.invoiceTitle"> - {{ order.invoiceTitle }}</template>
            </span>
          </div>
          <div v-if="order.note" class="flex">
            <span class="text-gray-500 w-20 flex-shrink-0">订单备注</span>
            <span class="text-gray-800">{{ order.note }}</span>
          </div>
        </div>
      </div>

      <!-- ========== Fee breakdown ========== -->
      <div class="bg-white rounded-xl p-5">
        <h2 class="font-bold text-gray-800 mb-4">费用明细</h2>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-500">商品总额</span>
            <span class="text-gray-800">&yen;{{ order.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">运费</span>
            <span class="text-gray-800">&yen;{{ order.freightAmount.toFixed(2) }}</span>
          </div>
          <div v-if="order.discountAmount && order.discountAmount > 0" class="flex justify-between">
            <span class="text-gray-500">优惠</span>
            <span class="text-promo">-&yen;{{ order.discountAmount.toFixed(2) }}</span>
          </div>
          <div class="border-t border-gray-100 pt-3 flex justify-between items-center">
            <span class="text-gray-500">实付金额</span>
            <span class="text-promo font-bold text-xl">&yen;{{ order.payAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { Order } from '~/composables/types'
import { ORDER_STATUS_MAP } from '~/composables/types'

const route = useRoute()
const { get, put, delete: del } = useRequest()

const PAY_TYPE_MAP: Record<number, string> = {
  1: '微信支付',
  2: '支付宝',
  3: '余额支付',
}

const INVOICE_TYPE_MAP: Record<number, string> = {
  0: '不开发票',
  1: '电子发票',
  2: '纸质发票',
}

const orderNo = computed(() => (route.query.orderNo as string) || '')
const order = ref<Order | null>(null)
const loading = ref(true)

// Status gradient colors
const STATUS_GRADIENT: Record<number, { from: string; to: string }> = {
  0: { from: '#F87171', to: '#DC2626' },   // pending pay - red
  1: { from: '#60A5FA', to: '#2563EB' },   // pending ship - blue
  2: { from: '#34D399', to: '#059669' },   // pending receive - green
  3: { from: '#A78BFA', to: '#7C3AED' },   // completed - purple
  4: { from: '#9CA3AF', to: '#6B7280' },   // cancelled - gray
  5: { from: '#FBBF24', to: '#D97706' },   // refunding - amber
  6: { from: '#9CA3AF', to: '#6B7280' },   // refunded - gray
  7: { from: '#F472B6', to: '#DB2777' },   // pending review - pink
}

const statusGradient = computed(() => {
  if (!order.value) return { from: '#9CA3AF', to: '#6B7280' }
  return STATUS_GRADIENT[order.value.status] || { from: '#9CA3AF', to: '#6B7280' }
})

// Status icon emoji
const STATUS_ICON: Record<number, string> = {
  0: '⏳',  // hourglass
  1: '📦',  // package
  2: '🚚',  // truck
  3: '✅',  // check mark
  4: '❌',  // cross mark
  5: '🔄',  // arrows clockwise
  6: '💰',  // money bag
  7: '⭐',  // star
}

const statusIcon = computed(() => {
  if (!order.value) return '📋'
  return STATUS_ICON[order.value.status] || '📋'
})

// Countdown for pending payment (30 min default)
const countdownText = ref('')
let countdownTimer: ReturnType<typeof setInterval> | null = null

const startPayCountdown = () => {
  if (!order.value || order.value.status !== 0) return
  // Calculate remaining time: 30 min from creation
  const created = new Date(order.value.createTime).getTime()
  const deadline = created + 30 * 60 * 1000

  const tick = () => {
    const remaining = deadline - Date.now()
    if (remaining <= 0) {
      countdownText.value = ''
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
      return
    }
    const min = Math.floor(remaining / 60000)
    const sec = Math.floor((remaining % 60000) / 1000)
    countdownText.value = `${min}分${sec.toString().padStart(2, '0')}秒`
  }

  tick()
  countdownTimer = setInterval(tick, 1000)
}

const handleCopy = (text: string) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text)
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}

const handleCancel = async () => {
  if (!confirm('确定要取消该订单吗？')) return
  try {
    await put(`/order/cancel/${orderNo.value}`)
    loadOrder()
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '取消失败'
    alert(msg)
  }
}

const handleConfirm = async () => {
  if (!confirm('确认已收到商品？')) return
  try {
    await put(`/order/confirm/${orderNo.value}`)
    loadOrder()
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '确认收货失败'
    alert(msg)
  }
}

const handleDelete = async () => {
  if (!confirm('确定要删除该订单吗？')) return
  try {
    await del(`/order/${orderNo.value}`)
    navigateTo('/order/list')
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '删除失败'
    alert(msg)
  }
}

const loadOrder = async () => {
  if (!orderNo.value) return
  loading.value = true
  try {
    order.value = await get<Order>(`/order/${orderNo.value}`)
    startPayCountdown()
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => loadOrder())

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>
