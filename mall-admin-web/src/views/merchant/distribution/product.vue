<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分销商品管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.productName" placeholder="搜索商品名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.distributable" placeholder="可分销" class="w-32" clearable>
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="spuId" label="商品ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="200" />
        <el-table-column prop="price" label="商品价格" width="120">
          <template #default="{ row }">
            {{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="一级佣金率" width="120">
          <template #default="{ row }">
            <span v-if="row.commissionRate1">{{ row.commissionRate1 }}%</span>
            <span v-else class="text-gray-400">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="二级佣金率" width="120">
          <template #default="{ row }">
            <span v-if="row.commissionRate2">{{ row.commissionRate2 }}%</span>
            <span v-else class="text-gray-400">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="可分销" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.distributable"
              :loading="row.switching"
              @change="handleSwitchChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">设置佣金</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- Edit Commission Dialog -->
    <el-dialog v-model="dialogVisible" title="设置分销佣金" width="500px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="商品名称">
          <span class="text-gray-600">{{ dialogForm.productName }}</span>
        </el-form-item>
        <el-form-item label="一级佣金率" prop="commissionRate1">
          <el-input-number v-model="dialogForm.commissionRate1" :min="0" :max="100" :precision="1" class="w-full" />
          <div class="text-xs text-gray-400 mt-1">百分比，如10表示10%</div>
        </el-form-item>
        <el-form-item label="二级佣金率" prop="commissionRate2">
          <el-input-number v-model="dialogForm.commissionRate2" :min="0" :max="100" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item label="三级佣金率" prop="commissionRate3">
          <el-input-number v-model="dialogForm.commissionRate3" :min="0" :max="100" :precision="1" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getMerchantDistributionProductList,
  updateMerchantDistributionProduct,
} from '@/api/merchant-distribution'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  productName: '',
  distributable: undefined as number | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantDistributionProductList(queryParams.value)
    tableData.value = (data.list || []).map((item: any) => ({ ...item, switching: false }))
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleSwitchChange(row: any) {
  row.switching = true
  try {
    await updateMerchantDistributionProduct(row.spuId, {
      distributable: row.distributable,
    })
    ElMessage.success(row.distributable ? '已开启分销' : '已关闭分销')
  } catch {
    row.distributable = !row.distributable
  } finally {
    row.switching = false
  }
}

// Edit commission dialog
const dialogVisible = ref(false)
const submitting = ref(false)
const dialogFormRef = ref<FormInstance>()
const editingSpuId = ref<number>(0)

const dialogForm = ref({
  productName: '',
  commissionRate1: 0,
  commissionRate2: 0,
  commissionRate3: 0,
})

const dialogRules: FormRules = {
  commissionRate1: [{ required: true, message: '请输入一级佣金率', trigger: 'blur' }],
}

function showEditDialog(row: any) {
  editingSpuId.value = row.spuId
  dialogForm.value = {
    productName: row.productName,
    commissionRate1: row.commissionRate1 || 0,
    commissionRate2: row.commissionRate2 || 0,
    commissionRate3: row.commissionRate3 || 0,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value?.validate()
  submitting.value = true
  try {
    await updateMerchantDistributionProduct(editingSpuId.value, {
      commissionRate1: dialogForm.value.commissionRate1,
      commissionRate2: dialogForm.value.commissionRate2,
      commissionRate3: dialogForm.value.commissionRate3,
    })
    ElMessage.success('佣金设置成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
