<template>
  <div>
    <h2 class="text-lg font-bold mb-4">入驻审核</h2>
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenantName" label="商家名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="businessLicense" label="营业执照号" width="180" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column prop="applyStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applyStatus)" size="small">
              {{ getStatusText(row.applyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <template v-if="row.applyStatus === 0">
              <el-button link type="success" size="small" @click="handleAudit(row, true)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleAudit(row, false)">拒绝</el-button>
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

    <el-dialog v-model="detailVisible" title="商家资质详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商家名称">{{ currentRow.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentRow.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号">{{ currentRow.businessLicense }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentRow.address }}</el-descriptions-item>
        <el-descriptions-item label="营业执照" :span="2">
          <el-image
            v-if="currentRow.licenseImage"
            :src="currentRow.licenseImage"
            :preview-src-list="[currentRow.licenseImage]"
            style="width: 200px; height: 140px"
            fit="contain"
          />
          <span v-else>未上传</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态" :span="2">
          <el-tag :type="getStatusType(currentRow.applyStatus)" size="small">
            {{ getStatusText(currentRow.applyStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow.auditRemark" label="审核备注" :span="2">{{ currentRow.auditRemark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="currentRow && currentRow.applyStatus === 0">
          <el-button type="success" @click="handleAudit(currentRow, true)">通过</el-button>
          <el-button type="danger" @click="handleAudit(currentRow, false)">拒绝</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantApplyList, auditTenantApply, type TenantApply } from '@/api/tenant'

const loading = ref(false)
const tableData = ref<TenantApply[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

const detailVisible = ref(false)
const currentRow = ref<TenantApply | null>(null)

function getStatusType(status: number) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

function getStatusText(status: number) {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getTenantApplyList({ status: 0, page: page.value, limit: limit.value })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleView(row: TenantApply) {
  currentRow.value = row
  detailVisible.value = true
}

async function handleAudit(row: TenantApply, pass: boolean) {
  if (pass) {
    await auditTenantApply(row.id, true)
    ElMessage.success('审核通过')
  } else {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入拒绝原因',
    })
    await auditTenantApply(row.id, false, reason)
    ElMessage.success('已拒绝')
  }
  detailVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>
