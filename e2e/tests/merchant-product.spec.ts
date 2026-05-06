import { test, expect } from '@playwright/test'
import { loginAsMerchant, navigateToPage, waitForTableLoad, getTableRowCount } from './utils/common'

test.describe('商户商品管理自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
    await navigateToPage(page, '/merchant/product/list')
  })

  test('1. 页面加载验证', async ({ page }) => {
    await expect(page.locator('h2:has-text("商品列表")')).toBeVisible({ timeout: 15000 })
    
    await expect(page.locator('input[placeholder="搜索商品名称"]')).toBeVisible()
    await expect(page.locator('button:has-text("搜索")')).toBeVisible()
    await expect(page.locator('button:has-text("发布商品")')).toBeVisible()
    
    await waitForTableLoad(page, 15000)
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-product-1-load.png', fullPage: true })
  })

  test('2. 关键词搜索功能', async ({ page }) => {
    await page.locator('input[placeholder="搜索商品名称"]').fill('测试')
    await page.locator('button:has-text("搜索")').click()
    await page.waitForTimeout(1500)
    
    await page.screenshot({ path: 'e2e/screenshots/merchant-product-2-search.png', fullPage: true })
    
    const clearBtn = page.locator('.el-input__clear')
    if (await clearBtn.isVisible().catch(() => false)) {
      await clearBtn.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
    }
  })

  test('3. 分类筛选功能', async ({ page }) => {
    const categorySelect = page.locator('.el-select').first()
    await categorySelect.click()
    await page.waitForTimeout(500)
    
    const firstOption = page.locator('.el-select-dropdown .el-tree-node__content').first()
    if (await firstOption.isVisible().catch(() => false)) {
      await firstOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
      
      await page.screenshot({ path: 'e2e/screenshots/merchant-product-3-category.png', fullPage: true })
      
      await categorySelect.click()
      await page.waitForTimeout(1000)
    } else {
      console.log('⚠️ 暂无分类数据，跳过分类筛选测试')
    }
  })

  test('4. 状态筛选功能', async ({ page }) => {
    const statuses = ['草稿', '待审核', '已上架', '已下架']
    
    for (const status of statuses) {
      await page.locator('.el-select').last().click()
      await page.waitForTimeout(1000)
      
      const option = page.locator(`.el-select-dropdown__item:has-text("${status}")`)
      if (await option.isVisible().catch(() => false)) {
        await option.click()
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)
        
        await page.screenshot({ path: `e2e/screenshots/merchant-product-4-status-${status}.png`, fullPage: true })
        
        await page.locator('.el-select').last().click()
        await page.waitForTimeout(500)
      }
    }
  })

  test('5. 商品上架功能', async ({ page }) => {
    const statusSelect = page.locator('.el-select').last()
    await statusSelect.click()
    await page.waitForTimeout(500)
    
    const draftOption = page.locator('.el-select-dropdown__item:has-text("草稿")')
    if (await draftOption.isVisible().catch(() => false)) {
      await draftOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
      
      const publishBtn = page.locator('button:has-text("上架")').first()
      if (await publishBtn.isVisible().catch(() => false)) {
        await publishBtn.click()
        await page.waitForTimeout(2000)
        console.log('✅ 商品上架测试完成')
        await page.screenshot({ path: 'e2e/screenshots/merchant-product-5-publish.png', fullPage: true })
      } else {
        console.log('ℹ️ 暂无草稿商品，跳过上架测试')
      }
    }
  })

  test('6. 商品下架功能', async ({ page }) => {
    await page.locator('.el-select').last().click()
    await page.waitForTimeout(500)
    
    const publishedOption = page.locator('.el-select-dropdown__item:has-text("已上架")')
    if (await publishedOption.isVisible().catch(() => false)) {
      await publishedOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
      
      const unpublishBtn = page.locator('button:has-text("下架")').first()
      if (await unpublishBtn.isVisible().catch(() => false)) {
        await unpublishBtn.click()
        await page.waitForTimeout(1000)
        await page.screenshot({ path: 'e2e/screenshots/merchant-product-6-unpublish.png', fullPage: true })
      } else {
        console.log('ℹ️ 暂无已上架商品，跳下架测试')
      }
    }
  })

  test('7. 编辑商品功能', async ({ page }) => {
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click()
      await page.waitForTimeout(2000)
      await expect(page).toHaveURL(/\/merchant\/product\/add/)
      await page.screenshot({ path: 'e2e/screenshots/merchant-product-7-edit.png', fullPage: true })
    } else {
      console.log('ℹ️ 暂无商品，跳过编辑测试')
    }
  })

  test('8. 跳转发布商品页面验证', async ({ page }) => {
    await page.locator('button:has-text("发布商品")').click()
    await page.waitForTimeout(1000)
    await expect(page).toHaveURL(/\/merchant\/product\/add/)
    await expect(page.locator('h2:has-text("发布商品")')).toBeVisible()
    await page.screenshot({ path: 'e2e/screenshots/merchant-product-8-add-page.png', fullPage: true })
  })

  test('9. 分页功能测试', async ({ page }) => {
    await navigateToPage(page, '/merchant/product/list')
    await page.waitForTimeout(1000)
    
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()
      
      if (!isDisabled) {
        await nextBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: 'e2e/screenshots/merchant-product-9-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️ 只有一页数据，分页下一页为禁用状态')
      }
    }
  })
})
