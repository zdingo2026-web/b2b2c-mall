import type { Page } from '@playwright/test'

/**
 * 平台管理后台登录
 * @param page Playwright Page对象
 * @param username 用户名
 * @param password 密码
 */
export async function loginAsPlatformAdmin(
  page: Page,
  username = 'admin',
  password = '123456'
) {
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(1000)

  // 检查是否已经登录
  const currentUrl = page.url()
  if (!currentUrl.includes('/login')) {
    return
  }

  // 选择平台管理角色
  const platformBtn = page.locator('.el-segmented__item-label').first()
  if (await platformBtn.isVisible().catch(() => false)) {
    await platformBtn.click()
    await page.waitForTimeout(300)
  }

  // 填写用户名密码
  const usernameInput = page.locator('input[placeholder="用户名"]')
  await usernameInput.fill(username)

  const passwordInput = page.locator('input[placeholder="密码"]')
  await passwordInput.fill(password)

  // 点击登录
  const loginBtn = page.locator('button:has-text("登录")')
  await loginBtn.click()
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(2000)

  // 验证登录成功
  await page.waitForURL('**/platform/**', { timeout: 10000 }).catch(() => {})
}

/**
 * 商户管理后台登录
 * @param page Playwright Page对象
 * @param username 用户名
 * @param password 密码
 */
export async function loginAsMerchant(
  page: Page,
  username = 'digital',
  password = '123456'
) {
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(1000)

  // 检查是否已经登录
  const currentUrl = page.url()
  if (!currentUrl.includes('/login')) {
    return
  }

  // 选择商户管理角色
  const merchantBtn = page.locator('.el-segmented__item-label').nth(1)
  if (await merchantBtn.isVisible().catch(() => false)) {
    await merchantBtn.click()
    await page.waitForTimeout(300)
  }

  // 填写用户名密码
  const usernameInput = page.locator('input[placeholder="用户名"]')
  await usernameInput.fill(username)

  const passwordInput = page.locator('input[placeholder="密码"]')
  await passwordInput.fill(password)

  // 点击登录
  const loginBtn = page.locator('button:has-text("登录")')
  await loginBtn.click()
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(2000)

  // 验证登录成功
  await page.waitForURL('**/merchant/**', { timeout: 10000 }).catch(() => {})
}

/**
 * 导航到商家列表页面
 * @param page Playwright Page对象
 */
export async function navigateToMerchantList(page: Page) {
  await page.goto('/platform/merchant/list')
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(1500)
}

/**
 * 通用页面导航函数
 * @param page Playwright Page对象
 * @param path 页面路径
 */
export async function navigateToPage(page: Page, path: string) {
  await page.goto(path)
  await page.waitForLoadState('networkidle')
  await page.waitForTimeout(1500)
}

/**
 * 等待表格加载完成
 * @param page Playwright Page对象
 */
export async function waitForTableLoad(page: Page, timeout = 10000) {
  try {
    await page.waitForSelector('.el-table__body-wrapper .el-table__row', {
      timeout
    })
  } catch {
    console.log('ℹ️  表格可能没有数据')
  }
  await page.waitForTimeout(500)
}

/**
 * 获取表格行数
 * @param page Playwright Page对象
 */
export async function getTableRowCount(page: Page): Promise<number> {
  const rows = page.locator('.el-table__body-wrapper .el-table__row')
  return await rows.count()
}

/**
 * 点击操作按钮
 * @param page Playwright Page对象
 * @param rowIndex 行号（从0开始）
 * @param buttonText 按钮文本
 */
export async function clickActionButton(page: Page, rowIndex: number, buttonText: string) {
  const row = page.locator('.el-table__body-wrapper .el-table__row').nth(rowIndex)
  const button = row.locator(`button:has-text("${buttonText}")`)
  await button.click()
  await page.waitForTimeout(500)
}
