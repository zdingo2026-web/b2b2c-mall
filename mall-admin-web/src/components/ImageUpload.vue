<template>
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :show-file-list="false"
      :before-upload="handleBeforeUpload"
      :http-request="handleUpload"
      :accept="accept"
    >
      <div v-if="modelValue" class="image-upload__preview">
        <img :src="modelValue" class="image-upload__img" />
        <div class="image-upload__mask">
          <el-icon class="image-upload__icon" @click.stop="handleRemove"><Delete /></el-icon>
        </div>
      </div>
      <el-icon v-else class="image-upload__placeholder"><Plus /></el-icon>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/system'

const props = withDefaults(defineProps<{
  modelValue?: string
  accept?: string
  maxSize?: number
}>(), {
  accept: 'image/jpeg,image/png,image/gif,image/webp',
  maxSize: 5,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const uploadUrl = '#'

function handleBeforeUpload(file: File) {
  if (!props.accept.split(',').includes(file.type)) {
    ElMessage.error('不支持的图片格式')
    return false
  }
  if (file.size / 1024 / 1024 > props.maxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

async function handleUpload(options: { file: File }) {
  try {
    const url = await uploadFile(options.file)
    emit('update:modelValue', url)
  } catch {
    ElMessage.error('上传失败')
  }
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 104px;
  height: 104px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.image-upload :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.image-upload__placeholder {
  font-size: 28px;
  color: var(--el-text-color-placeholder);
}

.image-upload__preview {
  position: relative;
  width: 100%;
  height: 100%;
}

.image-upload__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-upload__mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-upload__preview:hover .image-upload__mask {
  opacity: 1;
}

.image-upload__icon {
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
</style>
