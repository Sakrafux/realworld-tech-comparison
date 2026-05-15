# This Dockerfile assumes the JAR has been built already
# e.g., via 'mvn package -DskipTests' or 'mvn clean install'
# from the root of the monorepo.
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the pre-built JAR
COPY target/java-micronaut-vertical-0.1.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
