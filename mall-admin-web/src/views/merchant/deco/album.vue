<template>
  <div>
    <h2 class="text-lg font-bold mb-4">相册管理</h2>
    <el-card>
      <div class="mb-4 flex gap-4 flex-wrap">
        <el-button type="success" @click="showCreateAlbumDialog">新建相册</el-button>
      </div>

      <!-- Album List -->
      <el-table :data="albumList" stripe v-loading="albumLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="albumName" label="相册名称" min-width="160" />
        <el-table-column prop="imageCount" label="图片数" width="80" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openImageManager(row)">管理图片</el-button>
            <el-button link type="primary" size="small" @click="showEditAlbumDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteAlbum(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Album Dialog -->
    <el-dialog v-model="albumDialogVisible" :title="isEditAlbum ? '编辑相册' : '新建相册'" width="500px" destroy-on-close>
      <el-form ref="albumFormRef" :model="albumForm" :rules="albumRules" label-width="80px">
        <el-form-item label="相册名称" prop="albumName">
          <el-input v-model="albumForm.albumName" placeholder="请输入相册名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="相册描述">
          <el-input v-model="albumForm.description" type="textarea" :rows="3" placeholder="请输入相册描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="albumDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="albumSubmitting" @click="handleAlbumSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Image Manager Drawer -->
    <el-drawer v-model="imageDrawerVisible" :title="`图片管理 - ${currentAlbum?.albumName || ''}`" size="700px" destroy-on-close>
      <div class="mb-4 flex gap-4">
        <el-upload
          :show-file-list="false"
          :http-request="handleUploadImage"
          accept="image/*"
          multiple
        >
          <el-button type="primary">上传图片</el-button>
        </el-upload>
      </div>

      <div v-loading="imageLoading" class="grid grid-cols-4 gap-4">
        <div v-for="img in imageList" :key="img.id" class="relative group">
          <el-image :src="img.url" fit="cover" class="w-full h-32 rounded border" />
          <div class="absolute top-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <el-button type="danger" size="small" circle @click="handleDeleteImage(img)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <p class="text-xs text-gray-500 mt-1 truncate">{{ img.imageName }}</p>
        </div>
        <el-empty v-if="!imageLoading && imageList.length === 0" description="暂无图片" class="col-span-4" />
      </div>

      <div v-if="imageTotal > imageParams.limit" class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="imageParams.page"
          v-model:page-size="imageParams.limit"
          layout="total, prev, pager, next"
          :total="imageTotal"
          @current-change="fetchImages"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import {
  getDecoAlbumList,
  createDecoAlbum,
  updateDecoAlbum,
  deleteDecoAlbum,
  getDecoImages,
  uploadDecoImage,
  deleteDecoImage,
} from '@/api/merchant-deco'

// --- Album ---
const albumLoading = ref(false)
const albumList = ref<any[]>([])

async function fetchAlbums() {
  albumLoading.value = true
  try {
    albumList.value = await getDecoAlbumList()
  } finally {
    albumLoading.value = false
  }
}

const albumDialogVisible = ref(false)
const isEditAlbum = ref(false)
const editingAlbumId = ref<number>(0)
const albumSubmitting = ref(false)
const albumFormRef = ref<FormInstance>()

const albumForm = ref({
  albumName: '',
  description: '',
})

const albumRules: FormRules = {
  albumName: [{ required: true, message: '请输入相册名称', trigger: 'blur' }],
}

function showCreateAlbumDialog() {
  isEditAlbum.value = false
  editingAlbumId.value = 0
  albumForm.value = { albumName: '', description: '' }
  albumDialogVisible.value = true
}

function showEditAlbumDialog(row: any) {
  isEditAlbum.value = true
  editingAlbumId.value = row.id
  albumForm.value = { albumName: row.albumName, description: row.description || '' }
  albumDialogVisible.value = true
}

async function handleAlbumSubmit() {
  await albumFormRef.value?.validate()
  albumSubmitting.value = true
  try {
    if (isEditAlbum.value) {
      await updateDecoAlbum(editingAlbumId.value, albumForm.value)
      ElMessage.success('修改成功')
    } else {
      await createDecoAlbum(albumForm.value)
      ElMessage.success('创建成功')
    }
    albumDialogVisible.value = false
    fetchAlbums()
  } finally {
    albumSubmitting.value = false
  }
}

async function handleDeleteAlbum(row: any) {
  await ElMessageBox.confirm(`确定删除相册「${row.albumName}」？相册内图片将一并删除！`, '警告', { type: 'warning' })
  await deleteDecoAlbum(row.id)
  ElMessage.success('已删除')
  fetchAlbums()
}

// --- Image Manager ---
const imageDrawerVisible = ref(false)
const currentAlbum = ref<any>(null)
const imageLoading = ref(false)
const imageList = ref<any[]>([])
const imageTotal = ref(0)

const imageParams = ref({
  page: 1,
  limit: 20,
})

function openImageManager(row: any) {
  currentAlbum.value = row
  imageParams.value.page = 1
  imageDrawerVisible.value = true
  fetchImages()
}

async function fetchImages() {
  if (!currentAlbum.value) return
  imageLoading.value = true
  try {
    const data = await getDecoImages(currentAlbum.value.id, imageParams.value)
    imageList.value = data.list
    imageTotal.value = data.total
  } finally {
    imageLoading.value = false
  }
}

async function handleUploadImage(options: any) {
  if (!currentAlbum.value) return
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    await uploadDecoImage(currentAlbum.value.id, formData)
    ElMessage.success('上传成功')
    fetchImages()
    // 更新相册列表中的图片计数
    fetchAlbums()
  } catch {
    ElMessage.error('上传失败')
  }
}

async function handleDeleteImage(img: any) {
  await ElMessageBox.confirm('确定删除该图片？', '提示', { type: 'warning' })
  await deleteDecoImage(img.id)
  ElMessage.success('已删除')
  fetchImages()
  fetchAlbums()
}

onMounted(fetchAlbums)
</script>
