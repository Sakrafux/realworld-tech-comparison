# Production image with Bun
FROM oven/bun:1-alpine AS base
ENV NODE_ENV=production
WORKDIR /app

# Copy dependency files
COPY package.json ./

# Install production dependencies
RUN bun install --production

# Copy source code and config
COPY src ./src
COPY tsconfig.json ./tsconfig.json

# Expose port
EXPOSE 8080

# Start server natively
CMD ["bun", "src/index.ts"]
