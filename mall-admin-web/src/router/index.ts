import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const Layout = () => import('@/layouts/AdminLayout.vue')

/** Public routes */
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
]

/** Platform admin routes */
const platformRoutes: RouteRecordRaw[] = [
  {
    path: '/platform',
    component: Layout,
    redirect: '/platform/dashboard',
    meta: { title: '平台管理', role: 'platform' },
    children: [
      {
        path: 'dashboard',
        name: 'PlatformDashboard',
        component: () => import('@/views/platform/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'merchant',
        name: 'MerchantManage',
        redirect: '/platform/merchant/list',
        meta: { title: '商家管理', icon: 'Shop' },
        children: [
          {
            path: 'list',
            name: 'MerchantList',
            component: () => import('@/views/platform/merchant/list.vue'),
            meta: { title: '商家列表' },
          },
          {
            path: 'audit',
            name: 'MerchantAudit',
            component: () => import('@/views/platform/merchant/audit.vue'),
            meta: { title: '入驻审核' },
          },
        ],
      },
      {
        path: 'product',
        name: 'ProductManage',
        redirect: '/platform/product/category',
        meta: { title: '商品管理', icon: 'Goods' },
        children: [
          {
            path: 'category',
            name: 'ProductCategory',
            component: () => import('@/views/platform/product/category/index.vue'),
            meta: { title: '分类管理' },
          },
          {
            path: 'brand',
            name: 'ProductBrand',
            component: () => import('@/views/platform/product/brand/index.vue'),
            meta: { title: '品牌管理' },
          },
          {
            path: 'list',
            name: 'PlatformProductList',
            component: () => import('@/views/platform/product/list/index.vue'),
            meta: { title: '自营商品' },
          },
          {
            path: 'add',
            name: 'PlatformProductAdd',
            component: () => import('@/views/platform/product/add.vue'),
            meta: { title: '新增商品' },
          },
        ],
      },
      {
        path: 'order',
        name: 'PlatformOrder',
        component: () => import('@/views/platform/order/index.vue'),
        meta: { title: '订单管理', icon: 'Document' },
      },
      {
        path: 'member',
        name: 'PlatformMember',
        component: () => import('@/views/platform/member/index.vue'),
        meta: { title: '会员管理', icon: 'User' },
      },
      {
        path: 'content',
        name: 'PlatformContent',
        redirect: '/platform/content/notice',
        meta: { title: '内容管理', icon: 'Reading' },
        children: [
          {
            path: 'notice',
            name: 'PlatformNotice',
            component: () => import('@/views/platform/notice/index.vue'),
            meta: { title: '公告管理' },
          },
        ],
      },
      {
        path: 'system',
        name: 'PlatformSystem',
        component: () => import('@/views/platform/system/index.vue'),
        meta: { title: '系统设置', icon: 'Setting' },
      },
    ],
  },
]

/** Merchant admin routes */
const merchantRoutes: RouteRecordRaw[] = [
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/dashboard',
    meta: { title: '商户管理', role: 'merchant' },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'product',
        name: 'MerchantProduct',
        redirect: '/merchant/product/list',
        meta: { title: '商品管理', icon: 'Goods' },
        children: [
          {
            path: 'list',
            name: 'MerchantProductList',
            component: () => import('@/views/merchant/product/list.vue'),
            meta: { title: '商品列表' },
          },
          {
            path: 'add',
            name: 'MerchantProductAdd',
            component: () => import('@/views/merchant/product/add.vue'),
            meta: { title: '发布商品' },
          },
        ],
      },
      {
        path: 'order',
        name: 'MerchantOrder',
        component: () => import('@/views/merchant/order/index.vue'),
        meta: { title: '订单管理', icon: 'Document' },
      },
      {
        path: 'refund',
        name: 'MerchantRefund',
        component: () => import('@/views/merchant/refund/index.vue'),
        meta: { title: '退换货管理', icon: 'RefreshRight' },
      },
      {
        path: 'shop',
        name: 'MerchantShop',
        component: () => import('@/views/merchant/shop/index.vue'),
        meta: { title: '店铺设置', icon: 'Shop' },
      },
      {
        path: 'admin',
        name: 'MerchantAdmin',
        component: () => import('@/views/merchant/admin/index.vue'),
        meta: { title: '子管理员', icon: 'User' },
      },
      {
        path: 'finance',
        name: 'MerchantFinance',
        component: () => import('@/views/merchant/finance/index.vue'),
        meta: { title: '资金管理', icon: 'Wallet' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', redirect: '/login' }, ...publicRoutes, ...platformRoutes, ...merchantRoutes],
})

/** Navigation guard */
router.beforeEach(async (to, _from, next) => {
  // Set page title
  document.title = `${to.meta.title || 'B2B2C商城'} - 管理后台`

  // Allow login page access without auth
  if (to.path === '/login') {
    next()
    return
  }

  // Check auth
  const token = getToken()
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // Check role-based access: find the role requirement from the route
  // or any matched parent route record
  const requiredRole = to.meta.role || to.matched.find(r => r.meta.role)?.meta.role
  if (requiredRole) {
    const userStore = useUserStore()
    // Ensure user info is loaded
    if (!userStore.userInfo) {
      try {
        await userStore.fetchAdminInfo()
      } catch {
        // Token invalid or expired - redirect to login
        userStore.logout()
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    }
    const userRole = userStore.role
    if (userRole !== requiredRole) {
      // Role mismatch - redirect to appropriate dashboard
      const redirectPath = userRole === 'platform' ? '/platform/dashboard' : '/merchant/dashboard'
      next(redirectPath)
      return
    }
  }

  next()
})

export default router
