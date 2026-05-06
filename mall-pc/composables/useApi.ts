import type {
  Category,
  Product,
  ProductListItem,
  Banner,
  Floor,
  PageResult,
  Address,
  Order,
  OrderCreate,
  PaymentResult,
  Favorite,
  Review,
  SearchHistory,
} from './types'

/** Home page data */
export function useHomeApi() {
  const { get } = useRequest()

  return {
    fetchBanners: () => get<Banner[]>('/home/banners'),
    fetchFloors: () => get<Floor[]>('/home/floors'),
    fetchNewProducts: () => get<ProductListItem[]>('/home/new-products'),
    fetchRecommendProducts: () => get<ProductListItem[]>('/home/recommend-products'),
  }
}

/** Category API */
export function useCategoryApi() {
  const { get } = useRequest()

  return {
    fetchTree: () => get<Category[]>('/category/tree'),
    fetchProducts: (categoryId: number, page = 1, pageSize = 20) =>
      get<PageResult<ProductListItem>>('/category/products', {
        categoryId,
        page,
        pageSize,
      }),
  }
}

/** Product API */
export function useProductApi() {
  const { get } = useRequest()

  return {
    fetchDetail: (id: number) => get<Product>(`/product/${id}`),
    fetchList: (params: {
      keyword?: string
      categoryId?: number
      sort?: string
      priceMin?: number
      priceMax?: number
      page?: number
      pageSize?: number
    }) => get<PageResult<ProductListItem>>('/product/list', params as Record<string, string | number | undefined>),
  }
}

/** Cart API */
export function useCartApi() {
  const { get, post, put, delete: del } = useRequest()

  return {
    fetchList: () => get<any[]>('/cart/list'),
    addItem: (data: { productId: number; skuId: number; quantity: number }) =>
      post('/cart/add', data),
    updateQuantity: (data: { itemId: number; quantity: number }) =>
      put('/cart/update', data),
    removeItem: (itemId: number) =>
      del(`/cart/remove/${itemId}`),
    clearCart: () => del('/cart/clear'),
  }
}

/** Address API */
export function useAddressApi() {
  const { get, post, put, delete: del } = useRequest()

  return {
    fetchList: () => get<Address[]>('/address/list'),
    fetchDetail: (id: number) => get<Address>(`/address/${id}`),
    create: (data: Omit<Address, 'id'>) => post('/address/create', data),
    update: (data: Address) => put('/address/update', data),
    remove: (id: number) => del(`/address/remove/${id}`),
    setDefault: (id: number) => put(`/address/set-default/${id}`),
  }
}

/** Order API */
export function useOrderApi() {
  const { get, post, put, delete: del } = useRequest()

  return {
    create: (data: OrderCreate) => post<{ orderNo: string; orderId: number }>('/order/create', data),
    fetchList: (params: { status?: number; page?: number; pageSize?: number }) =>
      get<PageResult<Order>>('/order/list', params as Record<string, string | number | undefined>),
    fetchDetail: (orderNo: string) => get<Order>(`/order/${orderNo}`),
    cancel: (orderNo: string) => put(`/order/cancel/${orderNo}`),
    confirm: (orderNo: string) => put(`/order/confirm/${orderNo}`),
    delete: (orderNo: string) => del(`/order/delete/${orderNo}`),
  }
}

/** Payment API */
export function usePaymentApi() {
  const { get, post } = useRequest()

  return {
    createPayment: (data: { orderNo: string; payMethod: number }) =>
      post<PaymentResult>('/payment/create', data),
    checkStatus: (orderNo: string) =>
      get<{ status: number; payTime: string }>(`/payment/status/${orderNo}`),
  }
}

/** Favorite API */
export function useFavoriteApi() {
  const { get, post, delete: del } = useRequest()

  return {
    fetchList: (page = 1, pageSize = 20) =>
      get<PageResult<Favorite>>('/favorite/list', { page, pageSize }),
    add: (productId: number) => post('/favorite/add', { productId }),
    remove: (productId: number) => del(`/favorite/remove/${productId}`),
    check: (productId: number) => get<{ favorited: boolean }>(`/favorite/check/${productId}`),
  }
}

/** Review API */
export function useReviewApi() {
  const { get, post } = useRequest()

  return {
    fetchByOrder: (orderId: number) => get<Review[]>(`/review/order/${orderId}`),
    fetchByProduct: (productId: number, page = 1, pageSize = 10) =>
      get<PageResult<Review>>(`/review/product/${productId}`, { page, pageSize }),
    create: (data: { orderItemId: number; score: number; content: string; images?: string[] }) =>
      post('/review/create', data),
  }
}

/** Search API */
export function useSearchApi() {
  const { get } = useRequest()

  return {
    fetchHistory: () => get<SearchHistory[]>('/search/history'),
    clearHistory: () => get('/search/clear-history'),
    fetchHot: () => get<string[]>('/search/hot'),
  }
}

/** Auth API */
export function useAuthApi() {
  const { post } = useRequest()

  return {
    sendSmsCode: (phone: string) => post('/auth/sms/send', { phone }),
    smsLogin: (data: { phone: string; code: string }) =>
      post<{ accessToken: string; refreshToken: string; userInfo: any }>('/auth/sms/login', data),
    register: (data: { phone: string; code: string; password: string }) =>
      post<{ accessToken: string; refreshToken: string; userInfo: any }>('/auth/register', data),
    login: (data: { account: string; password: string }) =>
      post<{ accessToken: string; refreshToken: string; userInfo: any }>('/auth/login', data),
    logout: () => post('/auth/logout'),
  }
}
