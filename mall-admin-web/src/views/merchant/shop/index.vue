<template>
  <div>
    <h2 class="text-lg font-bold mb-4">店铺设置</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="max-w-lg">
        <el-form-item label="店铺名称" prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="请输入店铺名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="店铺Logo">
          <ImageUpload v-model="form.logo" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入店铺描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-divider content-position="left">联系信息</el-divider>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { getMerchantShop, updateMerchantShop, type Tenant } from '@/api/tenant'

const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  tenantName: '',
  logo: '',
  description: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
})

const rules: FormRules = {
  tenantName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

async function loadShopInfo() {
  loading.value = true
  try {
    const data: Tenant = await getMerchantShop()
    form.value = {
      tenantName: data.tenantName,
      logo: data.logo || '',
      description: data.description || '',
      contactName: data.contactName || '',
      contactPhone: data.contactPhone || '',
      contactEmail: data.contactEmail || '',
      address: data.address || '',
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await updateMerchantShop(form.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadShopInfo)
</script>
