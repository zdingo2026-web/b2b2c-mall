<template>
  <view class="page-register">
    <!-- Gradient background -->
    <view class="register-bg">
      <view class="register-bg__deco register-bg__deco--1" />
      <view class="register-bg__deco register-bg__deco--2" />
    </view>

    <!-- Content -->
    <view class="register-content">
      <!-- Header -->
      <view class="register-header">
        <text class="register-header__title">注册账号</text>
        <text class="register-header__subtitle">创建您的商城账号</text>
      </view>

      <!-- White card form -->
      <view class="register-card">
        <!-- Phone input -->
        <view class="register-field">
          <view class="register-field__prefix">
            <RemixIcon name="smartphone-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="register-field__placeholder"
            class="register-field__input"
          />
        </view>

        <!-- Verification code input -->
        <view class="register-field">
          <view class="register-field__prefix">
            <RemixIcon name="shield-keyhole-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="code"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            placeholder-class="register-field__placeholder"
            class="register-field__input"
          />
          <view
            class="register-field__code-btn"
            :class="{ 'register-field__code-btn--disabled': countdown > 0 }"
            @tap="sendCode"
          >
            <text class="register-field__code-text">{{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}</text>
          </view>
        </view>

        <!-- Password input -->
        <view class="register-field">
          <view class="register-field__prefix">
            <RemixIcon name="lock-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="password"
            :password="!showPassword"
            maxlength="20"
            placeholder="请设置密码（6-20位）"
            placeholder-class="register-field__placeholder"
            class="register-field__input"
          />
          <view class="register-field__toggle" @tap="showPassword = !showPassword">
            <RemixIcon :name="showPassword ? 'eye-line' : 'eye-off-line'" :size="20" color="#9CA3AF" />
          </view>
        </view>

        <!-- Confirm password input -->
        <view class="register-field">
          <view class="register-field__prefix">
            <RemixIcon name="lock-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="confirmPassword"
            :password="!showConfirmPassword"
            maxlength="20"
            placeholder="请确认密码"
            placeholder-class="register-field__placeholder"
            class="register-field__input"
          />
          <view class="register-field__toggle" @tap="showConfirmPassword = !showConfirmPassword">
            <RemixIcon :name="showConfirmPassword ? 'eye-line' : 'eye-off-line'" :size="20" color="#9CA3AF" />
          </view>
        </view>

        <!-- Register button -->
        <view class="register-btn" @tap="handleRegister">
          <text class="register-btn__text">注 册</text>
        </view>
      </view>

      <!-- Agreement -->
      <view class="register-agreement">
        <view class="register-agreement__check" @tap="agreed = !agreed">
          <view v-if="agreed" class="register-agreement__box register-agreement__box--checked">
            <RemixIcon name="check-line" :size="12" color="#fff" />
          </view>
          <view v-else class="register-agreement__box" />
        </view>
        <text class="register-agreement__text">
          我已阅读并同意
          <text class="register-agreement__link" @tap.stop="openAgreement('user')">《用户协议》</text>
          和
          <text class="register-agreement__link" @tap.stop="openAgreement('privacy')">《隐私政策》</text>
        </text>
      </view>

      <!-- Login link -->
      <view class="register-login" @tap="navigateToLogin">
        <text class="register-login__text">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { http, setTokens } from '@/utils/request'
import RemixIcon from '@/components/RemixIcon.vue'

interface LoginResult {
  accessToken: string
  refreshToken: string
  userInfo: { userId: number; nickname: string; avatar: string; username: string }
}

const phone = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const countdown = ref(0)
const agreed = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

function isValidPhone(value: string): boolean {
  return /^1[3-9]\d{9}$/.test(value)
}

function isValidPassword(value: string): boolean {
  return value.length >= 6 && value.length <= 20
}

function sendCode(): void {
  if (countdown.value > 0) return
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  http.post(`/v1/member/auth/sms/send?phone=${encodeURIComponent(phone.value)}&bizType=2`).catch(() => {
    // For MVP, still start countdown even if API call fails
  })

  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)

  uni.showToast({ title: '验证码已发送', icon: 'success' })
}

async function handleRegister(): Promise<void> {
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value || code.value.length < 4) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!isValidPassword(password.value)) {
    uni.showToast({ title: '密码长度6-20位', icon: 'none' })
    return
  }
  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  try {
    const data = await http.post<LoginResult>('/v1/member/auth/register', {
      phone: phone.value,
      code: code.value,
      password: password.value,
    })
    setTokens(data.accessToken, data.refreshToken)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/mine/index' })
    }, 1500)
  } catch {
    // Error handled by request util
  }
}

function openAgreement(type: 'user' | 'privacy'): void {
  const urls: Record<string, string> = {
    user: '/pages/webview/index?url=user-agreement',
    privacy: '/pages/webview/index?url=privacy-policy',
  }
  uni.navigateTo({ url: urls[type] })
}

function navigateToLogin(): void {
  uni.navigateBack()
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.page-register {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

/* Gradient background */
.register-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.register-bg__deco {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
  background: #fff;

  &--1 {
    width: 500rpx;
    height: 500rpx;
    top: -120rpx;
    right: -100rpx;
  }

  &--2 {
    width: 360rpx;
    height: 360rpx;
    bottom: 80rpx;
    left: -80rpx;
  }
}

/* Content layer */
.register-content {
  position: relative;
  z-index: 1;
  padding: 0 48rpx;
  padding-top: 120rpx;
}

/* Header */
.register-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 48rpx;

  &__title {
    font-size: 44rpx;
    font-weight: 700;
    color: #fff;
  }

  &__subtitle {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.75);
    margin-top: 12rpx;
  }
}

/* Card form */
.register-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
}

.register-field {
  display: flex;
  align-items: center;
  height: 96rpx;
  border-bottom: 1rpx solid #f0f0f5;
  margin-bottom: 8rpx;

  &__prefix {
    width: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    height: 96rpx;
    font-size: 30rpx;
    color: #1f2937;
  }

  &__placeholder {
    color: #d1d5db;
    font-size: 30rpx;
  }

  &__code-btn {
    flex-shrink: 0;
    height: 60rpx;
    padding: 0 24rpx;
    border-radius: 30rpx;
    background: #667eea;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 12rpx;

    &--disabled {
      background: #e5e7eb;
    }
  }

  &__code-text {
    font-size: 24rpx;
    color: #fff;
    white-space: nowrap;

    .register-field__code-btn--disabled & {
      color: #9ca3af;
    }
  }

  &__toggle {
    width: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}

/* Register button */
.register-btn {
  margin-top: 48rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);

  &:active {
    opacity: 0.85;
    transform: scale(0.98);
  }

  &__text {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
    letter-spacing: 8rpx;
  }
}

/* Agreement */
.register-agreement {
  display: flex;
  align-items: flex-start;
  margin-top: 32rpx;
  padding: 0 8rpx;

  &__check {
    flex-shrink: 0;
    margin-right: 12rpx;
    padding-top: 2rpx;
  }

  &__box {
    width: 32rpx;
    height: 32rpx;
    border-radius: 6rpx;
    border: 2rpx solid rgba(255, 255, 255, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;

    &--checked {
      background: #667eea;
      border-color: #667eea;
    }
  }

  &__text {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.7);
    line-height: 36rpx;
  }

  &__link {
    color: #fff;
    font-weight: 500;
  }
}

/* Login link */
.register-login {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 48rpx;
  padding-bottom: env(safe-area-inset-bottom);

  &__text {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.7);
  }
}
</style>
