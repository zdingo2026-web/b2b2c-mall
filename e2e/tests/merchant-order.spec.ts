import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('商户订单管理自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/order')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("订单管理")')).toBeVisible({ timeout: 15000 })
    
    await expect(page.locator('input[placeholder="订单号"]')).toBeVisible()
    await expect(page.locator('.el-select')).toBeVisible()
    await expect(page.locator('button:has-text("搜索")')).toBeVisible()
    
    await waitForTableLoad(page, 15000)
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-order-1-load.png', fullPage: true })
  })

  test('2. 订单号搜索功能', async ({ page }) => {
    const firstRow = page.locator('.el-table__body-wrapper .el-table__row').first()
    if (await firstRow.isVisible().catch(() => false)) {
      const orderNo = await firstRow.locator('td').first().textContent()
      if (orderNo) {
        await page.locator('input[placeholder="订单号"]').fill(orderNo)
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)
        
        await page.screenshot({ path: 'e2e/screenshots/merchant-order-2-search.png', fullPage: true })

        const clearBtn = page.locator('.el-input__clear')
        if (await clearBtn.isVisible().catch(() => false)) {
          await clearBtn.click()
        } else {
          await page.locator('input[placeholder="订单号"]').clear()
        }
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)
      }
    } else {
      console.log('ℹ️ 暂无订单数据，跳过搜索测试')
    }
  })

  test('3. 订单状态筛选测试', async ({ page }) => {
    const statuses = ['待付款', '待发货', '待收货', '已完成', '已取消', '待评价']
    
    for (const status of statuses) {
      await page.locator('.el-select').click()
      await page.waitForTimeout(500)
      
      const option = page.locator(`.el-select-dropdown__item:has-text("${status}")`)
      if (await option.isVisible().catch(() => false)) {
        await option.click()
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)
        
        await page.screenshot({ path: `e2e/screenshots/merchant-order-3-status-${status}.png`, fullPage: true })
        
        await page.locator('.el-select').click()
        await page.waitForTimeout(500)
      }
    }
  })

  test('4. 查看订单详情功能', async ({ page }) => {
    const viewBtn = page.locator('button:has-text("详情")').first()
    if (await viewBtn.isVisible().catch(() => false)) {
      await viewBtn.click()
      await page.waitForTimeout(1000)
      
      await expect(page.locator('.el-dialog:has-text("订单详情")')).toBeVisible()
      await page.screenshot({ path: 'e2e/screenshots/merchant-order-4-detail.png', fullPage: true })
      
      await page.locator('.el-dialog__headerbtn').click()
      await expect(page.locator('.el-dialog')).not.toBeVisible({ timeout: 5000 })
    } else {
      console.log('ℹ️ 暂无订单，跳过详情查看测试')
    }
  })

  test('5. 订单发货功能测试', async ({ page }) => {
    await page.locator('.el-select').click()
    await page.waitForTimeout(500)
    
    const pendingOption = page.locator('.el-select-dropdown__item:has-text("待发货")')
    if (await pendingOption.isVisible().catch(() => false)) {
      await pendingOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
      
      const shipBtn = page.locator('button:has-text("发货")').first()
      if (await shipBtn.isVisible().catch(() => false)) {
        await shipBtn.click()
        await page.waitForTimeout(1000)
        
        await expect(page.locator('.el-dialog:has-text("订单发货")')).toBeVisible()
        
        await page.locator('.el-dialog .el-select').click()
        await page.waitForTimeout(500)
        await page.locator('.el-select-dropdown__item:has-text("顺丰速运")').click()
        await page.locator('input[placeholder="请输入物流单号"]').fill('SF1234567890')
        
        await page.screenshot({ path: 'e2e/screenshots/merchant-order-5-ship-form.png', fullPage: true })
        
        await page.locator('.el-dialog__headerbtn').click()
        await expect(page.locator('.el-dialog')).not.toBeVisible({ timeout: 5000 })
        
        console.log('ℹ️ 发货表单验证完成，未实际提交')
      } else {
        console.log('ℹ️ 暂无待发货订单，跳过发货测试')
      }
    }
  })

  test('6. 分页功能测试', async ({ page }) => {
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        await nextBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: 'e2e/screenshots/merchant-order-6-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️ 只有一页数据，分页下一页为禁用状态')
      }
    }
  })
})
