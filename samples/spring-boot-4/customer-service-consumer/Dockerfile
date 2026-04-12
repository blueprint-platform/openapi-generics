FROM maven:3.9.12-eclipse-temurin-21-noble AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY domain-contracts domain-contracts
COPY spring-boot-4 spring-boot-4

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -f spring-boot-4/pom.xml -U -q -T 2C -DskipTests --no-transfer-progress \
    -pl customer-service-consumer -am clean package && \
    JAR="$(ls -1 spring-boot-4/customer-service-consumer/target/customer-service-consumer-*.jar | head -n 1)" && \
    mkdir -p /layers && \
    cd /layers && \
    java -Djarmode=layertools -jar "/workspace/$JAR" extract

FROM eclipse-temurin:21.0.10_7-jre-noble
WORKDIR /app

RUN useradd -r -u 10001 -g root -s /bin/false -d /app appuser && \
    mkdir -p /app/logs && \
    chown -R 10001:0 /app

LABEL org.opencontainers.image.title="customer-service-consumer-sb4" \
      org.opencontainers.image.description="Customer Service Consumer Spring Boot 4" \
      org.opencontainers.image.source="https://github.com/blueprint-platform/openapi-generics" \
      org.opencontainers.image.licenses="MIT"

COPY --from=builder --chown=10001:0 /layers/dependencies/ ./
COPY --from=builder --chown=10001:0 /layers/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:0 /layers/spring-boot-loader/ ./
COPY --from=builder --chown=10001:0 /layers/application/ ./

USER 10001

ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

EXPOSE 8095
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]