<template>
  <view v-if="visible" class="sku-overlay" @tap="handleOverlayTap">
    <view class="sku-panel" :class="{ 'sku-panel--show': animShow }" @tap.stop>
      <!-- Header: product info bar -->
      <view class="sku-panel__header">
        <image
          class="sku-panel__img"
          :src="displayImage"
          mode="aspectFill"
        />
        <view class="sku-panel__info">
          <text class="sku-panel__price">&yen;{{ displayPrice }}</text>
          <text v-if="selectedSpecText" class="sku-panel__spec">已选: {{ selectedSpecText }}</text>
          <text v-else class="sku-panel__spec sku-panel__spec--hint">请选择完整规格</text>
          <text class="sku-panel__stock" :class="{ 'sku-panel__stock--zero': displayStock <= 0 }">
            库存: {{ displayStock }}件
          </text>
        </view>
        <view class="sku-panel__close" @tap="handleClose">
          <RemixIcon name="close-line" :size="36" color="#94A3B8" />
        </view>
      </view>

      <!-- Spec groups -->
      <scroll-view scroll-y class="sku-panel__body">
        <view v-for="(group, gIdx) in specGroups" :key="gIdx" class="spec-group">
          <text class="spec-group__title">{{ group.name }}</text>
          <view class="spec-group__values">
            <view
              v-for="val in group.values"
              :key="val.label"
              class="spec-pill"
              :class="{
                'spec-pill--active': isSelected(group.name, val.label),
                'spec-pill--disabled': val.disabled,
              }"
              @tap="handleSpecTap(group.name, val.label, val.disabled)"
            >
              <text class="spec-pill__text">{{ val.label }}</text>
              <text v-if="val.disabled" class="spec-pill__tag">缺货</text>
            </view>
          </view>
        </view>

        <!-- Quantity stepper -->
        <view class="quantity-row">
          <text class="quantity-row__label">数量</text>
          <view class="quantity-row__stepper">
            <view
              class="stepper-btn"
              :class="{ 'stepper-btn--disabled': quantity <= 1 }"
              @tap="handleDecrement"
            >
              <RemixIcon name="subtract-line" :size="24" :color="quantity <= 1 ? '#CBD5E1' : '#334155'" />
            </view>
            <text class="stepper-num">{{ quantity }}</text>
            <view
              class="stepper-btn"
              :class="{ 'stepper-btn--disabled': quantity >= displayStock }"
              @tap="handleIncrement"
            >
              <RemixIcon name="add-line" :size="24" :color="quantity >= displayStock ? '#CBD5E1' : '#334155'" />
            </view>
          </view>
        </view>

        <!-- Service notes -->
        <view class="service-notes">
          <RemixIcon name="shield-check-line" :size="24" color="#16A34A" />
          <text class="service-notes__text">7天无理由退换</text>
        </view>
      </scroll-view>

      <!-- Confirm button -->
      <view class="sku-panel__footer">
        <view
          class="sku-panel__confirm"
          :class="{ 'sku-panel__confirm--disabled': !canConfirm }"
          @tap="handleConfirm"
        >
          <text class="sku-panel__confirm-text">确认加入购物车</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import RemixIcon from '@/components/RemixIcon.vue'
import type { SkuVO } from '@/api/product'

interface SpecGroup {
  name: string
  values: { label: string; disabled: boolean }[]
}

interface Props {
  visible: boolean
  product: {
    id: string
    productName: string
    mainImage: string
    minPrice: string
    maxPrice: string
    originalPrice?: string
    totalStock: number
    skuList: { id: string; specValues: string; price: string; stock: number; image?: string }[]
    attributeList: { name: string; values: string[] }[]
  }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'confirm', data: { skuId: string; quantity: number; specValues: string }): void
}>()

const animShow = ref(false)
const selectedSpecs = ref<Record<string, string>>({})
const quantity = ref(1)

// Animate in/out
watch(
  () => props.visible,
  (val) => {
    if (val) {
      nextTick(() => {
        animShow.value = true
      })
    } else {
      animShow.value = false
    }
  },
)

// Reset state when product changes
watch(
  () => props.product?.id,
  () => {
    selectedSpecs.value = {}
    quantity.value = 1
  },
)

// Parse attributeList into spec groups with out-of-stock detection
const specGroups = computed<SpecGroup[]>(() => {
  if (!props.product?.attributeList?.length) {
    // Fallback: if no attributeList, build single group from skuList specValues
    if (props.product?.skuList?.length) {
      const allSpecs = props.product.skuList.map((s) => s.specValues)
      return [{
        name: '规格',
        values: allSpecs.map((spec, idx) => ({
          label: spec,
          disabled: props.product.skuList[idx].stock <= 0,
        })),
      }]
    }
    return []
  }

  return props.product.attributeList.map((attr) => ({
    name: attr.name,
    values: attr.values.map((val) => {
      // Check if ANY sku with this value is in stock
      const hasStock = props.product.skuList.some((sku) => {
        const parts = parseSpecValues(sku.specValues)
        return parts.includes(val) && sku.stock > 0
      })
      return { label: val, disabled: !hasStock }
    }),
  }))
})

// Parse specValues string into individual values
function parseSpecValues(specValues: string): string[] {
  if (!specValues) return []
  // Handle common separators: comma, semicolon, slash, Chinese comma
  return specValues
    .split(/[,;，；/]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

// Check if a spec value is selected
function isSelected(groupName: string, value: string): boolean {
  return selectedSpecs.value[groupName] === value
}

// Handle spec value tap
function handleSpecTap(groupName: string, value: string, disabled: boolean): void {
  if (disabled) return
  // Toggle: deselect if already selected
  if (selectedSpecs.value[groupName] === value) {
    const next = { ...selectedSpecs.value }
    delete next[groupName]
    selectedSpecs.value = next
  } else {
    selectedSpecs.value = { ...selectedSpecs.value, [groupName]: value }
  }
  // Reset quantity if matched SKU changes
  quantity.value = 1
}

// Find matching SKU based on selected specs
const matchedSku = computed(() => {
  if (!props.product?.skuList?.length) return null

  const selectedValues = Object.values(selectedSpecs.value)
  if (selectedValues.length === 0) return null

  // Try to find an SKU whose specValues contains ALL selected values
  const match = props.product.skuList.find((sku) => {
    const parts = parseSpecValues(sku.specValues)
    return selectedValues.every((v) => parts.includes(v))
  })

  return match || null
})

// Whether all spec groups have been selected
const allSpecsSelected = computed(() => {
  return specGroups.value.length > 0 && specGroups.value.every((g) => selectedSpecs.value[g.name])
})

// Display price
const displayPrice = computed(() => {
  if (matchedSku.value) return matchedSku.value.price
  if (props.product?.minPrice === props.product?.maxPrice) return props.product.minPrice
  return `${props.product.minPrice}-${props.product.maxPrice}`
})

// Display image
const displayImage = computed(() => {
  if (matchedSku.value?.image) return matchedSku.value.image
  return props.product?.mainImage || ''
})

// Display stock
const displayStock = computed(() => {
  if (matchedSku.value) return matchedSku.value.stock
  return props.product?.totalStock || 0
})

// Selected spec text
const selectedSpecText = computed(() => {
  if (!allSpecsSelected.value || !matchedSku.value) {
    const selected = Object.entries(selectedSpecs.value)
    if (selected.length === 0) return ''
    return selected.map(([, v]) => v).join(' ')
  }
  return matchedSku.value.specValues
})

// Whether confirm is allowed
const canConfirm = computed(() => {
  return allSpecsSelected.value && matchedSku.value && matchedSku.value.stock > 0
})

function handleDecrement() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

function handleIncrement() {
  if (quantity.value < displayStock.value) {
    quantity.value++
  }
}

function handleOverlayTap() {
  handleClose()
}

function handleClose() {
  animShow.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 250)
}

function handleConfirm() {
  if (!canConfirm.value) {
    uni.showToast({ title: '请选择完整规格', icon: 'none' })
    return
  }
  emit('confirm', {
    skuId: matchedSku.value!.id,
    quantity: quantity.value,
    specValues: matchedSku.value!.specValues,
  })
  handleClose()
}
</script>

<style lang="scss" scoped>
.sku-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.sku-panel {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
  transform: translateY(100%);
  transition: transform 0.25s ease-out;

  &--show {
    transform: translateY(0);
  }
}

/* Header */
.sku-panel__header {
  display: flex;
  padding: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
  position: relative;
}

.sku-panel__img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #F3F4F6;
}

.sku-panel__info {
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

.sku-panel__price {
  font-size: 40rpx;
  color: #E11148;
  font-weight: 700;
  line-height: 1.2;
}

.sku-panel__spec {
  font-size: 24rpx;
  color: #64748B;
  margin-top: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &--hint {
    color: #F97316;
  }
}

.sku-panel__stock {
  font-size: 24rpx;
  color: #16A34A;
  margin-top: 8rpx;

  &--zero {
    color: #94A3B8;
  }
}

.sku-panel__close {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Body scroll */
.sku-panel__body {
  flex: 1;
  padding: 0 32rpx;
  max-height: 500rpx;
}

/* Spec group */
.spec-group {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F5F5;

  &:last-child {
    border-bottom: none;
  }
}

.spec-group__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
  display: block;
  margin-bottom: 16rpx;
}

.spec-group__values {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

/* Spec pill */
.spec-pill {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 12rpx 28rpx;
  background: #FFFFFF;
  border: 2rpx solid #E2E8F0;
  border-radius: 32rpx;
  position: relative;

  &--active {
    background: #2563EB;
    border-color: #2563EB;

    .spec-pill__text {
      color: #FFFFFF;
    }

    .spec-pill__tag {
      background: rgba(255, 255, 255, 0.3);
      color: #FFFFFF;
    }
  }

  &--disabled {
    background: #F3F4F6;
    border-color: #F3F4F6;

    .spec-pill__text {
      color: #94A3B8;
      text-decoration: line-through;
    }

    .spec-pill__tag {
      background: #E2E8F0;
      color: #94A3B8;
    }
  }
}

.spec-pill__text {
  font-size: 26rpx;
  color: #334155;
  line-height: 1.2;
}

.spec-pill__tag {
  font-size: 18rpx;
  color: #94A3B8;
  background: #E2E8F0;
  padding: 2rpx 8rpx;
  border-radius: 8rpx;
  line-height: 1.2;
}

/* Quantity row */
.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.quantity-row__label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.quantity-row__stepper {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.stepper-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #E2E8F0;
  border-radius: 12rpx;
  background: #FFFFFF;

  &--disabled {
    background: #F9FAFB;
    border-color: #F0F0F0;
  }
}

.stepper-num {
  font-size: 30rpx;
  color: #1E293B;
  font-weight: 500;
  min-width: 60rpx;
  text-align: center;
}

/* Service notes */
.service-notes {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 0 24rpx;
}

.service-notes__text {
  font-size: 24rpx;
  color: #16A34A;
}

/* Footer */
.sku-panel__footer {
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F0F0F0;
}

.sku-panel__confirm {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E11148;
  border-radius: 44rpx;

  &--disabled {
    background: #CBD5E1;
  }
}

.sku-panel__confirm-text {
  font-size: 30rpx;
  color: #FFFFFF;
  font-weight: 600;
}
</style>
