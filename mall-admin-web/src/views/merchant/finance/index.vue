<template>
  <div>
    <h2 class="text-lg font-bold mb-4">资金管理</h2>

    <!-- Balance Cards -->
    <el-row :gutter="16" class="mb-6">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">可用余额</p>
            <p class="text-2xl font-bold text-blue-500">&yen;{{ balance.availableBalance ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">累计结算</p>
            <p class="text-2xl font-bold text-green-500">&yen;{{ balance.totalSettled ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <p class="text-gray-400 text-sm mb-2">待提现</p>
            <p class="text-2xl font-bold text-orange-500">&yen;{{ balance.pendingWithdrawal ?? '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Settlement Records -->
    <el-card>
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">结算记录</span>
          <el-button type="primary" @click="showWithdrawDialog">申请提现</el-button>
        </div>
      </template>

      <el-table :data="settleList" stripe v-loading="settleLoading">
        <el-table-column prop="settleNo" label="结算单号" width="200" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">&yen;{{ row.orderAmount }}</template>
        </el-table-column>
        <el-table-column label="佣金" width="120">
          <template #default="{ row }">&yen;{{ row.commissionAmount }}</template>
        </el-table-column>
        <el-table-column label="结算金额" width="120">
          <template #default="{ row }">
            <span class="text-green-500 font-bold">&yen;{{ row.settleAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="settle" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="settlePage"
          v-model:page-size="settleLimit"
          layout="total, prev, pager, next"
          :total="settleTotal"
          @current-change="fetchSettles"
        />
      </div>
    </el-card>

    <!-- Withdraw Dialog -->
    <el-dialog v-model="withdrawVisible" title="申请提现" width="400px" destroy-on-close>
      <el-form ref="withdrawFormRef" :model="withdrawForm" :rules="withdrawRules" label-width="80px">
        <el-form-item label="提现金额" prop="amount">
          <el-input-number v-model="withdrawForm.amount" :min="100" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <p class="text-gray-400 text-xs">最低提现金额：100元</p>
      </el-form>
      <template #footer>
        <el-button @click="withdrawVisible = false">取消</el-button>
        <el-button type="primary" :loading="withdrawSubmitting" @click="handleWithdraw">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getMerchantBalance, getMerchantSettleList, requestWithdraw, type BalanceVO, type TenantSettleVO } from '@/api/merchant-finance'

// Balance
const balance = ref<BalanceVO>({} as BalanceVO)

// Settlement
const settleLoading = ref(false)
const settleList = ref<TenantSettleVO[]>([])
const settleTotal = ref(0)
const settlePage = ref(1)
const settleLimit = ref(10)

// Withdraw dialog
const withdrawVisible = ref(false)
const withdrawSubmitting = ref(false)
const withdrawFormRef = ref<FormInstance>()
const withdrawForm = ref({ amount: 100 })
const withdrawRules: FormRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
}

async function fetchBalance() {
  const data = await getMerchantBalance()
  balance.value = data
}

async function fetchSettles() {
  settleLoading.value = true
  try {
    const data = await getMerchantSettleList({ page: settlePage.value, limit: settleLimit.value })
    settleList.value = data.list
    settleTotal.value = data.total
  } finally {
    settleLoading.value = false
  }
}

function showWithdrawDialog() {
  withdrawForm.value.amount = 100
  withdrawVisible.value = true
}

async function handleWithdraw() {
  await withdrawFormRef.value?.validate()
  withdrawSubmitting.value = true
  try {
    await requestWithdraw(withdrawForm.value.amount)
    ElMessage.success('提现申请已提交')
    withdrawVisible.value = false
    fetchBalance()
    fetchSettles()
  } finally {
    withdrawSubmitting.value = false
  }
}

onMounted(() => {
  fetchBalance()
  fetchSettles()
})
</script>
