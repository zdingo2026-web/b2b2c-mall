<template>
  <div>
    <h2 class="text-lg font-bold mb-4">会员管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-button type="primary" @click="fetchList">刷新</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="会员" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-avatar :src="row.avatar" :size="36">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div>
                <p class="font-medium">{{ row.nickname || '-' }}</p>
                <p class="text-xs text-gray-400">{{ row.phone || row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="memberNo" label="会员编号" width="130" />
        <el-table-column prop="memberLevel" label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.memberLevel ?? '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="红包余额" width="100">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.redPacketBalance ?? '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="couponCount" label="优惠券数" width="100" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="member" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
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
import StatusTag from '@/components/StatusTag.vue'
import { getMemberList, type MemberVO } from '@/api/member'

const loading = ref(false)
const tableData = ref<MemberVO[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

async function fetchList() {
  loading.value = true
  try {
    const data = await getMemberList({ page: page.value, limit: limit.value })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>
