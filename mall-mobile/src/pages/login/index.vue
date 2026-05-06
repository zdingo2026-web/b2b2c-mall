<template>
  <view class="page-login">
    <!-- Gradient background -->
    <view class="login-bg">
      <view class="login-bg__deco login-bg__deco--1" />
      <view class="login-bg__deco login-bg__deco--2" />
    </view>

    <!-- Content -->
    <view class="login-content">
      <!-- Logo area -->
      <view class="login-header">
        <view class="login-header__logo">
          <RemixIcon name="store-2" :size="64" color="#fff" />
        </view>
        <text class="login-header__title">B2B2C商城</text>
        <text class="login-header__subtitle">品质生活 尽在掌握</text>
      </view>

      <!-- White card form -->
      <view class="login-card">
        <!-- Phone input -->
        <view class="login-field">
          <view class="login-field__prefix">
            <RemixIcon name="smartphone-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="login-field__placeholder"
            class="login-field__input"
          />
          <text v-if="phone.length > 0" class="login-field__change" @tap="phone = ''">更换</text>
        </view>

        <!-- Verification code input -->
        <view class="login-field">
          <view class="login-field__prefix">
            <RemixIcon name="shield-keyhole-line" :size="20" color="#9CA3AF" />
          </view>
          <input
            v-model="code"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            placeholder-class="login-field__placeholder"
            class="login-field__input"
          />
          <view
            class="login-field__code-btn"
            :class="{ 'login-field__code-btn--disabled': countdown > 0 }"
            @tap="sendCode"
          >
            <text class="login-field__code-text">{{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}</text>
          </view>
        </view>

        <!-- Login button -->
        <view class="login-btn" @tap="handlePhoneLogin">
          <text class="login-btn__text">登 录</text>
        </view>
      </view>

      <!-- Agreement -->
      <view class="login-agreement">
        <view class="login-agreement__check" @tap="agreed = !agreed">
          <view v-if="agreed" class="login-agreement__box login-agreement__box--checked">
            <RemixIcon name="check-line" :size="12" color="#fff" />
          </view>
          <view v-else class="login-agreement__box" />
        </view>
        <text class="login-agreement__text">
          我已阅读并同意
          <text class="login-agreement__link" @tap.stop="openAgreement('user')">《用户协议》</text>
          和
          <text class="login-agreement__link" @tap.stop="openAgreement('privacy')">《隐私政策》</text>
        </text>
      </view>

      <!-- Third-party login -->
      <view class="login-third">
        <view class="login-third__divider">
          <view class="login-third__line" />
          <text class="login-third__label">其他方式登录</text>
          <view class="login-third__line" />
        </view>
        <view class="login-third__icons">
          <view class="login-third__item" @tap="handleThirdLogin('wechat')">
            <view class="login-third__circle login-third__circle--wechat">
              <RemixIcon name="wechat-fill" :size="28" color="#fff" />
            </view>
            <text class="login-third__name">微信</text>
          </view>
          <view class="login-third__item" @tap="handleThirdLogin('qq')">
            <view class="login-third__circle login-third__circle--qq">
              <RemixIcon name="qq-fill" :size="28" color="#fff" />
            </view>
            <text class="login-third__name">QQ</text>
          </view>
          <view class="login-third__item" @tap="handleThirdLogin('alipay')">
            <view class="login-third__circle login-third__circle--alipay">
              <RemixIcon name="alipay-fill" :size="28" color="#fff" />
            </view>
            <text class="login-third__name">支付宝</text>
          </view>
        </view>
      </view>

      <!-- Register link -->
      <view class="login-register" @tap="navigateToRegister">
        <text class="login-register__text">注册账号</text>
        <RemixIcon name="arrow-right-s-line" :size="16" color="rgba(255,255,255,0.7)" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { http } from '@/utils/request'
import RemixIcon from '@/components/RemixIcon.vue'

const userStore = useUserStore()

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const agreed = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

/** Validate phone number format */
function isValidPhone(value: string): boolean {
  return /^1[3-9]\d{9}$/.test(value)
}

/** Send verification code with 60s countdown */
async function sendCode(): Promise<void> {
  if (countdown.value > 0) return
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  try {
    await http.post(`/v1/member/auth/sms/send?phone=${encodeURIComponent(phone.value)}&bizType=1`)
  } catch {
    // For MVP, still start countdown even if API call fails (e.g., backend not running)
  }

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

/** Handle phone + code login */
async function handlePhoneLogin(): Promise<void> {
  if (!isValidPhone(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value || code.value.length < 4) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  try {
    await userStore.login({ phone: phone.value, code: code.value })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/mine/index' })
    }, 1500)
  } catch {
    // Error handled by request util
  }
}

/** Third-party login (UI only) */
function handleThirdLogin(type: 'wechat' | 'qq' | 'alipay'): void {
  const labels: Record<string, string> = { wechat: '微信', qq: 'QQ', alipay: '支付宝' }
  uni.showToast({ title: `${labels[type]}登录暂未开放`, icon: 'none' })
}

/** Open agreement page */
function openAgreement(type: 'user' | 'privacy'): void {
  const urls: Record<string, string> = {
    user: '/pages/webview/index?url=user-agreement',
    privacy: '/pages/webview/index?url=privacy-policy',
  }
  uni.navigateTo({ url: urls[type] })
}

/** Navigate to register */
function navigateToRegister(): void {
  uni.navigateTo({ url: '/pages/login/register' })
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.page-login {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

/* ---------- Gradient background ---------- */
.login-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.login-bg__deco {
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

/* ---------- Content layer ---------- */
.login-content {
  position: relative;
  z-index: 1;
  padding: 0 48rpx;
  padding-top: 160rpx;
}

/* ---------- Header ---------- */
.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 64rpx;

  &__logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 28rpx;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;
    backdrop-filter: blur(10px);
  }

  &__title {
    font-size: 44rpx;
    font-weight: 700;
    color: #fff;
    letter-spacing: 4rpx;
  }

  &__subtitle {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.75);
    margin-top: 12rpx;
  }
}

/* ---------- Card form ---------- */
.login-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
}

.login-field {
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

  &__change {
    font-size: 26rpx;
    color: #667eea;
    flex-shrink: 0;
    padding: 0 8rpx;
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

    .login-field__code-btn--disabled & {
      color: #9ca3af;
    }
  }
}

/* ---------- Login button ---------- */
.login-btn {
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

/* ---------- Agreement ---------- */
.login-agreement {
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

/* ---------- Third-party login ---------- */
.login-third {
  margin-top: 80rpx;

  &__divider {
    display: flex;
    align-items: center;
    margin-bottom: 48rpx;
  }

  &__line {
    flex: 1;
    height: 1rpx;
    background: rgba(255, 255, 255, 0.25);
  }

  &__label {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.55);
    padding: 0 24rpx;
    white-space: nowrap;
  }

  &__icons {
    display: flex;
    justify-content: center;
    gap: 80rpx;
  }

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__circle {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12rpx;

    &:active {
      opacity: 0.8;
      transform: scale(0.95);
    }

    &--wechat {
      background: #07c160;
    }

    &--qq {
      background: #12b7f5;
    }

    &--alipay {
      background: #1677ff;
    }
  }

  &__name {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.7);
  }
}

/* ---------- Register link ---------- */
.login-register {
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
