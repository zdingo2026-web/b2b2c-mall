<template>
  <div>
    <h2 class="text-lg font-bold mb-4">{{ isEdit ? '编辑优惠券' : '新增优惠券' }}</h2>
    <div class="flex gap-6">
      <!-- 左侧表单 -->
      <el-card class="flex-1">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
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
            <el-date-picker v-model="form.validTimeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
          </el-form-item>
          <el-form-item v-if="form.validType === 2" label="有效天数">
            <el-input-number v-model="form.validDays" :min="1" :step="1" />
            <span class="ml-2 text-gray-400 text-sm">领取后有效天数</span>
          </el-form-item>
          <el-form-item label="可叠加秒杀">
            <el-switch v-model="form.canStackSeckill" />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 右侧预览 -->
      <el-card class="w-80 shrink-0">
        <template #header><span class="font-bold">优惠券预览</span></template>
        <div class="border border-dashed border-gray-300 rounded-lg p-4">
          <div class="flex items-center justify-between mb-3">
            <el-tag :type="couponTypeTag(form.couponType)" size="small">{{ couponTypeLabel(form.couponType) }}</el-tag>
            <el-tag v-if="form.applyScope === 1" type="info" size="small">全场通用</el-tag>
          </div>
          <div class="text-center py-4">
            <span class="text-3xl font-bold text-red-500">
              {{ form.couponType === 2 ? form.couponValue + '折' : form.couponValue + '元' }}
            </span>
          </div>
          <p class="text-sm font-medium mb-2">{{ form.couponName || '优惠券名称' }}</p>
          <p class="text-xs text-gray-400 mb-1">
            {{ form.minAmount > 0 ? '满' + form.minAmount + '元可用' : '无门槛' }}
          </p>
          <p class="text-xs text-gray-400 mb-1">
            {{ form.validType === 1 ? (form.validTimeRange?.length === 2 ? form.validTimeRange[0] + ' ~ ' + form.validTimeRange[1] : '请选择有效时间') : '领取后' + form.validDays + '天内有效' }}
          </p>
          <p class="text-xs text-gray-400">每人限领{{ form.limitPerUser }}张</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCouponDetail, createCoupon, updateCoupon } from '@/api/promotion'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
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

async function fetchDetail(id: number) {
  try {
    const data = await getCouponDetail(id)
    if (data) {
      isEdit.value = true
      form.value = {
        couponName: data.couponName,
        couponType: data.couponType,
        couponValue: data.couponValue,
        minAmount: data.minAmount,
        totalCount: data.totalCount,
        limitPerUser: data.limitPerUser,
        applyScope: data.applyScope,
        validType: data.validType,
        validTimeRange: data.validType === 1 ? [data.validStartTime, data.validEndTime] : [],
        validDays: data.validDays || 7,
        canStackSeckill: data.canStackSeckill,
      }
    }
  } catch {
    ElMessage.error('获取优惠券详情失败')
  }
}

function handleBack() {
  router.push('/platform/promotion/coupon')
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
    if (isEdit.value && route.query.id) {
      await updateCoupon(Number(route.query.id), submitData)
      ElMessage.success('修改成功')
    } else {
      await createCoupon(submitData)
      ElMessage.success('创建成功')
    }
    router.push('/platform/promotion/coupon')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (route.query.id) {
    fetchDetail(Number(route.query.id))
  }
})
</script>
