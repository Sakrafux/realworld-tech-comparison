import { defineConfig } from '@playwright/test';
import { baseConfig } from './src/playwright.base';

export default defineConfig({
    ...baseConfig,
    use: { ...baseConfig.use, baseURL: 'http://localhost:3000' },
    testDir: './'
});
