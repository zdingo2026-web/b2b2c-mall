<template>
  <div>
    <h2 class="text-lg font-bold mb-4">退换货管理</h2>
    <el-card>
      <el-table :data="refundList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="refundNo" label="退款单号" width="200" />
        <el-table-column label="退款金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.refundAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="refund" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" @click="handleRefund(row, true)">同意</el-button>
              <el-button link type="danger" size="small" @click="handleRefund(row, false)">拒绝</el-button>
            </template>
            <span v-else class="text-gray-400 text-sm">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchRefunds"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getRefundList, handleRefund as handleRefundApi, type OrderRefund } from '@/api/order'

const loading = ref(false)
const refundList = ref<OrderRefund[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

async function fetchRefunds() {
  loading.value = true
  try {
    const data = await getRefundList({ page: page.value, limit: limit.value })
    refundList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleRefund(row: OrderRefund, pass: boolean) {
  if (pass) {
    await ElMessageBox.confirm('确定同意退款？', '提示', { type: 'warning' })
    await handleRefundApi(row.id, true)
    ElMessage.success('已同意退款')
  } else {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
      inputPattern: /\S+/,
      inputErrorMessage: '请输入拒绝原因',
    })
    await handleRefundApi(row.id, false, reason)
    ElMessage.success('已拒绝退款')
  }
  fetchRefunds()
}

onMounted(fetchRefunds)
</script>
