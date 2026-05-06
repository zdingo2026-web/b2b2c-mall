<template>
  <div>
    <h2 class="text-lg font-bold mb-6">平台工作台</h2>

    <el-row :gutter="16" class="mb-6">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">今日成交额</p>
            <p class="text-2xl font-bold text-blue-500">&yen;{{ overview.todayOrderAmount ?? '0.00' }}</p>
            <p class="text-xs text-gray-400 mt-1">昨日 &yen;{{ overview.yesterdayOrderAmount ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">今日订单数</p>
            <p class="text-2xl font-bold text-green-500">{{ overview.todayOrderCount ?? 0 }}</p>
            <p class="text-xs text-gray-400 mt-1">昨日 {{ overview.yesterdayOrderCount ?? 0 }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">新增会员</p>
            <p class="text-2xl font-bold text-primary-500">{{ overview.todayNewMember ?? 0 }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">今日访问量</p>
            <p class="text-2xl font-bold text-red-500">{{ overview.todayVisitCount ?? 0 }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <span class="font-bold">待办事项</span>
      </template>
      <el-empty v-if="pendingTasks.length === 0" description="暂无待办事项" />
      <div v-else>
        <div v-for="task in pendingTasks" :key="task.label" class="flex items-center justify-between py-3 border-b last:border-0">
          <span>{{ task.label }}</span>
          <el-button link type="primary" @click="task.action">{{ task.count }} 项待处理</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPlatformOverview, type PlatformOverview } from '@/api/statistics'
import { getTenantList } from '@/api/tenant'
import { getPlatformSpuList } from '@/api/product'

const router = useRouter()
const overview = ref<PlatformOverview>({} as PlatformOverview)
const pendingTasks = ref<{ label: string; count: number; action: () => void }[]>([])

onMounted(async () => {
  const [overviewData, tenants, products] = await Promise.allSettled([
    getPlatformOverview(),
    getTenantList({ status: 0, limit: 1 }),
    getPlatformSpuList({ status: 1, limit: 1 }),
  ])

  if (overviewData.status === 'fulfilled') {
    overview.value = overviewData.value
  }

  const tasks: typeof pendingTasks.value = []
  if (tenants.status === 'fulfilled' && tenants.value.total > 0) {
    tasks.push({
      label: '待审核商家',
      count: tenants.value.total,
      action: () => router.push('/platform/merchant/audit'),
    })
  }
  if (products.status === 'fulfilled' && products.value.total > 0) {
    tasks.push({
      label: '待审核商品',
      count: products.value.total,
      action: () => router.push('/platform/product/list'),
    })
  }
  pendingTasks.value = tasks
})
</script>
