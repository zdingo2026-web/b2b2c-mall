<template>
  <div>
    <h2 class="text-lg font-bold mb-4">公告管理</h2>
    <el-card>
      <div class="mb-4 flex items-center justify-between">
        <div class="flex gap-4">
          <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
            <el-option label="禁用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="公告内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="noticeTypeTag(row.noticeType)">{{ noticeTypeLabel(row.noticeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新增公告'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公告内容" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="4" placeholder="请输入公告内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择类型" class="w-full">
            <el-option label="活动" :value="1" />
            <el-option label="通知" :value="2" />
            <el-option label="物流" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
import { getNoticeList, createNotice, updateNotice, deleteNotice, type Notice } from '@/api/notice'

const loading = ref(false)
const tableData = ref<Notice[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const statusFilter = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  title: '',
  noticeType: 1,
  status: 1,
  sort: 0,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
}

function noticeTypeLabel(type: number): string {
  const map: Record<number, string> = { 1: '活动', 2: '通知', 3: '物流' }
  return map[type] || '未知'
}

function noticeTypeTag(type: number): string {
  const map: Record<number, string> = { 1: 'danger', 2: 'warning', 3: '' }
  return map[type] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getNoticeList({ page: page.value, limit: limit.value, status: statusFilter.value })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = 0
  form.value = { title: '', noticeType: 1, status: 1, sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row: Notice) {
  isEdit.value = true
  editId.value = row.id
  form.value = { title: row.title, noticeType: row.noticeType, status: row.status, sort: row.sort }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateNotice(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createNotice(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Notice) {
  await ElMessageBox.confirm(`确定删除该公告？`, '提示', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
