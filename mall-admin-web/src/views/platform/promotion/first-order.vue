<template>
  <div>
    <h2 class="text-lg font-bold mb-4">首单优惠设置</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="启用首单优惠">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="优惠类型" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio :value="1">满减</el-radio>
            <el-radio :value="2">折扣</el-radio>
            <el-radio :value="3">固定金额减免</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">
            {{ form.discountType === 1 ? '元(满减金额)' : form.discountType === 2 ? '折扣(1-9.9)' : '元(减免金额)' }}
          </span>
        </el-form-item>
        <el-form-item label="最高优惠" prop="maxDiscount">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" :step="1" />
          <span class="ml-2 text-gray-400 text-sm">元，0表示不限</span>
        </el-form-item>
        <el-form-item label="适用范围" prop="applyScope">
          <el-select v-model="form.applyScope" placeholder="请选择适用范围" class="w-64">
            <el-option label="全场通用" :value="1" />
            <el-option label="指定分类" :value="2" />
            <el-option label="指定商品" :value="3" />
          </el-select>
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
import { getFirstOrderConfig, saveFirstOrderConfig } from '@/api/promotion'

const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  enabled: false,
  discountType: 1,
  discountValue: 0,
  maxDiscount: 0,
  applyScope: 1,
})

const rules: FormRules = {
  discountType: [{ required: true, message: '请选择优惠类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  applyScope: [{ required: true, message: '请选择适用范围', trigger: 'change' }],
}

async function fetchConfig() {
  loading.value = true
  try {
    const data = await getFirstOrderConfig()
    if (data) {
      form.value = {
        enabled: data.enabled ?? false,
        discountType: data.discountType ?? 1,
        discountValue: data.discountValue ?? 0,
        maxDiscount: data.maxDiscount ?? 0,
        applyScope: data.applyScope ?? 1,
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await saveFirstOrderConfig(form.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>
