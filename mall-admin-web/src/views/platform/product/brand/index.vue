<template>
  <div>
    <h2 class="text-lg font-bold mb-4">品牌管理</h2>
    <el-card>
      <div class="mb-4">
        <el-button type="primary" @click="handleAdd">新增品牌</el-button>
      </div>

      <el-table :data="brandList" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="brandName" label="品牌名称" min-width="150" />
        <el-table-column prop="logo" label="品牌LOGO" width="100">
          <template #default="{ row }">
            <el-image v-if="row.logo" :src="row.logo" style="width: 40px; height: 40px" fit="contain" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !brandList.length" description="暂无品牌数据" />

      <div class="mt-4 flex justify-end" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="limit"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑品牌' : '新增品牌'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="form.brandName" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="品牌LOGO">
          <ImageUpload v-model="form.logo" />
        </el-form-item>
        <el-form-item label="品牌描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入品牌描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import ImageUpload from '@/components/ImageUpload.vue'
import { getBrandList, createBrand, updateBrand, deleteBrand, type ProductBrand } from '@/api/product'

const loading = ref(false)
const brandList = ref<ProductBrand[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  brandName: '',
  logo: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const rules: FormRules = {
  brandName: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getBrandList({ page: page.value, limit: limit.value })
    brandList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { brandName: '', logo: '', description: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function handleEdit(row: ProductBrand) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    brandName: row.brandName,
    logo: row.logo,
    description: row.description,
    sortOrder: row.sortOrder,
    status: row.status,
  }
  dialogVisible.value = true
}

async function handleDelete(row: ProductBrand) {
  await ElMessageBox.confirm(`确定删除品牌「${row.brandName}」？`, '提示', { type: 'warning' })
  await deleteBrand(row.id)
  ElMessage.success('已删除')
  fetchList()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateBrand(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createBrand(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>
