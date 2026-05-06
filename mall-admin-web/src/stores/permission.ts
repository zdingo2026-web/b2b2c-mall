import { defineStore } from 'pinia'

interface RouteItem {
  path: string
  name: string
  meta?: {
    title: string
    icon?: string
    hidden?: boolean
  }
  children?: RouteItem[]
}

/** Platform admin menu */
const platformMenus: RouteItem[] = [
  {
    path: '/platform/dashboard',
    name: 'PlatformDashboard',
    meta: { title: '工作台', icon: 'Odometer' },
  },
  {
    path: '/platform/merchant',
    name: 'PlatformMerchant',
    meta: { title: '商家管理', icon: 'Shop' },
    children: [
      { path: '/platform/merchant/list', name: 'MerchantList', meta: { title: '商家列表' } },
      { path: '/platform/merchant/audit', name: 'MerchantAudit', meta: { title: '入驻审核' } },
    ],
  },
  {
    path: '/platform/product',
    name: 'PlatformProduct',
    meta: { title: '商品管理', icon: 'Goods' },
    children: [
      { path: '/platform/product/category', name: 'ProductCategory', meta: { title: '分类管理' } },
      { path: '/platform/product/brand', name: 'ProductBrand', meta: { title: '品牌管理' } },
      { path: '/platform/product/list', name: 'ProductList', meta: { title: '自营商品' } },
    ],
  },
  {
    path: '/platform/order',
    name: 'PlatformOrder',
    meta: { title: '订单管理', icon: 'Document' },
  },
  {
    path: '/platform/content',
    name: 'PlatformContent',
    meta: { title: '内容管理', icon: 'Reading' },
    children: [
      { path: '/platform/content/notice', name: 'PlatformNotice', meta: { title: '公告管理', icon: 'notification' } },
    ],
  },
  {
    path: '/platform/member',
    name: 'PlatformMember',
    meta: { title: '会员管理', icon: 'User' },
  },
  {
    path: '/platform/system',
    name: 'PlatformSystem',
    meta: { title: '系统设置', icon: 'Setting' },
  },
]

/** Merchant admin menu */
const merchantMenus: RouteItem[] = [
  {
    path: '/merchant/dashboard',
    name: 'MerchantDashboard',
    meta: { title: '工作台', icon: 'Odometer' },
  },
  {
    path: '/merchant/product',
    name: 'MerchantProduct',
    meta: { title: '商品管理', icon: 'Goods' },
    children: [
      { path: '/merchant/product/list', name: 'MerchantProductList', meta: { title: '商品列表' } },
      { path: '/merchant/product/add', name: 'MerchantProductAdd', meta: { title: '发布商品' } },
    ],
  },
  {
    path: '/merchant/order',
    name: 'MerchantOrder',
    meta: { title: '订单管理', icon: 'Document' },
  },
  {
    path: '/merchant/refund',
    name: 'MerchantRefund',
    meta: { title: '退换货管理', icon: 'RefreshRight' },
  },
  {
    path: '/merchant/shop',
    name: 'MerchantShop',
    meta: { title: '店铺设置', icon: 'Shop' },
  },
  {
    path: '/merchant/admin',
    name: 'MerchantAdmin',
    meta: { title: '子管理员', icon: 'User' },
  },
  {
    path: '/merchant/finance',
    name: 'MerchantFinance',
    meta: { title: '资金管理', icon: 'Wallet' },
  },
]

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    menuList: [] as RouteItem[],
  }),

  getters: {
    menus: (state): RouteItem[] => state.menuList,
  },

  actions: {
    generateMenus(role: 'platform' | 'merchant') {
      this.menuList = role === 'platform' ? platformMenus : merchantMenus
    },

    clearMenus() {
      this.menuList = []
    },
  },
})
