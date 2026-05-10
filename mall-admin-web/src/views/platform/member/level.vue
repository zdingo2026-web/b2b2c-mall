<template>
  <div>
    <h2 class="text-lg font-bold mb-4">会员等级设置</h2>
    <el-card>
      <div class="mb-4 flex justify-end">
        <el-button type="primary" @click="handleAdd">新增等级</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="levelName" label="等级名称" min-width="120" />
        <el-table-column prop="levelIcon" label="等级图标" width="100">
          <template #default="{ row }">
            <el-image v-if="row.levelIcon" :src="row.levelIcon" class="w-8 h-8" fit="contain" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="requiredGrowth" label="所需成长值" width="120" />
        <el-table-column prop="pointsMultiplier" label="积分倍率" width="100">
          <template #default="{ row }">{{ row.pointsMultiplier }}x</template>
        </el-table-column>
        <el-table-column prop="exclusiveDiscount" label="专属折扣" width="100">
          <template #default="{ row }">{{ row.exclusiveDiscount }}折</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑等级' : '新增等级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="等级图标" prop="levelIcon">
          <el-input v-model="form.levelIcon" placeholder="图标URL" />
        </el-form-item>
        <el-form-item label="所需成长值" prop="requiredGrowth">
          <el-input-number v-model="form.requiredGrowth" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="积分倍率" prop="pointsMultiplier">
          <el-input-number v-model="form.pointsMultiplier" :min="1" :max="10" :precision="1" :step="0.5" />
          <span class="ml-2 text-gray-400 text-sm">倍</span>
        </el-form-item>
        <el-form-item label="专属折扣" prop="exclusiveDiscount">
          <el-input-number v-model="form.exclusiveDiscount" :min="1" :max="9.9" :precision="1" :step="0.1" />
          <span class="ml-2 text-gray-400 text-sm">折</span>
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
import { getMemberLevelList, createMemberLevel, updateMemberLevel, deleteMemberLevel } from '@/api/member-manage'

const loading = ref(false)
const tableData = ref<any[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  levelName: '',
  levelIcon: '',
  requiredGrowth: 0,
  pointsMultiplier: 1,
  exclusiveDiscount: 9.9,
})

const rules: FormRules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  requiredGrowth: [{ required: true, message: '请输入所需成长值', trigger: 'blur' }],
  pointsMultiplier: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }],
  exclusiveDiscount: [{ required: true, message: '请输入专属折扣', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getMemberLevelList()
    tableData.value = Array.isArray(data) ? data : data.list || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = {
    levelName: '',
    levelIcon: '',
    requiredGrowth: 0,
    pointsMultiplier: 1,
    exclusiveDiscount: 9.9,
  }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    levelName: row.levelName,
    levelIcon: row.levelIcon,
    requiredGrowth: row.requiredGrowth,
    pointsMultiplier: row.pointsMultiplier,
    exclusiveDiscount: row.exclusiveDiscount,
  }
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除等级「${row.levelName}」？`, '提示', { type: 'warning' })
  await deleteMemberLevel(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateMemberLevel(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createMemberLevel(form.value)
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
