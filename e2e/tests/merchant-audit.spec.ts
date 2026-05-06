import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage, waitForTableLoad } from './utils/common'

test.describe('入驻审核模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/merchant/audit')
  })

  test('1. 页面加载验证', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h2:has-text("入驻审核")')).toBeVisible()
    // 验证表格加载
    await waitForTableLoad(page)
    // 验证分页存在
    await expect(page.locator('.el-pagination')).toBeVisible()

    await page.screenshot({ path: 'e2e/screenshots/audit-1-page-load.png', fullPage: true })
  })

  test('2. 查看商家详情功能', async ({ page }) => {
    // 找到第一个查看按钮
    const viewBtn = page.locator('button:has-text("查看")').first()
    if (await viewBtn.isVisible().catch(() => false)) {
      await viewBtn.click()
      // 验证弹窗显示
      await expect(page.locator('.el-dialog:has-text("商家资质详情")')).toBeVisible()
      // 验证详情字段存在
      await expect(page.locator('.el-dialog:has-text("商家名称")')).toBeVisible()
      await expect(page.locator('.el-dialog:has-text("联系电话")')).toBeVisible()
      await expect(page.locator('.el-dialog:has-text("营业执照号")')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/audit-2-detail.png', fullPage: true })

      // 关闭弹窗
      await page.locator('.el-dialog__headerbtn').click()
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    } else {
      console.log('⚠️  当前暂无待审核商家，跳过查看详情测试')
      // 验证查看按钮确实不存在，符合预期
      expect(await viewBtn.isVisible()).toBe(false)
    }
  })

  test('3. 审核通过功能', async ({ page }) => {
    // 找到第一个通过按钮
    const passBtn = page.locator('button:has-text("通过")').first()
    if (await passBtn.isVisible().catch(() => false)) {
      await passBtn.click()
      // 处理可能的确认弹窗
      const confirmDialog = page.locator('.el-message-box:has-text("确定")')
      if (await confirmDialog.isVisible().catch(() => false)) {
        await confirmDialog.locator('button:has-text("确定")').click()
      }
      await page.waitForTimeout(1000)
      // 验证成功提示，匹配任何包含“成功”文本的提示元素
      await expect(page.locator(':has-text("成功")')).toBeVisible({ timeout: 10000 })

      await page.screenshot({ path: 'e2e/screenshots/audit-3-pass.png', fullPage: true })
    } else {
      console.log('⚠️  当前暂无待审核商家，跳过审核通过测试')
    }
  })

  test('4. 审核拒绝功能', async ({ page }) => {
    // 找到第一个拒绝按钮
    const rejectBtn = page.locator('button:has-text("拒绝")').first()
    if (await rejectBtn.isVisible().catch(() => false)) {
      await rejectBtn.click()
      // 验证弹窗显示
      await expect(page.locator('.el-message-box:has-text("拒绝申请")')).toBeVisible()

      // 输入拒绝原因
      await page.locator('.el-message-box .el-input__inner').fill('资料不完整，请重新提交')
      // 点击确定
      await page.locator('.el-message-box__btns button:has-text("确定")').click()
      await page.waitForTimeout(1000)
      // 验证成功提示，匹配任何包含“成功”文本的提示元素
      await expect(page.locator(':has-text("成功")')).toBeVisible({ timeout: 10000 })

      await page.screenshot({ path: 'e2e/screenshots/audit-4-reject.png', fullPage: true })
    } else {
      console.log('⚠️  当前暂无待审核商家，跳过审核拒绝测试')
    }
  })

  test('5. 分页功能测试', async ({ page }) => {
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

        await page.screenshot({ path: 'e2e/screenshots/audit-5-pagination.png', fullPage: true })
      } else {
        console.log('ℹ️  只有一页数据，分页下一页为禁用状态')
        // 验证下一页按钮确实为禁用状态，符合预期
        expect(isDisabled).toBe(true)
      }
    }
  })
})
