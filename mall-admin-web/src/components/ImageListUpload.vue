<template>
  <div class="image-list-upload">
    <div class="image-list-upload__list">
      <div v-for="(url, index) in modelValue" :key="index" class="image-list-upload__item">
        <img :src="url" class="image-list-upload__img" />
        <div class="image-list-upload__mask">
          <el-icon @click.stop="handleRemove(index)"><Delete /></el-icon>
          <el-icon v-if="index > 0" @click.stop="handleMove(index, index - 1)"><Back /></el-icon>
          <el-icon v-if="index < modelValue.length - 1" @click.stop="handleMove(index, index + 1)"><Right /></el-icon>
        </div>
      </div>
      <el-upload
        :show-file-list="false"
        :http-request="handleUpload"
        :before-upload="handleBeforeUpload"
        accept="image/jpeg,image/png,image/gif,image/webp"
      >
        <div class="image-list-upload__add">
          <el-icon><Plus /></el-icon>
        </div>
      </el-upload>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Delete, Plus, Back, Right } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/system'

const props = withDefaults(defineProps<{
  modelValue: string[]
  max?: number
}>(), {
  max: 9,
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

function handleBeforeUpload(file: File) {
  if (props.modelValue.length >= props.max) {
    ElMessage.error(`最多上传 ${props.max} 张图片`)
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleUpload(options: { file: File }) {
  try {
    const url = await uploadFile(options.file)
    emit('update:modelValue', [...props.modelValue, url])
  } catch {
    ElMessage.error('上传失败')
  }
}

function handleRemove(index: number) {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}

function handleMove(from: number, to: number) {
  const next = [...props.modelValue]
  const item = next.splice(from, 1)[0]
  next.splice(to, 0, item)
  emit('update:modelValue', next)
}
</script>

<style scoped>
.image-list-upload__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-list-upload__item {
  position: relative;
  width: 104px;
  height: 104px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
}

.image-list-upload__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-list-upload__mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
}

.image-list-upload__item:hover .image-list-upload__mask {
  opacity: 1;
}

.image-list-upload__add {
  width: 104px;
  height: 104px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--el-text-color-placeholder);
  cursor: pointer;
  transition: border-color 0.3s;
}

.image-list-upload__add:hover {
  border-color: var(--el-color-primary);
}
</style>
