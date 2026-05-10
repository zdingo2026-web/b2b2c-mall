<template>
  <div class="deco-editor">
    <!-- Top Bar -->
    <div class="h-12 bg-white border-b flex items-center justify-between px-4 mb-0">
      <div class="flex items-center gap-2">
        <el-button @click="goBack" text size="small">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <span class="font-bold text-sm">{{ templateName || '装修编辑器' }}</span>
      </div>
      <div class="flex gap-2">
        <el-button size="small" @click="handlePreview">预览</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button size="small" type="success" :loading="publishing" @click="handlePublish">发布</el-button>
      </div>
    </div>

    <!-- Three Column Layout -->
    <div class="flex" style="height: calc(100vh - 48px)">
      <!-- Left: Component Library (200px) -->
      <div class="w-[200px] bg-white border-r overflow-y-auto p-3">
        <h3 class="text-sm font-bold mb-3 text-gray-600">组件库</h3>
        <div v-for="group in componentGroups" :key="group.name" class="mb-4">
          <div class="text-xs text-gray-400 mb-2">{{ group.name }}</div>
          <div class="grid grid-cols-2 gap-2">
            <div
              v-for="comp in group.components"
              :key="comp.type"
              class="flex flex-col items-center justify-center p-2 border rounded cursor-pointer hover:bg-blue-50 hover:border-blue-300 transition-colors"
              draggable="true"
              @dragstart="onDragStart($event, comp.type)"
            >
              <el-icon :size="20" class="mb-1 text-gray-500"><component :is="comp.icon" /></el-icon>
              <span class="text-xs text-gray-600">{{ comp.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Center: Canvas (375px) -->
      <div class="flex-1 bg-gray-100 overflow-y-auto flex justify-center py-6">
        <div
          class="w-[375px] min-h-[667px] bg-white shadow-md rounded"
          @dragover.prevent
          @drop="onDropToCanvas"
        >
          <div v-if="canvasComponents.length === 0" class="h-full flex items-center justify-center text-gray-300">
            <div class="text-center">
              <el-icon :size="48"><Plus /></el-icon>
              <p class="mt-2 text-sm">拖拽组件到此处</p>
            </div>
          </div>
          <draggable
            v-else
            v-model="canvasComponents"
            item-key="id"
            class="min-h-[667px]"
            ghost-class="opacity-30"
          >
            <template #item="{ element, index }">
              <div
                class="relative border-2 cursor-pointer transition-colors"
                :class="selectedIndex === index ? 'border-blue-500' : 'border-transparent hover:border-blue-200'"
                @click="selectComponent(index)"
              >
                <!-- Component Preview -->
                <div class="p-2">
                  <!-- Banner -->
                  <div v-if="element.type === 'banner'" class="w-full h-32 bg-gradient-to-r from-blue-400 to-purple-400 rounded flex items-center justify-center text-white text-sm">
                    轮播图组件 ({{ element.data?.images?.length || 0 }}张)
                  </div>
                  <!-- Image Nav -->
                  <div v-else-if="element.type === 'image-nav'" class="grid grid-cols-4 gap-2 py-2">
                    <div v-for="i in 4" :key="i" class="flex flex-col items-center">
                      <div class="w-10 h-10 bg-gray-200 rounded-full mb-1" />
                      <span class="text-xs text-gray-400">导航{{ i }}</span>
                    </div>
                  </div>
                  <!-- Product List -->
                  <div v-else-if="element.type === 'product-list'" class="py-2">
                    <div class="text-sm font-bold mb-2 px-2">{{ element.data?.title || '商品推荐' }}</div>
                    <div class="grid grid-cols-2 gap-2 px-2">
                      <div v-for="i in (element.data?.count || 4)" :key="i" class="bg-gray-50 rounded p-1">
                        <div class="w-full h-20 bg-gray-200 rounded mb-1" />
                        <div class="h-3 bg-gray-200 rounded w-3/4 mb-1" />
                        <div class="h-3 bg-red-100 rounded w-1/2" />
                      </div>
                    </div>
                  </div>
                  <!-- Rich Text -->
                  <div v-else-if="element.type === 'rich-text'" class="p-2 text-sm text-gray-500">
                    {{ element.data?.content || '富文本内容区域' }}
                  </div>
                  <!-- Title -->
                  <div v-else-if="element.type === 'title'" class="p-2">
                    <div class="text-base font-bold">{{ element.data?.text || '标题组件' }}</div>
                  </div>
                  <!-- Coupon -->
                  <div v-else-if="element.type === 'coupon'" class="p-2">
                    <div class="bg-red-50 rounded p-2 text-center text-red-500 text-sm">优惠券组件</div>
                  </div>
                  <!-- Blank -->
                  <div v-else-if="element.type === 'blank'" class="h-6" />
                  <!-- Search -->
                  <div v-else-if="element.type === 'search'" class="p-2">
                    <div class="bg-gray-100 rounded-full px-4 py-2 text-sm text-gray-400">搜索商品</div>
                  </div>
                  <!-- Default -->
                  <div v-else class="p-4 text-center text-gray-400 text-sm">
                    {{ element.type }} 组件
                  </div>
                </div>
                <!-- Delete Button -->
                <div
                  v-if="selectedIndex === index"
                  class="absolute -top-3 -right-3 z-10"
                >
                  <el-button type="danger" size="small" circle @click.stop="removeComponent(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </div>

      <!-- Right: Property Panel (300px) -->
      <div class="w-[300px] bg-white border-l overflow-y-auto p-4">
        <template v-if="selectedIndex >= 0 && canvasComponents[selectedIndex]">
          <h3 class="text-sm font-bold mb-4 text-gray-600">
            组件属性 - {{ getComponentLabel(canvasComponents[selectedIndex].type) }}
          </h3>
          <el-form label-width="80px" size="small">
            <!-- Banner Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'banner'">
              <el-form-item label="自动播放">
                <el-switch v-model="canvasComponents[selectedIndex].data.autoplay" />
              </el-form-item>
              <el-form-item label="间隔时间">
                <el-input-number v-model="canvasComponents[selectedIndex].data.interval" :min="1000" :step="1000" class="w-full" />
              </el-form-item>
            </template>
            <!-- Product List Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'product-list'">
              <el-form-item label="标题">
                <el-input v-model="canvasComponents[selectedIndex].data.title" placeholder="模块标题" />
              </el-form-item>
              <el-form-item label="显示数量">
                <el-input-number v-model="canvasComponents[selectedIndex].data.count" :min="1" :max="20" class="w-full" />
              </el-form-item>
              <el-form-item label="布局">
                <el-radio-group v-model="canvasComponents[selectedIndex].data.layout">
                  <el-radio value="grid">网格</el-radio>
                  <el-radio value="list">列表</el-radio>
                </el-radio-group>
              </el-form-item>
            </template>
            <!-- Title Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'title'">
              <el-form-item label="标题文字">
                <el-input v-model="canvasComponents[selectedIndex].data.text" placeholder="请输入标题" />
              </el-form-item>
              <el-form-item label="对齐方式">
                <el-radio-group v-model="canvasComponents[selectedIndex].data.align">
                  <el-radio value="left">左对齐</el-radio>
                  <el-radio value="center">居中</el-radio>
                </el-radio-group>
              </el-form-item>
            </template>
            <!-- Rich Text Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'rich-text'">
              <el-form-item label="内容">
                <el-input
                  v-model="canvasComponents[selectedIndex].data.content"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入富文本内容"
                />
              </el-form-item>
            </template>
            <!-- Image Nav Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'image-nav'">
              <el-form-item label="每行数量">
                <el-input-number v-model="canvasComponents[selectedIndex].data.cols" :min="3" :max="5" class="w-full" />
              </el-form-item>
            </template>
            <!-- Blank Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'blank'">
              <el-form-item label="高度(px)">
                <el-input-number v-model="canvasComponents[selectedIndex].data.height" :min="5" :max="100" class="w-full" />
              </el-form-item>
            </template>
            <!-- Search Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'search'">
              <el-form-item label="占位文字">
                <el-input v-model="canvasComponents[selectedIndex].data.placeholder" placeholder="搜索框占位文字" />
              </el-form-item>
              <el-form-item label="圆角样式">
                <el-switch v-model="canvasComponents[selectedIndex].data.rounded" />
              </el-form-item>
            </template>
            <!-- Coupon Props -->
            <template v-if="canvasComponents[selectedIndex].type === 'coupon'">
              <el-form-item label="显示数量">
                <el-input-number v-model="canvasComponents[selectedIndex].data.count" :min="1" :max="6" class="w-full" />
              </el-form-item>
            </template>
          </el-form>
        </template>
        <div v-else class="text-center text-gray-400 mt-10">
          <el-icon :size="40"><Setting /></el-icon>
          <p class="mt-2 text-sm">点击画布中的组件<br/>编辑其属性</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Delete, Setting, Picture, Grid, Goods, Document, EditPen, Tickets, Search, SemiSelect } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { getDecoTemplateDetail, createDecoTemplate } from '@/api/merchant-deco'

const route = useRoute()
const router = useRouter()

const templateName = ref('')
const templateId = ref<number>(0)
const saving = ref(false)
const publishing = ref(false)
const selectedIndex = ref(-1)

interface CanvasComponent {
  id: string
  type: string
  data: Record<string, any>
}

const canvasComponents = ref<CanvasComponent[]>([])

const componentGroups = [
  {
    name: '基础组件',
    components: [
      { type: 'search', label: '搜索栏', icon: Search },
      { type: 'banner', label: '轮播图', icon: Picture },
      { type: 'image-nav', label: '图文导航', icon: Grid },
      { type: 'title', label: '标题', icon: EditPen },
    ],
  },
  {
    name: '业务组件',
    components: [
      { type: 'product-list', label: '商品组', icon: Goods },
      { type: 'coupon', label: '优惠券', icon: Tickets },
      { type: 'rich-text', label: '富文本', icon: Document },
    ],
  },
  {
    name: '辅助组件',
    components: [
      { type: 'blank', label: '空白间隔', icon: SemiSelect },
    ],
  },
]

const componentLabelMap: Record<string, string> = {
  'search': '搜索栏',
  'banner': '轮播图',
  'image-nav': '图文导航',
  'title': '标题',
  'product-list': '商品组',
  'coupon': '优惠券',
  'rich-text': '富文本',
  'blank': '空白间隔',
}

function getComponentLabel(type: string) {
  return componentLabelMap[type] || type
}

function getDefaultData(type: string): Record<string, any> {
  const defaults: Record<string, any> = {
    'search': { placeholder: '搜索商品', rounded: true },
    'banner': { images: [], autoplay: true, interval: 3000 },
    'image-nav': { items: [], cols: 4 },
    'title': { text: '标题', align: 'left' },
    'product-list': { title: '商品推荐', count: 4, layout: 'grid' },
    'coupon': { count: 3 },
    'rich-text': { content: '' },
    'blank': { height: 24 },
  }
  return { ...(defaults[type] || {}) }
}

let idCounter = 0
function generateId() {
  return `comp_${Date.now()}_${++idCounter}`
}

function onDragStart(event: DragEvent, type: string) {
  event.dataTransfer?.setData('componentType', type)
}

function onDropToCanvas(event: DragEvent) {
  const type = event.dataTransfer?.getData('componentType')
  if (!type) return
  const comp: CanvasComponent = {
    id: generateId(),
    type,
    data: getDefaultData(type),
  }
  canvasComponents.value.push(comp)
  selectedIndex.value = canvasComponents.value.length - 1
}

function selectComponent(index: number) {
  selectedIndex.value = index
}

function removeComponent(index: number) {
  canvasComponents.value.splice(index, 1)
  if (selectedIndex.value >= canvasComponents.value.length) {
    selectedIndex.value = canvasComponents.value.length - 1
  }
}

async function loadTemplate() {
  const id = Number(route.query.templateId)
  if (!id) return
  templateId.value = id
  try {
    const detail = await getDecoTemplateDetail(id)
    templateName.value = detail.templateName || ''
    const config = detail.componentConfig
    if (Array.isArray(config)) {
      canvasComponents.value = config.map((item: any) => ({
        id: item.id || generateId(),
        type: item.type,
        data: item.data || getDefaultData(item.type),
      }))
    }
  } catch {
    ElMessage.error('获取模板详情失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      id: templateId.value || undefined,
      templateName: templateName.value || '未命名模板',
      componentConfig: canvasComponents.value.map(c => ({
        type: c.type,
        data: c.data,
      })),
    }
    const result = await createDecoTemplate(payload)
    if (!templateId.value && result?.id) {
      templateId.value = result.id
    }
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  await ElMessageBox.confirm('确定发布？发布后将对用户可见。', '提示', { type: 'warning' })
  publishing.value = true
  try {
    await handleSave()
    ElMessage.success('发布成功')
  } finally {
    publishing.value = false
  }
}

function handlePreview() {
  const config = JSON.stringify(canvasComponents.value)
  const html = generatePreviewHtml(config)
  const win = window.open('', '_blank')
  if (win) {
    win.document.write(html)
    win.document.close()
  }
}

function generatePreviewHtml(config: string) {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>预览</title>
  <style>
    body { margin: 0; padding: 0; background: #f5f5f5; display: flex; justify-content: center; }
    .preview-container { width: 375px; min-height: 100vh; background: #fff; }
    .comp { padding: 8px; }
    .banner-placeholder { height: 128px; background: linear-gradient(to right, #60a5fa, #a78bfa); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; }
    .product-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
    .product-item { background: #f9fafb; border-radius: 4px; padding: 4px; }
    .product-img { height: 80px; background: #e5e7eb; border-radius: 4px; margin-bottom: 4px; }
    .product-title { height: 12px; background: #e5e7eb; border-radius: 2px; width: 75%; margin-bottom: 4px; }
    .product-price { height: 12px; background: #fee2e2; border-radius: 2px; width: 50%; }
    .search-bar { background: #f3f4f6; border-radius: 9999px; padding: 8px 16px; color: #9ca3af; font-size: 14px; }
    .nav-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; padding: 8px 0; }
    .nav-item { text-align: center; }
    .nav-icon { width: 40px; height: 40px; background: #e5e7eb; border-radius: 50%; margin: 0 auto 4px; }
    .nav-label { font-size: 12px; color: #9ca3af; }
    .title-comp { font-size: 16px; font-weight: bold; padding: 8px; }
    .coupon-comp { background: #fef2f2; border-radius: 8px; padding: 16px; text-align: center; color: #ef4444; }
    .blank-comp { height: 24px; }
    .rich-text { font-size: 14px; color: #6b7280; padding: 8px; }
  </style>
</head>
<body>
  <div class="preview-container" id="app"></div>
  <script>
    const components = ${config};
    const container = document.getElementById('app');
    components.forEach(comp => {
      const div = document.createElement('div');
      div.className = 'comp';
      switch(comp.type) {
        case 'banner':
          div.innerHTML = '<div class="banner-placeholder">轮播图</div>';
          break;
        case 'search':
          div.innerHTML = '<div class="search-bar">' + (comp.data.placeholder || '搜索商品') + '</div>';
          break;
        case 'image-nav':
          let navHtml = '<div class="nav-grid">';
          for(let i=0;i<(comp.data.cols||4);i++) navHtml += '<div class="nav-item"><div class="nav-icon"></div><div class="nav-label">导航'+(i+1)+'</div></div>';
          navHtml += '</div>';
          div.innerHTML = navHtml;
          break;
        case 'title':
          div.innerHTML = '<div class="title-comp" style="text-align:' + (comp.data.align||'left') + '">' + (comp.data.text||'标题') + '</div>';
          break;
        case 'product-list':
          let pHtml = '<div style="font-weight:bold;padding:8px;">' + (comp.data.title||'商品推荐') + '</div><div class="product-grid">';
          for(let i=0;i<(comp.data.count||4);i++) pHtml += '<div class="product-item"><div class="product-img"></div><div class="product-title"></div><div class="product-price"></div></div>';
          pHtml += '</div>';
          div.innerHTML = pHtml;
          break;
        case 'coupon':
          div.innerHTML = '<div class="coupon-comp">优惠券</div>';
          break;
        case 'rich-text':
          div.innerHTML = '<div class="rich-text">' + (comp.data.content||'富文本内容') + '</div>';
          break;
        case 'blank':
          div.innerHTML = '<div class="blank-comp" style="height:' + (comp.data.height||24) + 'px"></div>';
          break;
        default:
          div.innerHTML = '<div style="padding:16px;text-align:center;color:#9ca3af;">' + comp.type + '</div>';
      }
      container.appendChild(div);
    });
  <\/script>
</body>
</html>`
}

function goBack() {
  router.push('/merchant/deco/template')
}

onMounted(() => {
  loadTemplate()
})
</script>

<style scoped>
.deco-editor :deep(.el-card) {
  border-radius: 0;
}
</style>
