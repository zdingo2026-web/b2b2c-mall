<template>
  <div>
    <h2 class="text-lg font-bold mb-4">拼团活动管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.activityName" placeholder="搜索活动名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.status" placeholder="状态" class="w-32" clearable>
          <el-option label="未开始" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="showCreateDialog">新增拼团活动</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" min-width="160" />
        <el-table-column label="拼团人数" width="100">
          <template #default="{ row }">
            {{ row.groupSize }}人团
          </template>
        </el-table-column>
        <el-table-column label="拼团时效" width="100">
          <template #default="{ row }">
            {{ row.expireHours }}小时
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleEnd(row)">结束</el-button>
            <el-button v-if="row.status === 0" link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑拼团活动' : '新增拼团活动'" width="600px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="dialogForm.activityName" placeholder="请输入活动名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="拼团人数" prop="groupSize">
          <el-input-number v-model="dialogForm.groupSize" :min="2" :max="100" class="w-full" />
        </el-form-item>
        <el-form-item label="拼团时效" prop="expireHours">
          <el-input-number v-model="dialogForm.expireHours" :min="1" :max="168" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">拼团发起后的有效时长（小时）</div>
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
  getMerchantGroupBuyList,
  getMerchantGroupBuyDetail,
  createMerchantGroupBuy,
  updateMerchantGroupBuy,
  deleteMerchantGroupBuy,
  startMerchantGroupBuy,
  endMerchantGroupBuy,
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
    const data = await getMerchantGroupBuyList(queryParams.value)
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
  groupSize: 3,
  expireHours: 24,
  dateRange: null as [Date, Date] | null,
  description: '',
})

const dialogRules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  groupSize: [{ required: true, message: '请输入拼团人数', trigger: 'blur' }],
  expireHours: [{ required: true, message: '请输入拼团时效', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = 0
  dialogForm.value = { activityName: '', groupSize: 3, expireHours: 24, dateRange: null, description: '' }
  dialogVisible.value = true
}

async function showEditDialog(row: any) {
  isEdit.value = true
  editingId.value = row.id
  try {
    const detail = await getMerchantGroupBuyDetail(row.id)
    dialogForm.value = {
      activityName: detail.activityName,
      groupSize: detail.groupSize || 3,
      expireHours: detail.expireHours || 24,
      dateRange: detail.startTime && detail.endTime ? [new Date(detail.startTime), new Date(detail.endTime)] : null,
      description: detail.description || '',
    }
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取详情失败')
  }
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
      await updateMerchantGroupBuy(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantGroupBuy(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleStart(row: any) {
  await ElMessageBox.confirm(`确定开始拼团活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await startMerchantGroupBuy(row.id)
  ElMessage.success('活动已开始')
  fetchList()
}

async function handleEnd(row: any) {
  await ElMessageBox.confirm(`确定结束拼团活动「${row.activityName}」？结束后不可恢复！`, '提示', { type: 'warning' })
  await endMerchantGroupBuy(row.id)
  ElMessage.success('活动已结束')
  fetchList()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除拼团活动「${row.activityName}」？删除后不可恢复！`, '警告', { type: 'warning' })
  await deleteMerchantGroupBuy(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
