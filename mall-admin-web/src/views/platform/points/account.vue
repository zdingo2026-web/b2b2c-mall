<template>
  <div>
    <h2 class="text-lg font-bold mb-4">积分账户管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索会员昵称/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="memberId" label="会员ID" width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="balance" label="积分余额" width="120">
          <template #default="{ row }">
            <span class="text-orange-500 font-bold">{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalEarned" label="累计获得" width="120" />
        <el-table-column prop="totalSpent" label="累计消费" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
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
import { getPointsAccountList } from '@/api/points'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')

async function fetchList() {
  loading.value = true
  try {
    const data = await getPointsAccountList({
      keyword: keywordFilter.value.trim(),
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
