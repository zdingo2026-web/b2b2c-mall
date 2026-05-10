<template>
  <div>
    <h2 class="text-lg font-bold mb-4">佣金记录</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索分销员姓名/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="levelFilter" placeholder="佣金等级" class="w-32" clearable @change="fetchList">
          <el-option label="一级" :value="1" />
          <el-option label="二级" :value="2" />
          <el-option label="三级" :value="3" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待结算" :value="0" />
          <el-option label="已结算" :value="1" />
          <el-option label="已冻结" :value="2" />
          <el-option label="已退回" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="distributorName" label="分销员" width="120" />
        <el-table-column prop="commissionAmount" label="佣金金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.commissionAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.level }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="commissionStatusTag(row.status)" size="small">{{ commissionStatusLabel(row.status) }}</el-tag>
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
import { getCommissionList } from '@/api/distribution'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const levelFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)

function commissionStatusLabel(status: number): string {
  const map: Record<number, string> = { 0: '待结算', 1: '已结算', 2: '已冻结', 3: '已退回' }
  return map[status] || '未知'
}

function commissionStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[status] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getCommissionList({
      keyword: keywordFilter.value.trim(),
      level: levelFilter.value,
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
