<template>
  <div>
    <h2 class="text-lg font-bold mb-4">优惠券管理</h2>
    <el-card>
      <div class="mb-4 flex items-center justify-between">
        <div class="flex gap-4">
          <el-input v-model="keywordFilter" placeholder="搜索优惠券名称" class="w-64" clearable @keyup.enter="fetchList" />
          <el-select v-model="couponTypeFilter" placeholder="优惠券类型" class="w-32" clearable @change="fetchList">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
            <el-option label="已启用" :value="1" />
            <el-option label="已禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">新增优惠券</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="couponName" label="优惠券名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="couponType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="couponTypeTag(row.couponType)" size="small">{{ couponTypeLabel(row.couponType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="couponValue" label="优惠面额" width="100">
          <template #default="{ row }">
            {{ row.couponType === 2 ? row.couponValue + '折' : row.couponValue + '元' }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="最低消费" width="100">
          <template #default="{ row }">{{ row.minAmount > 0 ? row.minAmount + '元' : '无门槛' }}</template>
        </el-table-column>
        <el-table-column prop="totalCount" label="发放总量" width="100" />
        <el-table-column prop="remainCount" label="剩余数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="coupon" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新增优惠券'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="form.couponName" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="couponType">
          <el-select v-model="form.couponType" placeholder="请选择类型" class="w-full">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠面额" prop="couponValue">
          <el-input-number v-model="form.couponValue" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">{{ form.couponType === 2 ? '折扣(1-9.9)' : '元' }}</span>
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">0表示无门槛</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="每人限领" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="适用范围" prop="applyScope">
          <el-select v-model="form.applyScope" placeholder="请选择适用范围" class="w-full">
            <el-option label="全场通用" :value="1" />
            <el-option label="指定分类" :value="2" />
            <el-option label="指定商品" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期类型" prop="validType">
          <el-radio-group v-model="form.validType">
            <el-radio :value="1">固定时间</el-radio>
            <el-radio :value="2">领取后N天</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.validType === 1" label="有效时间">
          <el-date-picker v-model="form.validTimeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item v-if="form.validType === 2" label="有效天数">
          <el-input-number v-model="form.validDays" :min="1" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">领取后有效天数</span>
        </el-form-item>
        <el-form-item label="可叠加秒杀">
          <el-switch v-model="form.canStackSeckill" />
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
import { getCouponList, createCoupon, updateCoupon, deleteCoupon, type CouponTemplate } from '@/api/promotion'

const loading = ref(false)
const tableData = ref<CouponTemplate[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const couponTypeFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  couponName: '',
  couponType: 1,
  couponValue: 0,
  minAmount: 0,
  totalCount: 100,
  limitPerUser: 1,
  applyScope: 1,
  validType: 1,
  validTimeRange: [] as string[],
  validDays: 7,
  canStackSeckill: false,
})

const rules: FormRules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  couponValue: [{ required: true, message: '请输入优惠面额', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
}

function couponTypeLabel(type: number): string {
  const map: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
  return map[type] || '未知'
}

function couponTypeTag(type: number): string {
  const map: Record<number, string> = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[type] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getCouponList({
      keyword: keywordFilter.value.trim(),
      couponType: couponTypeFilter.value,
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
    couponName: '',
    couponType: 1,
    couponValue: 0,
    minAmount: 0,
    totalCount: 100,
    limitPerUser: 1,
    applyScope: 1,
    validType: 1,
    validTimeRange: [],
    validDays: 7,
    canStackSeckill: false,
  }
  dialogVisible.value = true
}

function handleEdit(row: CouponTemplate) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    couponName: row.couponName,
    couponType: row.couponType,
    couponValue: row.couponValue,
    minAmount: row.minAmount,
    totalCount: row.totalCount,
    limitPerUser: row.limitPerUser,
    applyScope: row.applyScope,
    validType: row.validType,
    validTimeRange: row.validType === 1 ? [row.validStartTime, row.validEndTime] : [],
    validDays: row.validDays || 7,
    canStackSeckill: row.canStackSeckill,
  }
  dialogVisible.value = true
}

async function handleDelete(row: CouponTemplate) {
  await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」？`, '提示', { type: 'warning' })
  await deleteCoupon(row.id as number)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const submitData: any = { ...form.value }
    if (form.value.validType === 1 && form.value.validTimeRange?.length === 2) {
      submitData.validStartTime = form.value.validTimeRange[0]
      submitData.validEndTime = form.value.validTimeRange[1]
    }
    delete submitData.validTimeRange
    if (isEdit.value && editId.value) {
      await updateCoupon(editId.value as number, submitData)
      ElMessage.success('修改成功')
    } else {
      await createCoupon(submitData)
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
