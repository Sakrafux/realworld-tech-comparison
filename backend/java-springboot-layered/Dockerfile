# This Dockerfile assumes the JAR has been built already
# e.g., via 'mvn package -DskipTests' or 'mvn clean install'
# from the root of the monorepo.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR
COPY target/realworld-1.0.0-SNAPSHOT.jar app.jar

# Default environment variables
ENV SERVER_PORT=8080
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/realworld
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
