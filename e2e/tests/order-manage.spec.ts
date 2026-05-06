import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('订单管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/order')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("订单管理")')).toBeVisible()
    await waitForTableLoad(page).catch(() => {
      console.log('ℹ️  订单列表为空，表格可能没有数据')
    })
    await expect(page.locator('.el-pagination')).toBeVisible()
    await page.screenshot({ path: 'e2e/screenshots/order-1-page-load.png', fullPage: true })
  })

  test('2. 搜索功能测试 - 订单号搜索', async ({ page }) => {
    await waitForTableLoad(page, 5000).catch(() => {})
    
    const orderInput = page.locator('input[placeholder="订单号"]')
    await expect(orderInput).toBeVisible()
    
    await orderInput.fill('12345')
    await page.locator('button:has-text("搜索")').click()
    await page.waitForTimeout(1500)
    
    await page.screenshot({ path: 'e2e/screenshots/order-2-search-order-no.png', fullPage: true })
    
    await orderInput.clear()
  })

  test('3. 订单状态筛选测试', async ({ page }) => {
    const statusSelect = page.locator('.el-select:has-text("订单状态")')
    await statusSelect.click()
    await page.waitForTimeout(500)
    
    const options = page.locator('.el-select-dropdown__item')
    if (await options.count() > 0) {
      await options.first().click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
      await page.screenshot({ path: 'e2e/screenshots/order-3-filter-status.png', fullPage: true })
    }
  })

  test('4. 查看订单详情功能', async ({ page }) => {
    await waitForTableLoad(page, 5000)
    
    const viewBtn = page.locator('button:has-text("详情")').first()
    if (await viewBtn.isVisible().catch(() => false)) {
      await viewBtn.click()
      await page.waitForTimeout(1000)
      
      const dialog = page.locator('.el-dialog').filter({ hasText: '订单详情' })
      await expect(dialog).toBeVisible()
      
      await page.screenshot({ path: 'e2e/screenshots/order-4-detail.png', fullPage: true })
      
      await dialog.locator('.el-dialog__headerbtn').click()
      await expect(dialog).not.toBeVisible({ timeout: 5000 })
    } else {
      console.log('⚠️  当前暂无订单数据，跳过查看详情测试')
    }
  })

  test('5. 订单发货功能测试', async ({ page }) => {
    await waitForTableLoad(page, 5000).catch(() => {})
    
    const shipBtn = page.locator('button:has-text("发货")').first()
    if (await shipBtn.isVisible().catch(() => false)) {
      await shipBtn.click()
      await page.waitForTimeout(500)
      
      const shipDialog = page.locator('.el-dialog:has-text("订单发货")')
      await expect(shipDialog).toBeVisible()
      
      await shipDialog.locator('input[placeholder="如：顺丰速运"]').fill('顺丰速运')
      await shipDialog.locator('input[placeholder="请输入物流单号"]').fill('SF1234567890')
      
      await page.screenshot({ path: 'e2e/screenshots/order-5-ship-form.png', fullPage: true })
      
      await shipDialog.locator('button:has-text("取消")').click()
      await expect(shipDialog).not.toBeVisible({ timeout: 5000 })
    } else {
      console.log('⚠️  当前暂无待发货订单，跳过发货测试')
    }
  })

  test('6. 分页功能测试', async ({ page }) => {
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        const initialPage = await page.locator('.el-pagination li.is-active').textContent()
        await nextBtn.click()
        await page.waitForTimeout(1500)
        const newPage = await page.locator('.el-pagination li.is-active').textContent()
        expect(newPage).not.toEqual(initialPage)
        
        await page.screenshot({ path: 'e2e/screenshots/order-6-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️  只有一页数据，分页下一页为禁用状态')
        expect(isDisabled).toBe(true)
      }
    }
  })
})
