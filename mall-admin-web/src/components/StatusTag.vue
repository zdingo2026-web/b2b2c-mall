<template>
  <el-tag :type="tagType" size="small">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: number
  type: 'tenant' | 'product' | 'order' | 'pay' | 'refund' | 'member' | 'admin' | 'settle'
}>()

const config: Record<string, Record<number, { label: string; type: '' | 'success' | 'warning' | 'danger' | 'info' }>> = {
  tenant: {
    0: { label: '待审核', type: 'warning' },
    1: { label: '已通过', type: 'success' },
    2: { label: '已拒绝', type: 'danger' },
    3: { label: '已禁用', type: 'info' },
  },
  product: {
    0: { label: '草稿', type: 'info' },
    1: { label: '待审核', type: 'warning' },
    2: { label: '已上架', type: 'success' },
    3: { label: '已下架', type: 'danger' },
    4: { label: '审核拒绝', type: 'danger' },
  },
  order: {
    0: { label: '待付款', type: 'warning' },
    1: { label: '待发货', type: '' },
    2: { label: '待收货', type: '' },
    3: { label: '已完成', type: 'success' },
    4: { label: '已取消', type: 'info' },
    5: { label: '退款中', type: 'danger' },
    6: { label: '已退款', type: 'info' },
  },
  pay: {
    0: { label: '未支付', type: 'warning' },
    1: { label: '已支付', type: 'success' },
    2: { label: '已退款', type: 'info' },
    3: { label: '退款中', type: 'danger' },
  },
  refund: {
    0: { label: '待处理', type: 'warning' },
    1: { label: '已同意', type: 'success' },
    2: { label: '已拒绝', type: 'danger' },
  },
  member: {
    0: { label: '正常', type: 'success' },
    1: { label: '禁用', type: 'danger' },
  },
  admin: {
    0: { label: '已禁用', type: 'danger' },
    1: { label: '正常', type: 'success' },
  },
  settle: {
    0: { label: '待结算', type: 'warning' },
    1: { label: '已结算', type: 'success' },
  },
}

const tagType = computed(() => config[props.type]?.[props.status]?.type ?? 'info')
const label = computed(() => config[props.type]?.[props.status]?.label ?? '未知')
</script>
