<template>
  <div class="rich-text-editor">
    <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
    <Editor
      :defaultConfig="editorConfig"
      :modelValue="modelValue"
      :mode="mode"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { uploadFile } from '@/api/system'

const props = withDefaults(defineProps<{
  modelValue?: string
  mode?: 'default' | 'simple'
}>(), {
  modelValue: '',
  mode: 'default',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorRef = shallowRef<IDomEditor>()

const toolbarConfig: Partial<IToolbarConfig> = {}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        try {
          const url = await uploadFile(file)
          insertFn(url, '', '')
        } catch {
          // uploadFile already shows error message
        }
      },
    },
  },
}

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
}

function handleChange(editor: IDomEditor) {
  // Sanitize output to strip any injected script tags or event handlers
  const raw = editor.getHtml()
  const sanitized = raw
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/\s+on\w+\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]+)/gi, '')
    .replace(/(href|src)\s*=\s*(?:"javascript:[^"]*"|'javascript:[^']*')/gi, '')
  emit('update:modelValue', sanitized)
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style src="@wangeditor/editor/dist/css/style.css"></style>

<style scoped>
.rich-text-editor {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
}

.rich-text-editor :deep(.w-e-toolbar) {
  border-bottom: 1px solid var(--el-border-color);
}

.rich-text-editor :deep(.w-e-text-container) {
  min-height: 300px;
}

.rich-text-editor :deep(.w-e-text-placeholder) {
  top: 10px;
  left: 10px;
}
</style>
