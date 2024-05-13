FROM gradle:8-jdk21 AS builder

WORKDIR /usr/src/app/

COPY . .
RUN gradle build

FROM gcr.io/distroless/java21-debian12

ARG version=2.2.0
WORKDIR /usr/src/app/

COPY --from=builder /usr/src/app/build/libs/tempsimulator-0.0.1.jar ./
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$version/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/app/opentelemetry-javaagent.jar

ENTRYPOINT [ "java", "-jar", "tempsimulator-0.0.1.jar" ]
