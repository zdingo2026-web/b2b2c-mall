<template>
  <div class="min-h-screen flex items-center justify-center" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <!-- White card -->
    <div class="w-[420px] bg-white rounded-2xl shadow-2xl px-10 py-12">
      <!-- Title -->
      <h1 class="text-2xl font-bold text-gray-800 text-center mb-8">欢迎登录</h1>

      <!-- Error message -->
      <p v-if="errorMsg" class="text-red-500 text-sm mb-4 text-center">{{ errorMsg }}</p>

      <!-- Phone input -->
      <div class="relative mb-4">
        <div class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
          </svg>
        </div>
        <input
          v-model="form.phone"
          type="tel"
          maxlength="11"
          class="w-full h-12 pl-10 pr-4 border border-gray-200 rounded-lg text-sm focus:border-[#667eea] focus:outline-none focus:ring-2 focus:ring-[#667eea]/20 transition"
          placeholder="请输入手机号"
        />
      </div>

      <!-- Verification code input + button -->
      <div class="flex gap-3 mb-6">
        <div class="relative flex-1">
          <div class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <input
            v-model="form.code"
            type="text"
            maxlength="6"
            class="w-full h-12 pl-10 pr-4 border border-gray-200 rounded-lg text-sm focus:border-[#667eea] focus:outline-none focus:ring-2 focus:ring-[#667eea]/20 transition"
            placeholder="请输入验证码"
          />
        </div>
        <button
          type="button"
          class="h-12 px-5 rounded-lg text-sm font-medium flex-shrink-0 transition"
          :class="countdown > 0
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-[#2563EB] text-white hover:bg-[#1d4ed8]'"
          :disabled="countdown > 0"
          @click="handleSendCode"
        >
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </div>

      <!-- Login button -->
      <button
        class="w-full h-12 rounded-lg text-white font-medium text-base transition-opacity disabled:opacity-60"
        style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        :disabled="submitting"
        @click="handleLogin"
      >
        {{ submitting ? '登录中...' : '登 录' }}
      </button>

      <!-- Agreement -->
      <div class="flex items-start mt-5">
        <input
          v-model="agreed"
          type="checkbox"
          class="mt-0.5 mr-2 w-4 h-4 accent-[#667eea] rounded"
        />
        <p class="text-xs text-gray-400 leading-5">
          我已阅读并同意
          <a href="javascript:;" class="text-[#667eea]">《用户协议》</a>
          和
          <a href="javascript:;" class="text-[#667eea]">《隐私政策》</a>
        </p>
      </div>

      <!-- Divider -->
      <div class="flex items-center my-6">
        <div class="flex-1 h-px bg-gray-200"></div>
        <span class="px-3 text-xs text-gray-400">其他方式登录</span>
        <div class="flex-1 h-px bg-gray-200"></div>
      </div>

      <!-- Third-party login -->
      <div class="flex items-center justify-center gap-8">
        <button class="flex flex-col items-center gap-1 group">
          <span class="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center group-hover:bg-green-100 transition">
            <svg class="w-5 h-5 text-green-500" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm3.164 4.508c-1.842-.004-3.536.55-4.795 1.592-1.39 1.153-2.112 2.774-2.112 4.508 0 1.07.25 2.093.718 3.024a9.03 9.03 0 003.157 1.436c.437.103.893.158 1.357.158.67 0 1.32-.107 1.937-.3l1.283.75a.28.28 0 00.142.047c.136 0 .247-.112.247-.25 0-.06-.025-.122-.041-.18l-.263-1.002a.504.504 0 01.181-.564C18.653 18.603 19.6 16.9 19.6 15.1c0-2.573-2.144-4.594-4.838-4.6zm-1.592 2.704c.545 0 .987.449.987 1.002a.995.995 0 01-.987 1.002.995.995 0 01-.987-1.002c0-.553.442-1.002.987-1.002zm3.185 0c.545 0 .987.449.987 1.002a.995.995 0 01-.987 1.002.995.995 0 01-.987-1.002c0-.553.442-1.002.987-1.002z"/>
            </svg>
          </span>
          <span class="text-xs text-gray-500 group-hover:text-green-600">微信</span>
        </button>
        <button class="flex flex-col items-center gap-1 group">
          <span class="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center group-hover:bg-blue-100 transition">
            <svg class="w-5 h-5 text-blue-500" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69.01-.03.01-.14-.07-.2-.08-.06-.19-.04-.27-.02-.12.02-1.96 1.25-5.54 3.67-.52.36-1 .53-1.42.52-.47-.01-1.37-.26-2.03-.48-.82-.27-1.47-.42-1.42-.88.03-.24.37-.49 1.02-.75 3.99-1.74 6.65-2.89 7.99-3.44 3.81-1.58 4.6-1.86 5.12-1.87.11 0 .37.03.54.17.14.12.18.28.2.45-.01.06.01.24 0 .38z"/>
            </svg>
          </span>
          <span class="text-xs text-gray-500 group-hover:text-blue-600">QQ</span>
        </button>
        <button class="flex flex-col items-center gap-1 group">
          <span class="w-10 h-10 rounded-full bg-sky-50 flex items-center justify-center group-hover:bg-sky-100 transition">
            <svg class="w-5 h-5 text-sky-500" viewBox="0 0 24 24" fill="currentColor">
              <path d="M21.422 15.358c-.047-.094-.345-.725-.987-1.827l-.032-.054c-.268-.46-.523-.879-.766-1.262.338-1.286.52-2.594.52-3.815C20.157 3.612 16.493 0 12 0 7.506 0 3.843 3.612 3.843 8.4c0 1.22.182 2.528.52 3.814-.243.384-.498.803-.766 1.263l-.032.054c-.642 1.102-.94 1.733-.987 1.827-.33.66-.498 1.2-.498 1.642 0 .768.42 1.272 1.08 1.272.396 0 .828-.18 1.374-.57.18.42.558.744 1.044.744.564 0 1.02-.408 1.272-.9.312.216.66.348 1.044.348.696 0 1.248-.468 1.452-1.092.516.168 1.08.264 1.656.264h.002c.576 0 1.14-.096 1.656-.264.204.624.756 1.092 1.452 1.092.384 0 .732-.132 1.044-.348.252.492.708.9 1.272.9.486 0 .864-.324 1.044-.744.546.39.978.57 1.374.57.66 0 1.08-.504 1.08-1.272 0-.442-.168-.982-.578-1.642z"/>
            </svg>
          </span>
          <span class="text-xs text-gray-500 group-hover:text-sky-600">支付宝</span>
        </button>
      </div>

      <!-- Register link -->
      <div class="text-center mt-8">
        <NuxtLink to="/register" class="text-sm text-[#667eea] hover:text-[#764ba2] font-medium transition">
          注册账号 &gt;
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const userStore = useUserStore()
const { post } = useRequest()
const route = useRoute()

const form = ref({ phone: '', code: '' })
const agreed = ref(false)
const submitting = ref(false)
const errorMsg = ref('')
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const redirect = computed(() => (route.query.redirect as string) || '/')

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!/^1\d{10}$/.test(form.value.phone)) {
    errorMsg.value = '请输入正确的手机号'
    return
  }
  try {
    await post('/auth/sms/send', { phone: form.value.phone })
    startCountdown()
    errorMsg.value = ''
  } catch (err: unknown) {
    errorMsg.value = err instanceof Error ? err.message : '验证码发送失败'
  }
}

const handleLogin = async () => {
  if (!form.value.phone || !form.value.code) {
    errorMsg.value = '请填写手机号和验证码'
    return
  }
  if (!agreed.value) {
    errorMsg.value = '请阅读并同意用户协议和隐私政策'
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    await userStore.smsLogin({ phone: form.value.phone, code: form.value.code })
    navigateTo(redirect.value)
  } catch (err: unknown) {
    errorMsg.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    submitting.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>
