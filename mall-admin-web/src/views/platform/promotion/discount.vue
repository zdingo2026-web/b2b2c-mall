<template>
  <div>
    <h2 class="text-lg font-bold mb-4">限时折扣活动</h2>
    <el-card>
      <div class="mb-4 flex items-center justify-between">
        <div class="flex gap-4">
          <el-input v-model="keywordFilter" placeholder="搜索活动名称" class="w-64" clearable @keyup.enter="fetchList" />
          <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">新增折扣活动</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="discountType" label="折扣类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.discountType === 1 ? 'danger' : 'warning'" size="small">
              {{ row.discountType === 1 ? '满减' : '折扣' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountValue" label="优惠值" width="100">
          <template #default="{ row }">
            {{ row.discountType === 1 ? row.discountValue + '元' : row.discountValue + '折' }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="最低消费" width="100">
          <template #default="{ row }">{{ row.minAmount > 0 ? row.minAmount + '元' : '无门槛' }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="activity" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑折扣活动' : '新增折扣活动'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="折扣类型" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio :value="1">满减</el-radio>
            <el-radio :value="2">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">{{ form.discountType === 1 ? '元' : '折扣(1-9.9)' }}</span>
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">0表示无门槛</span>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
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
import StatusTag from '@/components/StatusTag.vue'
import { getDiscountList, createDiscount, updateDiscount, deleteDiscount } from '@/api/promotion'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const statusFilter = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  activityName: '',
  discountType: 1,
  discountValue: 0,
  minAmount: 0,
  timeRange: [] as string[],
})

const rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  discountType: [{ required: true, message: '请选择折扣类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getDiscountList({
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

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = {
    activityName: '',
    discountType: 1,
    discountValue: 0,
    minAmount: 0,
    timeRange: [],
  }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    activityName: row.activityName,
    discountType: row.discountType,
    discountValue: row.discountValue,
    minAmount: row.minAmount,
    timeRange: [row.startTime, row.endTime],
  }
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除折扣活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await deleteDiscount(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const submitData: any = {
      activityName: form.value.activityName,
      discountType: form.value.discountType,
      discountValue: form.value.discountValue,
      minAmount: form.value.minAmount,
      startTime: form.value.timeRange[0],
      endTime: form.value.timeRange[1],
    }
    if (isEdit.value && editId.value) {
      await updateDiscount(editId.value, submitData)
      ElMessage.success('修改成功')
    } else {
      await createDiscount(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
