<template>
  <div>
    <h2 class="text-lg font-bold mb-4">积分订单</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索订单号/商品名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待发货" :value="0" />
          <el-option label="已发货" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productName" label="商品" min-width="150" show-overflow-tooltip />
        <el-table-column prop="pointsPrice" label="积分" width="100">
          <template #default="{ row }">
            <span class="text-orange-500">{{ row.pointsPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cashPrice" label="现金" width="100">
          <template #default="{ row }">
            {{ row.cashPrice > 0 ? '¥' + row.cashPrice : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusTag(row.status)" size="small">{{ orderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="limit"
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
import { getPointsOrderList } from '@/api/points'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const statusFilter = ref<number | undefined>(undefined)

function orderStatusLabel(status: number): string {
  const map: Record<number, string> = { 0: '待发货', 1: '已发货', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

function orderStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getPointsOrderList({
      keyword: keywordFilter.value.trim(),
      status: statusFilter.value,
      page: page.value,
      limit: limit.value,
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>
