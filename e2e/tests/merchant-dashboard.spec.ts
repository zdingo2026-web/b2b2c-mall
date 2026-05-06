import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage } from './utils/common'

test.describe('商户工作台自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/dashboard')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("商户工作台")')).toBeVisible({ timeout: 15000 })
    
    await expect(page.locator('.el-card').nth(0)).toBeVisible()
    await expect(page.locator('.el-card').nth(1)).toBeVisible()
    await expect(page.locator('.el-card').nth(2)).toBeVisible()
    await expect(page.locator('.el-card').nth(3)).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-1-load.png', fullPage: true })
  })

  test('2. 统计数据展示验证', async ({ page }) => {
    const todayAmount = page.locator('p:has-text("今日成交额")').locator('xpath=following-sibling::p').first()
    const todayCount = page.locator('p:has-text("今日订单数")').locator('xpath=following-sibling::p').first()
    const todayAvg = page.locator('p:has-text("今日客单价")').locator('xpath=following-sibling::p').first()
    const totalAmount = page.locator('p:has-text("累计成交额")').locator('xpath=following-sibling::p').first()
    
    await expect(todayAmount).toBeVisible()
    await expect(todayCount).toBeVisible()
    await expect(todayAvg).toBeVisible()
    await expect(totalAmount).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-2-stats.png' })
  })

  test('3. 趋势图表展示验证', async ({ page }) => {
    await expect(page.locator('.el-card').filter({ hasText: '近7天趋势' })).toBeVisible()
    await expect(page.locator('.trend-chart')).toBeVisible()
    
    const bars = page.locator('.trend-bar')
    await expect(bars).toHaveCount(7)
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-3-trend.png' })
  })

  test('4. 商品销量排行展示验证', async ({ page }) => {
    await expect(page.locator('.el-card').filter({ hasText: '商品销量排行' })).toBeVisible()
    await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-4-ranking.png' })
  })

  test('5. 待处理事项展示验证', async ({ page }) => {
    await expect(page.locator('.el-card').filter({ hasText: '待处理事项' })).toBeVisible()
    await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-5-tasks.png' })
  })

  test('6. 点击待处理事项跳转验证', async ({ page }) => {
    const taskButton = page.locator('button:has-text("项待处理")').first()
    if (await taskButton.isVisible().catch(() => false)) {
      await taskButton.click()
      await page.waitForTimeout(1000)
      await expect(page).toHaveURL(/\/merchant\/order/)
      await page.screenshot({ path: 'e2e/screenshots/merchant-dashboard-6-navigate.png' })
    } else {
      console.log('ℹ️ 暂无待处理事项，跳过跳转测试')
    }
  })
})
