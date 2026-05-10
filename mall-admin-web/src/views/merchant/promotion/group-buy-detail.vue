<template>
  <div>
    <div class="mb-4 flex items-center gap-4">
      <el-button @click="goBack" text>
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <h2 class="text-lg font-bold">{{ isEdit ? '编辑拼团活动' : '新增拼团活动' }}</h2>
    </div>

    <el-card v-loading="pageLoading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="max-w-2xl">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="拼团人数" prop="groupSize">
          <el-input-number v-model="form.groupSize" :min="2" :max="100" class="w-full" />
        </el-form-item>
        <el-form-item label="拼团时效" prop="expireHours">
          <el-input-number v-model="form.expireHours" :min="1" :max="168" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">拼团发起后的有效时长（小时）</div>
        </el-form-item>
        <el-form-item label="拼团优惠价" prop="groupPrice">
          <el-input-number v-model="form.groupPrice" :min="0" :precision="2" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">成团后的优惠价格</div>
        </el-form-item>
        <el-form-item label="限购数量" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="活动时间" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动说明" />
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
  getMerchantGroupBuyDetail,
  createMerchantGroupBuy,
  updateMerchantGroupBuy,
} from '@/api/merchant-promotion'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)
const pageLoading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  activityName: '',
  groupSize: 3,
  expireHours: 24,
  groupPrice: 0,
  limitPerUser: 1,
  dateRange: null as [Date, Date] | null,
  description: '',
})

const rules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  groupSize: [{ required: true, message: '请输入拼团人数', trigger: 'blur' }],
  expireHours: [{ required: true, message: '请输入拼团时效', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
}

async function loadDetail() {
  const id = Number(route.query.id)
  if (!id) return
  pageLoading.value = true
  try {
    const detail = await getMerchantGroupBuyDetail(id)
    form.value = {
      activityName: detail.activityName,
      groupSize: detail.groupSize || 3,
      expireHours: detail.expireHours || 24,
      groupPrice: detail.groupPrice || 0,
      limitPerUser: detail.limitPerUser || 1,
      dateRange: detail.startTime && detail.endTime ? [new Date(detail.startTime), new Date(detail.endTime)] : null,
      description: detail.description || '',
    }
  } catch {
    ElMessage.error('获取拼团活动详情失败')
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
      await updateMerchantGroupBuy(Number(route.query.id), payload)
      ElMessage.success('修改成功')
    } else {
      await createMerchantGroupBuy(payload)
      ElMessage.success('创建成功')
    }
    goBack()
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/merchant/promotion/group-buy')
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>
