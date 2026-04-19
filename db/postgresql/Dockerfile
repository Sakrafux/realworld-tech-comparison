FROM postgres:latest

# Copy initialization scripts in specific order
COPY 0_triggers.sql /docker-entrypoint-initdb.d/
COPY 1_tables.sql /docker-entrypoint-initdb.d/
COPY 2_sequences.sql /docker-entrypoint-initdb.d/
COPY 3_triggers_on_tables.sql /docker-entrypoint-initdb.d/
COPY 4_intial_data.sql /docker-entrypoint-initdb.d/

# Set default environment variables
ENV POSTGRES_DB=realworld
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_USER=postgres
