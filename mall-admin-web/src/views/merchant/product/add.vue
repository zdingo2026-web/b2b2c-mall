<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-lg font-bold">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card v-loading="pageLoading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <h3 class="text-base font-bold mb-4 border-b pb-2">基本信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副标题" prop="subTitle">
              <el-input v-model="form.subTitle" placeholder="请输入副标题" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'categoryName', value: 'id', children: 'children' }"
                placeholder="请选择分类"
                check-strictly
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brandId">
              <el-select v-model="form.brandId" placeholder="请选择品牌" clearable class="w-full">
                <el-option v-for="brand in brandList" :key="brand.id" :label="brand.brandName" :value="brand.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品标签" prop="tagType">
              <el-select v-model="form.tagType" placeholder="请选择标签" class="w-full">
                <el-option label="无" :value="0" />
                <el-option label="百亿补贴" :value="1" />
                <el-option label="品牌特卖" :value="2" />
                <el-option label="新品首发" :value="3" />
                <el-option label="以旧换新" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="划线价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :controls="false" placeholder="请输入原价" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <h3 class="text-base font-bold mb-4 mt-6 border-b pb-2">商品图片</h3>
        <el-form-item label="主图" prop="mainImage">
          <ImageUpload v-model="form.mainImage" />
        </el-form-item>
        <el-form-item label="商品图集">
          <ImageListUpload v-model="form.images" :max="9" />
        </el-form-item>

        <h3 class="text-base font-bold mb-4 mt-6 border-b pb-2">商品描述</h3>
        <el-form-item label="商品详情">
          <RichTextEditor v-model="form.description" />
        </el-form-item>

        <h3 class="text-base font-bold mb-4 mt-6 border-b pb-2">SKU规格</h3>
        <div class="mb-4">
          <el-button type="primary" size="small" @click="handleAddSku">添加规格</el-button>
        </div>
        <el-table :data="form.skuList" border size="small" class="mb-4">
          <el-table-column label="规格名称" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.skuName" size="small" placeholder="如：红色-XL" />
            </template>
          </el-table-column>
          <el-table-column label="规格值" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.specValues" size="small" placeholder="如：颜色:红色 尺码:XL" />
            </template>
          </el-table-column>
          <el-table-column label="SKU编码" width="140">
            <template #default="{ row }">
              <el-input v-model="row.skuCode" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="售价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.price" size="small" :min="0" :precision="2" :controls="false" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="原价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.originalPrice" size="small" :min="0" :precision="2" :controls="false" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="库存" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.stock" size="small" :min="0" :controls="false" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="规格图" width="90">
            <template #default="{ row }">
              <ImageUpload v-model="row.image" />
            </template>
          </el-table-column>
          <el-table-column label="重量(kg)" width="110">
            <template #default="{ row }">
              <el-input-number v-model="row.weight" size="small" :min="0" :precision="2" :controls="false" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="form.skuList.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <h3 class="text-base font-bold mb-4 mt-6 border-b pb-2">属性参数</h3>
        <div class="mb-4">
          <el-button type="primary" size="small" @click="handleAddAttribute">添加属性</el-button>
        </div>
        <el-table :data="form.attributeList" border size="small" class="mb-6">
          <el-table-column label="属性名" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.attributeName" size="small" placeholder="如：材质" />
            </template>
          </el-table-column>
          <el-table-column label="属性值" min-width="200">
            <template #default="{ row }">
              <el-input v-model="row.attributeValue" size="small" placeholder="如：纯棉" />
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-select v-model="row.attributeType" size="small">
                <el-option label="规格" :value="0" />
                <el-option label="参数" :value="1" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" fixed="right">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="form.attributeList.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>

      <div class="flex justify-end gap-4">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import ImageUpload from '@/components/ImageUpload.vue'
import ImageListUpload from '@/components/ImageListUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import {
  getMerchantCategoryTree,
  getBrandList,
  createMerchantSpu,
  updateMerchantSpu,
  getMerchantSpuDetail,
  type CategoryTreeVO,
  type ProductBrand,
  type SpuDetailVO,
} from '@/api/product'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.query.id
const pageLoading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const categoryTree = ref<CategoryTreeVO[]>([])
const brandList = ref<ProductBrand[]>([])

const form = ref({
  categoryId: undefined as number | string | undefined,
  brandId: undefined as number | string | undefined,
  productName: '',
  subTitle: '',
  mainImage: '',
  images: [] as string[],
  description: '',
  tagType: 0,
  originalPrice: undefined as number | undefined,
  skuList: [] as {
    skuName: string
    skuCode: string
    specValues: string
    price: string
    originalPrice: number
    stock: number
    image: string
    weight: number
  }[],
  attributeList: [] as {
    attributeId: number
    attributeName: string
    attributeValue: string
    attributeType: number
  }[],
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  mainImage: [{ required: true, message: '请上传主图', trigger: 'change' }],
}

function handleAddSku() {
  form.value.skuList.push({
    skuName: '',
    skuCode: '',
    specValues: '',
    price: '0',
    originalPrice: '0',
    stock: 0,
    image: '',
    weight: '0',
  })
}

function handleAddAttribute() {
  form.value.attributeList.push({
    attributeId: Date.now(),
    attributeName: '',
    attributeValue: '',
    attributeType: 1,
  })
}

async function loadDetail(id: string) {
  pageLoading.value = true
  try {
    const detail: SpuDetailVO = await getMerchantSpuDetail(id)
    form.value = {
      categoryId: detail.categoryId,
      brandId: detail.brandId,
      productName: detail.productName,
      subTitle: detail.subTitle,
      mainImage: detail.mainImage,
      images: detail.images || [],
      description: detail.description,
      tagType: (detail as any).tagType ?? 0,
      originalPrice: (detail as any).originalPrice ?? undefined,
      skuList: detail.skuList?.map((s) => ({
        skuName: s.skuName,
        skuCode: s.skuCode,
        specValues: s.specValues,
        price: s.price,
        originalPrice: s.originalPrice,
        stock: s.stock,
        image: s.image,
        weight: s.weight,
      })) || [],
      attributeList: detail.attributeList?.map((a) => ({
        attributeId: a.attributeId,
        attributeName: a.attributeName,
        attributeValue: a.attributeValue,
        attributeType: a.attributeType,
      })) || [],
    }
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit && route.query.id) {
      await updateMerchantSpu(route.query.id as string, form.value as any)
      ElMessage.success('修改成功')
    } else {
      await createMerchantSpu(form.value as any)
      ElMessage.success('发布成功')
    }
    router.back()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const [tree, brandsData] = await Promise.all([getMerchantCategoryTree(), getBrandList()])
  categoryTree.value = tree
  brandList.value = brandsData.list

  if (isEdit && route.query.id) {
    await loadDetail(route.query.id as string)
  }
})
</script>
