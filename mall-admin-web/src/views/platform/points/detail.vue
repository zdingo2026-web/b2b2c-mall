<template>
  <div>
    <h2 class="text-lg font-bold mb-4">积分明细</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索会员昵称/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="typeFilter" placeholder="变动类型" class="w-32" clearable @change="fetchList">
          <el-option label="获得" :value="1" />
          <el-option label="消费" :value="2" />
          <el-option label="过期" :value="3" />
          <el-option label="调整" :value="4" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="memberName" label="会员" min-width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动积分" width="120">
          <template #default="{ row }">
            <span :class="row.changeAmount > 0 ? 'text-green-500' : 'text-red-500'">
              {{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="变动后余额" width="120" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
import { getPointsDetailList } from '@/api/points'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const typeFilter = ref<number | undefined>(undefined)

function typeLabel(type: number): string {
  const map: Record<number, string> = { 1: '获得', 2: '消费', 3: '过期', 4: '调整' }
  return map[type] || '未知'
}

function typeTag(type: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'info', 4: '' }
  return map[type] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getPointsDetailList({
      keyword: keywordFilter.value.trim(),
      type: typeFilter.value,
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
