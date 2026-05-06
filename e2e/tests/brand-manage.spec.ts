import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('品牌管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/product/brand')
  })

  test('1. 页面加载验证', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h2:has-text("品牌管理")')).toBeVisible()
    // 验证新增按钮存在
    await expect(page.locator('button:has-text("新增品牌")')).toBeVisible()
    // 验证表格加载
    await waitForTableLoad(page)

    await page.screenshot({ path: 'e2e/screenshots/brand-1-page-load.png', fullPage: true })
  })

  test('2. 新增品牌功能', async ({ page }) => {
    // 点击新增品牌
    await page.locator('button:has-text("新增品牌")').click()
    // 验证弹窗显示
    await expect(page.locator('.el-dialog:has-text("新增品牌")')).toBeVisible()

    // 填写表单
    const brandName = `测试品牌${Date.now()}`
    await page.locator('input[placeholder="请输入品牌名称"]').fill(brandName)
    await page.locator('textarea[placeholder="请输入品牌描述"]').fill('这是一个测试品牌描述')
    await page.locator('.el-input-number input').fill('50')
    // 状态保持启用

    await page.screenshot({ path: 'e2e/screenshots/brand-2-add-form.png', fullPage: true })

    // 提交
    await page.locator('.el-dialog button:has-text("确定")').click()
    // 验证成功提示
    await expect(page.locator('.el-message--success')).toBeVisible()

    // 验证品牌已添加到列表
    await waitForTableLoad(page)
    await expect(page.locator('.el-table__body-wrapper .cell').filter({ hasText: brandName })).toBeVisible()

    await page.screenshot({ path: 'e2e/screenshots/brand-2-add-result.png', fullPage: true })
  })

  test('3. 编辑品牌功能', async ({ page }) => {
    // 找到第一个编辑按钮
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click()
      // 验证弹窗显示
      await expect(page.locator('.el-dialog:has-text("编辑品牌")')).toBeVisible()

      // 修改品牌名称
      const newName = `编辑后品牌${Date.now()}`
      await page.locator('input[placeholder="请输入品牌名称"]').fill(newName)
      await page.locator('textarea[placeholder="请输入品牌描述"]').fill('这是编辑后的品牌描述')
      await page.locator('.el-input-number input').fill('100')

      await page.screenshot({ path: 'e2e/screenshots/brand-3-edit-form.png', fullPage: true })

      // 提交
      await page.locator('.el-dialog button:has-text("确定")').click()
      // 验证成功提示
      await expect(page.locator('.el-message--success')).toBeVisible()

      // 验证名称已更新
      await waitForTableLoad(page)
      await expect(page.locator('.el-table__body-wrapper .cell').filter({ hasText: newName })).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/brand-3-edit-result.png', fullPage: true })
    } else {
      console.log('⚠️  暂无品牌数据，跳过编辑测试')
    }
  })

  test('4. 状态切换验证', async ({ page }) => {
    // 找到第一个状态标签
    const statusTag = page.locator('.el-table .el-tag').first()
    if (await statusTag.isVisible().catch(() => false)) {
      const statusText = await statusTag.textContent()
      console.log(`当前品牌状态：${statusText}`)

      // 验证状态标签颜色
      if (statusText === '启用') {
        await expect(statusTag).toHaveClass(/success/)
      } else {
        await expect(statusTag).toHaveClass(/danger/)
      }

      await page.screenshot({ path: 'e2e/screenshots/brand-4-status.png', fullPage: true })
    } else {
      console.log('⚠️  暂无品牌数据，跳过状态验证')
    }
  })

  test('5. 删除品牌功能', async ({ page }) => {
    // 找到最后一个删除按钮
    const deleteBtns = page.locator('button:has-text("删除")')
    const count = await deleteBtns.count()
    if (count > 0) {
      const deleteBtn = deleteBtns.nth(count - 1)
      const brandName = await deleteBtn.locator('xpath=../../preceding-sibling::td[3]').textContent() || '测试品牌'

      await deleteBtn.click()
      // 验证确认弹窗
      await expect(page.locator('.el-message-box:has-text("确定删除品牌")')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/brand-5-delete-confirm.png', fullPage: true })

      // 确认删除
      await page.locator('.el-message-box__btns button:has-text("确定")').click()
      // 验证成功提示
      await expect(page.locator('.el-message--success')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/brand-5-delete-result.png', fullPage: true })
    } else {
      console.log('⚠️  暂无品牌数据，跳过删除测试')
    }
  })
})
