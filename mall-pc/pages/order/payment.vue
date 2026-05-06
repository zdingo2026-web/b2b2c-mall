<template>
  <div class="max-w-3xl mx-auto px-4 py-10">
    <div class="bg-white rounded-lg p-8">
      <!-- Order info -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-primary-100 rounded-full mx-auto mb-4 flex items-center justify-center">
          <svg class="w-8 h-8 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h1 class="text-xl font-bold mb-2">订单提交成功</h1>
        <p class="text-gray-500">订单号: {{ orderNo }}</p>
        <p class="text-2xl font-bold text-promo mt-4">应付金额: &yen;{{ orderAmount }}</p>
        <p class="text-sm text-gray-400 mt-2">请在30分钟内完成支付</p>
      </div>

      <!-- Payment methods -->
      <div class="mb-8">
        <h2 class="font-bold mb-4">选择支付方式</h2>
        <div class="space-y-3">
          <div
            v-for="method in payMethods"
            :key="method.value"
            class="flex items-center gap-4 p-4 border rounded cursor-pointer transition-colors"
            :class="selectedMethod === method.value ? 'border-primary-500 bg-primary-50' : 'hover:border-primary-300'"
            @click="selectedMethod = method.value"
          >
            <div class="w-10 h-10 rounded flex items-center justify-center text-xl" :class="method.bgClass">
              {{ method.icon }}
            </div>
            <span class="font-medium">{{ method.label }}</span>
            <div class="ml-auto">
              <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center" :class="selectedMethod === method.value ? 'border-primary-500' : 'border-gray-300'">
                <div v-if="selectedMethod === method.value" class="w-3 h-3 bg-primary-500 rounded-full"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- QR Code area (for WeChat/Alipay) -->
      <div v-if="qrCodeUrl" class="text-center mb-8">
        <p class="text-sm text-gray-500 mb-3">请使用{{ selectedMethod === 1 ? '微信' : '支付宝' }}扫码支付</p>
        <div class="w-48 h-48 bg-white border-2 border-gray-200 rounded-lg mx-auto flex items-center justify-center">
          <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="支付二维码" class="w-44 h-44" />
          <p v-else class="text-gray-400 text-sm">二维码加载中...</p>
        </div>
        <p class="text-xs text-gray-400 mt-2">支付完成后将自动跳转</p>
      </div>

      <!-- Pay button -->
      <div class="text-center">
        <button
          class="bg-red-500 text-white px-16 py-3 rounded text-lg hover:bg-red-600 transition-colors disabled:opacity-50"
          :disabled="paying || !selectedMethod"
          @click="handlePay"
        >
          {{ paying ? '支付中...' : '确认支付' }}
        </button>
      </div>

      <!-- Payment status overlay -->
      <div v-if="paymentStatus === 'success'" class="text-center mt-8">
        <div class="w-16 h-16 bg-green-100 rounded-full mx-auto mb-3 flex items-center justify-center">
          <svg class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 class="text-lg font-bold text-green-600 mb-2">支付成功</h3>
        <div class="flex items-center justify-center gap-4 mt-4">
          <NuxtLink :to="`/order/detail?orderNo=${orderNo}`" class="btn-primary">查看订单</NuxtLink>
          <NuxtLink to="/" class="btn-outline">返回首页</NuxtLink>
        </div>
      </div>

      <div v-if="paymentStatus === 'failed'" class="text-center mt-8">
        <div class="w-16 h-16 bg-red-100 rounded-full mx-auto mb-3 flex items-center justify-center">
          <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <h3 class="text-lg font-bold text-red-600 mb-2">支付失败</h3>
        <p class="text-gray-500 text-sm mb-4">请重新选择支付方式或稍后重试</p>
        <button class="btn-primary" @click="paymentStatus = 'pending'">重新支付</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const { get, post } = useRequest()

const orderNo = computed(() => (route.query.orderNo as string) || '')
const orderAmount = ref('0.00')
const selectedMethod = ref(1)
const paying = ref(false)
const qrCodeUrl = ref('')
const paymentStatus = ref<'pending' | 'success' | 'failed'>('pending')

let pollTimer: ReturnType<typeof setInterval> | null = null

const payMethods = [
  { value: 1, label: '微信支付', icon: '', bgClass: 'bg-green-100 text-green-600' },
  { value: 2, label: '支付宝', icon: '', bgClass: 'bg-blue-100 text-blue-600' },
  { value: 3, label: '银行卡支付', icon: '', bgClass: 'bg-gray-100 text-gray-600' },
]

const handlePay = async () => {
  if (!orderNo.value || !selectedMethod.value) return
  paying.value = true
  try {
    const data = await post<{ payUrl: string; qrCodeUrl: string }>('/payment/create', {
      orderNo: orderNo.value,
      payMethod: selectedMethod.value,
    })
    if (data.qrCodeUrl) {
      qrCodeUrl.value = data.qrCodeUrl
      startPolling()
    } else if (data.payUrl) {
      // For bank card, redirect
      window.open(data.payUrl, '_blank')
      startPolling()
    }
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '支付发起失败'
    alert(msg)
  } finally {
    paying.value = false
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    try {
      const data = await get<{ status: number; payTime: string }>(`/payment/status/${orderNo.value}`)
      if (data.status === 1) {
        paymentStatus.value = 'success'
        stopPolling()
      } else if (data.status === 2) {
        paymentStatus.value = 'failed'
        stopPolling()
      }
    } catch {
      // keep polling
    }
  }, 3000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const loadOrderAmount = async () => {
  if (!orderNo.value) return
  try {
    const order = await get<{ payAmount: number }>(`/order/${orderNo.value}`)
    orderAmount.value = order.payAmount?.toFixed(2) || '0.00'
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadOrderAmount()
})

onUnmounted(() => {
  stopPolling()
})
</script>
