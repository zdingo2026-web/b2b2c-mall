<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">我的订单</h1>

    <!-- Tabs -->
    <div class="flex border-b mb-6 bg-white rounded-t-lg px-4">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="px-6 py-3 text-sm transition-colors"
        :class="activeTab === tab.value ? 'border-b-2 border-primary-500 text-primary-500 font-bold' : 'text-gray-500 hover:text-primary-500'"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- Orders -->
    <div v-else-if="orders.length > 0" class="space-y-4">
      <div v-for="order in orders" :key="order.id" class="bg-white rounded-lg overflow-hidden">
        <!-- Order header -->
        <div class="bg-gray-50 px-6 py-3 flex items-center justify-between text-sm">
          <div class="flex items-center gap-4">
            <span class="font-medium text-gray-700">{{ order.tenantName || '平台自营' }}</span>
            <span class="text-gray-400">{{ order.createTime }}</span>
          </div>
          <span class="font-bold" :class="statusClass(order.status)">{{ order.statusText || ORDER_STATUS_MAP[order.status] }}</span>
        </div>
        <!-- Order items -->
        <div class="px-6 py-4">
          <div v-for="item in (order.items || [])" :key="item.id" class="flex items-center gap-4 py-2">
            <div class="w-16 h-16 bg-gray-100 rounded overflow-hidden flex-shrink-0">
              <img v-if="item.image" :src="item.image" :alt="item.productName" class="w-full h-full object-cover" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm text-gray-700 line-clamp-2">{{ item.productName }}</p>
              <p class="text-xs text-gray-400 mt-1">{{ item.spec }}</p>
            </div>
            <div class="text-right">
              <p class="text-sm font-bold">&yen;{{ item.price.toFixed(2) }}</p>
              <p class="text-xs text-gray-400">x{{ item.quantity }}</p>
            </div>
          </div>
        </div>
        <!-- Order footer -->
        <div class="px-6 py-3 border-t flex items-center justify-between">
          <div class="text-sm">
            <span class="text-gray-500">共{{ (order.items || []).reduce((s, i) => s + i.quantity, 0) }}件商品</span>
            <span class="ml-4">合计: <span class="text-promo font-bold">&yen;{{ order.payAmount.toFixed(2) }}</span></span>
          </div>
          <div class="flex items-center gap-3">
            <NuxtLink :to="`/order/detail?orderNo=${order.orderNo}`" class="px-4 py-1.5 border rounded text-sm hover:bg-gray-50">查看详情</NuxtLink>
            <button v-if="order.status === 0" class="px-4 py-1.5 border rounded text-sm hover:bg-gray-50" @click="handleCancel(order.orderNo)">取消订单</button>
            <button v-if="order.status === 0" class="px-4 py-1.5 bg-primary-500 text-white rounded text-sm hover:bg-primary-600" @click="handlePay(order.orderNo)">付款</button>
            <button v-if="order.status === 1" class="px-4 py-1.5 border rounded text-sm hover:bg-gray-50" @click="handleRemind">提醒发货</button>
            <button v-if="order.status === 2" class="px-4 py-1.5 border rounded text-sm hover:bg-gray-50">查看物流</button>
            <button v-if="order.status === 2" class="px-4 py-1.5 bg-primary-500 text-white rounded text-sm hover:bg-primary-600" @click="handleConfirm(order.orderNo)">确认收货</button>
            <button v-if="order.status === 7 || (order.status === 3 && !order.isReviewed)" class="px-4 py-1.5 bg-primary-500 text-white rounded text-sm hover:bg-primary-600">评价</button>
            <button v-if="order.status === 3 && order.isReviewed" class="px-4 py-1.5 border rounded text-sm hover:bg-gray-50">再次购买</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="bg-white rounded-lg text-center py-20">
      <svg class="w-20 h-20 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
      <p class="text-gray-400 mb-4">暂无订单</p>
      <NuxtLink to="/" class="btn-primary inline-block">去逛逛</NuxtLink>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="mt-6 flex justify-center">
      <div class="flex items-center gap-2">
        <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page <= 1" @click="page--">上一页</button>
        <span class="text-sm text-gray-500">{{ page }} / {{ totalPages }}</span>
        <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page >= totalPages" @click="page++">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Order } from '~/composables/types'
import { ORDER_STATUS_MAP } from '~/composables/types'

const { get, put } = useRequest()
const userStore = useUserStore()

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '待评价', value: '7' },
]

const activeTab = ref('all')
const orders = ref<Order[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize))

const statusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'text-orange-500',
    1: 'text-blue-500',
    2: 'text-purple-500',
    3: 'text-green-500',
    4: 'text-gray-400',
    5: 'text-red-500',
    6: 'text-gray-400',
    7: 'text-primary-500',
  }
  return map[status] || 'text-gray-500'
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  page.value = 1
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number | undefined> = {
      page: page.value,
      pageSize,
    }
    if (activeTab.value !== 'all') {
      params.status = Number(activeTab.value)
    }
    const data = await get<{ list: Order[]; total: number }>('/order/list', params)
    orders.value = data.list || []
    total.value = data.total || 0
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePay = (orderNo: string) => {
  navigateTo(`/order/payment?orderNo=${orderNo}`)
}

const handleRemind = () => {
  alert('已提醒发货')
}

const handleCancel = async (orderNo: string) => {
  if (!confirm('确定要取消该订单吗？')) return
  try {
    await put(`/order/cancel/${orderNo}`)
    loadOrders()
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '取消失败'
    alert(msg)
  }
}

const handleConfirm = async (orderNo: string) => {
  if (!confirm('确认已收到商品？')) return
  try {
    await put(`/order/confirm/${orderNo}`)
    loadOrders()
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '确认收货失败'
    alert(msg)
  }
}

watch(page, () => loadOrders())

onMounted(() => {
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  loadOrders()
})
</script>
