<template>
  <div>
    <h2 class="text-lg font-bold mb-4">积分规则管理</h2>

    <!-- 积分获取规则 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">积分获取规则</span>
          <el-button type="primary" size="small" @click="handleAddRule">新增规则</el-button>
        </div>
      </template>
      <el-table :data="ruleList" stripe v-loading="ruleLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ ruleTypeLabel(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column prop="pointsValue" label="积分值" width="100" />
        <el-table-column prop="multiplier" label="倍率" width="80" />
        <el-table-column prop="dailyLimit" label="每日上限" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" @change="handleToggleRule(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditRule(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 积分消费规则 -->
    <el-card>
      <template #header><span class="font-bold">积分消费规则</span></template>
      <el-form ref="consumeFormRef" :model="consumeForm" label-width="130px" v-loading="consumeLoading">
        <el-form-item label="积分抵扣比例">
          <el-input-number v-model="consumeForm.exchangeRate" :min="1" :max="1000" />
          <span class="ml-2 text-gray-400 text-sm">积分=1元</span>
        </el-form-item>
        <el-form-item label="最大抵扣比例(%)">
          <el-input-number v-model="consumeForm.maxDeductRate" :min="0" :max="100" :precision="1" :step="5" />
          <span class="ml-2 text-gray-400 text-sm">订单金额的最大抵扣百分比</span>
        </el-form-item>
        <el-form-item label="积分有效期">
          <el-radio-group v-model="consumeForm.validityType">
            <el-radio :value="1">永久有效</el-radio>
            <el-radio :value="2">固定天数</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="consumeForm.validityType === 2" label="有效天数">
          <el-input-number v-model="consumeForm.validityDays" :min="1" :max="3650" />
          <span class="ml-2 text-gray-400 text-sm">天</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="consumeSaving" @click="handleSaveConsumeRule">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog v-model="ruleDialogVisible" :title="isEditRule ? '编辑规则' : '新增规则'" width="500px">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleFormRules" label-width="100px">
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" class="w-full">
            <el-option label="签到" :value="1" />
            <el-option label="购物" :value="2" />
            <el-option label="评价" :value="3" />
            <el-option label="分享" :value="4" />
            <el-option label="注册" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="积分值" prop="pointsValue">
          <el-input-number v-model="ruleForm.pointsValue" :min="0" />
        </el-form-item>
        <el-form-item label="倍率" prop="multiplier">
          <el-input-number v-model="ruleForm.multiplier" :min="1" :max="10" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="每日上限" prop="dailyLimit">
          <el-input-number v-model="ruleForm.dailyLimit" :min="0" />
          <span class="ml-2 text-gray-400 text-sm">0表示不限</span>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="ruleForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitting" @click="handleSubmitRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getPointsRuleList,
  savePointsRule,
  togglePointsRule,
  getPointsConsumeRule,
  savePointsConsumeRule,
  type PointsRule,
  type PointsConsumeRule,
} from '@/api/points'

// 获取规则
const ruleLoading = ref(false)
const ruleList = ref<PointsRule[]>([])

async function fetchRuleList() {
  ruleLoading.value = true
  try {
    const data = await getPointsRuleList()
    ruleList.value = data || []
  } finally {
    ruleLoading.value = false
  }
}

function ruleTypeLabel(type: number): string {
  const map: Record<number, string> = { 1: '签到', 2: '购物', 3: '评价', 4: '分享', 5: '注册' }
  return map[type] || '未知'
}

async function handleToggleRule(row: PointsRule) {
  await togglePointsRule(row.id as number)
  ElMessage.success('状态已切换')
  fetchRuleList()
}

// 新增/编辑规则
const ruleDialogVisible = ref(false)
const isEditRule = ref(false)
const ruleSubmitting = ref(false)
const ruleFormRef = ref<FormInstance>()

const ruleForm = ref({
  ruleType: 1,
  ruleName: '',
  pointsValue: 10,
  multiplier: 1,
  dailyLimit: 0,
  sortOrder: 0,
})

const ruleFormRules: FormRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  pointsValue: [{ required: true, message: '请输入积分值', trigger: 'blur' }],
}

function handleAddRule() {
  isEditRule.value = false
  ruleForm.value = { ruleType: 1, ruleName: '', pointsValue: 10, multiplier: 1, dailyLimit: 0, sortOrder: 0 }
  ruleDialogVisible.value = true
}

function handleEditRule(row: PointsRule) {
  isEditRule.value = true
  ruleForm.value = {
    ruleType: row.ruleType,
    ruleName: row.ruleName,
    pointsValue: row.pointsValue,
    multiplier: row.multiplier,
    dailyLimit: row.dailyLimit,
    sortOrder: row.sortOrder,
  }
  ruleDialogVisible.value = true
}

async function handleSubmitRule() {
  await ruleFormRef.value?.validate()
  ruleSubmitting.value = true
  try {
    await savePointsRule(ruleForm.value)
    ElMessage.success('保存成功')
    ruleDialogVisible.value = false
    fetchRuleList()
  } finally {
    ruleSubmitting.value = false
  }
}

// 消费规则
const consumeLoading = ref(false)
const consumeSaving = ref(false)
const consumeForm = ref<PointsConsumeRule>({
  id: 0,
  exchangeRate: 100,
  maxDeductRate: 30,
  validityType: 1,
  validityDays: 365,
})

async function fetchConsumeRule() {
  consumeLoading.value = true
  try {
    const data = await getPointsConsumeRule()
    if (data) consumeForm.value = data
  } finally {
    consumeLoading.value = false
  }
}

async function handleSaveConsumeRule() {
  consumeSaving.value = true
  try {
    await savePointsConsumeRule(consumeForm.value)
    ElMessage.success('保存成功')
  } finally {
    consumeSaving.value = false
  }
}

onMounted(() => {
  fetchRuleList()
  fetchConsumeRule()
})
</script>
