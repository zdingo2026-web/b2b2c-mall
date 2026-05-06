<template>
  <el-container class="h-screen">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="transition-all duration-300">
      <div class="h-full bg-[#304156] flex flex-col">
        <!-- Logo -->
        <div class="h-[56px] flex items-center justify-center border-b border-[#3d4f5f]">
          <h1 v-if="!isCollapsed" class="text-white text-lg font-bold">B2B2C商城</h1>
          <h1 v-else class="text-white text-lg font-bold">M</h1>
        </div>
        <!-- Menu -->
        <el-scrollbar class="flex-1">
          <el-menu
            :default-active="currentRoute"
            :collapse="isCollapsed"
            :collapse-transition="false"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#2563EB"
            router
          >
            <template v-for="menu in permissionStore.menus" :key="menu.path">
              <!-- Menu with children -->
              <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
                <template #title>
                  <el-icon v-if="menu.meta && menu.meta.icon">
                    <component :is="menu.meta.icon" />
                  </el-icon>
                  <span>{{ menu.meta ? menu.meta.title : '' }}</span>
                </template>
                <el-menu-item
                  v-for="child in menu.children"
                  :key="child.path"
                  :index="child.path"
                >
                  <span>{{ child.meta ? child.meta.title : '' }}</span>
                </el-menu-item>
              </el-sub-menu>
              <!-- Menu without children -->
              <el-menu-item v-else :index="menu.path">
                <el-icon v-if="menu.meta && menu.meta.icon">
                  <component :is="menu.meta.icon" />
                </el-icon>
                <template #title>
                  <span>{{ menu.meta ? menu.meta.title : '' }}</span>
                </template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>
      </div>
    </el-aside>

    <!-- Main area -->
    <el-container>
      <!-- Header -->
      <el-header class="h-[56px] flex items-center justify-between border-b bg-white px-4">
        <div class="flex items-center gap-4">
          <el-icon class="cursor-pointer text-xl" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="flex items-center gap-4">
          <el-tag :type="userStore.isAdmin ? 'danger' : 'warning'" size="small">
            {{ userStore.isAdmin ? '平台管理' : '商户管理' }}
          </el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="cursor-pointer flex items-center gap-2">
              <el-avatar :size="32">
                {{ userStore.adminInfo ? userStore.adminInfo.nickname ? userStore.adminInfo.nickname.charAt(0) : 'A' : 'A' }}
              </el-avatar>
              <span class="text-sm">{{ userStore.adminInfo ? userStore.adminInfo.nickname || '管理员' : '管理员' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="bg-[#f0f2f5] p-4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Fold, Expand } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const isCollapsed = ref(false)
const currentRoute = computed(() => route.path)

onMounted(() => {
  userStore.hydrate()
  if (userStore.adminInfo?.role) {
    permissionStore.generateMenus(userStore.adminInfo.role as 'platform' | 'merchant')
  } else {
    userStore.fetchAdminInfo().then(() => {
      if (userStore.adminInfo?.role) {
        permissionStore.generateMenus(userStore.adminInfo.role as 'platform' | 'merchant')
      }
    })
  }
})

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await userStore.logout()
      permissionStore.clearMenus()
      router.push('/login')
    } catch {
      // Cancelled
    }
  }
}
</script>
