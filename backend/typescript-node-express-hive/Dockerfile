# Build stage
FROM node:24-alpine AS builder

# Install pnpm
RUN npm install -g pnpm

ENV CI=true

WORKDIR /app

# Copy lockfile, package.json and workspace config
COPY pnpm-lock.yaml package.json pnpm-workspace.yaml ./

# Install dependencies
RUN pnpm install --frozen-lockfile

# Copy source code
COPY . .

# Build the application
RUN pnpm run build

# Final stage
FROM node:24-alpine

# Install pnpm
RUN npm install -g pnpm

ENV CI=true

WORKDIR /app

# Copy built files and dependency manifest
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/pnpm-lock.yaml ./pnpm-lock.yaml
COPY --from=builder /app/pnpm-workspace.yaml ./pnpm-workspace.yaml

# Install only production dependencies
RUN pnpm install --prod --frozen-lockfile

# Expose port
EXPOSE 8080

# Start the application
CMD ["node", "dist/index.js"]
