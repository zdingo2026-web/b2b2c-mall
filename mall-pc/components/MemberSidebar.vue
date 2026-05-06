<template>
  <aside class="w-48 bg-white rounded-lg p-4 flex-shrink-0">
    <div class="mb-6 text-center pb-4 border-b">
      <div class="w-16 h-16 bg-primary-100 rounded-full mx-auto mb-2 flex items-center justify-center text-2xl text-primary-500">
        {{ userStore.userInfo?.avatar ? '' : 'U' }}
      </div>
      <p class="font-bold text-gray-800 truncate">{{ userStore.nickname || '未登录' }}</p>
    </div>
    <nav class="space-y-1">
      <NuxtLink
        to="/member"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'profile' }"
      >
        个人信息
      </NuxtLink>
      <NuxtLink
        to="/member/order"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'order' }"
      >
        我的订单
      </NuxtLink>
      <NuxtLink
        to="/member/address"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'address' }"
      >
        收货地址
      </NuxtLink>
      <NuxtLink
        to="/member/favorites"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'favorites' }"
      >
        我的收藏
      </NuxtLink>
      <NuxtLink
        to="/member/review"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'review' }"
      >
        我的评价
      </NuxtLink>
      <NuxtLink
        to="/member/footprint"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'footprint' }"
      >
        浏览历史
      </NuxtLink>
      <NuxtLink
        to="/member/bankcard"
        class="block px-4 py-2 rounded text-sm hover:bg-primary-50 hover:text-primary-500"
        :class="{ 'bg-primary-50 text-primary-500 font-bold': activeMenu === 'bankcard' }"
      >
        我的银行卡
      </NuxtLink>
    </nav>
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  activeMenu: 'profile' | 'order' | 'address' | 'favorites' | 'review' | 'footprint' | 'bankcard'
}>()

const userStore = useUserStore()
</script>
