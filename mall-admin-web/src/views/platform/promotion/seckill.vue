<template>
  <div>
    <h2 class="text-lg font-bold mb-4">秒杀活动管理</h2>
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
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="paymentTimeout" label="支付超时(分)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="activity" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '新增秒杀活动'" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
        </el-form-item>
        <el-form-item label="支付超时(分)" prop="paymentTimeout">
          <el-input-number v-model="form.paymentTimeout" :min="1" :max="60" />
        </el-form-item>
        <el-form-item label="秒杀商品">
          <el-table :data="form.skuList" border size="small">
            <el-table-column prop="skuId" label="SKU ID" width="100">
              <template #default="{ row }">
                <el-input v-model="row.skuId" size="small" placeholder="SKU ID" />
              </template>
            </el-table-column>
            <el-table-column prop="seckillPrice" label="秒杀价" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.seckillPrice" :min="0" :precision="2" size="small" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column prop="seckillStock" label="秒杀库存" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.seckillStock" :min="1" size="small" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column prop="limitPerUser" label="限购数量" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.limitPerUser" :min="1" size="small" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="form.skuList.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link size="small" class="mt-2" @click="form.skuList.push({ skuId: '', seckillPrice: 0, seckillStock: 1, limitPerUser: 1 })">+ 添加SKU</el-button>
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
import { getSeckillList, createSeckill, updateSeckill, deleteSeckill, startSeckill, endSeckill, type SeckillActivity } from '@/api/promotion'

const loading = ref(false)
const tableData = ref<SeckillActivity[]>([])
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
  timeRange: [] as string[],
  paymentTimeout: 15,
  skuList: [] as { skuId: string; seckillPrice: number; seckillStock: number; limitPerUser: number }[],
})

const rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  paymentTimeout: [{ required: true, message: '请输入支付超时时间', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getSeckillList({
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
    timeRange: [],
    paymentTimeout: 15,
    skuList: [],
  }
  dialogVisible.value = true
}

function handleEdit(row: SeckillActivity) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    activityName: row.activityName,
    timeRange: [row.startTime, row.endTime],
    paymentTimeout: row.paymentTimeout,
    skuList: [],
  }
  dialogVisible.value = true
}

async function handleStart(row: SeckillActivity) {
  await ElMessageBox.confirm(`确定开始秒杀活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await startSeckill(row.id as number)
  ElMessage.success('活动已开始')
  fetchList()
}

async function handleEnd(row: SeckillActivity) {
  await ElMessageBox.confirm(`确定结束秒杀活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await endSeckill(row.id as number)
  ElMessage.success('活动已结束')
  fetchList()
}

async function handleDelete(row: SeckillActivity) {
  await ElMessageBox.confirm(`确定删除秒杀活动「${row.activityName}」？`, '提示', { type: 'warning' })
  await deleteSeckill(row.id as number)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const submitData: any = {
      activityName: form.value.activityName,
      startTime: form.value.timeRange[0],
      endTime: form.value.timeRange[1],
      paymentTimeout: form.value.paymentTimeout,
      skuList: form.value.skuList,
    }
    if (isEdit.value && editId.value) {
      await updateSeckill(editId.value as number, submitData)
      ElMessage.success('修改成功')
    } else {
      await createSeckill(submitData)
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
