<template>
  <div>
    <h2 class="text-lg font-bold mb-4">拼团活动管理</h2>
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
        <el-button type="primary" @click="handleAdd">新增活动</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="spuId" label="商品SPU ID" width="120" />
        <el-table-column prop="groupPrice" label="拼团价" width="100">
          <template #default="{ row }">{{ row.groupPrice }}元</template>
        </el-table-column>
        <el-table-column prop="groupNum" label="成团人数" width="100" />
        <el-table-column prop="limitPerUser" label="每人限购" width="100" />
        <el-table-column prop="duration" label="拼团时长(时)" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="activity" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleEnd(row)">结束</el-button>
            <el-button v-if="row.status === 0" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑拼团活动' : '新增拼团活动'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="商品SPU ID" prop="spuId">
          <el-input-number v-model="form.spuId" :min="1" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="拼团价" prop="groupPrice">
          <el-input-number v-model="form.groupPrice" :min="0" :precision="2" controls-position="right" />
          <span class="ml-2 text-gray-400 text-sm">元</span>
        </el-form-item>
        <el-form-item label="成团人数" prop="groupNum">
          <el-input-number v-model="form.groupNum" :min="2" />
        </el-form-item>
        <el-form-item label="每人限购" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" />
        </el-form-item>
        <el-form-item label="拼团时长(时)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="72" />
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
import { getGroupBuyList, createGroupBuy, updateGroupBuy, deleteGroupBuy, startGroupBuy, endGroupBuy, type GroupBuyActivity } from '@/api/promotion'

const loading = ref(false)
const tableData = ref<GroupBuyActivity[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const statusFilter = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  activityName: '',
  spuId: 0,
  groupPrice: 0,
  groupNum: 2,
  limitPerUser: 1,
  duration: 24,
  timeRange: [] as string[],
})

const rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  spuId: [{ required: true, message: '请输入商品SPU ID', trigger: 'blur' }],
  groupPrice: [{ required: true, message: '请输入拼团价', trigger: 'blur' }],
  groupNum: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getGroupBuyList({
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
    spuId: 0,
    groupPrice: 0,
    groupNum: 2,
    limitPerUser: 1,
    duration: 24,
    timeRange: [],
  }
  dialogVisible.value = true
}

function handleEdit(row: GroupBuyActivity) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    activityName: row.activityName,
    spuId: row.spuId,
    groupPrice: row.groupPrice,
    groupNum: row.groupNum,
    limitPerUser: row.limitPerUser,
    duration: row.duration,
    timeRange: [row.startTime, row.endTime],
  }
  dialogVisible.value = true
}

async function handleStart(row: GroupBuyActivity) {
  await ElMessageBox.confirm(`确定开始拼团活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await startGroupBuy(row.id as number)
  ElMessage.success('活动已开始')
  fetchList()
}

async function handleEnd(row: GroupBuyActivity) {
  await ElMessageBox.confirm(`确定结束拼团活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await endGroupBuy(row.id as number)
  ElMessage.success('活动已结束')
  fetchList()
}

async function handleDelete(row: GroupBuyActivity) {
  await ElMessageBox.confirm(`确定删除拼团活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await deleteGroupBuy(row.id as number)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const submitData: any = {
      activityName: form.value.activityName,
      spuId: form.value.spuId,
      groupPrice: form.value.groupPrice,
      groupNum: form.value.groupNum,
      limitPerUser: form.value.limitPerUser,
      duration: form.value.duration,
      startTime: form.value.timeRange[0],
      endTime: form.value.timeRange[1],
    }
    if (isEdit.value && editId.value) {
      await updateGroupBuy(editId.value as number, submitData)
      ElMessage.success('修改成功')
    } else {
      await createGroupBuy(submitData)
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
