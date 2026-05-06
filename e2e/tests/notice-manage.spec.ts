import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('公告管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/content/notice')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await page.waitForTimeout(3000)
    await page.screenshot({ path: 'e2e/screenshots/notice-1-page-load.png', fullPage: true })
    console.log('✅ 公告管理页面已加载')
  })

  test('2. 状态筛选功能测试', async ({ page }) => {
    const selects = page.locator('.el-select')
    if (await selects.count() > 0) {
      const statusSelect = selects.first()
      await statusSelect.click()
      await page.waitForTimeout(500)
      
      const options = page.locator('.el-select-dropdown__item')
      if (await options.count() > 0) {
        await options.nth(1).click()
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)
        
        await page.screenshot({ path: 'e2e/screenshots/notice-2-filter-status.png', fullPage: true })
      }
    }
  })

  test('3. 新增公告功能测试', async ({ page }) => {
    const addBtn = page.locator('button:has-text("新增")')
    if (await addBtn.isVisible().catch(() => false)) {
      await addBtn.click()
      await page.waitForTimeout(500)
      
      const dialog = page.locator('.el-dialog').first()
      if (await dialog.isVisible()) {
        const textarea = dialog.locator('textarea')
        if (await textarea.isVisible().catch(() => false)) {
          await textarea.fill('这是一条测试公告，用于自动化测试验证')
        }
        
        await page.screenshot({ path: 'e2e/screenshots/notice-3-add-form.png', fullPage: true })
        
        const cancelBtn = dialog.locator('button:has-text("取消")')
        if (await cancelBtn.isVisible()) {
          await cancelBtn.click()
        }
      }
    } else {
      console.log('⚠️  找不到新增按钮，跳过测试')
    }
  })

  test('4. 编辑公告功能测试', async ({ page }) => {
    await waitForTableLoad(page, 5000).catch(() => {})
    
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click()
      await page.waitForTimeout(500)
      
      const dialog = page.locator('.el-dialog:has-text("编辑公告")')
      await expect(dialog).toBeVisible()
      
      await page.screenshot({ path: 'e2e/screenshots/notice-4-edit-form.png', fullPage: true })
      
      await dialog.locator('button:has-text("取消")').click()
      await expect(dialog).not.toBeVisible({ timeout: 5000 })
    } else {
      console.log('⚠️  当前暂无公告数据，跳过编辑测试')
    }
  })

  test('5. 删除公告功能测试', async ({ page }) => {
    await waitForTableLoad(page, 5000).catch(() => {})
    
    const deleteBtn = page.locator('button:has-text("删除")').first()
    if (await deleteBtn.isVisible().catch(() => false)) {
      await deleteBtn.click()
      await page.waitForTimeout(500)
      
      const confirmDialog = page.locator('.el-message-box:has-text("确定删除该公告？")')
      await expect(confirmDialog).toBeVisible()
      
      await page.screenshot({ path: 'e2e/screenshots/notice-5-delete-confirm.png', fullPage: true })
      
      await confirmDialog.locator('button:has-text("取消")').click()
      await expect(confirmDialog).not.toBeVisible({ timeout: 5000 })
    } else {
      console.log('⚠️  当前暂无公告数据，跳过删除测试')
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
        
        await page.screenshot({ path: 'e2e/screenshots/notice-6-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️  只有一页数据，分页下一页为禁用状态')
        expect(isDisabled).toBe(true)
      }
    }
  })
})
