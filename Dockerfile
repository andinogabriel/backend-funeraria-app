# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/andinogabriel/backend-funeraria-app" \
      org.opencontainers.image.title="backend-funeraria-app"

RUN addgroup --system spring \
    && adduser --system --ingroup spring spring

COPY --from=build /workspace/target/backend-funeraria-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
