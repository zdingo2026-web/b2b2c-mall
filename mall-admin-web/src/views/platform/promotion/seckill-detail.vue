<template>
  <div>
    <h2 class="text-lg font-bold mb-4">{{ isEdit ? '编辑秒杀活动' : '新增秒杀活动' }}</h2>
    <el-card>
      <el-steps :active="activeStep" finish-status="success" class="mb-6">
        <el-step title="活动信息" />
        <el-step title="商品配置" />
      </el-steps>

      <!-- 第一步: 活动信息 -->
      <div v-show="activeStep === 0">
        <el-form ref="step1FormRef" :model="form" :rules="step1Rules" label-width="110px">
          <el-form-item label="活动名称" prop="activityName">
            <el-input v-model="form.activityName" placeholder="请输入活动名称" />
          </el-form-item>
          <el-form-item label="活动时间" prop="timeRange">
            <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
          </el-form-item>
          <el-form-item label="支付超时(分)" prop="paymentTimeout">
            <el-input-number v-model="form.paymentTimeout" :min="1" :max="60" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleNext">下一步</el-button>
            <el-button @click="handleBack">返回</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步: 商品配置 -->
      <div v-show="activeStep === 1">
        <el-table :data="form.skuList" border size="small" class="mb-4">
          <el-table-column prop="skuId" label="SKU ID" width="120">
            <template #default="{ row }">
              <el-input v-model="row.skuId" size="small" placeholder="SKU ID" />
            </template>
          </el-table-column>
          <el-table-column prop="seckillPrice" label="秒杀价" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.seckillPrice" :min="0" :precision="2" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column prop="seckillStock" label="秒杀库存" width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.seckillStock" :min="1" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column prop="limitPerUser" label="限购数量" width="140">
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
        <el-button type="primary" link size="small" @click="addSkuRow">+ 添加SKU</el-button>

        <div class="mt-6">
          <el-button @click="activeStep = 0">上一步</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSeckillDetail, createSeckill, updateSeckill } from '@/api/promotion'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const activeStep = ref(0)
const submitting = ref(false)
const step1FormRef = ref<FormInstance>()

interface SkuItem {
  skuId: string
  seckillPrice: number
  seckillStock: number
  limitPerUser: number
}

const form = ref({
  activityName: '',
  timeRange: [] as string[],
  paymentTimeout: 15,
  skuList: [] as SkuItem[],
})

const step1Rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  paymentTimeout: [{ required: true, message: '请输入支付超时时间', trigger: 'blur' }],
}

function addSkuRow() {
  form.value.skuList.push({ skuId: '', seckillPrice: 0, seckillStock: 1, limitPerUser: 1 })
}

async function handleNext() {
  await step1FormRef.value?.validate()
  activeStep.value = 1
}

async function fetchDetail(id: number) {
  try {
    const data = await getSeckillDetail(id)
    if (data) {
      isEdit.value = true
      form.value = {
        activityName: data.activityName,
        timeRange: [data.startTime, data.endTime],
        paymentTimeout: data.paymentTimeout,
        skuList: data.skuList || [],
      }
    }
  } catch {
    ElMessage.error('获取秒杀活动详情失败')
  }
}

function handleBack() {
  router.push('/platform/promotion/seckill')
}

async function handleSubmit() {
  submitting.value = true
  try {
    const submitData: any = {
      activityName: form.value.activityName,
      startTime: form.value.timeRange[0],
      endTime: form.value.timeRange[1],
      paymentTimeout: form.value.paymentTimeout,
      skuList: form.value.skuList,
    }
    if (isEdit.value && route.query.id) {
      await updateSeckill(Number(route.query.id), submitData)
      ElMessage.success('修改成功')
    } else {
      await createSeckill(submitData)
      ElMessage.success('创建成功')
    }
    router.push('/platform/promotion/seckill')
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
