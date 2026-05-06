<template>
  <div>
    <h2 class="text-lg font-bold mb-4">子管理员管理</h2>
    <el-card>
      <div class="mb-4">
        <el-button type="primary" @click="showCreateDialog">添加子管理员</el-button>
      </div>

      <el-table :data="adminList" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column label="角色" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.roleType === 1 ? 'danger' : ''" size="small">
              {{ row.roleType === 1 ? '主管理员' : '子管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="admin" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.roleType !== 1">
              <el-button link type="primary" @click="showEditDialog(row)">编辑</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else class="text-gray-400">--</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑子管理员' : '添加子管理员'" width="500px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="dialogForm.username" placeholder="请输入用户名" maxlength="50" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="dialogForm.password" type="password" placeholder="请输入密码(6-20位)" show-password maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="dialogForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialogForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialogForm.email" placeholder="请输入邮箱" />
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
import StatusTag from '@/components/StatusTag.vue'
import {
  getMerchantAdminList,
  createMerchantAdmin,
  updateMerchantAdmin,
  updateMerchantAdminStatus,
  deleteMerchantAdmin,
  type TenantAdminVO,
} from '@/api/merchant-admin'

const loading = ref(false)
const adminList = ref<TenantAdminVO[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | string>('')
const submitting = ref(false)
const dialogFormRef = ref<FormInstance>()

const dialogForm = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
})

const dialogRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
}

async function fetchList() {
  loading.value = true
  try {
    adminList.value = await getMerchantAdminList()
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = ''
  dialogForm.value = { username: '', password: '', realName: '', phone: '', email: '' }
  dialogVisible.value = true
}

function showEditDialog(row: TenantAdminVO) {
  isEdit.value = true
  editingId.value = row.id
  dialogForm.value = { username: row.username, password: '', realName: row.realName || '', phone: row.phone || '', email: row.email || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateMerchantAdmin(editingId.value, {
        realName: dialogForm.value.realName,
        phone: dialogForm.value.phone,
        email: dialogForm.value.email,
      })
      ElMessage.success('修改成功')
    } else {
      await createMerchantAdmin(dialogForm.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: TenantAdminVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}管理员「${row.username}」吗？`, '提示', { type: 'warning' })
  await updateMerchantAdminStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  fetchList()
}

async function handleDelete(row: TenantAdminVO) {
  await ElMessageBox.confirm(`确定删除管理员「${row.username}」吗？此操作不可恢复。`, '提示', { type: 'danger' })
  await deleteMerchantAdmin(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
