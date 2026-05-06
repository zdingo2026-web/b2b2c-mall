<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="order" />
      <div class="flex-1">
        <h2 class="font-bold text-lg mb-4">我的订单</h2>

        <!-- Tabs -->
        <div class="flex border-b mb-4">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="px-5 py-2.5 text-sm transition-colors"
            :class="activeTab === tab.value ? 'border-b-2 border-primary-500 text-primary-500 font-bold' : 'text-gray-500 hover:text-primary-500'"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-16">
          <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- Orders -->
        <div v-else-if="orders.length > 0" class="space-y-3">
          <div v-for="order in orders" :key="order.id" class="bg-white rounded-lg overflow-hidden border">
            <div class="bg-gray-50 px-4 py-2 flex items-center justify-between text-sm">
              <span class="text-gray-500">{{ order.createTime }}</span>
              <span class="font-medium" :class="statusColor(order.status)">{{ order.statusText || ORDER_STATUS_MAP[order.status] }}</span>
            </div>
            <div class="px-4 py-3">
              <div v-for="item in (order.items || [])" :key="item.id" class="flex items-center gap-3 py-2">
                <NuxtLink :to="`/product/${item.productId}`" class="w-14 h-14 bg-gray-100 rounded overflow-hidden flex-shrink-0">
                  <img v-if="item.image" :src="item.image" :alt="item.productName" class="w-full h-full object-cover" />
                </NuxtLink>
                <div class="flex-1 min-w-0">
                  <p class="text-sm text-gray-700 line-clamp-1">{{ item.productName }}</p>
                  <p class="text-xs text-gray-400">{{ item.spec }}</p>
                </div>
                <div class="text-right text-sm">
                  <p>&yen;{{ item.price.toFixed(2) }}</p>
                  <p class="text-gray-400">x{{ item.quantity }}</p>
                </div>
              </div>
            </div>
            <div class="px-4 py-2 border-t flex items-center justify-between text-sm">
              <span>合计: <span class="text-promo font-bold">&yen;{{ order.payAmount.toFixed(2) }}</span></span>
              <div class="flex gap-2">
                <NuxtLink :to="`/order/detail?orderNo=${order.orderNo}`" class="px-3 py-1 border rounded text-xs hover:bg-gray-50">详情</NuxtLink>
                <button
                  v-if="order.status === 0"
                  class="px-3 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600"
                  @click="navigateTo(`/order/payment?orderNo=${order.orderNo}`)"
                >
                  去支付
                </button>
                <button
                  v-if="order.status === 2"
                  class="px-3 py-1 bg-primary-500 text-white rounded text-xs hover:bg-primary-600"
                  @click="handleConfirm(order.orderNo)"
                >
                  确认收货
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="bg-white rounded-lg text-center py-16 text-gray-400">
          <p>暂无订单</p>
        </div>

        <!-- Pagination -->
        <div v-if="total > pageSize" class="mt-4 flex justify-center">
          <div class="flex items-center gap-2">
            <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page <= 1" @click="page--">上一页</button>
            <span class="text-sm text-gray-500">{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
            <button class="px-4 py-2 border rounded text-sm hover:bg-gray-50 disabled:opacity-50" :disabled="page >= Math.ceil(total / pageSize)" @click="page++">下一页</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Order } from '~/composables/types'
import { ORDER_STATUS_MAP } from '~/composables/types'

const route = useRoute()
const { get, put } = useRequest()

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '已完成', value: '3' },
]

const activeTab = ref('all')
const orders = ref<Order[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const statusColor = (status: number) => {
  const map: Record<number, string> = { 0: 'text-red-500', 1: 'text-primary-500', 2: 'text-blue-500', 3: 'text-green-500', 4: 'text-gray-400' }
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
    const params: Record<string, string | number | undefined> = { page: page.value, pageSize }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)
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

const handleConfirm = async (orderNo: string) => {
  if (!confirm('确认已收到商品？')) return
  try {
    await put(`/order/confirm/${orderNo}`)
    loadOrders()
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : '操作失败')
  }
}

onMounted(() => {
  const status = route.query.status as string
  if (status && status !== 'all') {
    activeTab.value = status
  }
  loadOrders()
})

watch(page, () => loadOrders())
</script>
