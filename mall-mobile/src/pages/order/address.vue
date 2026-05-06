<template>
  <view class="page-address">
    <!-- Address cards -->
    <view v-for="addr in addresses" :key="addr.id" class="address-card" @tap="handleSelect(addr)">
      <view class="address-card__main">
        <view class="address-card__row">
          <text class="address-card__name">{{ addr.receiverName }}</text>
          <text class="address-card__phone">{{ addr.receiverPhone }}</text>
          <view v-if="addr.tag" class="address-card__tag" :class="tagClass(addr.tag)">{{ addr.tag }}</view>
          <view v-else-if="addr.isDefault === 1" class="address-card__tag tag-default">默认</view>
        </view>
        <text class="address-card__detail">{{ addr.provinceName }}{{ addr.cityName }}{{ addr.districtName }}{{ addr.detailAddress }}</text>
      </view>
      <view class="address-card__bottom">
        <view class="address-card__default-row" @tap.stop="handleSetDefault(addr)">
          <view class="address-card__checkbox" :class="{ checked: addr.isDefault === 1 }">
            <RemixIcon v-if="addr.isDefault === 1" name="check-line" size="20" color="#fff" />
          </view>
          <text class="address-card__default-text">设为默认</text>
        </view>
        <view class="address-card__actions">
          <view class="address-card__action" @tap.stop="handleEdit(addr)">
            <RemixIcon name="edit-line" size="28" color="#64748B" />
          </view>
          <view class="address-card__action" @tap.stop="handleDelete(addr)">
            <text class="address-card__delete-text">删除</text>
          </view>
        </view>
      </view>
    </view>

    <Empty v-if="!loading && !addresses.length" text="暂无收货地址" />

    <!-- Add button (fixed bottom) -->
    <view class="address-add" @tap="handleAdd">
      <RemixIcon name="add-line" size="32" color="#2563EB" />
      <text class="address-add__text">新增收货地址</text>
    </view>

    <!-- Address form bottom sheet -->
    <view v-if="showForm" class="address-form-overlay" @tap="showForm = false">
      <view class="address-form" @tap.stop>
        <text class="address-form__title">{{ isEdit ? '编辑地址' : '新增地址' }}</text>
        <scroll-view scroll-y class="address-form__scroll">
          <view class="address-form__field">
            <text class="address-form__label">收货人</text>
            <input v-model="form.receiverName" placeholder="请输入收货人姓名" />
          </view>
          <view class="address-form__field">
            <text class="address-form__label">手机号</text>
            <input v-model="form.receiverPhone" type="number" maxlength="11" placeholder="请输入手机号" />
          </view>
          <view class="address-form__field">
            <text class="address-form__label">省份</text>
            <input v-model="form.provinceName" placeholder="省份" />
          </view>
          <view class="address-form__field">
            <text class="address-form__label">城市</text>
            <input v-model="form.cityName" placeholder="城市" />
          </view>
          <view class="address-form__field">
            <text class="address-form__label">区县</text>
            <input v-model="form.districtName" placeholder="区/县" />
          </view>
          <view class="address-form__field" style="align-items: flex-start;">
            <text class="address-form__label">详细地址</text>
            <textarea v-model="form.detailAddress" placeholder="街道、楼牌号等" class="address-form__textarea" />
          </view>
          <view class="address-form__field">
            <text class="address-form__label">地址标签</text>
            <view class="address-form__tags">
              <view
                v-for="t in tagOptions"
                :key="t"
                class="address-form__tag-pill"
                :class="{ active: form.tag === t }"
                @tap="form.tag = form.tag === t ? '' : t"
              >{{ t }}</view>
            </view>
          </view>
          <view class="address-form__field">
            <text class="address-form__label">设为默认</text>
            <switch :checked="form.isDefault === 1" @change="form.isDefault = form.isDefault === 1 ? 0 : 1" color="#2563EB" />
          </view>
        </scroll-view>
        <view class="address-form__btn" @tap="handleSave">保存</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Empty from '@/components/Empty.vue'
import RemixIcon from '@/components/RemixIcon.vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, type MemberAddressVO } from '@/api/address'

const loading = ref(false)
const addresses = ref<MemberAddressVO[]>([])
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref<string | null>(null)
let selectMode = false

const tagOptions = ['家', '公司', '学校']

const form = ref<MemberAddressVO>({
  receiverName: '',
  receiverPhone: '',
  provinceName: '',
  cityName: '',
  districtName: '',
  detailAddress: '',
  isDefault: 0,
  tag: '',
})

onLoad((query) => {
  selectMode = query?.select === 'true'
  loadAddresses()
})

const tagClass = (tag: string) => {
  const map: Record<string, string> = { '家': 'tag-home', '公司': 'tag-office', '学校': 'tag-school' }
  return map[tag] || 'tag-default'
}

async function loadAddresses() {
  loading.value = true
  try {
    addresses.value = await getAddressList()
  } finally {
    loading.value = false
  }
}

function handleSelect(addr: MemberAddressVO) {
  if (selectMode) {
    const pages = getCurrentPages()
    const prevPage = pages[pages.length - 2] as any
    if (prevPage?.onSelectAddress) {
      prevPage.onSelectAddress(addr)
    }
    uni.navigateBack()
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { receiverName: '', receiverPhone: '', provinceName: '', cityName: '', districtName: '', detailAddress: '', isDefault: 0, tag: '' }
  showForm.value = true
}

function handleEdit(addr: MemberAddressVO) {
  isEdit.value = true
  editId.value = addr.id!
  form.value = { ...addr }
  showForm.value = true
}

async function handleDelete(addr: MemberAddressVO) {
  const [, res] = await uni.showModal({ title: '提示', content: '确定删除此地址？' }) as any
  if (res?.confirm) {
    await deleteAddress(addr.id!)
    loadAddresses()
  }
}

async function handleSetDefault(addr: MemberAddressVO) {
  if (addr.isDefault === 1) return
  await updateAddress(addr.id!, { ...addr, isDefault: 1 })
  loadAddresses()
}

async function handleSave() {
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.detailAddress) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (isEdit.value && editId.value) {
    await updateAddress(editId.value, form.value)
  } else {
    await addAddress(form.value)
  }
  uni.showToast({ title: '保存成功', icon: 'success' })
  showForm.value = false
  loadAddresses()
}
</script>

<style lang="scss" scoped>
.page-address { min-height: 100vh; background: #F9FAFB; padding: 16rpx 24rpx 160rpx; }

.address-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 16rpx;
}
.address-card__main { margin-bottom: 20rpx; }
.address-card__row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx; }
.address-card__name { font-size: 30rpx; font-weight: 600; color: #1E293B; }
.address-card__phone { font-size: 26rpx; color: #64748B; }
.address-card__tag {
  font-size: 20rpx;
  border-radius: 6rpx;
  padding: 2rpx 12rpx;
  line-height: 1.6;
}
.tag-default { color: #2563EB; background: #EFF6FF; }
.tag-home { color: #059669; background: #ECFDF5; }
.tag-office { color: #F97316; background: #FFF7ED; }
.tag-school { color: #8B5CF6; background: #F5F3FF; }
.address-card__detail { font-size: 26rpx; color: #64748B; line-height: 1.6; }

.address-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1rpx solid #F3F4F6;
  padding-top: 20rpx;
}
.address-card__default-row { display: flex; align-items: center; gap: 8rpx; }
.address-card__checkbox {
  width: 32rpx; height: 32rpx;
  border: 2rpx solid #D1D5DB;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  &.checked { background: #2563EB; border-color: #2563EB; }
}
.address-card__default-text { font-size: 24rpx; color: #64748B; }
.address-card__actions { display: flex; align-items: center; gap: 24rpx; }
.address-card__action { display: flex; align-items: center; }
.address-card__delete-text { font-size: 24rpx; color: #EF4444; }

.address-add {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 28rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}
.address-add__text { font-size: 30rpx; color: #2563EB; font-weight: 500; }

.address-form-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 200; display: flex; align-items: flex-end; }
.address-form { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 32rpx; max-height: 85vh; }
.address-form__title { font-size: 32rpx; font-weight: 600; display: block; text-align: center; margin-bottom: 24rpx; }
.address-form__scroll { max-height: 60vh; }
.address-form__field { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 0; border-bottom: 1rpx solid #F3F4F6; }
.address-form__label { font-size: 28rpx; color: #1E293B; width: 140rpx; flex-shrink: 0; }
.address-form__field input { flex: 1; font-size: 28rpx; text-align: right; }
.address-form__textarea { width: 100%; height: 120rpx; font-size: 28rpx; background: #F9FAFB; border-radius: 8rpx; padding: 12rpx; box-sizing: border-box; margin-top: 8rpx; }
.address-form__tags { display: flex; gap: 16rpx; flex: 1; justify-content: flex-end; }
.address-form__tag-pill {
  font-size: 24rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  border: 1rpx solid #D1D5DB;
  color: #64748B;
  &.active { background: #2563EB; color: #fff; border-color: #2563EB; }
}
.address-form__btn { background: #2563EB; color: #fff; text-align: center; padding: 24rpx; border-radius: 40rpx; font-size: 30rpx; font-weight: 500; margin-top: 32rpx; }
</style>
