<template>
  <div class="h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-purple-600">
    <div class="w-[420px] bg-white rounded-lg shadow-xl p-8">
      <h1 class="text-2xl font-bold text-center mb-2">B2B2C商城管理后台</h1>
      <p class="text-gray-400 text-center text-sm mb-8">请使用管理员账号登录</p>

      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" size="large">
        <el-form-item prop="role">
          <el-segmented v-model="loginForm.role" :options="roleOptions" block size="large" />
        </el-form-item>
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { platformLogin, merchantLogin } from '@/api/auth'
import { setTokens } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

// Explicitly initialize stores (no auto-import)
void userStore
void permissionStore

const formRef = ref<FormInstance>()
const loading = ref(false)

const roleOptions = [
  { label: '平台管理', value: 'platform' },
  { label: '商户管理', value: 'merchant' },
]

const loginForm = reactive({
  username: '',
  password: '',
  role: 'platform' as 'platform' | 'merchant',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const form = formRef.value
  if (!form) return

  await form.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const loginFn = loginForm.role === 'platform' ? platformLogin : merchantLogin
      const data = await loginFn({ username: loginForm.username, password: loginForm.password })

      userStore.token = data.accessToken
      userStore.refreshToken = data.refreshToken
      userStore.userInfo = data.userInfo
      userStore.cachedUserType = data.userInfo.userType
      localStorage.setItem('mall_admin_user_type', String(data.userInfo.userType))
      setTokens(data.accessToken, data.refreshToken)
      permissionStore.generateMenus(loginForm.role)

      const redirect = (router.currentRoute.value.query.redirect as string) ||
        (loginForm.role === 'platform' ? '/platform/dashboard' : '/merchant/dashboard')
      router.push(redirect)
    } catch {
      // Error already handled by request interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>
