<template>
  <div>
    <h2 class="text-lg font-bold mb-4">装修页面管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.pageName" placeholder="搜索页面名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.pageType" placeholder="页面类型" class="w-36" clearable>
          <el-option label="首页" value="home" />
          <el-option label="自定义页" value="custom" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="showCreateDialog">新增页面</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pageName" label="页面名称" min-width="160" />
        <el-table-column label="页面类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.pageType === 'home' ? '首页' : '自定义页' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.published ? 'success' : 'info'" size="small">
              {{ row.published ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="!row.published" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑装修页面' : '新增装修页面'" width="800px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="页面名称" prop="pageName">
          <el-input v-model="dialogForm.pageName" placeholder="请输入页面名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="页面类型" prop="pageType">
          <el-select v-model="dialogForm.pageType" placeholder="请选择页面类型" class="w-full">
            <el-option label="首页" value="home" />
            <el-option label="自定义页" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="页面标题" prop="pageTitle">
          <el-input v-model="dialogForm.pageTitle" placeholder="请输入页面标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="组件配置" prop="componentConfig">
          <el-input
            v-model="dialogForm.componentConfig"
            type="textarea"
            :rows="12"
            placeholder='请输入组件配置JSON，例如: [{"type":"banner","data":{}}]'
          />
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
import {
  getDecoPageList,
  getDecoPageDetail,
  saveDecoPage,
  publishDecoPage,
  deleteDecoPage,
} from '@/api/merchant-deco'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  pageName: '',
  pageType: undefined as string | undefined,
  page: 1,
  limit: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getDecoPageList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number>(0)
const submitting = ref(false)
const dialogFormRef = ref<FormInstance>()

const dialogForm = ref({
  pageName: '',
  pageType: 'custom',
  pageTitle: '',
  componentConfig: '[]',
})

const dialogRules: FormRules = {
  pageName: [{ required: true, message: '请输入页面名称', trigger: 'blur' }],
  pageType: [{ required: true, message: '请选择页面类型', trigger: 'change' }],
  componentConfig: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          callback()
          return
        }
        try {
          JSON.parse(value)
          callback()
        } catch {
          callback(new Error('请输入合法的JSON格式'))
        }
      },
      trigger: 'blur',
    },
  ],
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = 0
  dialogForm.value = { pageName: '', pageType: 'custom', pageTitle: '', componentConfig: '[]' }
  dialogVisible.value = true
}

async function showEditDialog(row: any) {
  isEdit.value = true
  editingId.value = row.id
  try {
    const detail = await getDecoPageDetail(row.id)
    dialogForm.value = {
      pageName: detail.pageName,
      pageType: detail.pageType || 'custom',
      pageTitle: detail.pageTitle || '',
      componentConfig: typeof detail.componentConfig === 'string'
        ? detail.componentConfig
        : JSON.stringify(detail.componentConfig || [], null, 2),
    }
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取详情失败')
  }
}

async function handleSubmit() {
  await dialogFormRef.value?.validate()
  submitting.value = true
  try {
    const payload: any = {
      ...dialogForm.value,
      componentConfig: JSON.parse(dialogForm.value.componentConfig || '[]'),
    }
    if (isEdit.value) {
      payload.id = editingId.value
    }
    await saveDecoPage(payload)
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
    dialogVisible.value = false
    fetchList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row: any) {
  await ElMessageBox.confirm(`确定发布页面「${row.pageName}」？发布后将对用户可见。`, '提示', { type: 'warning' })
  await publishDecoPage(row.id)
  ElMessage.success('发布成功')
  fetchList()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除页面「${row.pageName}」？删除后不可恢复！`, '警告', { type: 'warning' })
  await deleteDecoPage(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
