<template>
  <div>
    <h2 class="text-lg font-bold mb-4">商家结算管理</h2>

    <el-tabs v-model="activeTab">
      <!-- 结算列表 -->
      <el-tab-pane label="结算列表" name="settlement">
        <el-card>
          <div class="mb-4 flex gap-4 items-center flex-wrap">
            <el-input v-model="settleKeyword" placeholder="搜索商家名称" class="w-64" clearable @keyup.enter="fetchSettlementList" />
            <el-select v-model="settleStatusFilter" placeholder="状态" class="w-32" clearable @change="fetchSettlementList">
              <el-option label="待结算" :value="0" />
              <el-option label="已结算" :value="1" />
            </el-select>
            <el-button type="primary" @click="fetchSettlementList">搜索</el-button>
          </div>

          <el-table :data="settlementList" stripe v-loading="settleLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="settlementNo" label="结算单号" width="180" show-overflow-tooltip />
            <el-table-column prop="tenantName" label="商家名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="periodStart" label="结算周期" width="200">
              <template #default="{ row }">{{ row.periodStart }} ~ {{ row.periodEnd }}</template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="80" />
            <el-table-column prop="orderTotalAmount" label="订单总额" width="120">
              <template #default="{ row }">{{ row.orderTotalAmount }}元</template>
            </el-table-column>
            <el-table-column prop="platformCommission" label="平台佣金" width="120">
              <template #default="{ row }">{{ row.platformCommission }}元</template>
            </el-table-column>
            <el-table-column prop="merchantAmount" label="商家金额" width="120">
              <template #default="{ row }">{{ row.merchantAmount }}元</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <StatusTag :status="row.status" type="settle" />
              </template>
            </el-table-column>
            <el-table-column prop="settleTime" label="结算时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" link type="primary" size="small" @click="handleSettle(row)">确认结算</el-button>
                <el-button v-else link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="settlePage"
              v-model:page-size="settleLimit"
              layout="total, prev, pager, next"
              :total="settleTotal"
              @current-change="fetchSettlementList"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 冻结记录 -->
      <el-tab-pane label="冻结记录" name="freeze">
        <el-card>
          <el-table :data="freezeList" stripe v-loading="freezeLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="tenantName" label="商家名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="actionType" label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.actionType === 1 ? 'danger' : 'success'" size="small">
                  {{ row.actionType === 1 ? '冻结' : '解冻' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="notifyMerchant" label="通知商家" width="100">
              <template #default="{ row }">
                <el-tag :type="row.notifyMerchant ? 'success' : 'info'" size="small">{{ row.notifyMerchant ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unfreezeTime" label="解冻时间" width="180" />
            <el-table-column prop="createTime" label="操作时间" width="180" />
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="freezePage"
              v-model:page-size="freezeLimit"
              layout="total, prev, pager, next"
              :total="freezeTotal"
              @current-change="fetchFreezeList"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 入驻配置 -->
      <el-tab-pane label="入驻配置" name="config">
        <el-card>
          <el-form ref="settleConfigFormRef" :model="settleConfigForm" label-width="120px" v-loading="configLoading">
            <el-form-item label="启用结算">
              <el-switch v-model="settleConfigForm.enabled" />
            </el-form-item>
            <el-form-item label="结算须知">
              <el-input v-model="settleConfigForm.settleNotice" type="textarea" :rows="4" placeholder="请输入结算须知" />
            </el-form-item>
            <el-form-item label="结算协议">
              <el-input v-model="settleConfigForm.settleAgreement" type="textarea" :rows="6" placeholder="请输入结算协议内容" />
            </el-form-item>
            <el-form-item label="自动审核">
              <el-switch v-model="settleConfigForm.autoAudit" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="configSaving" @click="handleSaveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 结算详情弹窗 -->
    <el-dialog v-model="detailVisible" title="结算详情" width="600px">
      <el-descriptions v-if="currentSettlement" :column="2" border>
        <el-descriptions-item label="结算单号">{{ currentSettlement.settlementNo }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentSettlement.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="结算周期">{{ currentSettlement.periodStart }} ~ {{ currentSettlement.periodEnd }}</el-descriptions-item>
        <el-descriptions-item label="订单数">{{ currentSettlement.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="订单总额">{{ currentSettlement.orderTotalAmount }}元</el-descriptions-item>
        <el-descriptions-item label="平台佣金">{{ currentSettlement.platformCommission }}元</el-descriptions-item>
        <el-descriptions-item label="商家金额">{{ currentSettlement.merchantAmount }}元</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentSettlement.status" type="settle" />
        </el-descriptions-item>
        <el-descriptions-item label="结算时间">{{ currentSettlement.settleTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import {
  getSettlementList,
  settleSettlement,
  getFreezeList,
  getSettleConfig,
  saveSettleConfig,
  type TenantSettlement,
  type TenantFreezeRecord,
  type TenantSettleConfig,
} from '@/api/tenant-manage'

const activeTab = ref('settlement')

// 结算列表
const settleLoading = ref(false)
const settlementList = ref<TenantSettlement[]>([])
const settleTotal = ref(0)
const settlePage = ref(1)
const settleLimit = ref(10)
const settleKeyword = ref('')
const settleStatusFilter = ref<number | undefined>(undefined)

const detailVisible = ref(false)
const currentSettlement = ref<TenantSettlement | null>(null)

async function fetchSettlementList() {
  settleLoading.value = true
  try {
    const data = await getSettlementList({
      keyword: settleKeyword.value.trim(),
      status: settleStatusFilter.value,
      page: settlePage.value,
      limit: settleLimit.value,
    })
    settlementList.value = data.list
    settleTotal.value = data.total
  } finally {
    settleLoading.value = false
  }
}

async function handleSettle(row: TenantSettlement) {
  await ElMessageBox.confirm(`确定结算商家「${row.tenantName}」的结算单？`, '提示', { type: 'warning' })
  await settleSettlement(row.id as number)
  ElMessage.success('结算成功')
  fetchSettlementList()
}

function handleViewDetail(row: TenantSettlement) {
  currentSettlement.value = row
  detailVisible.value = true
}

// 冻结记录
const freezeLoading = ref(false)
const freezeList = ref<TenantFreezeRecord[]>([])
const freezeTotal = ref(0)
const freezePage = ref(1)
const freezeLimit = ref(10)

async function fetchFreezeList() {
  freezeLoading.value = true
  try {
    const data = await getFreezeList({
      page: freezePage.value,
      limit: freezeLimit.value,
    })
    freezeList.value = data.list
    freezeTotal.value = data.total
  } finally {
    freezeLoading.value = false
  }
}

// 入驻配置
const configLoading = ref(false)
const configSaving = ref(false)
const settleConfigForm = ref<TenantSettleConfig>({
  id: 0,
  enabled: true,
  settleNotice: '',
  settleAgreement: '',
  autoAudit: false,
})

async function fetchSettleConfig() {
  configLoading.value = true
  try {
    const data = await getSettleConfig()
    if (data) settleConfigForm.value = data
  } finally {
    configLoading.value = false
  }
}

async function handleSaveConfig() {
  configSaving.value = true
  try {
    await saveSettleConfig(settleConfigForm.value)
    ElMessage.success('配置已保存')
  } finally {
    configSaving.value = false
  }
}

// 切换tab时加载数据
watch(activeTab, (val) => {
  if (val === 'settlement') fetchSettlementList()
  else if (val === 'freeze') fetchFreezeList()
  else if (val === 'config') fetchSettleConfig()
})

onMounted(fetchSettlementList)
</script>
