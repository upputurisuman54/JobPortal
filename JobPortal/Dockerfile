# Stage 1: Build the jar using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the jar using a lightweight JDK image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/JobPortal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]