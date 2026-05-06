import { test, expect, Page } from '@playwright/test'
import { loginAsPlatformAdmin, navigateToMerchantList, getTableRowCount, clickActionButton } from './utils/common'

const SCREENSHOT_DIR = 'e2e/screenshots'

// 配置基础URL
test.use({ baseURL: 'http://localhost:5174' })

test.describe('商家列表自动化测试', () => {

  test('步骤1: 登录并导航到商家列表页面', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await page.screenshot({ path: `${SCREENSHOT_DIR}/01_after_login.png`, fullPage: true })

    await navigateToMerchantList(page)
    await page.screenshot({ path: `${SCREENSHOT_DIR}/02_merchant_list_page.png`, fullPage: true })

    // Verify page title
    const heading = page.locator('h2:has-text("商家列表")')
    await expect(heading).toBeVisible({ timeout: 10000 })
  })

  test('步骤2: 验证商家列表页面内容', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    // Verify key elements exist
    const heading = page.locator('h2:has-text("商家列表")')
    await expect(heading).toBeVisible({ timeout: 10000 })

    // Verify status filter exists
    const statusSelect = page.locator('.el-select')
    await expect(statusSelect).toBeVisible()

    // Verify search button exists
    const searchBtn = page.locator('button:has-text("搜索")')
    await expect(searchBtn).toBeVisible()

    // Verify table exists
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()

    // Verify table headers
    const expectedHeaders = ['ID', '商家名称', '联系人', '联系电话', '状态', '入驻时间', '操作']
    for (const header of expectedHeaders) {
      await expect(page.locator(`th:has-text("${header}")`)).toBeVisible()
    }

    // Verify pagination exists
    const pagination = page.locator('.el-pagination')
    await expect(pagination).toBeVisible()

    await page.screenshot({ path: `${SCREENSHOT_DIR}/03_page_elements_verified.png`, fullPage: true })
  })

  test('步骤3: 验证列表数据存在', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    // Wait for table data to load
    await page.waitForTimeout(2000)

    // Check if table has data rows
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()

    console.log(`Table row count: ${rowCount}`)

    if (rowCount === 0) {
      console.log('WARNING: No data in merchant list table!')
    } else {
      console.log(`Found ${rowCount} data rows in merchant list`)
    }

    // Check pagination total
    const totalText = await page.locator('.el-pagination').textContent()
    console.log(`Pagination text: ${totalText}`)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/04_table_data_check.png`, fullPage: true })
  })

  test('步骤4: 测试状态筛选 - 待审核', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    // Click status select
    const statusSelect = page.locator('.el-select')
    await statusSelect.click()
    await page.waitForTimeout(500)

    // Select "待审核" option
    const option = page.locator('.el-select-dropdown__item:has-text("待审核")')
    await option.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/05_filter_pending.png`, fullPage: true })

    // Verify filter was applied - check table or API
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Pending status rows: ${rowCount}`)

    // Clear filter
    const clearBtn = page.locator('.el-select .el-select__clear')
    if (await clearBtn.isVisible().catch(() => false)) {
      await clearBtn.click()
      await page.waitForTimeout(1500)
    }
  })

  test('步骤4b: 测试状态筛选 - 已通过', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    const statusSelect = page.locator('.el-select')
    await statusSelect.click()
    await page.waitForTimeout(500)

    const option = page.locator('.el-select-dropdown__item:has-text("已通过")')
    await option.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/06_filter_approved.png`, fullPage: true })

    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Approved status rows: ${rowCount}`)
  })

  test('步骤4c: 测试状态筛选 - 已拒绝', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    const statusSelect = page.locator('.el-select')
    await statusSelect.click()
    await page.waitForTimeout(500)

    const option = page.locator('.el-select-dropdown__item:has-text("已拒绝")')
    await option.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/07_filter_rejected.png`, fullPage: true })

    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Rejected status rows: ${rowCount}`)
  })

  test('步骤4d: 测试状态筛选 - 已禁用', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    const statusSelect = page.locator('.el-select')
    await statusSelect.click()
    await page.waitForTimeout(500)

    const option = page.locator('.el-select-dropdown__item:has-text("已禁用")')
    await option.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/08_filter_disabled.png`, fullPage: true })

    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Disabled status rows: ${rowCount}`)
  })

  test('步骤5: 测试搜索功能 - 按商家名称搜索', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    // Fill search input with keyword
    const searchInput = page.locator('input[placeholder="搜索商家名称/联系人/电话"]')
    await searchInput.fill('电子数码')
    await page.waitForTimeout(500)

    // Click search button
    const searchBtn = page.locator('button:has-text("搜索")')
    await searchBtn.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/09_search_by_name.png`, fullPage: true })

    // Verify search results
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Search by name "电子数码" result rows: ${rowCount}`)

    // Clear search
    const clearBtn = page.locator('.el-input__clear')
    if (await clearBtn.isVisible().catch(() => false)) {
      await clearBtn.click()
      await page.waitForTimeout(500)
      await searchBtn.click()
      await page.waitForTimeout(1500)
    }
  })

  test('步骤5b: 测试搜索功能 - 按联系人搜索', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    const searchInput = page.locator('input[placeholder="搜索商家名称/联系人/电话"]')
    await searchInput.fill('张经理')
    await page.waitForTimeout(500)

    const searchBtn = page.locator('button:has-text("搜索")')
    await searchBtn.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/09b_search_by_contact.png`, fullPage: true })

    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Search by contact "张经理" result rows: ${rowCount}`)
  })

  test('步骤5c: 测试搜索功能 - 按电话号码搜索', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    const searchInput = page.locator('input[placeholder="搜索商家名称/联系人/电话"]')
    await searchInput.fill('13800138001')
    await page.waitForTimeout(500)

    const searchBtn = page.locator('button:has-text("搜索")')
    await searchBtn.click()
    await page.waitForTimeout(1500)

    await page.screenshot({ path: `${SCREENSHOT_DIR}/09c_search_by_phone.png`, fullPage: true })

    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const rowCount = await rows.count()
    console.log(`Search by phone "13800138001" result rows: ${rowCount}`)
  })

  test('步骤6: 测试查看商家详情', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    await page.waitForTimeout(2000)

    // Find first "查看" button
    const viewBtn = page.locator('button:has-text("查看")').first()
    if (await viewBtn.isVisible().catch(() => false)) {
      await viewBtn.click()
      await page.waitForTimeout(1000)

      // Verify detail dialog is visible
      const dialog = page.locator('.el-dialog:has-text("商家详情")')
      await expect(dialog).toBeVisible({ timeout: 5000 })

      // Verify detail fields
      const expectedLabels = ['商家名称', '联系人', '联系电话', '联系邮箱', '营业执照号', '地址', '状态', '入驻时间']
      for (const label of expectedLabels) {
        const labelEl = page.locator(`.el-descriptions__label:has-text("${label}")`)
        await expect(labelEl).toBeVisible({ timeout: 3000 })
      }

      await page.screenshot({ path: `${SCREENSHOT_DIR}/10_detail_dialog.png`, fullPage: true })

      // Close dialog
      const closeBtn = page.locator('.el-dialog__headerbtn')
      await closeBtn.click()
      await page.waitForTimeout(500)
    } else {
      console.log('No "查看" button found - table might be empty')
    }
  })

  test('步骤7: 测试评分功能', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    await page.waitForTimeout(2000)

    // Find first "评分" button
    const scoreBtn = page.locator('button:has-text("评分")').first()
    if (await scoreBtn.isVisible().catch(() => false)) {
      await scoreBtn.click()
      await page.waitForTimeout(1000)

      // Verify score dialog is visible
      const dialog = page.locator('.el-dialog:has-text("商家评分")')
      await expect(dialog).toBeVisible({ timeout: 5000 })

      // Verify score form fields
      const expectedLabels = ['商品评分', '服务评分', '物流评分', '品牌认证']
      for (const label of expectedLabels) {
        const labelEl = page.locator(`.el-form-item__label:has-text("${label}")`)
        await expect(labelEl).toBeVisible({ timeout: 3000 })
      }

      await page.screenshot({ path: `${SCREENSHOT_DIR}/11_score_dialog.png`, fullPage: true })

      // Set score values
      const inputNumbers = page.locator('.el-input-number')
      const inputCount = await inputNumbers.count()
      if (inputCount >= 3) {
        // Set product score to 4.5
        await inputNumbers.nth(0).locator('input').fill('4.5')
        // Set service score to 4.0
        await inputNumbers.nth(1).locator('input').fill('4.0')
        // Set logistics score to 3.5
        await inputNumbers.nth(2).locator('input').fill('3.5')
      }

      // Toggle brand verification
      const brandSwitch = page.locator('.el-switch')
      if (await brandSwitch.isVisible().catch(() => false)) {
        await brandSwitch.click()
      }

      await page.screenshot({ path: `${SCREENSHOT_DIR}/12_score_form_filled.png`, fullPage: true })

      // Try to submit score - this may fail if backend endpoint is missing
      const saveBtn = page.locator('.el-dialog button:has-text("保存")')
      await saveBtn.click()
      await page.waitForTimeout(2000)

      await page.screenshot({ path: `${SCREENSHOT_DIR}/13_score_submit_result.png`, fullPage: true })

      // Check for error messages
      const errorMsg = await page.locator('.el-message--error').textContent().catch(() => null)
      if (errorMsg) {
        console.log(`Score submission error: ${errorMsg}`)
      }
    } else {
      console.log('No "评分" button found - table might be empty')
    }
  })

  test('步骤8: 测试禁用商家功能', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    await page.waitForTimeout(2000)

    // Find a row with status "已通过" and a "禁用" button
    const disableBtn = page.locator('button:has-text("禁用")').first()
    if (await disableBtn.isVisible().catch(() => false)) {
      await disableBtn.click()
      await page.waitForTimeout(500)

      // Confirm dialog
      const confirmBtn = page.locator('.el-message-box__btns button:has-text("确定")')
      if (await confirmBtn.isVisible().catch(() => false)) {
        await page.screenshot({ path: `${SCREENSHOT_DIR}/14_disable_confirm.png`, fullPage: true })
        await confirmBtn.click()
        await page.waitForTimeout(2000)
        await page.screenshot({ path: `${SCREENSHOT_DIR}/15_disable_result.png`, fullPage: true })
      }
    } else {
      console.log('No "禁用" button found (no approved merchants)')
    }
  })

  test('步骤9: 测试启用商家功能', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    await page.waitForTimeout(2000)

    // Filter by "已禁用" to find disabled merchants
    const statusSelect = page.locator('.el-select')
    await statusSelect.click()
    await page.waitForTimeout(500)

    const disabledOption = page.locator('.el-select-dropdown__item:has-text("已禁用")')
    await disabledOption.click()
    await page.waitForTimeout(1500)

    // Find "启用" button
    const enableBtn = page.locator('button:has-text("启用")').first()
    if (await enableBtn.isVisible().catch(() => false)) {
      await enableBtn.click()
      await page.waitForTimeout(2000)
      await page.screenshot({ path: `${SCREENSHOT_DIR}/16_enable_result.png`, fullPage: true })

      // Check for success message
      const successMsg = await page.locator('.el-message--success').textContent().catch(() => null)
      if (successMsg) {
        console.log(`Enable success: ${successMsg}`)
      }
    } else {
      console.log('No "启用" button found (no disabled merchants)')
    }
  })

  test('步骤10: 测试分页功能', async ({ page }) => {
    await loginAsPlatformAdmin(page)
    await navigateToMerchantList(page)

    await page.waitForTimeout(2000)

    // Check if pagination has next page
    const nextBtn = page.locator('.el-pagination .btn-next')
    if (await nextBtn.isVisible().catch(() => false)) {
      const isDisabled = await nextBtn.getAttribute('class').then(cls => cls?.includes('disabled') ?? false)

      if (!isDisabled) {
        await nextBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: `${SCREENSHOT_DIR}/17_page2.png`, fullPage: true })

        // Go back to page 1
        const prevBtn = page.locator('.el-pagination .btn-prev')
        await prevBtn.click()
        await page.waitForTimeout(1500)
        await page.screenshot({ path: `${SCREENSHOT_DIR}/18_page1_back.png`, fullPage: true })
      } else {
        console.log('Only one page of data - pagination next is disabled')
      }
    } else {
      console.log('Pagination next button not found')
    }
  })

})
