<template>
  <div>
    <div class="mb-4 flex items-center gap-4">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2 class="text-lg font-bold">{{ isEdit ? '编辑优惠券' : '新增优惠券' }}</h2>
    </div>

    <el-card v-loading="pageLoading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="max-w-2xl">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="form.couponName" placeholder="请输入优惠券名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" class="w-full">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type === 1" label="最低消费" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item v-if="form.type === 1 || form.type === 3" label="优惠金额" prop="discountAmount">
          <el-input-number v-model="form.discountAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="折扣率" prop="discountRate">
          <el-input-number v-model="form.discountRate" :min="0.1" :max="9.9" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="每人限领" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMerchantCouponDetail,
  createMerchantCoupon,
  updateMerchantCoupon,
} from '@/api/merchant-promotion'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const pageLoading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  couponName: '',
  type: 1,
  minAmount: 0,
  discountAmount: 0,
  discountRate: 9.5,
  totalCount: 100,
  limitPerUser: 1,
  dateRange: null as [Date, Date] | null,
})

const rules: FormRules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
}

async function loadDetail() {
  const id = Number(route.query.id)
  if (!id) return
  pageLoading.value = true
  try {
    const detail = await getMerchantCouponDetail(id)
    form.value = {
      couponName: detail.couponName,
      type: detail.type,
      minAmount: detail.minAmount || 0,
      discountAmount: detail.discountAmount || 0,
      discountRate: detail.discountRate || 9.5,
      totalCount: detail.totalCount,
      limitPerUser: detail.limitPerUser || 1,
      dateRange: detail.startTime && detail.endTime ? [new Date(detail.startTime), new Date(detail.endTime)] : null,
    }
  } catch {
    ElMessage.error('获取优惠券详情失败')
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload: any = {
      ...form.value,
      startTime: form.value.dateRange?.[0]?.toISOString(),
      endTime: form.value.dateRange?.[1]?.toISOString(),
    }
    delete payload.dateRange
    if (isEdit.value) {
      await updateMerchantCoupon(Number(route.query.id), payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantCoupon(payload)
      ElMessage.success('创建成功')
    }
    goBack()
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/merchant/promotion/coupon')
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>
