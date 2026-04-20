FROM postgres:latest

# Copy initialization scripts in specific order
COPY ddl.sql /docker-entrypoint-initdb.d/

# Set default environment variables
ENV POSTGRES_DB=realworld
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_USER=postgres
