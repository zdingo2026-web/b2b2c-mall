// ========== Common ==========
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// ========== Category ==========
export interface Category {
  id: number
  name: string
  icon: string
  parentId: number
  sort: number
  children?: Category[]
}

// ========== Product ==========
export interface Product {
  id: number
  name: string
  subTitle: string
  mainImage: string
  images: string[]
  price: number
  originalPrice: number
  sales: number
  storeId: number
  storeName: string
  categoryId: number
  categoryName: string
  tagType?: number
  attrs?: ProductAttr[]
  skus?: ProductSku[]
  detail?: string
}

export interface ProductAttr {
  name: string
  values: string[]
}

export interface ProductSku {
  id: number
  attrs: string
  price: number
  stock: number
  image: string
}

export interface ProductListItem {
  id: number
  name: string
  mainImage: string
  price: number
  originalPrice: number
  sales: number
  storeName: string
  tagType?: number
}

// ========== Banner & Floor ==========
export interface Banner {
  id: number
  imageUrl: string
  linkUrl: string
  title: string
  sort: number
}

export interface Floor {
  id: number
  title: string
  categoryId: number
  products: ProductListItem[]
}

// ========== Notice & Quick Entry ==========
export interface NoticeVO {
  id: number
  title: string
  noticeType: number
}

export interface QuickEntryVO {
  name: string
  icon: string
  bgColor: string
  iconColor: string
  linkType: number
  linkUrl: string
}

// ========== Cart ==========
export interface CartItem {
  id: number
  productId: number
  skuId: number
  name: string
  spec: string
  price: number
  quantity: number
  image: string
  storeName: string
  storeId: number
  stock: number
  selected: boolean
}

// ========== Address ==========
export interface Address {
  id: number
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
  tag?: '家' | '公司' | '学校'
}

// ========== Coupon ==========
export interface Coupon {
  id: number
  name: string
  type: number
  discount: number
  minAmount: number
  startTime: string
  endTime: string
  available: boolean
}

// ========== Order ==========
export interface OrderCreate {
  addressId: number
  note: string
  items: OrderItemInput[]
}

export interface OrderItemInput {
  skuId: number
  quantity: number
}

export interface Order {
  id: number
  orderNo: string
  status: number
  statusText: string
  totalAmount: number
  payAmount: number
  freightAmount: number
  discountAmount?: number
  note: string
  addressName: string
  addressPhone: string
  addressDetail: string
  addressIsDefault?: boolean
  storeId?: number
  tenantName?: string
  deliveryNo?: string
  deliveryCompany?: string
  deliveryStatus?: string
  payType?: number
  invoiceType?: number
  invoiceTitle?: string
  createTime: string
  payTime: string
  shipTime: string
  finishTime: string
  isReviewed?: number
  items: OrderItem[]
}

export interface OrderItem {
  id: number
  productId: number
  skuId: number
  productName: string
  spec: string
  price: number
  quantity: number
  image: string
  storeName: string
}

// ========== Payment ==========
export interface PaymentResult {
  orderNo: string
  payUrl: string
  qrCodeUrl: string
}

// ========== Favorite ==========
export interface Favorite {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  createTime: string
}

// ========== BankCard ==========
export interface BankCard {
  id: number
  bankName: string
  bankCode: string
  cardNoMask: string
  cardType: number
  cardColor: string
  bankLogo: string
  expiryDate: string
  isDefault: number
}

// ========== Footprint ==========
export interface FootprintGroup {
  label: string
  items: {
    id: number
    productName: string
    mainImage: string
    minPrice: string
  }[]
}

// ========== OrderLogistics ==========
export interface OrderLogistics {
  deliveryNo: string
  deliveryCompany: string
  deliveryStatus: number
  deliveryStatusText: string
  traces: { time: string; description: string }[]
}

// ========== Review ==========
export interface Review {
  id: number
  orderId: number
  orderItemId: number
  productId: number
  productName: string
  productImage: string
  content: string
  images: string[]
  score: number
  createTime: string
}

// ========== Search ==========
export interface SearchHistory {
  keyword: string
  time: string
}

// ========== User ==========
export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  email?: string
  memberLevel?: number
  points?: number
  redPacketBalance?: string
  couponCount?: number
  memberNo?: string
}

export const ORDER_STATUS_MAP: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款',
  7: '待评价',
}
