import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('商户资金管理自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/finance')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("资金管理")')).toBeVisible({ timeout: 15000 })
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-finance-1-load.png', fullPage: true })
  })

  test('2. 资金统计卡片验证', async ({ page }) => {
    const cards = page.locator('.el-row .el-col')
    await expect(cards).toHaveCount(3)
    
    await expect(page.locator('p:has-text("可用余额")')).toBeVisible()
    await expect(page.locator('p:has-text("累计结算")')).toBeVisible()
    await expect(page.locator('p:has-text("待提现")')).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-finance-2-cards.png' })
  })

  test('3. 结算记录表格验证', async ({ page }) => {
    await expect(page.locator('.el-card:has-text("结算记录")')).toBeVisible()
    
    await waitForTableLoad(page, 10000)
    
    const headers = ['结算单号', '订单金额', '佣金', '结算金额', '状态', '创建时间']
    for (const header of headers) {
      await expect(page.locator('.el-table__header th', { hasText: header })).toBeVisible()
    }
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-finance-3-table.png' })
  })

  test('4. 申请提现按钮验证', async ({ page }) => {
    const withdrawBtn = page.locator('button:has-text("申请提现")')
    await expect(withdrawBtn).toBeVisible()
    await expect(withdrawBtn).toBeEnabled()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-finance-4-withdraw-btn.png' })
  })

  test('5. 申请提现对话框验证', async ({ page }) => {
    const withdrawBtn = page.locator('button:has-text("申请提现")')
    if (await withdrawBtn.isVisible().catch(() => false)) {
      await withdrawBtn.click()
      await page.waitForTimeout(1000)
      
      await expect(page.locator('.el-dialog:has-text("申请提现")')).toBeVisible()
      
      const amountInput = page.locator('.el-input-number')
      await expect(amountInput).toBeVisible()
      
      await expect(page.locator('p:has-text("最低提现金额：100元")')).toBeVisible()
      
      await page.screenshot({ path: 'e2e/screenshots/merchant-finance-5-dialog.png' })
      
      await page.locator('.el-dialog__headerbtn').click()
      await expect(page.locator('.el-dialog')).not.toBeVisible({ timeout: 5000 })
    }
  })

  test('6. 分页功能测试', async ({ page }) => {
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        await nextBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: 'e2e/screenshots/merchant-finance-6-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️ 只有一页数据，分页下一页为禁用状态')
      }
    }
  })
})
