<template>
  <div>
    <div class="mb-4 flex items-center gap-4">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2 class="text-lg font-bold">{{ isEdit ? '编辑秒杀活动' : '新增秒杀活动' }}</h2>
    </div>

    <el-card v-loading="pageLoading">
      <el-steps :active="activeStep" finish-status="success" class="mb-6 max-w-xl mx-auto">
        <el-step title="基本信息" />
        <el-step title="活动商品" />
        <el-step title="活动规则" />
      </el-steps>

      <!-- Step 1: 基本信息 -->
      <div v-show="activeStep === 0">
        <el-form ref="step1FormRef" :model="step1Form" :rules="step1Rules" label-width="120px" class="max-w-2xl">
          <el-form-item label="活动名称" prop="activityName">
            <el-input v-model="step1Form.activityName" placeholder="请输入活动名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="活动时间" prop="dateRange">
            <el-date-picker
              v-model="step1Form.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              class="w-full"
            />
          </el-form-item>
          <el-form-item label="活动说明">
            <el-input v-model="step1Form.description" type="textarea" :rows="3" placeholder="请输入活动说明" />
          </el-form-item>
        </el-form>
        <div class="flex justify-end mt-4">
          <el-button type="primary" @click="nextStep(1)">下一步</el-button>
        </div>
      </div>

      <!-- Step 2: 活动商品 -->
      <div v-show="activeStep === 1">
        <el-form ref="step2FormRef" :model="step2Form" :rules="step2Rules" label-width="120px" class="max-w-2xl">
          <el-form-item label="选择商品" prop="productIds">
            <el-select
              v-model="step2Form.productIds"
              multiple
              filterable
              placeholder="请选择参与秒杀的商品"
              class="w-full"
            >
              <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="已选商品">
            <el-tag
              v-for="pid in step2Form.productIds"
              :key="pid"
              closable
              class="mr-2 mb-2"
              @close="removeProduct(pid)"
            >
              {{ getProductName(pid) }}
            </el-tag>
            <span v-if="step2Form.productIds.length === 0" class="text-gray-400 text-sm">暂未选择商品</span>
          </el-form-item>
        </el-form>
        <div class="flex justify-end mt-4 gap-2">
          <el-button @click="activeStep = 0">上一步</el-button>
          <el-button type="primary" @click="nextStep(2)">下一步</el-button>
        </div>
      </div>

      <!-- Step 3: 活动规则 -->
      <div v-show="activeStep === 2">
        <el-form ref="step3FormRef" :model="step3Form" :rules="step3Rules" label-width="120px" class="max-w-2xl">
          <el-form-item label="限购数量" prop="limitPerUser">
            <el-input-number v-model="step3Form.limitPerUser" :min="1" class="w-full" />
            <div class="text-xs text-gray-400 mt-1">每个用户限购数量</div>
          </el-form-item>
          <el-form-item label="秒杀价格" prop="seckillPrice">
            <el-input-number v-model="step3Form.seckillPrice" :min="0" :precision="2" class="w-full" />
            <div class="text-xs text-gray-400 mt-1">统一秒杀价格，如需单独设置请在活动创建后编辑商品</div>
          </el-form-item>
          <el-form-item label="虚拟销量" prop="virtualSales">
            <el-input-number v-model="step3Form.virtualSales" :min="0" class="w-full" />
          </el-form-item>
        </el-form>
        <div class="flex justify-end mt-4 gap-2">
          <el-button @click="activeStep = 1">上一步</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
        </div>
      </div>
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
  getMerchantSeckillDetail,
  createMerchantSeckill,
  updateMerchantSeckill,
} from '@/api/merchant-promotion'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const pageLoading = ref(false)
const submitting = ref(false)
const activeStep = ref(0)

// 模拟商品列表数据
const productList = ref<any[]>([])

// Step 1
const step1FormRef = ref<FormInstance>()
const step1Form = ref({
  activityName: '',
  dateRange: null as [Date, Date] | null,
  description: '',
})

const step1Rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

// Step 2
const step2FormRef = ref<FormInstance>()
const step2Form = ref({
  productIds: [] as number[],
})

const step2Rules: FormRules = {
  productIds: [{ required: true, type: 'array', min: 1, message: '请选择至少一个商品', trigger: 'change' }],
}

// Step 3
const step3FormRef = ref<FormInstance>()
const step3Form = ref({
  limitPerUser: 1,
  seckillPrice: 0,
  virtualSales: 0,
})

const step3Rules: FormRules = {
  limitPerUser: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
}

function getProductName(pid: number) {
  return productList.value.find(p => p.id === pid)?.productName || `商品#${pid}`
}

function removeProduct(pid: number) {
  step2Form.value.productIds = step2Form.value.productIds.filter(id => id !== pid)
}

async function nextStep(step: number) {
  if (step === 1) {
    await step1FormRef.value?.validate()
  } else if (step === 2) {
    await step2FormRef.value?.validate()
  }
  activeStep.value = step
}

async function loadDetail() {
  const id = Number(route.query.id)
  if (!id) return
  pageLoading.value = true
  try {
    const detail = await getMerchantSeckillDetail(id)
    step1Form.value = {
      activityName: detail.activityName,
      dateRange: detail.startTime && detail.endTime ? [new Date(detail.startTime), new Date(detail.endTime)] : null,
      description: detail.description || '',
    }
    step2Form.value.productIds = detail.productIds || []
    step3Form.value = {
      limitPerUser: detail.limitPerUser || 1,
      seckillPrice: detail.seckillPrice || 0,
      virtualSales: detail.virtualSales || 0,
    }
  } catch {
    ElMessage.error('获取秒杀活动详情失败')
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit() {
  await step3FormRef.value?.validate()
  submitting.value = true
  try {
    const payload: any = {
      ...step1Form.value,
      ...step2Form.value,
      ...step3Form.value,
      startTime: step1Form.value.dateRange?.[0]?.toISOString(),
      endTime: step1Form.value.dateRange?.[1]?.toISOString(),
    }
    delete payload.dateRange
    if (isEdit.value) {
      await updateMerchantSeckill(Number(route.query.id), payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantSeckill(payload)
      ElMessage.success('创建成功')
    }
    goBack()
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/merchant/promotion/seckill')
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>
