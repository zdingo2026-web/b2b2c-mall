<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分销设置</h2>
    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="max-w-2xl">
        <el-divider content-position="left">基础设置</el-divider>
        <el-form-item label="启用分销" prop="enabled">
          <el-switch v-model="form.enabled" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="分销层级" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio :value="1">一级分销</el-radio>
            <el-radio :value="2">二级分销</el-radio>
            <el-radio :value="3">三级分销</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">佣金设置</el-divider>
        <el-form-item label="一级佣金比例" prop="commissionRate1">
          <el-input-number v-model="form.commissionRate1" :min="0" :max="100" :precision="1" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">百分比，如10表示10%</div>
        </el-form-item>
        <el-form-item v-if="form.level >= 2" label="二级佣金比例" prop="commissionRate2">
          <el-input-number v-model="form.commissionRate2" :min="0" :max="100" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item v-if="form.level >= 3" label="三级佣金比例" prop="commissionRate3">
          <el-input-number v-model="form.commissionRate3" :min="0" :max="100" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item label="佣金计算方式" prop="commissionType">
          <el-radio-group v-model="form.commissionType">
            <el-radio value="order_amount">按订单金额</el-radio>
            <el-radio value="product_amount">按商品金额</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">提现设置</el-divider>
        <el-form-item label="最低提现金额" prop="minWithdrawAmount">
          <el-input-number v-model="form.minWithdrawAmount" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="提现方式" prop="withdrawMethods">
          <el-checkbox-group v-model="form.withdrawMethods">
            <el-checkbox value="alipay">支付宝</el-checkbox>
            <el-checkbox value="wechat">微信</el-checkbox>
            <el-checkbox value="bank">银行卡</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="提现审核" prop="withdrawAudit">
          <el-switch v-model="form.withdrawAudit" active-text="需要审核" inactive-text="自动通过" />
        </el-form-item>
        <el-form-item label="提现到账周期" prop="withdrawCycle">
          <el-select v-model="form.withdrawCycle" class="w-full">
            <el-option label="即时到账" value="instant" />
            <el-option label="T+1" value="t1" />
            <el-option label="T+3" value="t3" />
            <el-option label="T+7" value="t7" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">其他设置</el-divider>
        <el-form-item label="自动成为分销员" prop="autoBecome">
          <el-switch v-model="form.autoBecome" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="分销商招募说明">
          <el-input v-model="form.recruitDesc" type="textarea" :rows="3" placeholder="请输入分销商招募说明" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMerchantDistributionConfig,
  saveMerchantDistributionConfig,
} from '@/api/merchant-distribution'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  enabled: false,
  level: 2,
  commissionRate1: 10,
  commissionRate2: 5,
  commissionRate3: 2,
  commissionType: 'order_amount',
  minWithdrawAmount: 100,
  withdrawMethods: ['alipay', 'wechat'] as string[],
  withdrawAudit: true,
  withdrawCycle: 't1',
  autoBecome: false,
  recruitDesc: '',
})

const rules: FormRules = {
  commissionRate1: [{ required: true, message: '请输入一级佣金比例', trigger: 'blur' }],
  minWithdrawAmount: [{ required: true, message: '请输入最低提现金额', trigger: 'blur' }],
}

async function loadConfig() {
  loading.value = true
  try {
    const data = await getMerchantDistributionConfig()
    if (data) {
      form.value = {
        enabled: data.enabled ?? false,
        level: data.level ?? 2,
        commissionRate1: data.commissionRate1 ?? 10,
        commissionRate2: data.commissionRate2 ?? 5,
        commissionRate3: data.commissionRate3 ?? 2,
        commissionType: data.commissionType ?? 'order_amount',
        minWithdrawAmount: data.minWithdrawAmount ?? 100,
        withdrawMethods: data.withdrawMethods ?? ['alipay', 'wechat'],
        withdrawAudit: data.withdrawAudit ?? true,
        withdrawCycle: data.withdrawCycle ?? 't1',
        autoBecome: data.autoBecome ?? false,
        recruitDesc: data.recruitDesc ?? '',
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await saveMerchantDistributionConfig(form.value)
    ElMessage.success('保存成功')
  } finally {
    submitting.value = false
  }
}

onMounted(loadConfig)
</script>
