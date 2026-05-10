<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分类佣金配置</h2>
    <el-card>
      <div class="mb-4">
        <el-button type="primary" :loading="saving" @click="handleBatchSave">批量保存</el-button>
        <span class="ml-4 text-gray-400 text-sm">修改后请点击批量保存</span>
      </div>

      <el-table :data="commissionList" stripe v-loading="loading" border>
        <el-table-column prop="categoryId" label="分类ID" width="100" />
        <el-table-column prop="categoryName" label="分类名称" min-width="200" />
        <el-table-column label="一级佣金(%)" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.rateLevel1" :min="0" :max="100" :precision="1" :step="0.5" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="二级佣金(%)" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.rateLevel2" :min="0" :max="100" :precision="1" :step="0.5" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="三级佣金(%)" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.rateLevel3" :min="0" :max="100" :precision="1" :step="0.5" size="small" controls-position="right" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategoryCommissionList, batchSaveCategoryCommission, type CategoryCommission } from '@/api/tenant-manage'

const loading = ref(false)
const saving = ref(false)
const commissionList = ref<CategoryCommission[]>([])

async function fetchList() {
  loading.value = true
  try {
    const data = await getCategoryCommissionList()
    commissionList.value = (data || []).map((item: CategoryCommission) => ({ ...item }))
  } finally {
    loading.value = false
  }
}

async function handleBatchSave() {
  saving.value = true
  try {
    await batchSaveCategoryCommission(commissionList.value)
    ElMessage.success('保存成功')
    fetchList()
  } finally {
    saving.value = false
  }
}

onMounted(fetchList)
</script>
