<template>
  <div>
    <h2 class="text-lg font-bold mb-4">自营商品管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.keyword" placeholder="搜索商品名称" class="w-64" clearable @keyup.enter="fetchList" />
        <el-tree-select
          v-model="queryParams.categoryId"
          :data="categoryOptions"
          :props="{ label: 'categoryName', value: 'id', children: 'children' }"
          placeholder="选择分类"
          clearable
          check-strictly
          class="w-48"
        />
        <el-select v-model="queryParams.status" placeholder="状态" class="w-32" clearable>
          <el-option label="草稿" :value="0" />
          <el-option label="待审核" :value="1" />
          <el-option label="已上架" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增商品</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-image :src="row.mainImage" style="width: 50px; height: 50px" fit="cover" />
              <div>
                <p class="font-medium line-clamp-1">{{ row.productName }}</p>
                <p class="text-xs text-gray-400">{{ row.categoryName }} | {{ row.brandName }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.minPrice }}</span>
            <span v-if="row.maxPrice !== row.minPrice" class="text-red-500">-{{ row.maxPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="库存" width="80" />
        <el-table-column prop="totalSales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="product" />
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="商家" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 1" link type="success" size="small" @click="handleAudit(row, true)">通过</el-button>
            <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleAudit(row, false)">拒绝</el-button>
            <el-button v-if="row.status === 2" link type="warning" size="small" @click="handleUnpublish(row)">下架</el-button>
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

    <el-dialog v-model="detailVisible" title="商品详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商品名称" :span="2">{{ currentRow.productName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ currentRow.brandName }}</el-descriptions-item>
        <el-descriptions-item label="价格">&yen;{{ currentRow.minPrice }} - {{ currentRow.maxPrice }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentRow.totalStock }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="currentRow.status" type="product" />
        </el-descriptions-item>
        <el-descriptions-item label="商家">{{ currentRow.tenantName }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentRow?.mainImage" class="mt-4">
        <p class="text-sm text-gray-500 mb-2">商品主图</p>
        <el-image :src="currentRow.mainImage" style="width: 200px; height: 200px" fit="cover" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import StatusTag from '@/components/StatusTag.vue'
import {
  getPlatformSpuList,
  auditPlatformSpu,
  unpublishPlatformSpu,
  getCategoryTree,
  type SpuVO,
  type CategoryTreeVO,
} from '@/api/product'

const router = useRouter()
const loading = ref(false)
const tableData = ref<SpuVO[]>([])
const total = ref(0)
const categoryOptions = ref<CategoryTreeVO[]>([])

const queryParams = ref({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  limit: 10,
})

const detailVisible = ref(false)
const currentRow = ref<SpuVO | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const data = await getPlatformSpuList(queryParams.value)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  router.push('/platform/product/add')
}

function handleView(row: SpuVO) {
  currentRow.value = row
  detailVisible.value = true
}

async function handleAudit(row: SpuVO, pass: boolean) {
  if (pass) {
    await auditPlatformSpu(row.id, true)
    ElMessage.success('审核通过')
  } else {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      inputPattern: /\S+/,
      inputErrorMessage: '请输入拒绝原因',
    })
    await auditPlatformSpu(row.id, false, reason)
    ElMessage.success('已拒绝')
  }
  fetchList()
}

async function handleUnpublish(row: SpuVO) {
  await ElMessageBox.confirm(`确定下架商品「${row.productName}」？`, '提示', { type: 'warning' })
  await unpublishPlatformSpu(row.id)
  ElMessage.success('已下架')
  fetchList()
}

onMounted(async () => {
  categoryOptions.value = await getCategoryTree()
  fetchList()
})
</script>
