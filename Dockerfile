# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Strip CRLF from every text file the build context might have brought in with Windows line
# endings. The repository's .gitattributes already pins these files to LF, but a clone done
# before that policy landed (or made on a machine with `core.autocrlf=true` and never
# re-normalized) keeps CRLF in the working tree, which the JVM then reads as part of
# argument values from .mvn/jvm.config and refuses to start.
RUN find .mvn -type f -exec sed -i 's/\r$//' {} + \
    && sed -i 's/\r$//' mvnw \
    && chmod +x mvnw

# Resolve dependencies in their own layer, backed by a BuildKit cache mount, so a source-only
# change does not re-download the whole Maven dependency graph on every build.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -B -DskipTests package

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

# Container-aware heap sizing: the JVM default caps the heap at 25% of the container memory
# limit, which starves a single-process container. 75% leaves room for metaspace, thread
# stacks and direct buffers; ExitOnOutOfMemoryError lets the orchestrator restart a wedged JVM
# instead of keeping a half-dead process alive.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# 8081 = public API, 8082 = actuator (management port, never published publicly).
EXPOSE 8081 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl --fail --silent --show-error http://127.0.0.1:8082/actuator/health/liveness || exit 1

USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
