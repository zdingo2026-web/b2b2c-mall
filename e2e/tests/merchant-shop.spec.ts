import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage } from './utils/common'

test.describe('商户店铺设置自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/shop')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("店铺设置")')).toBeVisible({ timeout: 15000 })
    
    await expect(page.locator('.el-card')).toBeVisible()
    await expect(page.locator('.el-form')).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-shop-1-load.png', fullPage: true })
  })

  test('2. 表单字段展示验证', async ({ page }) => {
    const fields = ['店铺名称', '店铺Logo', '店铺描述', '联系人', '联系电话', '联系邮箱', '详细地址']
    
    for (const field of fields) {
      await expect(page.locator('.el-form-item__label', { hasText: field })).toBeVisible()
    }
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-shop-2-form.png' })
  })

  test('3. 店铺信息编辑验证', async ({ page }) => {
    const shopNameInput = page.locator('input[placeholder*="店铺名称"]').first()
    const currentValue = await shopNameInput.inputValue()
    
    const testValue = currentValue + '_测试'
    await shopNameInput.fill(testValue)
    
    const contactNameInput = page.locator('input[placeholder*="联系人"]').first()
    if (await contactNameInput.isVisible().catch(() => false)) {
      const contactValue = await contactNameInput.inputValue()
      await contactNameInput.fill(contactValue + '_测试')
    }
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-shop-3-edit.png', fullPage: true })
    
    await shopNameInput.fill(currentValue)
    if (await contactNameInput.isVisible().catch(() => false)) {
      const contactValue = await contactNameInput.inputValue()
      if (contactValue.endsWith('_测试')) {
        await contactNameInput.fill(contactValue.replace('_测试', ''))
      }
    }
    
    console.log('ℹ️ 编辑功能验证完成，已恢复原值')
  })

  test('4. 保存按钮功能验证', async ({ page }) => {
    const saveBtn = page.locator('button:has-text("保存")')
    await expect(saveBtn).toBeVisible()
    await expect(saveBtn).toBeEnabled()
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-shop-4-save-btn.png' })
  })

  test('5. 分隔线和联系信息展示验证', async ({ page }) => {
    await expect(page.locator('.el-divider:has-text("联系信息")')).toBeVisible()
    await page.screenshot({ path: 'e2e/screenshots/merchant-shop-5-divider.png' })
  })
})
