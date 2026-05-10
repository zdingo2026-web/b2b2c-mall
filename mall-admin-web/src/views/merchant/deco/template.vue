<template>
  <div>
    <h2 class="text-lg font-bold mb-4">模板管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.templateName" placeholder="搜索模板名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.source" placeholder="来源" class="w-36" clearable>
          <el-option label="系统" value="system" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="handleCreate">新建模板</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column label="缩略图" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.thumbnail"
              :src="row.thumbnail"
              fit="cover"
              class="w-20 h-14 rounded border"
              :preview-src-list="[row.thumbnail]"
            />
            <span v-else class="text-gray-400 text-sm">无预览</span>
          </template>
        </el-table-column>
        <el-table-column prop="templateName" label="模板名称" min-width="160" />
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.source === 'system' ? '' : 'success'" size="small">
              {{ row.source === 'system' ? '系统' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="componentCount" label="组件数" width="80" />
        <el-table-column prop="updateTime" label="更新时间" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" size="small" @click="handleUse(row)">使用</el-button>
            <el-button v-if="row.source !== 'system'" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- Create Template Dialog -->
    <el-dialog v-model="createDialogVisible" title="新建模板" width="500px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="createForm.templateName" placeholder="请输入模板名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDecoTemplateList,
  createDecoTemplate,
  getDecoTemplateDetail,
} from '@/api/merchant-deco'

const router = useRouter()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  templateName: '',
  source: undefined as string | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getDecoTemplateList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// Create template
const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()

const createForm = ref({
  templateName: '',
  description: '',
})

const createRules: FormRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
}

function handleCreate() {
  createForm.value = { templateName: '', description: '' }
  createDialogVisible.value = true
}

async function handleCreateSubmit() {
  await createFormRef.value?.validate()
  createSubmitting.value = true
  try {
    const result = await createDecoTemplate({
      ...createForm.value,
      componentConfig: [],
    })
    ElMessage.success('模板创建成功')
    createDialogVisible.value = false
    // 跳转到装修编辑器
    const templateId = result.id || result
    router.push({ path: '/merchant/deco/editor', query: { templateId: String(templateId) } })
  } finally {
    createSubmitting.value = false
  }
}

async function handleEdit(row: any) {
  router.push({ path: '/merchant/deco/editor', query: { templateId: String(row.id) } })
}

async function handleUse(row: any) {
  await ElMessageBox.confirm(`确定使用模板「${row.templateName}」？将基于此模板创建新页面。`, '提示', { type: 'info' })
  try {
    const detail = await getDecoTemplateDetail(row.id)
    router.push({
      path: '/merchant/deco/editor',
      query: { templateId: String(row.id), action: 'use' },
    })
  } catch {
    ElMessage.error('获取模板详情失败')
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除模板「${row.templateName}」？删除后不可恢复！`, '警告', { type: 'warning' })
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
