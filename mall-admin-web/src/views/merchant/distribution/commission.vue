<template>
  <div>
    <h2 class="text-lg font-bold mb-4">佣金记录</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.orderNo" placeholder="搜索订单号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.status" placeholder="佣金状态" class="w-36" clearable>
          <el-option label="待结算" :value="0" />
          <el-option label="已结算" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="distributorName" label="分销员" width="120" />
        <el-table-column prop="level" label="分销层级" width="100">
          <template #default="{ row }">
            <el-tag size="small">第{{ row.level }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderAmount" label="订单金额" width="120">
          <template #default="{ row }">
            {{ row.orderAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="佣金金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500 font-bold">{{ row.commission?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="commissionRate" label="佣金比例" width="100">
          <template #default="{ row }">
            {{ row.commissionRate }}%
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column prop="settleTime" label="结算时间" min-width="160" />
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
import { getMerchantCommissionList } from '@/api/merchant-distribution'

const statusMap: Record<number, string> = { 0: '待结算', 1: '已结算', 2: '已取消' }

function statusTagType(status: number) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'info'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  orderNo: '',
  status: undefined as number | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantCommissionList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>
