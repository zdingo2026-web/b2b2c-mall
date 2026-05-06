<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="flex gap-6">
      <MemberSidebar active-menu="bankcard" />
      <div class="flex-1">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-bold">我的银行卡</h2>
        </div>

        <div v-if="loading" class="text-center py-20 text-gray-400">加载中...</div>

        <div v-else-if="cards.length">
          <div v-for="card in cards" :key="card.id" class="rounded-xl p-6 mb-4 text-white relative overflow-hidden" :style="{ background: card.cardColor || '#2563EB' }">
            <div class="absolute top-0 right-0 w-40 h-40 rounded-full bg-white/10 -translate-y-1/2 translate-x-1/4" />
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center text-lg font-bold">
                {{ card.bankName.charAt(0) }}
              </div>
              <div>
                <p class="font-bold">{{ card.bankName }}</p>
                <p class="text-sm text-white/80">{{ {1:'借记卡',2:'信用卡'}[card.cardType] || '银行卡' }}</p>
              </div>
              <span v-if="card.isDefault === 1" class="ml-auto text-xs bg-yellow-400 text-yellow-900 px-2 py-0.5 rounded">默认</span>
            </div>
            <p class="text-2xl font-mono tracking-wider mb-3">{{ card.cardNoMask }}</p>
            <div class="flex items-center gap-6 text-sm text-white/70">
              <span v-if="card.expiryDate">有效期: {{ card.expiryDate }}</span>
            </div>
            <div class="flex gap-3 mt-4">
              <button v-if="card.isDefault !== 1" class="text-xs bg-white/20 hover:bg-white/30 px-3 py-1 rounded" @click="handleSetDefault(card.id)">设为默认</button>
              <button class="text-xs bg-white/20 hover:bg-red-500/50 px-3 py-1 rounded" @click="handleDelete(card.id)">删除</button>
            </div>
          </div>

          <!-- Security tip -->
          <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-center gap-3 mt-6">
            <svg class="w-5 h-5 text-yellow-600 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-2.332 9-7.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
            <p class="text-sm text-yellow-700">您的银行卡信息已加密保护</p>
          </div>
        </div>

        <div v-else class="bg-white rounded-lg text-center py-20">
          <p class="text-gray-400 mb-4">暂无银行卡</p>
        </div>

        <!-- Add button -->
        <button class="mt-4 w-full border-2 border-dashed border-gray-300 rounded-xl p-6 text-gray-400 hover:border-primary-500 hover:text-primary-500 transition-colors" @click="handleAdd">
          + 添加银行卡
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { BankCard } from '~/composables/types'

const { get, delete: del, put } = useRequest()

const cards = ref<BankCard[]>([])
const loading = ref(true)

const loadCards = async () => {
  loading.value = true
  try {
    cards.value = await get<BankCard[]>('/bank-card/list')
  } catch {
    cards.value = []
  } finally {
    loading.value = false
  }
}

const handleSetDefault = async (id: number) => {
  try {
    await put(`/bank-card/${id}/default`)
    loadCards()
  } catch {
    // ignore
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确定删除此银行卡？')) return
  try {
    await del(`/bank-card/${id}`)
    loadCards()
  } catch {
    // ignore
  }
}

const handleAdd = () => {
  alert('功能开发中')
}

onMounted(loadCards)
</script>
