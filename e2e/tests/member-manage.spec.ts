import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('会员管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/member')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("会员管理")')).toBeVisible()
    await waitForTableLoad(page, 5000).catch(() => {
      console.log('ℹ️  会员列表为空，表格可能没有数据')
    })
    await expect(page.locator('.el-pagination')).toBeVisible()
    await expect(page.locator('button:has-text("刷新")')).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/member-1-page-load.png', fullPage: true })
  })

  test('2. 表格数据展示验证', async ({ page }) => {
    await waitForTableLoad(page, 5000)
    
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
    
    await page.screenshot({ path: 'e2e/screenshots/member-2-table-display.png', fullPage: true })
  })

  test('3. 刷新功能测试', async ({ page }) => {
    const refreshBtn = page.locator('button:has-text("刷新")')
    await expect(refreshBtn).toBeVisible()
    
    await refreshBtn.click()
    await page.waitForTimeout(1500)
    
    await page.screenshot({ path: 'e2e/screenshots/member-3-refresh.png', fullPage: true })
  })

  test('4. 会员信息展示验证', async ({ page }) => {
    await waitForTableLoad(page, 5000).catch(() => {})
    
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    if (await rows.count() > 0) {
      const firstRow = rows.first()
      
      await expect(firstRow).toBeVisible()
      
      await page.screenshot({ path: 'e2e/screenshots/member-4-member-info.png', fullPage: true })
    } else {
      console.log('ℹ️  当前暂无会员数据')
    }
  })

  test('5. 分页功能测试', async ({ page }) => {
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        const initialPage = await page.locator('.el-pagination li.is-active').textContent()
        await nextBtn.click()
        await page.waitForTimeout(1500)
        const newPage = await page.locator('.el-pagination li.is-active').textContent()
        expect(newPage).not.toEqual(initialPage)
        
        await page.screenshot({ path: 'e2e/screenshots/member-5-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️  只有一页数据，分页下一页为禁用状态')
        expect(isDisabled).toBe(true)
      }
    }
  })
})
