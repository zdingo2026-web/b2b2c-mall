<template>
  <div>
    <h2 class="text-lg font-bold mb-4">商品分类管理</h2>
    <el-card>
      <div class="mb-4">
        <el-button type="primary" @click="handleAdd(null)">新增一级分类</el-button>
      </div>

      <el-tree
        v-loading="loading"
        :data="tree"
        :props="{ label: 'categoryName', children: 'children' }"
        default-expand-all
        node-key="id"
      >
        <template #default="{ node, data }">
          <div class="flex items-center justify-between flex-1 pr-4">
            <span>{{ data.categoryName }}</span>
            <div class="flex gap-2">
              <el-button link type="primary" size="small" @click.stop="handleAdd(data)">添加子分类</el-button>
              <el-button link type="primary" size="small" @click.stop="handleEdit(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!loading && !tree.length" description="暂无分类数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="无（一级分类）"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="图标">
          <ImageUpload v-model="form.icon" />
        </el-form-item>
        <el-form-item label="分类图片">
          <ImageUpload v-model="form.image" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  type CategoryTreeVO,
} from '@/api/product'

const loading = ref(false)
const tree = ref<CategoryTreeVO[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  categoryName: '',
  parentId: 0 as number | string,
  sortOrder: 0,
  icon: '',
  image: '',
  status: 1,
})

const rules: FormRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const parentTreeOptions = computed(() => {
  const root = [{ id: 0, categoryName: '一级分类', children: tree.value }]
  return root
})

async function fetchTree() {
  loading.value = true
  try {
    tree.value = await getCategoryTree()
  } finally {
    loading.value = false
  }
}

function handleAdd(parent: CategoryTreeVO | null) {
  isEdit.value = false
  editId.value = null
  form.value = {
    categoryName: '',
    parentId: parent?.id ?? 0,
    sortOrder: 0,
    icon: '',
    image: '',
    status: 1,
  }
  dialogVisible.value = true
}

function handleEdit(data: CategoryTreeVO) {
  isEdit.value = true
  editId.value = data.id
  form.value = {
    categoryName: data.categoryName,
    parentId: data.parentId,
    sortOrder: data.sortOrder,
    icon: data.icon,
    image: data.image,
    status: data.status,
  }
  dialogVisible.value = true
}

async function handleDelete(data: CategoryTreeVO) {
  await ElMessageBox.confirm(`确定删除分类「${data.categoryName}」？`, '提示', { type: 'warning' })
  await deleteCategory(data.id)
  ElMessage.success('已删除')
  fetchTree()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateCategory(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchTree()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchTree)
</script>
