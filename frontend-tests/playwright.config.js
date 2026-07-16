import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  expect: {
    timeout: 10000,
  },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  webServer: [
    {
      command: 'cd .. && mvn spring-boot:run -q',
      port: 8080,
      timeout: 120000,
      reuseExistingServer: !process.env.CI,
    },
    {
      command: 'npx http-server ../frontend -p 3000 --silent',
      port: 3000,
      reuseExistingServer: !process.env.CI,
    },
  ],
});
