<template>
  <div>
    <h2 class="text-lg font-bold mb-4">分销员管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 items-center flex-wrap">
        <el-input v-model="keywordFilter" placeholder="搜索姓名/手机号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="statusFilter" placeholder="状态" class="w-32" clearable @change="fetchList">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已禁用" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="memberId" label="会员ID" width="100" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="distributor" />
          </template>
        </el-table-column>
        <el-table-column prop="totalCommission" label="累计佣金" width="120">
          <template #default="{ row }">{{ row.totalCommission }}元</template>
        </el-table-column>
        <el-table-column prop="availableCommission" label="可用佣金" width="120">
          <template #default="{ row }">{{ row.availableCommission }}元</template>
        </el-table-column>
        <el-table-column prop="frozenCommission" label="冻结佣金" width="120">
          <template #default="{ row }">{{ row.frozenCommission }}元</template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleAudit(row, 3)">禁用</el-button>
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

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝申请" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { getDistributorList, auditDistributor, type Distributor } from '@/api/distribution'

const loading = ref(false)
const tableData = ref<Distributor[]>([])
const total = ref(0)
const page = ref(1)
const limit = ref(10)
const keywordFilter = ref('')
const statusFilter = ref<number | undefined>(undefined)

const rejectVisible = ref(false)
const rejectReason = ref('')
const currentId = ref<number>(0)

async function fetchList() {
  loading.value = true
  try {
    const data = await getDistributorList({
      keyword: keywordFilter.value.trim(),
      status: statusFilter.value,
      page: page.value,
      limit: limit.value,
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleAudit(row: Distributor, status: number) {
  await auditDistributor(row.id as number, status)
  ElMessage.success('操作成功')
  fetchList()
}

function handleReject(row: Distributor) {
  currentId.value = row.id as number
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  await auditDistributor(currentId.value, 2, rejectReason.value)
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>
