declare module '@wangeditor/editor-for-vue' {
  import type { DefineComponent } from 'vue'
  export const Editor: DefineComponent<any, any, any>
  export const Toolbar: DefineComponent<any, any, any>
}

declare module '@wangeditor/editor' {
  export interface IDomEditor {
    getHtml(): string
    destroy(): void
  }
  export interface IEditorConfig {
    placeholder?: string
    MENU_CONF?: Record<string, any>
  }
  export interface IToolbarConfig {
    [key: string]: any
  }
}
