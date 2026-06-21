# samples

Minimal, runnable sample set for the OpenAPI Generics platform.

These samples demonstrate how a Java-based contract is exposed through OpenAPI, consumed via a generated client, and reused by a downstream service without structural drift.

---

## Overview

Each sample stack contains:

* **Producer** → defines the API contract
* **Generated client** → built from OpenAPI
* **Consumer** → uses the generated client

The same contract flows through all layers.

---

## Stacks

Two equivalent stacks are provided:

| Stack         | Spring Boot | Producer | Consumer |
| ------------- | ----------- | -------- | -------- |
| spring-boot-3 | 3.x         | 8084     | 8085     |
| spring-boot-4 | 4.x         | 8094     | 8095     |

Behavior is identical. Only runtime differs.

---

## Project structure

```
samples
├── domain-contracts
├── spring-boot-3
├── spring-boot-4
└── type-coverage
```

Each stack is self-contained and runnable.

---

## Type Coverage Samples

The `type-coverage` directory contains focused validation samples used to verify OpenAPI projection, wrapper reconstruction, generated client behavior, and regression scenarios.

See the individual sample documentation for details.

## Prerequisites

* Docker 20+
* Docker Compose v2

---

## Run

### Spring Boot 3

```
cd spring-boot-3
docker compose up --build -d
```

### Spring Boot 4

```
cd spring-boot-4
docker compose up --build -d
```

---

## Verify

### Spring Boot 3

Single resource

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

Pagination

```bash
curl "http://localhost:8085/customer-service-consumer/customers?page=0&size=5"
```

Producer OpenAPI

```bash
curl http://localhost:8084/customer-service/v3/api-docs.yaml
```

---

### Spring Boot 4

Single resource

```bash
curl http://localhost:8095/customer-service-consumer/customers/1
```

Pagination

```bash
curl "http://localhost:8095/customer-service-consumer/customers?page=0&size=5"
```

Producer OpenAPI

```bash
curl http://localhost:8094/customer-service/v3/api-docs.yaml
```

---

## What to observe

* The response structure remains consistent across producer and consumer
* Nested generic structures (e.g. paginated responses) are preserved
* No manual mapping is required between layers

---

## Local run (optional)

From the `samples` directory:

```
mvn clean install
```

Run services individually if needed.

---

## Stop

```
docker compose down
```

---

## Notes

* In-memory data store (no external dependencies)
* Sample domain is intentionally simple
* Focus is on contract flow, not business logic

---

## Summary

These samples provide a minimal environment to validate that a contract defined in Java can be exposed, consumed, and reused consistently across service boundaries.
