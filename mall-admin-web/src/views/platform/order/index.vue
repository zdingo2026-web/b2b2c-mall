<template>
  <div>
    <h2 class="text-lg font-bold mb-4">订单管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" class="w-64" clearable @keyup.enter="fetchList" />
        <el-select v-model="queryParams.orderStatus" placeholder="订单状态" class="w-32" clearable>
          <el-option label="待付款" :value="0" />
          <el-option label="待发货" :value="1" />
          <el-option label="待收货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
          <el-option label="待评价" :value="7" />
        </el-select>
        <el-select v-model="queryParams.payStatus" placeholder="支付状态" class="w-32" clearable>
          <el-option label="未支付" :value="0" />
          <el-option label="已支付" :value="1" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="w-72"
        />
        <el-button type="primary" @click="fetchList">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div v-for="item in row.items?.slice(0, 2)" :key="item.id" class="flex items-center gap-2 py-1">
              <el-image :src="item.productImage" style="width: 36px; height: 36px" fit="cover" />
              <span class="text-sm line-clamp-1">{{ item.productName }} x{{ item.quantity }}</span>
            </div>
            <p v-if="row.items?.length > 2" class="text-xs text-gray-400">等{{ row.items.length }}件商品</p>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span class="text-red-500">&yen;{{ row.payAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.orderStatus" type="order" />
          </template>
        </el-table-column>
        <el-table-column prop="payStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.payStatus" type="pay" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 1" link type="success" size="small" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.orderStatus === 0" link type="danger" size="small" @click="handleCancel(row)">取消</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="currentRow">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <StatusTag :status="currentRow.orderStatus" type="order" />
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <StatusTag :status="currentRow.payStatus" type="pay" />
          </el-descriptions-item>
          <el-descriptions-item label="商品总额">&yen;{{ currentRow.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="运费">&yen;{{ currentRow.freightAmount }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">&yen;{{ currentRow.discountAmount }}</el-descriptions-item>
          <el-descriptions-item label="实付金额"><span class="text-red-500 font-bold">&yen;{{ currentRow.payAmount }}</span></el-descriptions-item>
          <el-descriptions-item v-if="currentRow.payTime" label="支付时间">{{ currentRow.payTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.payType" label="支付方式">{{ {1:'微信支付',2:'支付宝',3:'余额支付'}[currentRow.payType] || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.invoiceType" label="发票">{{ {0:'不开发票',1:'电子发票',2:'纸质发票'}[currentRow.invoiceType] || '-' }}{{ currentRow.invoiceTitle ? ` - ${currentRow.invoiceTitle}` : '' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.remark" label="备注" :span="2">{{ currentRow.remark }}</el-descriptions-item>
        </el-descriptions>

        <!-- Logistics info -->
        <template v-if="currentRow.deliveryNo">
          <p class="font-bold mb-2 mt-4">物流信息</p>
          <el-descriptions :column="2" border class="mb-4">
            <el-descriptions-item label="物流公司">{{ currentRow.deliveryCompany || currentRow.logisticsCompany || '-' }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ currentRow.deliveryNo || currentRow.logisticsNo || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <p class="font-bold mb-2">收货信息</p>
        <el-descriptions v-if="currentRow.address" :column="2" border class="mb-4">
          <el-descriptions-item label="收货人">{{ currentRow.address.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.address.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentRow.address.fullAddress }}</el-descriptions-item>
        </el-descriptions>

        <p class="font-bold mb-2">商品明细</p>
        <el-table :data="currentRow.items" border size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <el-image :src="row.productImage" style="width: 40px; height: 40px" fit="cover" />
                <div>
                  <p>{{ row.productName }}</p>
                  <p class="text-xs text-gray-400">{{ row.specValues }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">&yen;{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="subtotal" label="小计" width="100">
            <template #default="{ row }">&yen;{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>

        <div v-if="currentRow.orderStatus === 0 || currentRow.orderStatus === 1" class="mt-4 flex gap-3 justify-end">
          <el-button v-if="currentRow.orderStatus === 0" type="danger" @click="handleCancelFromDetail">取消订单</el-button>
          <el-button v-if="currentRow.orderStatus === 1" type="success" @click="handleShipFromDetail">发货</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Ship dialog -->
    <el-dialog v-model="shipVisible" title="订单发货" width="450px">
      <el-form label-width="100px">
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.deliveryCompany" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.deliveryNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { request } from '@/utils/request'
import { getPlatformOrderList, getPlatformOrderDetail, shipPlatformOrder, cancelPlatformOrder, type OrderMainVO } from '@/api/order'

const loading = ref(false)
const tableData = ref<OrderMainVO[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryParams = ref({
  orderNo: '',
  orderStatus: undefined as number | undefined,
  payStatus: undefined as number | undefined,
  beginTime: '',
  endTime: '',
  page: 1,
  limit: 10,
})

const detailVisible = ref(false)
const currentRow = ref<OrderMainVO | null>(null)

async function fetchList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value?.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  try {
    const data = await getPlatformOrderList(params)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleView(row: OrderMainVO) {
  const detail = await getPlatformOrderDetail(row.orderNo)
  currentRow.value = detail
  detailVisible.value = true
}

const shipVisible = ref(false)
const shipLoading = ref(false)
const shipForm = ref({ orderNo: '', deliveryCompany: '', deliveryNo: '' })

function handleShip(row: OrderMainVO) {
  shipForm.value = { orderNo: row.orderNo, deliveryCompany: '', deliveryNo: '' }
  shipVisible.value = true
}

async function submitShip() {
  if (!shipForm.value.deliveryCompany || !shipForm.value.deliveryNo) {
    ElMessage.warning('请填写物流信息')
    return
  }
  shipLoading.value = true
  try {
    await shipPlatformOrder(shipForm.value.orderNo, shipForm.value.deliveryCompany, shipForm.value.deliveryNo)
    ElMessage.success('发货成功')
    shipVisible.value = false
    fetchList()
  } finally {
    shipLoading.value = false
  }
}

async function handleCancel(row: OrderMainVO) {
  try {
    await ElMessageBox.confirm('确定取消该订单？取消后将释放库存。', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cancelPlatformOrder(row.orderNo)
    ElMessage.success('订单已取消')
    fetchList()
  } catch {
    // Cancelled or failed
  }
}

function handleShipFromDetail() {
  if (!currentRow.value) return
  detailVisible.value = false
  handleShip(currentRow.value)
}

async function handleCancelFromDetail() {
  if (!currentRow.value) return
  detailVisible.value = false
  await handleCancel(currentRow.value)
}

onMounted(fetchList)
</script>
