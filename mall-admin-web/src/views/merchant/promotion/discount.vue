<template>
  <div>
    <h2 class="text-lg font-bold mb-4">限时折扣管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.activityName" placeholder="搜索活动名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.status" placeholder="状态" class="w-32" clearable>
          <el-option label="未开始" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="showCreateDialog">新增限时折扣</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" min-width="160" />
        <el-table-column label="折扣力度" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="danger">{{ row.discountRate }}折</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" label="商品数" width="80" />
        <el-table-column label="活动时间" min-width="180">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑限时折扣' : '新增限时折扣'" width="600px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="dialogForm.activityName" placeholder="请输入活动名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-input-number v-model="dialogForm.discountRate" :min="0.1" :max="9.9" :precision="1" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">例如输入8.5表示8.5折</div>
        </el-form-item>
        <el-form-item label="限购数量" prop="limitPerUser">
          <el-input-number v-model="dialogForm.limitPerUser" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="活动时间" prop="dateRange">
          <el-date-picker
            v-model="dialogForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="dialogForm.description" type="textarea" :rows="3" placeholder="请输入活动说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMerchantDiscountList,
  createMerchantDiscount,
  updateMerchantDiscount,
  deleteMerchantDiscount,
} from '@/api/merchant-promotion'

const statusMap: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }

function statusTagType(status: number) {
  if (status === 0) return 'info'
  if (status === 1) return 'success'
  return 'danger'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  activityName: '',
  status: undefined as number | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantDiscountList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number>(0)
const submitting = ref(false)
const dialogFormRef = ref<FormInstance>()

const dialogForm = ref({
  activityName: '',
  discountRate: 8.5,
  limitPerUser: 1,
  dateRange: null as [Date, Date] | null,
  description: '',
})

const dialogRules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = 0
  dialogForm.value = { activityName: '', discountRate: 8.5, limitPerUser: 1, dateRange: null, description: '' }
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEdit.value = true
  editingId.value = row.id
  dialogForm.value = {
    activityName: row.activityName,
    discountRate: row.discountRate || 8.5,
    limitPerUser: row.limitPerUser || 1,
    dateRange: row.startTime && row.endTime ? [new Date(row.startTime), new Date(row.endTime)] : null,
    description: row.description || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value?.validate()
  submitting.value = true
  try {
    const payload: any = {
      ...dialogForm.value,
      startTime: dialogForm.value.dateRange?.[0]?.toISOString(),
      endTime: dialogForm.value.dateRange?.[1]?.toISOString(),
    }
    delete payload.dateRange
    if (isEdit.value) {
      await updateMerchantDiscount(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantDiscount(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除限时折扣「${row.activityName}」？删除后不可恢复！`, '警告', { type: 'warning' })
  await deleteMerchantDiscount(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
