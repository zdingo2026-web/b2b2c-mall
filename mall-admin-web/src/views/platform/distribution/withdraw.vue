<template>
  <div>
    <h2 class="text-lg font-bold mb-4">提现审核</h2>
    <el-card>
      <div class="mb-4 flex gap-4">
        <el-input v-model="keywordFilter" placeholder="搜索分销员姓名/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已打款" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="distributorName" label="分销员" width="120" />
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="withdrawMethod" label="提现方式" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="withdraw" />
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" @click="handleApprove(row)">审核通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button v-if="row.status === 1" link type="primary" size="small" @click="handleMarkPaid(row)">标记已打款</el-button>
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
    <el-dialog v-model="rejectVisible" title="拒绝提现" width="400px">
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

    <!-- 标记打款弹窗 -->
    <el-dialog v-model="paidVisible" title="标记已打款" width="400px">
      <el-form label-width="80px">
        <el-form-item label="打款备注">
          <el-input v-model="paymentRemark" type="textarea" :rows="3" placeholder="请输入打款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paidVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMarkPaid">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getWithdrawList, auditWithdraw, markWithdrawPaid } from '@/api/distribution'

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

// 打款相关
const paidVisible = ref(false)
const paymentRemark = ref('')
const currentPaidId = ref<number>(0)

async function fetchList() {
  loading.value = true
  try {
    const data = await getWithdrawList({
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
  await auditWithdraw(row.id, 1)
  ElMessage.success('审核通过')
  fetchList()
}

function handleReject(row: any) {
  currentRejectId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  await auditWithdraw(currentRejectId.value, 2, rejectReason.value)
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  fetchList()
}

function handleMarkPaid(row: any) {
  currentPaidId.value = row.id
  paymentRemark.value = ''
  paidVisible.value = true
}

async function confirmMarkPaid() {
  await markWithdrawPaid(currentPaidId.value, paymentRemark.value)
  ElMessage.success('已标记打款')
  paidVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>
