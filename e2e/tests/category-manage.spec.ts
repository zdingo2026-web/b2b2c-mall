import { test, expect } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToPage } from './utils/common'

test.describe('分类管理模块自动化测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToPage(page, '/platform/product/category')
  })

  test('1. 页面加载验证', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h2:has-text("商品分类管理")')).toBeVisible()
    // 验证新增按钮存在
    await expect(page.locator('button:has-text("新增一级分类")')).toBeVisible()
    // 验证树形结构加载
    await expect(page.locator('.el-tree')).toBeVisible()

    await page.screenshot({ path: 'e2e/screenshots/category-1-page-load.png', fullPage: true })
  })

  test('2. 新增一级分类功能', async ({ page }) => {
    // 点击新增一级分类
    await page.locator('button:has-text("新增一级分类")').click()
    // 验证弹窗显示
    await expect(page.locator('.el-dialog:has-text("新增分类")')).toBeVisible()

    // 填写表单
    const categoryName = `测试分类${Date.now()}`
    await page.locator('input[placeholder="请输入分类名称"]').fill(categoryName)
    await page.locator('.el-input-number input').fill('10')
    // 启用状态保持默认

    await page.screenshot({ path: 'e2e/screenshots/category-2-add-form.png', fullPage: true })

    // 提交
    await page.locator('.el-dialog button:has-text("确定")').click()
    // 验证成功提示
    await expect(page.locator('.el-message--success')).toBeVisible()

    // 验证分类已添加到树中
    await expect(page.locator(`.el-tree-node__content:has-text("${categoryName}")`)).toBeVisible()

    await page.screenshot({ path: 'e2e/screenshots/category-2-add-result.png', fullPage: true })
  })

  test('3. 编辑分类功能', async ({ page }) => {
    // 找到第一个编辑按钮
    const editBtn = page.locator('button:has-text("编辑")').first()
    if (await editBtn.isVisible().catch(() => false)) {
      await editBtn.click()
      // 验证弹窗显示
      await expect(page.locator('.el-dialog:has-text("编辑分类")')).toBeVisible()

      // 修改分类名称
      const newName = `编辑后分类${Date.now()}`
      await page.locator('input[placeholder="请输入分类名称"]').fill(newName)
      await page.locator('.el-input-number input').fill('20')

      await page.screenshot({ path: 'e2e/screenshots/category-3-edit-form.png', fullPage: true })

      // 提交
      await page.locator('.el-dialog button:has-text("确定")').click()
      // 验证成功提示
      await expect(page.locator('.el-message--success')).toBeVisible()

      // 验证名称已更新
      await expect(page.locator(`.el-tree-node__content:has-text("${newName}")`)).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/category-3-edit-result.png', fullPage: true })
    } else {
      console.log('⚠️  暂无分类数据，跳过编辑测试')
    }
  })

  test('4. 添加子分类功能', async ({ page }) => {
    // 找到第一个添加子分类按钮
    const addSubBtn = page.locator('button:has-text("添加子分类")').first()
    if (await addSubBtn.isVisible().catch(() => false)) {
      await addSubBtn.click()
      // 验证弹窗显示
      await expect(page.locator('.el-dialog:has-text("新增分类")')).toBeVisible()
      // 验证上级分类已自动选中
      const parentSelect = page.locator('label:has-text("上级分类")').locator('..').locator('input')
      await expect(parentSelect).toBeVisible()

      // 填写子分类信息
      const subCategoryName = `子分类${Date.now()}`
      await page.locator('input[placeholder="请输入分类名称"]').fill(subCategoryName)

      await page.screenshot({ path: 'e2e/screenshots/category-4-add-sub-form.png', fullPage: true })

      // 提交
      await page.locator('.el-dialog button:has-text("确定")').click()
      // 验证成功提示
      await expect(page.locator('.el-message--success')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/category-4-add-sub-result.png', fullPage: true })
    } else {
      console.log('⚠️  暂无一级分类，跳过添加子分类测试')
    }
  })

  test('5. 删除分类功能', async ({ page }) => {
    // 找到最后一个删除按钮（避免删除有子分类的）
    const deleteBtns = page.locator('button:has-text("删除")')
    const count = await deleteBtns.count()
    if (count > 0) {
      const deleteBtn = deleteBtns.nth(count - 1)
      const categoryName = await deleteBtn.locator('xpath=../preceding-sibling::span').textContent() || '测试分类'

      await deleteBtn.click()
      // 验证确认弹窗
      await expect(page.locator('.el-message-box:has-text("确定删除分类")')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/category-5-delete-confirm.png', fullPage: true })

      // 确认删除
      await page.locator('.el-message-box__btns button:has-text("确定")').click()
      // 验证成功提示
      await expect(page.locator('.el-message--success')).toBeVisible()

      await page.screenshot({ path: 'e2e/screenshots/category-5-delete-result.png', fullPage: true })
    } else {
      console.log('⚠️  暂无分类数据，跳过删除测试')
    }
  })
})
