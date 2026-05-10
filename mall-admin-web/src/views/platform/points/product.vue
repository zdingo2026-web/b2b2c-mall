<template>
  <div>
    <h2 class="text-lg font-bold mb-4">积分商品管理</h2>

    <!-- 积分分类管理 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">积分分类</span>
          <el-button type="primary" size="small" @click="handleAddCategory">新增分类</el-button>
        </div>
      </template>
      <el-table :data="categoryList" stripe v-loading="categoryLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" min-width="200" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditCategory(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 积分商品列表 -->
    <el-card>
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">积分商品</span>
          <el-button type="primary" @click="handleAddProduct">新增商品</el-button>
        </div>
      </template>
      <div class="mb-4 flex gap-4 items-center">
        <el-select v-model="categoryFilter" placeholder="分类" class="w-40" clearable @change="fetchProductList">
          <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchProductList">
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchProductList">搜索</el-button>
      </div>

      <el-table :data="productList" stripe v-loading="productLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productImage" label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.productImage" :src="row.productImage" style="width: 40px; height: 40px" fit="contain" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeType" label="兑换方式" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.exchangeType === 1 ? '纯积分' : '积分+现金' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsPrice" label="积分价格" width="100" />
        <el-table-column prop="cashPrice" label="现金价格" width="100">
          <template #default="{ row }">{{ row.cashPrice > 0 ? row.cashPrice + '元' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="pointsProduct" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditProduct(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggleProductStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="productPage"
          v-model:page-size="productLimit"
          layout="total, prev, pager, next"
          :total="productTotal"
          @current-change="fetchProductList"
        />
      </div>
    </el-card>

    <!-- 分类弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="isEditCategory ? '编辑分类' : '新增分类'" width="400px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="handleSubmitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品弹窗 -->
    <el-dialog v-model="productDialogVisible" :title="isEditProduct ? '编辑积分商品' : '新增积分商品'" width="550px">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" class="w-full">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="productForm.productImage" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="兑换方式" prop="exchangeType">
          <el-radio-group v-model="productForm.exchangeType">
            <el-radio :value="1">纯积分</el-radio>
            <el-radio :value="2">积分+现金</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分价格" prop="pointsPrice">
          <el-input-number v-model="productForm.pointsPrice" :min="1" />
        </el-form-item>
        <el-form-item v-if="productForm.exchangeType === 2" label="现金价格" prop="cashPrice">
          <el-input-number v-model="productForm.cashPrice" :min="0" :precision="2" />
          <span class="ml-2 text-gray-400 text-sm">元</span>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSubmitting" @click="handleSubmitProduct">确定</el-button>
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
  getPointsCategoryList,
  savePointsCategory,
  deletePointsCategory,
  getPointsProductList,
  createPointsProduct,
  updatePointsProduct,
  deletePointsProduct,
  updatePointsProductStatus,
  type PointsProduct,
} from '@/api/points'

// 分类相关
const categoryLoading = ref(false)
const categoryList = ref<any[]>([])
const categoryDialogVisible = ref(false)
const isEditCategory = ref(false)
const categorySubmitting = ref(false)
const categoryFormRef = ref<FormInstance>()
const editCategoryId = ref<number | string | null>(null)

const categoryForm = ref({ categoryName: '' })
const categoryRules: FormRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

async function fetchCategoryList() {
  categoryLoading.value = true
  try {
    const data = await getPointsCategoryList()
    categoryList.value = data || []
  } finally {
    categoryLoading.value = false
  }
}

function handleAddCategory() {
  isEditCategory.value = false
  editCategoryId.value = null
  categoryForm.value = { categoryName: '' }
  categoryDialogVisible.value = true
}

function handleEditCategory(row: any) {
  isEditCategory.value = true
  editCategoryId.value = row.id
  categoryForm.value = { categoryName: row.categoryName }
  categoryDialogVisible.value = true
}

async function handleSubmitCategory() {
  await categoryFormRef.value?.validate()
  categorySubmitting.value = true
  try {
    const data: any = { ...categoryForm.value }
    if (isEditCategory.value && editCategoryId.value) data.id = editCategoryId.value
    await savePointsCategory(data)
    ElMessage.success('保存成功')
    categoryDialogVisible.value = false
    fetchCategoryList()
  } finally {
    categorySubmitting.value = false
  }
}

async function handleDeleteCategory(row: any) {
  await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」？`, '提示', { type: 'warning' })
  await deletePointsCategory(row.id as number)
  ElMessage.success('删除成功')
  fetchCategoryList()
}

// 商品相关
const productLoading = ref(false)
const productList = ref<PointsProduct[]>([])
const productTotal = ref(0)
const productPage = ref(1)
const productLimit = ref(10)
const categoryFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)

const productDialogVisible = ref(false)
const isEditProduct = ref(false)
const productSubmitting = ref(false)
const productFormRef = ref<FormInstance>()
const editProductId = ref<number | string | null>(null)

const productForm = ref({
  categoryId: 0,
  productName: '',
  productImage: '',
  exchangeType: 1,
  pointsPrice: 100,
  cashPrice: 0,
  stock: 100,
})

const productRules: FormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  exchangeType: [{ required: true, message: '请选择兑换方式', trigger: 'change' }],
  pointsPrice: [{ required: true, message: '请输入积分价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

async function fetchProductList() {
  productLoading.value = true
  try {
    const data = await getPointsProductList({
      categoryId: categoryFilter.value,
      status: statusFilter.value,
      page: productPage.value,
      limit: productLimit.value,
    })
    productList.value = data.list
    productTotal.value = data.total
  } finally {
    productLoading.value = false
  }
}

function handleAddProduct() {
  isEditProduct.value = false
  editProductId.value = null
  productForm.value = {
    categoryId: 0,
    productName: '',
    productImage: '',
    exchangeType: 1,
    pointsPrice: 100,
    cashPrice: 0,
    stock: 100,
  }
  productDialogVisible.value = true
}

function handleEditProduct(row: PointsProduct) {
  isEditProduct.value = true
  editProductId.value = row.id
  productForm.value = {
    categoryId: row.categoryId,
    productName: row.productName,
    productImage: row.productImage,
    exchangeType: row.exchangeType,
    pointsPrice: row.pointsPrice,
    cashPrice: row.cashPrice,
    stock: row.stock,
  }
  productDialogVisible.value = true
}

async function handleSubmitProduct() {
  await productFormRef.value?.validate()
  productSubmitting.value = true
  try {
    if (isEditProduct.value && editProductId.value) {
      await updatePointsProduct(editProductId.value as number, productForm.value)
      ElMessage.success('修改成功')
    } else {
      await createPointsProduct(productForm.value)
      ElMessage.success('创建成功')
    }
    productDialogVisible.value = false
    fetchProductList()
  } finally {
    productSubmitting.value = false
  }
}

async function handleToggleProductStatus(row: PointsProduct) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  await ElMessageBox.confirm(`确定${action}该商品？`, '提示', { type: 'warning' })
  await updatePointsProductStatus(row.id as number, newStatus)
  ElMessage.success(`已${action}`)
  fetchProductList()
}

async function handleDeleteProduct(row: PointsProduct) {
  await ElMessageBox.confirm(`确定删除积分商品「${row.productName}」？`, '提示', { type: 'warning' })
  await deletePointsProduct(row.id as number)
  ElMessage.success('删除成功')
  fetchProductList()
}

onMounted(() => {
  fetchCategoryList()
  fetchProductList()
})
</script>
