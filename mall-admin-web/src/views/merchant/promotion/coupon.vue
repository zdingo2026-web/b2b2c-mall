<template>
  <div>
    <h2 class="text-lg font-bold mb-4">优惠券管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.couponName" placeholder="搜索优惠券名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.type" placeholder="优惠券类型" class="w-36" clearable>
          <el-option label="满减券" :value="1" />
          <el-option label="折扣券" :value="2" />
          <el-option label="无门槛券" :value="3" />
        </el-select>
        <el-select v-model="queryParams.status" placeholder="状态" class="w-32" clearable>
          <el-option label="未开始" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="showCreateDialog">新增优惠券</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="couponName" label="优惠券名称" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ couponTypeMap[row.type] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" min-width="140">
          <template #default="{ row }">
            <span v-if="row.type === 1">满{{ row.minAmount }}减{{ row.discountAmount }}</span>
            <span v-else-if="row.type === 2">{{ row.discountRate }}折</span>
            <span v-else-if="row.type === 3">立减{{ row.discountAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="发放总量" width="100" />
        <el-table-column prop="usedCount" label="已使用" width="80" />
        <el-table-column label="有效期" min-width="180">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] || '未知' }}</el-tag>
          </template>
        </el-table-column>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新增优惠券'" width="600px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="dialogForm.couponName" placeholder="请输入优惠券名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="dialogForm.type" placeholder="请选择类型" class="w-full">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="dialogForm.type === 1" label="最低消费" prop="minAmount">
          <el-input-number v-model="dialogForm.minAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item v-if="dialogForm.type === 1 || dialogForm.type === 3" label="优惠金额" prop="discountAmount">
          <el-input-number v-model="dialogForm.discountAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item v-if="dialogForm.type === 2" label="折扣率" prop="discountRate">
          <el-input-number v-model="dialogForm.discountRate" :min="0.1" :max="9.9" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="dialogForm.totalCount" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="每人限领" prop="limitPerUser">
          <el-input-number v-model="dialogForm.limitPerUser" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="dialogForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="w-full"
          />
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
  getMerchantCouponList,
  getMerchantCouponDetail,
  createMerchantCoupon,
  updateMerchantCoupon,
  deleteMerchantCoupon,
} from '@/api/merchant-promotion'

const couponTypeMap: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
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
  couponName: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantCouponList(queryParams.value)
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
  couponName: '',
  type: 1,
  minAmount: 0,
  discountAmount: 0,
  discountRate: 9.5,
  totalCount: 100,
  limitPerUser: 1,
  dateRange: null as [Date, Date] | null,
})

const dialogRules: FormRules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = 0
  dialogForm.value = {
    couponName: '',
    type: 1,
    minAmount: 0,
    discountAmount: 0,
    discountRate: 9.5,
    totalCount: 100,
    limitPerUser: 1,
    dateRange: null,
  }
  dialogVisible.value = true
}

async function showEditDialog(row: any) {
  isEdit.value = true
  editingId.value = row.id
  try {
    const detail = await getMerchantCouponDetail(row.id)
    dialogForm.value = {
      couponName: detail.couponName,
      type: detail.type,
      minAmount: detail.minAmount || 0,
      discountAmount: detail.discountAmount || 0,
      discountRate: detail.discountRate || 9.5,
      totalCount: detail.totalCount,
      limitPerUser: detail.limitPerUser || 1,
      dateRange: detail.startTime && detail.endTime ? [new Date(detail.startTime), new Date(detail.endTime)] : null,
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
      await updateMerchantCoupon(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantCoupon(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」？删除后不可恢复！`, '警告', { type: 'warning' })
  await deleteMerchantCoupon(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
