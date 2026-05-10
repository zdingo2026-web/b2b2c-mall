declare module 'vuedraggable' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'element-plus/dist/locale/zh-cn.mjs' {
  const zhCn: Record<string, any>
  export default zhCn
}
