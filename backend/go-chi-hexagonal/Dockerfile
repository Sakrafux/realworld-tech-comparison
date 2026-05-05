# Build stage
FROM golang:1.26-alpine AS builder

# Install necessary build tools (if any)
RUN apk add --no-cache git

WORKDIR /app

# Copy go mod and sum files
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the application code
COPY . .

# Build the application
# CGO_ENABLED=0 ensures a statically linked binary for alpine
RUN CGO_ENABLED=0 GOOS=linux go build -o /realworld ./cmd/realworld/main.go

# Run stage
FROM alpine:latest

# Add ca-certificates for HTTPS and tzdata for timezones
RUN apk add --no-cache ca-certificates tzdata

WORKDIR /

# Copy the binary from the builder stage
COPY --from=builder /realworld /realworld

# Copy any runtime resources if necessary (none currently identified for this project)

# Expose the application port
EXPOSE 8080

# Run the binary
CMD ["/realworld"]
