<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分销配置</h2>

    <!-- 分销配置表单 -->
    <el-card class="mb-4">
      <template #header><span class="font-bold">基础配置</span></template>
      <el-form ref="configFormRef" :model="configForm" label-width="120px" v-loading="configLoading">
        <el-form-item label="启用分销">
          <el-switch v-model="configForm.enabled" />
        </el-form-item>
        <el-form-item label="自动审核">
          <el-switch v-model="configForm.autoAudit" />
        </el-form-item>
        <el-form-item label="佣金计算基数">
          <el-select v-model="configForm.commissionBase" class="w-64">
            <el-option label="商品售价" :value="1" />
            <el-option label="实际支付金额" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="一级佣金比例(%)">
          <el-input-number v-model="configForm.rateLevel1" :min="0" :max="100" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="二级佣金比例(%)">
          <el-input-number v-model="configForm.rateLevel2" :min="0" :max="100" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="三级佣金比例(%)">
          <el-input-number v-model="configForm.rateLevel3" :min="0" :max="100" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="最低提现金额">
          <el-input-number v-model="configForm.minWithdraw" :min="0" :precision="2" :step="10" />
          <span class="ml-2 text-gray-400 text-sm">元</span>
        </el-form-item>
        <el-form-item label="佣金冻结天数">
          <el-input-number v-model="configForm.freezeDays" :min="0" :max="90" />
          <span class="ml-2 text-gray-400 text-sm">天</span>
        </el-form-item>
        <el-form-item label="每日提现上限">
          <el-input-number v-model="configForm.dailyWithdrawLimit" :min="0" :precision="2" :step="100" />
          <span class="ml-2 text-gray-400 text-sm">元，0表示不限</span>
        </el-form-item>
        <el-form-item label="提现方式">
          <el-input v-model="configForm.withdrawMethods" placeholder="如: 支付宝,微信" class="w-64" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="configSaving" @click="handleSaveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分销员排行榜 -->
    <el-card class="mb-4">
      <template #header><span class="font-bold">分销员排行榜</span></template>
      <el-table :data="rankList" stripe v-loading="rankLoading">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="totalCommission" label="累计佣金" width="120">
          <template #default="{ row }">{{ row.totalCommission }}元</template>
        </el-table-column>
        <el-table-column prop="orderCount" label="推广订单数" min-width="120" />
      </el-table>
    </el-card>

    <!-- 提现记录 -->
    <el-card>
      <template #header><span class="font-bold">提现记录</span></template>
      <el-table :data="withdrawList" stripe v-loading="withdrawLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="distributorId" label="分销员ID" width="100" />
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="{ row }">{{ row.amount }}元</template>
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
              <el-button link type="success" size="small" @click="handleAuditWithdraw(row, 1)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleRejectWithdraw(row)">拒绝</el-button>
            </template>
            <el-button v-if="row.status === 1" link type="primary" size="small" @click="handleMarkPaid(row)">标记已打款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="withdrawPage"
          v-model:page-size="withdrawLimit"
          layout="total, prev, pager, next"
          :total="withdrawTotal"
          @current-change="fetchWithdrawList"
        />
      </div>
    </el-card>

    <!-- 拒绝提现弹窗 -->
    <el-dialog v-model="rejectWithdrawVisible" title="拒绝提现" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectWithdrawVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRejectWithdraw">确定拒绝</el-button>
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
import {
  getDistributionConfig,
  saveDistributionConfig,
  getWithdrawList,
  auditWithdraw,
  markWithdrawPaid,
  getDistributorRank,
  type DistributionConfig,
  type WithdrawRecord,
} from '@/api/distribution'

// 配置相关
const configLoading = ref(false)
const configSaving = ref(false)
const configForm = ref<DistributionConfig>({
  id: 0,
  enabled: false,
  autoAudit: false,
  commissionBase: 1,
  rateLevel1: 10,
  rateLevel2: 5,
  rateLevel3: 2,
  minWithdraw: 100,
  freezeDays: 7,
  dailyWithdrawLimit: 0,
  withdrawMethods: '支付宝,微信',
})

async function fetchConfig() {
  configLoading.value = true
  try {
    const data = await getDistributionConfig()
    if (data) configForm.value = data
  } finally {
    configLoading.value = false
  }
}

async function handleSaveConfig() {
  configSaving.value = true
  try {
    await saveDistributionConfig(configForm.value)
    ElMessage.success('配置已保存')
  } finally {
    configSaving.value = false
  }
}

// 排行榜相关
const rankLoading = ref(false)
const rankList = ref<any[]>([])

async function fetchRank() {
  rankLoading.value = true
  try {
    const data = await getDistributorRank(10)
    rankList.value = data || []
  } finally {
    rankLoading.value = false
  }
}

// 提现记录相关
const withdrawLoading = ref(false)
const withdrawList = ref<WithdrawRecord[]>([])
const withdrawTotal = ref(0)
const withdrawPage = ref(1)
const withdrawLimit = ref(10)

async function fetchWithdrawList() {
  withdrawLoading.value = true
  try {
    const data = await getWithdrawList({ page: withdrawPage.value, limit: withdrawLimit.value })
    withdrawList.value = data.list
    withdrawTotal.value = data.total
  } finally {
    withdrawLoading.value = false
  }
}

// 审核提现
const rejectWithdrawVisible = ref(false)
const rejectReason = ref('')
const currentWithdrawId = ref<number>(0)

async function handleAuditWithdraw(row: WithdrawRecord, status: number) {
  await auditWithdraw(row.id as number, status)
  ElMessage.success('操作成功')
  fetchWithdrawList()
}

function handleRejectWithdraw(row: WithdrawRecord) {
  currentWithdrawId.value = row.id as number
  rejectReason.value = ''
  rejectWithdrawVisible.value = true
}

async function confirmRejectWithdraw() {
  await auditWithdraw(currentWithdrawId.value, 2, rejectReason.value)
  ElMessage.success('已拒绝')
  rejectWithdrawVisible.value = false
  fetchWithdrawList()
}

// 标记打款
const paidVisible = ref(false)
const paymentRemark = ref('')
const currentPaidId = ref<number>(0)

function handleMarkPaid(row: WithdrawRecord) {
  currentPaidId.value = row.id as number
  paymentRemark.value = ''
  paidVisible.value = true
}

async function confirmMarkPaid() {
  await markWithdrawPaid(currentPaidId.value, paymentRemark.value)
  ElMessage.success('已标记打款')
  paidVisible.value = false
  fetchWithdrawList()
}

onMounted(() => {
  fetchConfig()
  fetchRank()
  fetchWithdrawList()
})
</script>
