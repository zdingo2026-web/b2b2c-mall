<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分销商管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.keyword" placeholder="搜索分销员名称/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.status" placeholder="状态" class="w-32" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="已冻结" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="分销员" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :size="28" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="parentName" label="上级分销员" width="120" />
        <el-table-column prop="orderCount" label="推广订单" width="100" />
        <el-table-column label="累计佣金" width="120">
          <template #default="{ row }">
            <span class="text-red-500 font-bold">{{ row.totalCommission?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="可提现佣金" width="120">
          <template #default="{ row }">
            <span class="text-green-600 font-bold">{{ row.availableCommission?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subCount" label="下级人数" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleFreeze(row)">冻结</el-button>
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handleUnfreeze(row)">解冻</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantDistributorList } from '@/api/merchant-distribution'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantDistributorList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleFreeze(row: any) {
  await ElMessageBox.confirm(`确定冻结分销员「${row.nickname}」？冻结后该分销员将无法继续推广。`, '警告', { type: 'warning' })
  ElMessage.success('已冻结')
  fetchList()
}

async function handleUnfreeze(row: any) {
  await ElMessageBox.confirm(`确定解冻分销员「${row.nickname}」？`, '提示', { type: 'info' })
  ElMessage.success('已解冻')
  fetchList()
}

onMounted(fetchList)
</script>
