<template>
  <div>
    <h2 class="text-lg font-bold mb-4">新人礼包设置</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="启用新人礼包">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="礼包名称" prop="packName">
          <el-input v-model="form.packName" placeholder="请输入礼包名称" class="w-64" />
        </el-form-item>
        <el-form-item label="礼包描述" prop="packDesc">
          <el-input v-model="form.packDesc" type="textarea" :rows="3" placeholder="请输入礼包描述" class="w-64" />
        </el-form-item>
        <el-form-item label="包含优惠券" prop="couponIds">
          <el-select v-model="form.couponIds" multiple placeholder="请选择优惠券" class="w-96">
            <el-option v-for="item in couponOptions" :key="item.id" :label="item.couponName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="赠送积分">
          <el-input-number v-model="form.pointsGift" :min="0" :step="10" />
          <span class="ml-2 text-gray-400 text-sm">新用户注册后赠送的积分数量</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getNewcomerPack, saveNewcomerPack } from '@/api/promotion'
import { getCouponList } from '@/api/promotion'

const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const couponOptions = ref<any[]>([])

const form = ref({
  enabled: false,
  packName: '',
  packDesc: '',
  couponIds: [] as number[],
  pointsGift: 0,
})

const rules: FormRules = {
  packName: [{ required: true, message: '请输入礼包名称', trigger: 'blur' }],
  packDesc: [{ required: true, message: '请输入礼包描述', trigger: 'blur' }],
}

async function fetchConfig() {
  loading.value = true
  try {
    const data = await getNewcomerPack()
    if (data) {
      form.value = {
        enabled: data.enabled ?? false,
        packName: data.packName ?? '',
        packDesc: data.packDesc ?? '',
        couponIds: data.couponIds ?? [],
        pointsGift: data.pointsGift ?? 0,
      }
    }
  } finally {
    loading.value = false
  }
}

async function fetchCouponOptions() {
  try {
    const data = await getCouponList({ page: 1, limit: 200, status: 1 })
    couponOptions.value = data.list || []
  } catch {
    couponOptions.value = []
  }
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await saveNewcomerPack(form.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
  fetchCouponOptions()
})
</script>
