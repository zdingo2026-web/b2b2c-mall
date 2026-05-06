<template>
  <div>
    <h2 class="text-lg font-bold mb-4">商家列表</h2>
    <el-card>
      <div class="mb-4 flex gap-4 items-center flex-wrap">
        <el-input
          v-model="keywordFilter"
          placeholder="搜索商家名称/联系人/电话"
          class="w-64"
          clearable
          @keyup.enter="fetchList"
        />
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已禁用" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenantName" label="商家名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="tenant" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" size="small" @click="handleScore(row)">评分</el-button>
            <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleDisable(row)">禁用</el-button>
            <el-button v-if="row.status === 3" link type="success" size="small" @click="handleEnable(row)">启用</el-button>
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

    <el-dialog v-model="detailVisible" title="商家详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商家名称">{{ currentRow.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentRow.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号">{{ currentRow.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ currentRow.address }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentRow.status" type="tenant" />
        </el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="scoreVisible" title="商家评分" width="500px">
      <el-form ref="scoreFormRef" :model="scoreForm" label-width="100px">
        <el-form-item label="商品评分">
          <el-input-number v-model="scoreForm.scoreProduct" :min="0" :max="5" :step="0.1" :precision="1" />
          <span class="ml-2 text-gray-400 text-sm">0.0 - 5.0</span>
        </el-form-item>
        <el-form-item label="服务评分">
          <el-input-number v-model="scoreForm.scoreService" :min="0" :max="5" :step="0.1" :precision="1" />
          <span class="ml-2 text-gray-400 text-sm">0.0 - 5.0</span>
        </el-form-item>
        <el-form-item label="物流评分">
          <el-input-number v-model="scoreForm.scoreLogistics" :min="0" :max="5" :step="0.1" :precision="1" />
          <span class="ml-2 text-gray-400 text-sm">0.0 - 5.0</span>
        </el-form-item>
        <el-form-item label="品牌认证">
          <el-switch v-model="scoreForm.brandVerified" :active-value="1" :inactive-value="0" active-text="已认证" inactive-text="未认证" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" :loading="scoreSubmitting" @click="handleSubmitScore">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getTenantList, enableTenant, disableTenant, setTenantScore, type Tenant } from '@/api/tenant'

const loading = ref(false)
const tableData = ref<Tenant[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const statusFilter = ref<number | undefined>(undefined)
const keywordFilter = ref('')

const detailVisible = ref(false)
const currentRow = ref<Tenant | null>(null)

const scoreVisible = ref(false)
const scoreSubmitting = ref(false)
const scoreFormRef = ref<FormInstance>()
const scoreTenantId = ref<number | string>(0)

const scoreForm = ref({
  scoreProduct: 0,
  scoreService: 0,
  scoreLogistics: 0,
  brandVerified: 0,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getTenantList({
      status: statusFilter.value,
      keyword: keywordFilter.value.trim(),
      page: page.value,
      limit: limit.value
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleView(row: Tenant) {
  currentRow.value = row
  detailVisible.value = true
}

function handleScore(row: Tenant) {
  scoreTenantId.value = row.id
  scoreForm.value = {
    scoreProduct: row.scoreProduct ?? 0,
    scoreService: row.scoreService ?? 0,
    scoreLogistics: row.scoreLogistics ?? 0,
    brandVerified: row.brandVerified ?? 0,
  }
  scoreVisible.value = true
}

async function handleSubmitScore() {
  scoreSubmitting.value = true
  try {
    await setTenantScore(scoreTenantId.value, scoreForm.value)
    ElMessage.success('评分已保存')
    scoreVisible.value = false
    fetchList()
  } finally {
    scoreSubmitting.value = false
  }
}

async function handleDisable(row: Tenant) {
  await ElMessageBox.confirm(`确定禁用商家「${row.tenantName}」？`, '提示', { type: 'warning' })
  await disableTenant(row.id)
  ElMessage.success('已禁用')
  fetchList()
}

async function handleEnable(row: Tenant) {
  await enableTenant(row.id)
  ElMessage.success('已启用')
  fetchList()
}

onMounted(fetchList)
</script>
