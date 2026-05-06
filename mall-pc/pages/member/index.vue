<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="profile" />
      <div class="flex-1">
        <!-- User profile card -->
        <div class="bg-white rounded-lg p-6 mb-4">
          <div class="flex items-center gap-6">
            <div class="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center text-3xl text-primary-500 font-bold">
              {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
            </div>
            <div>
              <h2 class="text-xl font-bold">{{ userStore.userInfo?.nickname || '未设置昵称' }}</h2>
              <div class="flex items-center gap-2 mt-1">
                <span v-if="userStore.userInfo?.memberLevel" class="text-xs bg-primary-50 text-primary-500 px-2 py-0.5 rounded">V{{ userStore.userInfo.memberLevel }}会员</span>
                <span class="text-gray-400 text-sm">{{ userStore.userInfo?.phone || '' }}</span>
                <span v-if="userStore.userInfo?.memberNo" class="text-gray-300 text-xs">ID: {{ userStore.userInfo.memberNo }}</span>
              </div>
            </div>
            <button class="ml-auto btn-outline text-sm" @click="showEditProfile = true">编辑资料</button>
          </div>
        </div>

        <!-- Asset info card -->
        <div class="bg-white rounded-lg p-6 mb-4">
          <div class="grid grid-cols-4 gap-4 text-center">
            <div class="cursor-pointer hover:text-primary-500">
              <p class="text-xl font-bold text-primary-500">{{ memberAssets.balance || '0.00' }}</p>
              <p class="text-xs text-gray-500 mt-1">余额</p>
            </div>
            <div class="cursor-pointer hover:text-orange-500">
              <p class="text-xl font-bold text-orange-500">{{ memberAssets.couponCount || 0 }}</p>
              <p class="text-xs text-gray-500 mt-1">优惠券</p>
            </div>
            <div class="cursor-pointer hover:text-green-500">
              <p class="text-xl font-bold text-green-500">{{ memberAssets.points || 0 }}</p>
              <p class="text-xs text-gray-500 mt-1">积分</p>
            </div>
            <div class="cursor-pointer hover:text-red-500">
              <p class="text-xl font-bold text-red-500">{{ memberAssets.redPacketBalance || '0.00' }}</p>
              <p class="text-xs text-gray-500 mt-1">红包</p>
            </div>
          </div>
        </div>

        <!-- Stats grid -->
        <div class="grid grid-cols-4 gap-4 mb-4">
          <NuxtLink to="/member/order?status=0" class="bg-white rounded-lg p-4 text-center hover:shadow-md transition-shadow">
            <p class="text-2xl font-bold text-orange-500">{{ orderCounts.pending }}</p>
            <p class="text-sm text-gray-500 mt-1">待付款</p>
          </NuxtLink>
          <NuxtLink to="/member/order?status=1" class="bg-white rounded-lg p-4 text-center hover:shadow-md transition-shadow">
            <p class="text-2xl font-bold text-primary-500">{{ orderCounts.paid }}</p>
            <p class="text-sm text-gray-500 mt-1">待发货</p>
          </NuxtLink>
          <NuxtLink to="/member/order?status=2" class="bg-white rounded-lg p-4 text-center hover:shadow-md transition-shadow">
            <p class="text-2xl font-bold text-purple-500">{{ orderCounts.shipped }}</p>
            <p class="text-sm text-gray-500 mt-1">待收货</p>
          </NuxtLink>
          <NuxtLink to="/member/order?status=7" class="bg-white rounded-lg p-4 text-center hover:shadow-md transition-shadow">
            <p class="text-2xl font-bold text-primary-500">{{ orderCounts.review || 0 }}</p>
            <p class="text-sm text-gray-500 mt-1">待评价</p>
          </NuxtLink>
        </div>

        <!-- Profile info -->
        <div class="bg-white rounded-lg p-6">
          <h3 class="font-bold mb-4">个人信息</h3>
          <div class="space-y-4">
            <div class="flex items-center gap-4">
              <span class="w-20 text-gray-500 text-sm">头像</span>
              <div class="w-16 h-16 bg-primary-50 rounded-full flex items-center justify-center text-primary-500 text-2xl font-bold">
                {{ userStore.userInfo?.avatar ? '' : (userStore.userInfo?.nickname?.charAt(0) || 'U') }}
              </div>
            </div>
            <div class="flex items-center gap-4">
              <span class="w-20 text-gray-500 text-sm">昵称</span>
              <span class="text-gray-800">{{ userStore.userInfo?.nickname || '未设置' }}</span>
            </div>
            <div class="flex items-center gap-4">
              <span class="w-20 text-gray-500 text-sm">手机号</span>
              <span class="text-gray-800">{{ userStore.userInfo?.phone || '未绑定' }}</span>
            </div>
            <div class="flex items-center gap-4">
              <span class="w-20 text-gray-500 text-sm">邮箱</span>
              <span class="text-gray-800">{{ userStore.userInfo?.email || '未绑定' }}</span>
            </div>
          </div>
          <div class="mt-6 pt-4 border-t">
            <button class="text-red-500 text-sm hover:text-red-600" @click="handleLogout">退出登录</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Profile Modal -->
    <div v-if="showEditProfile" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showEditProfile = false">
      <div class="bg-white rounded-lg w-[480px] p-6">
        <h3 class="font-bold text-lg mb-4">编辑个人信息</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm text-gray-600 mb-1">昵称</label>
            <input v-model="editForm.nickname" class="w-full h-10 px-3 border rounded text-sm" placeholder="请输入昵称" />
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button class="px-6 py-2 border rounded text-sm hover:bg-gray-50" @click="showEditProfile = false">取消</button>
          <button class="btn-primary text-sm" @click="handleSaveProfile">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const userStore = useUserStore()
const { put } = useRequest()

const showEditProfile = ref(false)
const editForm = ref({ nickname: '' })
const orderCounts = ref({ pending: 0, paid: 0, shipped: 0, review: 0 })
const memberAssets = ref({ balance: '0.00', couponCount: 0, points: 0, redPacketBalance: '0.00' })

watch(showEditProfile, (val) => {
  if (val) {
    editForm.value.nickname = userStore.userInfo?.nickname || ''
  }
})

const handleSaveProfile = async () => {
  try {
    await put('/auth/profile', editForm.value)
    await userStore.fetchUserInfo()
    showEditProfile.value = false
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '保存失败'
    alert(msg)
  }
}

const handleLogout = async () => {
  if (!confirm('确定要退出登录吗？')) return
  await userStore.logout()
  navigateTo('/')
}

const loadOrderCounts = async () => {
  try {
    const { get } = useRequest()
    const data = await get<{ pending: number; paid: number; shipped: number; review: number }>('/order/counts')
    orderCounts.value = data
  } catch {
    // ignore
  }
}

const loadMemberAssets = async () => {
  try {
    const { get } = useRequest()
    const data = await get<{ balance: string; couponCount: number; points: number; redPacketBalance: string }>('/member/assets')
    memberAssets.value = data
  } catch {
    // ignore
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUserInfo()
    loadOrderCounts()
    loadMemberAssets()
  }
})
</script>
