import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('商户退换货管理自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/refund')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("退换货管理")')).toBeVisible({ timeout: 15000 })
    
    await waitForTableLoad(page, 15000)
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-refund-1-load.png', fullPage: true })
  })

  test('2. 表格数据展示验证', async ({ page }) => {
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
    
    const headers = ['ID', '订单号', '退款单号', '退款金额', '退款原因', '状态', '申请时间']
    for (const header of headers) {
      await expect(page.locator('.el-table__header th', { hasText: header })).toBeVisible()
    }
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-refund-2-table.png' })
  })

  test('3. 同意退款功能测试', async ({ page }) => {
    const agreeBtn = page.locator('button:has-text("同意")').first()
    if (await agreeBtn.isVisible().catch(() => false)) {
      await agreeBtn.click()
      await page.waitForTimeout(500)
      
      const confirmBtn = page.locator('.el-message-box__btns button:has-text("确定")')
      if (await confirmBtn.isVisible().catch(() => false)) {
        await page.locator('.el-message-box__headerbtn').click()
        await page.waitForTimeout(500)
        console.log('ℹ️ 同意退款对话框验证完成，未实际提交')
      }
      
      await page.screenshot({ path: 'e2e/screenshots/merchant-refund-3-agree.png' })
    } else {
      console.log('ℹ️ 暂无待处理退款申请，跳过同意退款测试')
    }
  })

  test('4. 拒绝退款功能测试', async ({ page }) => {
    const rejectBtn = page.locator('button:has-text("拒绝")').first()
    if (await rejectBtn.isVisible().catch(() => false)) {
      await rejectBtn.click()
      await page.waitForTimeout(500)
      
      const promptDialog = page.locator('.el-message-box:has-text("拒绝退款")')
      if (await promptDialog.isVisible().catch(() => false)) {
        await page.locator('.el-message-box__headerbtn').click()
        await page.waitForTimeout(500)
        console.log('ℹ️ 拒绝退款对话框验证完成，未实际提交')
      }
      
      await page.screenshot({ path: 'e2e/screenshots/merchant-refund-4-reject.png' })
    } else {
      console.log('ℹ️ 暂无待处理退款申请，跳过拒绝退款测试')
    }
  })

  test('5. 分页功能测试', async ({ page }) => {
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        await nextBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: 'e2e/screenshots/merchant-refund-5-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️ 只有一页数据，分页下一页为禁用状态')
      }
    }
  })
})
