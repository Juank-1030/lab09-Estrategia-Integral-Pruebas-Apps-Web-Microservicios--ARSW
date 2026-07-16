import { test, expect } from '@playwright/test';

test.describe('Order Management E2E', () => {

  test('usuario puede crear un pedido exitosamente', async ({ page }) => {
    await page.goto('/');

    await page.fill('[data-testid="customer-id"]', 'CUS-01');
    await page.fill('[data-testid="order-total"]', '120000');
    await page.click('[data-testid="create-order"]');

    await expect(page.locator('[data-testid="order-status"]')).toContainText('CREATED');
    await expect(page.locator('[data-testid="order-id"]')).toBeVisible();
  });

  test('usuario ve error si el total es inválido', async ({ page }) => {
    await page.goto('/');

    await page.fill('[data-testid="customer-id"]', 'CUS-01');
    await page.fill('[data-testid="order-total"]', '-10');
    await page.click('[data-testid="create-order"]');

    await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
  });

  test('usuario puede consultar un pedido por ID', async ({ page }) => {
    await page.goto('/');

    await page.fill('[data-testid="customer-id"]', 'CUS-01');
    await page.fill('[data-testid="order-total"]', '50000');
    await page.click('[data-testid="create-order"]');

    await expect(page.locator('[data-testid="order-id"]')).toBeVisible();
    const orderId = await page.locator('[data-testid="order-id"]').textContent();

    await page.fill('[data-testid="search-order-id"]', orderId.trim());
    await page.click('[data-testid="search-order"]');

    await expect(page.locator('[data-testid="order-detail"]')).toBeVisible();
  });

});
