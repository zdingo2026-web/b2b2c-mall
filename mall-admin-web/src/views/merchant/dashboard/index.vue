<template>
  <div>
    <h2 class="text-lg font-bold mb-6">商户工作台</h2>

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
            <p class="text-gray-400 text-sm mb-2">今日客单价</p>
            <p class="text-2xl font-bold text-primary-500">&yen;{{ overview.todayAvgOrderPrice ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">累计成交额</p>
            <p class="text-2xl font-bold text-red-500">&yen;{{ overview.totalOrderAmount ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-6">
      <!-- Trend Chart -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <span class="font-bold">近7天趋势</span>
          </template>
          <div class="trend-chart">
            <div v-for="item in trendData" :key="item.date" class="trend-bar-group">
              <div class="trend-bar-wrapper">
                <div class="trend-amount text-xs text-gray-400 mb-1">&yen;{{ item.orderAmount }}</div>
                <div
                  class="trend-bar"
                  :style="{ height: getBarHeight(item.orderCount) + 'px' }"
                />
                <div class="trend-count text-xs text-center text-blue-500 mt-1">{{ item.orderCount }}</div>
              </div>
              <div class="trend-date text-xs text-gray-400 text-center mt-2">{{ item.date }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Product Ranking -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <span class="font-bold">商品销量排行</span>
          </template>
          <el-table :data="productRanking" size="small" max-height="300">
            <el-table-column label="#" width="40">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="商品" min-width="160">
              <template #default="{ row }">
                <div class="flex items-center gap-2">
                  <el-image :src="row.mainImage" class="w-8 h-8 rounded" fit="cover" />
                  <span class="truncate">{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="销量" width="70" prop="totalSales" />
            <el-table-column label="金额" width="90">
              <template #default="{ row }">&yen;{{ row.totalAmount }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-if="productRanking.length === 0" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <span class="font-bold">待处理事项</span>
      </template>
      <el-empty v-if="pendingTasks.length === 0" description="暂无待处理事项" />
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
import { getMerchantOverview, getMerchantTrend, getMerchantProductRanking, type MerchantOverview, type TrendData, type ProductRank } from '@/api/statistics'
import { getMerchantOrderList } from '@/api/order'

const router = useRouter()
const overview = ref<MerchantOverview>({} as MerchantOverview)
const trendData = ref<TrendData[]>([])
const productRanking = ref<ProductRank[]>([])
const pendingTasks = ref<{ label: string; count: number; action: () => void }[]>([])

const maxOrderCount = ref(1)

function getBarHeight(count: number): number {
  if (maxOrderCount.value === 0) return 0
  return Math.max(4, Math.round((count / maxOrderCount.value) * 120))
}

onMounted(async () => {
  const [overviewData, pendingOrders, trendResult, rankingResult] = await Promise.allSettled([
    getMerchantOverview(),
    getMerchantOrderList({ orderStatus: 1, limit: 1 }),
    getMerchantTrend(7),
    getMerchantProductRanking(),
  ])

  if (overviewData.status === 'fulfilled') {
    overview.value = overviewData.value
  }

  if (trendResult.status === 'fulfilled') {
    trendData.value = trendResult.value
    maxOrderCount.value = Math.max(1, ...trendResult.value.map(t => t.orderCount))
  }

  if (rankingResult.status === 'fulfilled') {
    productRanking.value = rankingResult.value
  }

  const tasks: typeof pendingTasks.value = []
  if (pendingOrders.status === 'fulfilled' && pendingOrders.value.total > 0) {
    tasks.push({
      label: '待发货订单',
      count: pendingOrders.value.total,
      action: () => router.push('/merchant/order'),
    })
  }
  pendingTasks.value = tasks
})
</script>

<style scoped>
.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 180px;
  padding: 0 8px;
}
.trend-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.trend-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  flex: 1;
}
.trend-bar {
  width: 32px;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.3s ease;
}
</style>
