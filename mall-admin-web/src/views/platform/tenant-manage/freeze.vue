<template>
  <div>
    <h2 class="text-lg font-bold mb-4">商家冻结记录</h2>
    <el-card>
      <div class="mb-4 flex items-center justify-between">
        <div class="flex gap-4">
          <el-input v-model="keywordFilter" placeholder="搜索商家名称" class="w-64" clearable @keyup.enter="fetchList" />
          <el-select v-model="actionTypeFilter" placeholder="操作类型" class="w-32" clearable @change="fetchList">
            <el-option label="冻结" :value="1" />
            <el-option label="解冻" :value="2" />
          </el-select>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </div>
        <el-button type="danger" @click="handleFreeze">冻结商家</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenantName" label="商家" min-width="150" show-overflow-tooltip />
        <el-table-column prop="actionType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.actionType === 1 ? 'danger' : 'success'" size="small">
              {{ row.actionType === 1 ? '冻结' : '解冻' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="notifyMerchant" label="通知商家" width="100">
          <template #default="{ row }">
            <el-tag :type="row.notifyMerchant ? 'success' : 'info'" size="small">
              {{ row.notifyMerchant ? '已通知' : '未通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unfreezeTime" label="解冻时间" width="180">
          <template #default="{ row }">{{ row.unfreezeTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.actionType === 1" link type="success" size="small" @click="handleUnfreeze(row)">解冻</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 冻结弹窗 -->
    <el-dialog v-model="freezeVisible" title="冻结商家" width="500px">
      <el-form ref="freezeFormRef" :model="freezeForm" :rules="freezeRules" label-width="100px">
        <el-form-item label="商家" prop="tenantId">
          <el-input v-model.number="freezeForm.tenantId" placeholder="请输入商家ID" />
        </el-form-item>
        <el-form-item label="冻结原因" prop="reason">
          <el-input v-model="freezeForm.reason" type="textarea" :rows="3" placeholder="请输入冻结原因" />
        </el-form-item>
        <el-form-item label="通知商家">
          <el-switch v-model="freezeForm.notifyMerchant" />
        </el-form-item>
        <el-form-item label="自动解冻时间">
          <el-date-picker v-model="freezeForm.unfreezeTime" type="datetime" placeholder="选择自动解冻时间(可选)" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="freezeVisible = false">取消</el-button>
        <el-button type="danger" :loading="freezeSubmitting" @click="confirmFreeze">确定冻结</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getFreezeList, freezeTenant, unfreezeTenant } from '@/api/tenant-manage'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const actionTypeFilter = ref<number | undefined>(undefined)

// 冻结相关
const freezeVisible = ref(false)
const freezeSubmitting = ref(false)
const freezeFormRef = ref<FormInstance>()
const freezeForm = ref({
  tenantId: 0,
  reason: '',
  notifyMerchant: true,
  unfreezeTime: '',
})

const freezeRules: FormRules = {
  tenantId: [{ required: true, message: '请输入商家ID', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入冻结原因', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getFreezeList({
      keyword: keywordFilter.value.trim(),
      actionType: actionTypeFilter.value,
      page: page.value,
      limit: limit.value,
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleFreeze() {
  freezeForm.value = {
    tenantId: 0,
    reason: '',
    notifyMerchant: true,
    unfreezeTime: '',
  }
  freezeVisible.value = true
}

async function confirmFreeze() {
  await freezeFormRef.value?.validate()
  freezeSubmitting.value = true
  try {
    await freezeTenant(freezeForm.value.tenantId, {
      reason: freezeForm.value.reason,
      notifyMerchant: freezeForm.value.notifyMerchant,
      unfreezeTime: freezeForm.value.unfreezeTime,
    })
    ElMessage.success('商家已冻结')
    freezeVisible.value = false
    fetchList()
  } finally {
    freezeSubmitting.value = false
  }
}

async function handleUnfreeze(row: any) {
  await ElMessageBox.confirm(`确定解冻商家「${row.tenantName}」？`, '提示', { type: 'warning' })
  await unfreezeTenant(row.tenantId)
  ElMessage.success('商家已解冻')
  fetchList()
}

onMounted(fetchList)
</script>
