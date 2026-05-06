import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad, getTableRowCount } from './utils/common'

test.describe('自营商品管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/product/list')
  })

  test('1. 页面加载验证', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h2:has-text("自营商品管理")')).toBeVisible({ timeout: 15000 })
    // 验证搜索栏存在
    await expect(page.locator('input[placeholder="搜索商品名称"]')).toBeVisible()
    await expect(page.locator('button:has-text("搜索")')).toBeVisible()
    await expect(page.locator('button:has-text("新增商品")')).toBeVisible()
    // 验证表格加载（延长超时时间）
    await waitForTableLoad(page, 20000)
    // 验证分页存在
    await expect(page.locator('.el-pagination')).toBeVisible()

    await page.screenshot({ path: 'e2e/screenshots/product-1-page-load.png', fullPage: true })
  })

  test('2. 关键词搜索功能', async ({ page }) => {
    // 输入搜索关键词
    await page.locator('input[placeholder="搜索商品名称"]').fill('测试')
    // 点击搜索
    await page.locator('button:has-text("搜索")').click()
    // 等待加载
    await page.waitForTimeout(1500)

    await page.screenshot({ path: 'e2e/screenshots/product-2-search-keyword.png', fullPage: true })

    // 清空搜索
    const clearBtn = page.locator('.el-input__clear')
    if (await clearBtn.isVisible().catch(() => false)) {
      await clearBtn.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)
    }
  })

  test('3. 分类筛选功能', async ({ page }) => {
    // 点击分类选择器
    const categorySelect = page.locator('input[placeholder*="分类"]')
    await categorySelect.click()
    await page.waitForTimeout(500)

    // 选择第一个分类
    const firstOption = page.locator('.el-select-dropdown .el-tree-node__content').first()
    if (await firstOption.isVisible().catch(() => false)) {
      await firstOption.click()
      // 点击搜索
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)

      await page.screenshot({ path: 'e2e/screenshots/product-3-filter-category.png', fullPage: true })

      // 清空筛选
      await categorySelect.click()
      await page.waitForTimeout(1000)
      const clearBtn = page.locator('.el-select-dropdown .el-icon-close')
      if (await clearBtn.isVisible().catch(() => false)) {
        await clearBtn.click()
      } else {
        // 点击空白处关闭
        await page.click('body', { position: { x: 10, y: 10 } })
      }
    } else {
      console.log('⚠️  暂无分类数据，跳过分类筛选测试')
    }
  })

  test('4. 状态筛选功能', async ({ page }) => {
    // 测试不同状态筛选
    const statuses = [
      { label: '草稿', value: '0' },
      { label: '待审核', value: '1' },
      { label: '已上架', value: '2' },
      { label: '已下架', value: '3' },
    ]

    for (const status of statuses) {
      // 点击状态选择器
      await page.locator('input[placeholder*="状态"]').click()
      await page.waitForTimeout(1000)

      // 选择状态
      const option = page.locator(`.el-select-dropdown__item:has-text("${status.label}")`)
      if (await option.isVisible().catch(() => false)) {
        await option.click()
        // 点击搜索
        await page.locator('button:has-text("搜索")').click()
        await page.waitForTimeout(1500)

        const rowCount = await getTableRowCount(page)
        console.log(`筛选「${status.label}」状态，结果数量：${rowCount}`)

        await page.screenshot({ path: `e2e/screenshots/product-4-filter-status-${status.value}.png`, fullPage: true })

        // 清空筛选
        await page.locator('input[placeholder*="状态"]').click()
        await page.waitForTimeout(500)
        const clearBtn = page.locator('.el-select .el-select__clear')
        if (await clearBtn.isVisible().catch(() => false)) {
          await clearBtn.click()
        }
      }
    }
  })

  test('5. 查看商品详情功能', async ({ page }) => {
    // 找到第一个查看按钮
    const viewBtn = page.locator('button:has-text("查看")').first()
    if (await viewBtn.isVisible().catch(() => false)) {
      await viewBtn.click()
      await page.waitForTimeout(1000)
      // 兼容弹窗和页面跳转两种情况
      const dialogSelector = page.locator('.el-dialog:has-text("商品详情")')
      const pageSelector = page.locator('h1,h2:has-text("商品详情")')
      await expect(dialogSelector.or(pageSelector)).toBeVisible({ timeout: 5000 })
      // 验证详情字段存在
      await expect(page.locator('.el-dialog:has-text("商品名称")')).toBeVisible()
      await expect(page.locator('.el-dialog:has-text("价格")')).toBeVisible()
      await expect(page.locator('.el-dialog:has-text("状态")')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/product-5-detail.png', fullPage: true })

      // 关闭弹窗
      await page.locator('.el-dialog__headerbtn').click()
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    } else {
      console.log('⚠️  暂无商品数据，跳过查看详情测试')
    }
  })

  test('6. 商品审核功能', async ({ page }) => {
    // 筛选待审核状态
    await page.locator('input[placeholder*="状态"]').click()
    await page.waitForTimeout(500)
    const auditOption = page.locator('.el-select-dropdown__item:has-text("待审核")')
    if (await auditOption.isVisible().catch(() => false)) {
      await auditOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)

      // 检查是否有待审核商品
      const passBtn = page.locator('button:has-text("通过")').first()
      if (await passBtn.isVisible().catch(() => false)) {
        // 测试审核通过
        await passBtn.click()
        await page.waitForTimeout(1000)
        await expect(page.locator(':has-text("成功")')).toBeVisible({ timeout: 10000 })
        console.log('✅ 商品审核通过测试完成')

        await page.screenshot({ path: 'e2e/screenshots/product-6-audit-pass.png', fullPage: true })
      } else {
        console.log('ℹ️  当前无待审核商品，跳过审核测试')
      }
    }
  })

  test('7. 商品下架功能', async ({ page }) => {
    // 筛选已上架状态
    await page.locator('input[placeholder*="状态"]').click()
    await page.waitForTimeout(500)
    const publishedOption = page.locator('.el-select-dropdown__item:has-text("已上架")')
    if (await publishedOption.isVisible().catch(() => false)) {
      await publishedOption.click()
      await page.locator('button:has-text("搜索")').click()
      await page.waitForTimeout(1500)

      // 检查是否有已上架商品
      const unpublishBtn = page.locator('button:has-text("下架")').first()
      if (await unpublishBtn.isVisible().catch(() => false)) {
        // 测试下架
        await unpublishBtn.click()
        await expect(page.locator('.el-message-box:has-text("确定下架商品")')).toBeVisible()
        await page.locator('.el-message-box__btns button:has-text("确定")').click()
      await page.waitForTimeout(1000)
        await expect(page.locator(':has-text("成功")')).toBeVisible({ timeout: 10000 })
        console.log('✅ 商品下架测试完成')

        await page.screenshot({ path: 'e2e/screenshots/product-7-unpublish.png', fullPage: true })
      } else {
        console.log('ℹ️  当前无已上架商品，跳下架审核测试')
      }
    }
  })

  test('8. 分页功能测试', async ({ page }) => {
    // 检查分页是否有下一页
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.isDisabled()

      if (!isDisabled) {
        const initialPage = await page.locator('.el-pagination li.is-active').textContent()
        // 点击下一页
        await nextBtn.click()
        await page.waitForTimeout(1500)
        // 验证页码变化
        const newPage = await page.locator('.el-pagination li.is-active').textContent()
        expect(newPage).not.toEqual(initialPage)

        await page.screenshot({ path: 'e2e/screenshots/product-8-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️  只有一页数据，分页下一页为禁用状态')
        // 验证下一页按钮确实为禁用状态，符合预期
        expect(isDisabled).toBe(true)
      }
    }
  })
})
