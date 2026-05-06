<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6">会员中心</h1>
    <div class="flex gap-6">
      <MemberSidebar active-menu="address" />
      <div class="flex-1">
        <div class="flex items-center justify-between mb-4">
          <h2 class="font-bold text-lg">收货地址</h2>
          <button class="btn-primary text-sm flex items-center gap-1" @click="showModal = true">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            新增收货地址
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-16">
          <div class="inline-block w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <!-- Address list -->
        <div v-else-if="addresses.length > 0" class="space-y-3">
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="bg-white rounded-lg p-5 border transition-all hover:shadow-md"
            :class="addr.isDefault ? 'border-primary-500 shadow-sm' : 'hover:border-gray-400'"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <span class="font-bold text-gray-800">{{ addr.name }}</span>
                  <span class="text-gray-500">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault" class="text-xs bg-primary-500 text-white px-2 py-0.5 rounded">默认</span>
                  <span v-if="addr.tag" class="text-xs px-2 py-0.5 rounded" :class="tagClass(addr.tag)">{{ addr.tag }}</span>
                </div>
                <p class="text-sm text-gray-500">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
              </div>
              <div class="flex items-center gap-4 ml-4">
                <button class="text-gray-400 hover:text-primary-500 transition-colors" @click="editAddress(addr)">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </button>
                <label v-if="!addr.isDefault" class="flex items-center gap-1 text-sm text-gray-400 hover:text-primary-500 cursor-pointer transition-colors">
                  <input type="checkbox" class="rounded" :checked="false" @change="handleSetDefault(addr.id)" />
                  设为默认
                </label>
                <button class="text-gray-400 hover:text-red-500 transition-colors" @click="handleRemove(addr.id)">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else class="bg-white rounded-lg text-center py-16 text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <p class="mb-4">暂无收货地址</p>
          <button class="btn-primary text-sm" @click="showModal = true">添加地址</button>
        </div>
      </div>
    </div>

    <!-- Address Modal -->
    <AddressModal
      :visible="showModal"
      :address="editingAddress"
      @close="showModal = false; editingAddress = null"
      @saved="onAddressSaved"
    />
  </div>
</template>

<script setup lang="ts">
import type { Address } from '~/composables/types'

const { get, put, delete: del } = useRequest()

const addresses = ref<Address[]>([])
const loading = ref(true)
const showModal = ref(false)
const editingAddress = ref<Address | null>(null)

const tagClass = (tag?: string) => {
  if (tag === '家') return 'bg-green-50 text-green-600'
  if (tag === '公司') return 'bg-orange-50 text-orange-600'
  if (tag === '学校') return 'bg-blue-50 text-blue-600'
  return 'bg-gray-50 text-gray-500'
}

const loadAddresses = async () => {
  loading.value = true
  try {
    addresses.value = await get<Address[]>('/address/list')
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

const editAddress = (addr: Address) => {
  editingAddress.value = addr
  showModal.value = true
}

const onAddressSaved = () => {
  loadAddresses()
}

const handleSetDefault = async (id: number) => {
  try {
    await put(`/address/set-default/${id}`)
    loadAddresses()
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : '设置失败')
  }
}

const handleRemove = async (id: number) => {
  if (!confirm('确定要删除该地址吗？')) return
  try {
    await del(`/address/remove/${id}`)
    loadAddresses()
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : '删除失败')
  }
}

onMounted(() => loadAddresses())
</script>
