import { defineConfig } from 'vitest/config';
import { config } from 'dotenv';

export default defineConfig({
    test: {
        include: ['tests/**/*.spec.ts'],
        environment: 'node',
        // Parses the .env file and injects all variables into the test environment
        env: config().parsed,
    },
});
