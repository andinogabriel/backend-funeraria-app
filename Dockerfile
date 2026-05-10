# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# Strip CRLF from every text file the build context might have brought in with Windows line
# endings. The repository's .gitattributes already pins these files to LF, but a clone done
# before that policy landed (or made on a machine with `core.autocrlf=true` and never
# re-normalized) keeps CRLF in the working tree, which the JVM then reads as part of
# argument values from .mvn/jvm.config and refuses to start. Doing the strip here makes the
# image build deterministic regardless of how the host's Git is configured — works on
# Linux, macOS and Windows (and CI) without any host-side intervention.
RUN find .mvn -type f -exec sed -i 's/\r$//' {} + \
    && sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/andinogabriel/backend-funeraria-app" \
      org.opencontainers.image.title="backend-funeraria-app"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system spring \
    && adduser --system --ingroup spring spring

COPY --from=build /workspace/target/backend-funeraria-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl --fail --silent --show-error http://127.0.0.1:8081/actuator/health/liveness || exit 1

USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
