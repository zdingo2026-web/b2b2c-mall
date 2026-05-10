<template>
  <div>
    <h2 class="text-lg font-bold mb-4">支付密码重置</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索会员昵称/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待处理" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="memberName" label="会员" min-width="120" show-overflow-tooltip />
        <el-table-column prop="reason" label="重置原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="resetStatusTag(row.status)" size="small">{{ resetStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
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

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝重置申请" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaypwdResetList, approvePaypwdReset, rejectPaypwdReset } from '@/api/member-manage'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const statusFilter = ref<number | undefined>(undefined)

// 拒绝相关
const rejectVisible = ref(false)
const rejectReason = ref('')
const currentRejectId = ref<number>(0)

function resetStatusLabel(status: number): string {
  const map: Record<number, string> = { 0: '待处理', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

function resetStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getPaypwdResetList({
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

async function handleApprove(row: any) {
  await approvePaypwdReset(row.id)
  ElMessage.success('已通过，密码已重置')
  fetchList()
}

function handleReject(row: any) {
  currentRejectId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  await rejectPaypwdReset(currentRejectId.value, rejectReason.value)
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>
