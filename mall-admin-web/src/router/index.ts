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
        redirect: '/platform/member/list',
        meta: { title: '会员管理', icon: 'User' },
        children: [
          {
            path: 'list',
            name: 'MemberList',
            component: () => import('@/views/platform/member/index.vue'),
            meta: { title: '会员列表' },
          },
          {
            path: 'level',
            name: 'MemberLevel',
            component: () => import('@/views/platform/member/level.vue'),
            meta: { title: '会员等级' },
          },
          {
            path: 'realname',
            name: 'MemberRealname',
            component: () => import('@/views/platform/member/realname.vue'),
            meta: { title: '实名认证' },
          },
          {
            path: 'paypwd-reset',
            name: 'PaypwdReset',
            component: () => import('@/views/platform/member/paypwd-reset.vue'),
            meta: { title: '支付密码重置' },
          },
        ],
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
        path: 'promotion',
        name: 'PlatformPromotion',
        redirect: '/platform/promotion/coupon',
        meta: { title: '营销管理', icon: 'Present' },
        children: [
          {
            path: 'coupon',
            name: 'PlatformCoupon',
            component: () => import('@/views/platform/promotion/coupon.vue'),
            meta: { title: '优惠券' },
          },
          {
            path: 'seckill',
            name: 'PlatformSeckill',
            component: () => import('@/views/platform/promotion/seckill.vue'),
            meta: { title: '秒杀活动' },
          },
          {
            path: 'group-buy',
            name: 'PlatformGroupBuy',
            component: () => import('@/views/platform/promotion/group-buy.vue'),
            meta: { title: '拼团活动' },
          },
          {
            path: 'groupon',
            name: 'PlatformGroupon',
            component: () => import('@/views/platform/promotion/groupon.vue'),
            meta: { title: '团购活动' },
          },
          {
            path: 'discount',
            name: 'PlatformDiscount',
            component: () => import('@/views/platform/promotion/discount.vue'),
            meta: { title: '限时折扣' },
          },
          {
            path: 'newcomer-pack',
            name: 'NewcomerPack',
            component: () => import('@/views/platform/promotion/newcomer-pack.vue'),
            meta: { title: '新人礼包' },
          },
          {
            path: 'first-order',
            name: 'FirstOrder',
            component: () => import('@/views/platform/promotion/first-order.vue'),
            meta: { title: '首单优惠' },
          },
          {
            path: 'coupon-detail',
            name: 'CouponDetail',
            component: () => import('@/views/platform/promotion/coupon-detail.vue'),
            meta: { title: '优惠券详情', hidden: true },
          },
          {
            path: 'seckill-detail',
            name: 'SeckillDetail',
            component: () => import('@/views/platform/promotion/seckill-detail.vue'),
            meta: { title: '秒杀详情', hidden: true },
          },
        ],
      },
      {
        path: 'distribution',
        name: 'PlatformDistribution',
        redirect: '/platform/distribution/config',
        meta: { title: '分销管理', icon: 'Share' },
        children: [
          {
            path: 'config',
            name: 'DistributionConfig',
            component: () => import('@/views/platform/distribution/config.vue'),
            meta: { title: '分销配置' },
          },
          {
            path: 'distributor',
            name: 'DistributorManage',
            component: () => import('@/views/platform/distribution/distributor.vue'),
            meta: { title: '分销员管理' },
          },
          {
            path: 'commission',
            name: 'CommissionRecord',
            component: () => import('@/views/platform/distribution/commission.vue'),
            meta: { title: '佣金记录' },
          },
          {
            path: 'withdraw',
            name: 'WithdrawAudit',
            component: () => import('@/views/platform/distribution/withdraw.vue'),
            meta: { title: '提现审核' },
          },
        ],
      },
      {
        path: 'points',
        name: 'PlatformPoints',
        redirect: '/platform/points/rule',
        meta: { title: '积分管理', icon: 'Medal' },
        children: [
          {
            path: 'rule',
            name: 'PointsRule',
            component: () => import('@/views/platform/points/rule.vue'),
            meta: { title: '积分规则' },
          },
          {
            path: 'product',
            name: 'PointsProduct',
            component: () => import('@/views/platform/points/product.vue'),
            meta: { title: '积分商品' },
          },
          {
            path: 'account',
            name: 'PointsAccount',
            component: () => import('@/views/platform/points/account.vue'),
            meta: { title: '积分账户' },
          },
          {
            path: 'detail',
            name: 'PointsDetail',
            component: () => import('@/views/platform/points/detail.vue'),
            meta: { title: '积分明细' },
          },
          {
            path: 'order',
            name: 'PointsOrder',
            component: () => import('@/views/platform/points/order.vue'),
            meta: { title: '积分订单' },
          },
        ],
      },
      {
        path: 'tenant-manage',
        name: 'TenantManage',
        redirect: '/platform/tenant-manage/level',
        meta: { title: '商家运营', icon: 'OfficeBuilding' },
        children: [
          {
            path: 'level',
            name: 'TenantLevel',
            component: () => import('@/views/platform/tenant-manage/level.vue'),
            meta: { title: '商家等级' },
          },
          {
            path: 'commission',
            name: 'CategoryCommission',
            component: () => import('@/views/platform/tenant-manage/commission.vue'),
            meta: { title: '分类佣金' },
          },
          {
            path: 'settlement',
            name: 'TenantSettlement',
            component: () => import('@/views/platform/tenant-manage/settlement.vue'),
            meta: { title: '结算管理' },
          },
          {
            path: 'freeze',
            name: 'TenantFreeze',
            component: () => import('@/views/platform/tenant-manage/freeze.vue'),
            meta: { title: '冻结记录' },
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
        path: 'promotion',
        name: 'MerchantPromotion',
        redirect: '/merchant/promotion/coupon',
        meta: { title: '营销管理', icon: 'Present' },
        children: [
          {
            path: 'coupon',
            name: 'MerchantCoupon',
            component: () => import('@/views/merchant/promotion/coupon.vue'),
            meta: { title: '优惠券' },
          },
          {
            path: 'seckill',
            name: 'MerchantSeckill',
            component: () => import('@/views/merchant/promotion/seckill.vue'),
            meta: { title: '秒杀活动' },
          },
          {
            path: 'group-buy',
            name: 'MerchantGroupBuy',
            component: () => import('@/views/merchant/promotion/group-buy.vue'),
            meta: { title: '拼团活动' },
          },
          {
            path: 'groupon',
            name: 'MerchantGroupon',
            component: () => import('@/views/merchant/promotion/groupon.vue'),
            meta: { title: '团购活动' },
          },
          {
            path: 'discount',
            name: 'MerchantDiscount',
            component: () => import('@/views/merchant/promotion/discount.vue'),
            meta: { title: '限时折扣' },
          },
          {
            path: 'coupon-detail',
            name: 'MerchantCouponDetail',
            component: () => import('@/views/merchant/promotion/coupon-detail.vue'),
            meta: { title: '优惠券详情', hidden: true },
          },
          {
            path: 'seckill-detail',
            name: 'MerchantSeckillDetail',
            component: () => import('@/views/merchant/promotion/seckill-detail.vue'),
            meta: { title: '秒杀活动详情', hidden: true },
          },
          {
            path: 'group-buy-detail',
            name: 'MerchantGroupBuyDetail',
            component: () => import('@/views/merchant/promotion/group-buy-detail.vue'),
            meta: { title: '拼团活动详情', hidden: true },
          },
        ],
      },
      {
        path: 'deco',
        name: 'MerchantDeco',
        redirect: '/merchant/deco/page',
        meta: { title: '店铺装修', icon: 'Brush' },
        children: [
          {
            path: 'page',
            name: 'MerchantDecoPage',
            component: () => import('@/views/merchant/deco/page.vue'),
            meta: { title: '页面管理' },
          },
          {
            path: 'album',
            name: 'MerchantDecoAlbum',
            component: () => import('@/views/merchant/deco/album.vue'),
            meta: { title: '相册管理' },
          },
          {
            path: 'template',
            name: 'MerchantDecoTemplate',
            component: () => import('@/views/merchant/deco/template.vue'),
            meta: { title: '模板管理' },
          },
          {
            path: 'editor',
            name: 'MerchantDecoEditor',
            component: () => import('@/views/merchant/deco/editor.vue'),
            meta: { title: '装修编辑器', hidden: true },
          },
        ],
      },
      {
        path: 'distribution',
        name: 'MerchantDistribution',
        redirect: '/merchant/distribution/config',
        meta: { title: '分销管理', icon: 'Share' },
        children: [
          {
            path: 'config',
            name: 'MerchantDistributionConfig',
            component: () => import('@/views/merchant/distribution/config.vue'),
            meta: { title: '分销设置' },
          },
          {
            path: 'distributor',
            name: 'MerchantDistributor',
            component: () => import('@/views/merchant/distribution/distributor.vue'),
            meta: { title: '分销商管理' },
          },
          {
            path: 'commission',
            name: 'MerchantCommission',
            component: () => import('@/views/merchant/distribution/commission.vue'),
            meta: { title: '佣金记录' },
          },
          {
            path: 'product',
            name: 'MerchantDistributionProduct',
            component: () => import('@/views/merchant/distribution/product.vue'),
            meta: { title: '分销商品' },
          },
        ],
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
