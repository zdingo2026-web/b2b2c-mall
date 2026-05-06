<template>
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="handleClose">
    <div class="bg-white rounded-lg w-[560px] max-h-[80vh] overflow-auto">
      <div class="px-6 py-4 border-b flex items-center justify-between">
        <h3 class="font-bold text-lg">{{ isEdit ? '编辑地址' : '新增地址' }}</h3>
        <button class="text-gray-400 hover:text-gray-600" @click="handleClose">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
        </button>
      </div>
      <div class="px-6 py-4 space-y-4">
        <div class="flex gap-4">
          <div class="flex-1">
            <label class="block text-sm text-gray-600 mb-1">收货人 <span class="text-red-500">*</span></label>
            <input v-model="form.name" class="w-full h-10 px-3 border rounded text-sm" placeholder="请输入收货人姓名" />
          </div>
          <div class="flex-1">
            <label class="block text-sm text-gray-600 mb-1">手机号 <span class="text-red-500">*</span></label>
            <input v-model="form.phone" class="w-full h-10 px-3 border rounded text-sm" placeholder="请输入手机号" />
          </div>
        </div>
        <div class="flex gap-4">
          <div class="flex-1">
            <label class="block text-sm text-gray-600 mb-1">省份 <span class="text-red-500">*</span></label>
            <input v-model="form.province" class="w-full h-10 px-3 border rounded text-sm" placeholder="省份" />
          </div>
          <div class="flex-1">
            <label class="block text-sm text-gray-600 mb-1">城市 <span class="text-red-500">*</span></label>
            <input v-model="form.city" class="w-full h-10 px-3 border rounded text-sm" placeholder="城市" />
          </div>
          <div class="flex-1">
            <label class="block text-sm text-gray-600 mb-1">区县 <span class="text-red-500">*</span></label>
            <input v-model="form.district" class="w-full h-10 px-3 border rounded text-sm" placeholder="区县" />
          </div>
        </div>
        <div>
          <label class="block text-sm text-gray-600 mb-1">详细地址 <span class="text-red-500">*</span></label>
          <textarea v-model="form.detail" class="w-full h-20 px-3 py-2 border rounded text-sm" placeholder="街道、门牌号等"></textarea>
        </div>
        <div class="flex items-center gap-2">
          <input v-model="form.isDefault" type="checkbox" id="addr-default" class="rounded" />
          <label for="addr-default" class="text-sm text-gray-600">设为默认地址</label>
        </div>
        <div>
          <label class="block text-sm text-gray-600 mb-2">地址标签</label>
          <div class="flex gap-3">
            <button
              v-for="t in addressTags"
              :key="t.value"
              class="px-4 py-1.5 rounded-full text-sm border transition-colors"
              :class="form.tag === t.value ? t.activeClass : 'border-gray-200 text-gray-500 hover:border-gray-400'"
              @click="form.tag = form.tag === t.value ? undefined : t.value"
            >
              {{ t.label }}
            </button>
          </div>
        </div>
      </div>
      <div class="px-6 py-4 border-t flex justify-end gap-3">
        <button class="px-6 py-2 border rounded text-sm hover:bg-gray-50" @click="handleClose">取消</button>
        <button class="btn-primary text-sm" @click="handleSave" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Address } from '~/composables/types'

const props = defineProps<{
  visible: boolean
  address?: Address | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved'): void
}>()

const { post, put } = useRequest()
const saving = ref(false)

const isEdit = computed(() => !!props.address?.id)

const form = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
  tag: undefined as '家' | '公司' | '学校' | undefined,
})

const addressTags = [
  { label: '家', value: '家' as const, activeClass: 'bg-green-50 border-green-400 text-green-600' },
  { label: '公司', value: '公司' as const, activeClass: 'bg-orange-50 border-orange-400 text-orange-600' },
  { label: '学校', value: '学校' as const, activeClass: 'bg-blue-50 border-blue-400 text-blue-600' },
]

watch(() => props.visible, (val) => {
  if (val && props.address) {
    form.value = { ...props.address, tag: props.address.tag || undefined }
  } else if (val) {
    form.value = { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false, tag: undefined }
  }
})

const handleClose = () => emit('close')

const handleSave = async () => {
  const { name, phone, province, city, district, detail, isDefault } = form.value
  if (!name || !phone || !province || !city || !district || !detail) {
    alert('请填写完整地址信息')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && props.address?.id) {
      await put('/address/update', { ...form.value, id: props.address.id })
    } else {
      await post('/address/create', form.value)
    }
    emit('saved')
    emit('close')
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '保存失败'
    alert(msg)
  } finally {
    saving.value = false
  }
}
</script>
