<template>
  <div>
    <h2 class="text-lg font-bold mb-4">商家等级管理</h2>
    <el-card>
      <div class="mb-4">
        <el-button type="primary" @click="handleAdd">新增等级</el-button>
      </div>

      <el-table :data="levelList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="levelName" label="等级名称" min-width="150" />
        <el-table-column prop="levelIcon" label="等级图标" width="100">
          <template #default="{ row }">
            <el-image v-if="row.levelIcon" :src="row.levelIcon" style="width: 30px; height: 30px" fit="contain" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="commissionDiscount" label="佣金折扣(%)" width="130">
          <template #default="{ row }">{{ row.commissionDiscount }}%</template>
        </el-table-column>
        <el-table-column prop="minScore" label="最低评分" width="100" />
        <el-table-column prop="sortWeight" label="排序权重" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑等级' : '新增等级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="等级图标">
          <el-input v-model="form.levelIcon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="佣金折扣(%)" prop="commissionDiscount">
          <el-input-number v-model="form.commissionDiscount" :min="0" :max="100" :precision="1" :step="5" />
          <span class="ml-2 text-gray-400 text-sm">百分比，如80表示8折</span>
        </el-form-item>
        <el-form-item label="最低评分" prop="minScore">
          <el-input-number v-model="form.minScore" :min="0" :max="5" :precision="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="排序权重" prop="sortWeight">
          <el-input-number v-model="form.sortWeight" :min="0" :max="999" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTenantLevelList, createTenantLevel, updateTenantLevel, deleteTenantLevel, type TenantLevel } from '@/api/tenant-manage'

const loading = ref(false)
const levelList = ref<TenantLevel[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  levelName: '',
  levelIcon: '',
  commissionDiscount: 100,
  minScore: 0,
  sortWeight: 0,
})

const rules: FormRules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  commissionDiscount: [{ required: true, message: '请输入佣金折扣', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getTenantLevelList()
    levelList.value = data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { levelName: '', levelIcon: '', commissionDiscount: 100, minScore: 0, sortWeight: 0 }
  dialogVisible.value = true
}

function handleEdit(row: TenantLevel) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    levelName: row.levelName,
    levelIcon: row.levelIcon,
    commissionDiscount: row.commissionDiscount,
    minScore: row.minScore,
    sortWeight: row.sortWeight,
  }
  dialogVisible.value = true
}

async function handleDelete(row: TenantLevel) {
  await ElMessageBox.confirm(`确定删除等级「${row.levelName}」？`, '提示', { type: 'warning' })
  await deleteTenantLevel(row.id as number)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateTenantLevel(editId.value as number, form.value)
      ElMessage.success('修改成功')
    } else {
      await createTenantLevel(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
