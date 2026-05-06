<template>
  <div>
    <h2 class="text-lg font-bold mb-4">系统设置</h2>

    <el-tabs v-model="activeGroup" @tab-change="fetchConfigs">
      <el-tab-pane v-for="group in configGroups" :key="group.key" :label="group.label" :name="group.key" />
    </el-tabs>

    <el-card v-loading="loading">
      <el-form label-width="200px" class="max-w-2xl">
        <el-form-item v-for="config in configList" :key="config.id" :label="config.configDesc || config.configKey">
          <div class="flex items-center gap-2 w-full">
            <el-input v-model="editMap[config.id]" :placeholder="config.configKey" class="flex-1" />
            <el-button type="primary" size="small" @click="handleSave(config)">保存</el-button>
          </div>
          <div class="text-xs text-gray-400 mt-1">配置键: {{ config.configKey }}</div>
        </el-form-item>
      </el-form>
      <el-empty v-if="!loading && configList.length === 0" description="暂无配置项" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig, type ConfigVO } from '@/api/system'

const configGroups = [
  { key: 'basic', label: '基础设置' },
  { key: 'trade', label: '交易设置' },
  { key: 'sms', label: '短信设置' },
  { key: 'storage', label: '存储设置' },
]

const loading = ref(false)
const activeGroup = ref('basic')
const configList = ref<ConfigVO[]>([])
const editMap = reactive<Record<number, string>>({})

async function fetchConfigs() {
  loading.value = true
  try {
    const data = await getConfigList(activeGroup.value)
    configList.value = data
    data.forEach((c) => {
      editMap[c.id] = c.configValue
    })
  } finally {
    loading.value = false
  }
}

async function handleSave(config: ConfigVO) {
  const value = editMap[config.id]
  if (value === undefined) return
  await updateConfig({ id: config.id, configValue: value })
  ElMessage.success('保存成功')
  fetchConfigs()
}

onMounted(fetchConfigs)
</script>
