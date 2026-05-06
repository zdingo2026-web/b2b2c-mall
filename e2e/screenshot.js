const { chromium } = require('@playwright/test');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();

  // 登录
  await page.goto('http://localhost:5174/login');
  await page.waitForTimeout(1000);

  // 选择平台管理
  await page.locator('.el-segmented__item-label').first().click();
  await page.waitForTimeout(300);

  // 填写账号密码
  await page.locator('input[placeholder="用户名"]').fill('admin');
  await page.locator('input[placeholder="密码"]').fill('123456');
  await page.locator('button:has-text("登录")').click();
  await page.waitForTimeout(2000);

  // 导航到商家列表
  await page.goto('http://localhost:5174/platform/merchant/list');
  await page.waitForTimeout(3000);

  // 截图
  await page.screenshot({ path: 'merchant-list-final.png', fullPage: true });
  console.log('截图已保存为 merchant-list-final.png');

  await browser.close();
})();
