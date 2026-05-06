<template>
  <div class="min-h-screen flex flex-col">
    <!-- Header -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4">
        <!-- Top bar -->
        <div class="flex items-center justify-between h-10 text-sm text-gray-500 border-b border-gray-100">
          <div class="flex items-center gap-4">
            <template v-if="userStore.isLoggedIn">
              <span>Hi, {{ userStore.nickname || userStore.phone }}</span>
              <button class="hover:text-primary-500" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <NuxtLink to="/login" class="hover:text-primary-500">登录</NuxtLink>
              <NuxtLink to="/login?mode=register" class="hover:text-primary-500">注册</NuxtLink>
            </template>
          </div>
          <div class="flex items-center gap-4">
            <NuxtLink to="/order/list" class="hover:text-primary-500">我的订单</NuxtLink>
            <NuxtLink to="/member" class="hover:text-primary-500">会员中心</NuxtLink>
          </div>
        </div>
        <!-- Main header -->
        <div class="flex items-center justify-between h-16">
          <NuxtLink to="/" class="text-2xl font-bold text-primary-500">MALL</NuxtLink>
          <!-- Search -->
          <div class="flex-1 max-w-xl mx-8">
            <div class="flex">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索商品"
                class="flex-1 h-10 px-4 border border-r-0 border-gray-300 rounded-l focus:outline-none focus:border-primary-500"
                @keyup.enter="handleSearch"
              />
              <button class="btn-primary rounded-l-none h-10" @click="handleSearch">搜索</button>
            </div>
          </div>
          <!-- Cart -->
          <NuxtLink to="/cart" class="relative flex items-center gap-1 text-gray-600 hover:text-primary-500">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
            </svg>
            <span>购物车</span>
            <span v-if="cartStore.totalCount > 0" class="absolute -top-2 -right-4 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              {{ cartStore.totalCount > 99 ? '99+' : cartStore.totalCount }}
            </span>
          </NuxtLink>
        </div>
        <!-- Category nav -->
        <nav class="flex items-center h-10 text-sm">
          <NuxtLink to="/category" class="px-4 py-2 bg-primary-500 text-white rounded-t hover:bg-primary-600 transition-colors">全部商品分类</NuxtLink>
          <NuxtLink to="/" class="px-4 py-2 hover:text-primary-500 transition-colors" active-class="text-primary-500 font-bold">首页</NuxtLink>
          <NuxtLink to="/product?sort=new" class="px-4 py-2 hover:text-primary-500 transition-colors">新品上架</NuxtLink>
        </nav>
      </div>
    </header>

    <!-- Main content -->
    <main class="flex-1">
      <NuxtPage />
    </main>

    <!-- Footer -->
    <footer class="bg-gray-800 text-gray-400 mt-auto">
      <div class="max-w-7xl mx-auto px-4 py-12">
        <div class="grid grid-cols-4 gap-8">
          <div>
            <h3 class="text-white font-bold mb-4">购物指南</h3>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white">购物流程</a></li>
              <li><a href="#" class="hover:text-white">会员介绍</a></li>
              <li><a href="#" class="hover:text-white">常见问题</a></li>
            </ul>
          </div>
          <div>
            <h3 class="text-white font-bold mb-4">配送方式</h3>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white">上门自提</a></li>
              <li><a href="#" class="hover:text-white">快递运输</a></li>
            </ul>
          </div>
          <div>
            <h3 class="text-white font-bold mb-4">支付方式</h3>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white">微信支付</a></li>
              <li><a href="#" class="hover:text-white">支付宝</a></li>
            </ul>
          </div>
          <div>
            <h3 class="text-white font-bold mb-4">售后服务</h3>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white">退换货政策</a></li>
              <li><a href="#" class="hover:text-white">退换货流程</a></li>
            </ul>
          </div>
        </div>
        <div class="border-t border-gray-700 mt-8 pt-8 text-center text-sm">
          <p>&copy; 2026 B2B2C商城. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    navigateTo(`/search?keyword=${encodeURIComponent(keyword)}`)
  }
}

const handleLogout = async () => {
  if (!confirm('确定要退出登录吗？')) return
  await userStore.logout()
  navigateTo('/')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})
</script>
