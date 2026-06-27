# samples

Minimal, runnable sample set for the OpenAPI Generics platform.

These samples demonstrate how a Java-based contract is exposed through OpenAPI, consumed through a generated client, and reused by a downstream service without structural drift.

The focus is contract flow:

```text
Producer Service
      ↓
Java Contract
      ↓
OpenAPI Projection
      ↓
Generated Client
      ↓
Consumer Service
```

The generated client reconstructs the original contract shape instead of redefining equivalent wrapper models.

---

## Table of Contents

- [Overview](#overview)
- [Sample Layout](#sample-layout)
- [Available Sample Types](#available-sample-types)
- [Prerequisites](#prerequisites)
- [Spring Boot 3 Stack](#spring-boot-3-stack)
- [Spring Boot 4 Stack](#spring-boot-4-stack)
- [Type Coverage Samples](#type-coverage-samples)
- [Local Maven Build](#local-maven-build)
- [What to Observe](#what-to-observe)
- [Notes](#notes)
- [Summary](#summary)

---

## Overview

Each runnable integration stack contains three layers:

| Layer                | Responsibility                                                           |
|----------------------|--------------------------------------------------------------------------|
| **Producer**         | Defines the Java API contract and publishes the OpenAPI document.        |
| **Generated client** | Is generated from the OpenAPI document using OpenAPI Generics.           |
| **Consumer**         | Uses the generated client and exposes downstream verification endpoints. |

The same contract moves through all layers.

No manual wrapper reconstruction is required in the consumer.

No DTO translation layer is required between producer and consumer.

---

## Sample Layout

```text
samples
├── domain-contracts
├── spring-boot-3
│   ├── customer-service
│   ├── customer-service-client
│   └── customer-service-consumer
├── spring-boot-4
│   ├── customer-service
│   ├── customer-service-client
│   └── customer-service-consumer
└── type-coverage
    ├── service-response
    └── byoe-response
```

| Directory          | Purpose                                                                                  |
|--------------------|------------------------------------------------------------------------------------------|
| `domain-contracts` | Shared domain and contract types used by the sample stacks.                              |
| `spring-boot-3`    | Runnable Spring Boot 3 producer → generated client → consumer stack.                     |
| `spring-boot-4`    | Runnable Spring Boot 4 producer → generated client → consumer stack.                     |
| `type-coverage`    | Focused validation suites for supported generic response shapes and regression coverage. |

---

## Available Sample Types

The repository contains two kinds of samples.

| Sample Type               | Purpose                                                                                                                                                  |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Integration stacks**    | Runnable producer → generated client → consumer applications for Spring Boot 3 and Spring Boot 4.                                                        |
| **Type coverage samples** | Focused regression suites that validate supported generic response shapes, BYOE contracts, collections, pages, and application-owned generic containers. |

The integration stacks are intended for first-time users who want to run the platform end-to-end.

The type coverage samples are intended for validating the core OpenAPI Generics reconstruction pipeline across many response shapes.

---

## Prerequisites

For Docker-based sample execution:

- Docker 20+
- Docker Compose v2

For local Maven builds:

- Java 17+
- Maven 3.9+

---

## Spring Boot 3 Stack

Use this stack for the standard Spring Boot 3 validation path.

It runs the complete OpenAPI Generics lifecycle:

```text
Spring Boot 3 Producer
        ↓
OpenAPI Document
        ↓
Generated Java Client
        ↓
Spring Boot 3 Consumer
```

### Services

| Service  | Port | URL                                               |
|----------|------|---------------------------------------------------|
| Producer | 8084 | `http://localhost:8084/customer-service`          |
| Consumer | 8085 | `http://localhost:8085/customer-service-consumer` |

### Start

From the `samples` directory:

```bash
cd spring-boot-3
docker compose up --build -d
```

### Verify Producer

OpenAPI document:

```bash
curl http://localhost:8084/customer-service/v3/api-docs.yaml
```

Swagger UI:

```text
http://localhost:8084/customer-service/swagger-ui/index.html
```

### Verify Consumer

Single resource:

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

Paginated response:

```bash
curl "http://localhost:8085/customer-service-consumer/customers?page=0&size=5"
```

The consumer uses the generated client to call the producer.

The response should preserve the same contract shape across producer, OpenAPI, generated client, and consumer.

### Stop

From the `samples/spring-boot-3` directory:

```bash
docker compose down
```

---

## Spring Boot 4 Stack

Use this stack when validating the Spring Boot 4 runtime path.

It runs the same OpenAPI Generics lifecycle as the Spring Boot 3 stack, but on the Spring Boot 4 baseline:

```text
Spring Boot 4 Producer
        ↓
OpenAPI Document
        ↓
Generated Java Client
        ↓
Spring Boot 4 Consumer
```

### Services

| Service  | Port | URL                                               |
|----------|------|---------------------------------------------------|
| Producer | 8094 | `http://localhost:8094/customer-service`          |
| Consumer | 8095 | `http://localhost:8095/customer-service-consumer` |

### Start

From the `samples` directory:

```bash
cd spring-boot-4
docker compose up --build -d
```

### Verify Producer

OpenAPI document:

```bash
curl http://localhost:8094/customer-service/v3/api-docs.yaml
```

Swagger UI:

```text
http://localhost:8094/customer-service/swagger-ui/index.html
```

### Verify Consumer

Single resource:

```bash
curl http://localhost:8095/customer-service-consumer/customers/1
```

Paginated response:

```bash
curl "http://localhost:8095/customer-service-consumer/customers?page=0&size=5"
```

The behavior is intentionally equivalent to the Spring Boot 3 stack.

The purpose is to verify that the same OpenAPI Generics contract flow works across the Spring Boot 4 runtime baseline.

### Stop

From the `samples/spring-boot-4` directory:

```bash
docker compose down
```

---

## Type Coverage Samples

The `type-coverage` directory contains focused validation suites for the core OpenAPI Generics reconstruction pipeline.

Unlike the Spring Boot integration stacks, these samples are not intended to demonstrate application architecture or business workflows.

They verify contract-preservation behavior across supported response shapes, including:

- scalar payloads
- value payloads
- enum payloads
- DTO payloads
- `List<T>` payloads
- `Set<T>` payloads
- built-in `Page<T>` payloads
- application-owned generic containers such as `Paging<T>` and `Window<T>`
- platform-owned `ServiceResponse<T>` envelopes
- user-owned BYOE `ApiResponse<T>` envelopes

Available type-coverage samples:

| Sample                           | Purpose                                                                                 |
|----------------------------------|-----------------------------------------------------------------------------------------|
| `type-coverage/service-response` | Validates the canonical platform-provided `ServiceResponse<T>` contract.                |
| `type-coverage/byoe-response`    | Validates Bring Your Own Envelope support using a user-owned `ApiResponse<T>` contract. |

These samples are used as executable regression suites for projection metadata, vendor extensions, wrapper reconstruction, generated client typing, runtime deserialization, and consumer compatibility.

See [`type-coverage/README.md`](type-coverage/README.md) for the full validation matrix.

---

## Local Maven Build

Docker is the recommended path for running the integration stacks.

For local build verification, run from the `samples` directory:

```bash
mvn clean install
```

This builds the sample modules and verifies that generated client artifacts compile successfully.

Individual services can also be started locally from their module directories when needed.

---

## What to Observe

The important part is not the sample business domain.

The important part is that the contract shape remains stable across the full lifecycle.

Observe that:

- the producer owns the Java contract
- OpenAPI acts as a projection of that contract
- the generated client reconstructs the wrapper shape
- the consumer uses generated artifacts directly
- no manual DTO mapping is required between producer and consumer
- paginated generic responses keep their generic structure
- generated wrappers extend reusable contract types instead of redefining equivalent models

For example, generated response wrappers should remain thin contract bindings rather than duplicated envelope implementations:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated class binds the generic parameters.

It does not become the owner of the envelope structure.

---

## Notes

- Samples use in-memory data.
- No external database is required.
- The sample domain is intentionally simple.
- The focus is contract projection, generated client reconstruction, and downstream reuse.
- Spring Boot 3 and Spring Boot 4 stacks are intentionally equivalent.
- Type-coverage samples provide focused regression validation beyond the customer-service integration flow.
- You do not need to run the Spring Boot 3 and Spring Boot 4 stacks at the same time.

---

## Summary

These samples provide runnable environments for validating the OpenAPI Generics contract lifecycle.

They prove that a Java contract can be projected into OpenAPI, reconstructed into a generated Java client, and reused by a downstream consumer without redefining equivalent wrapper or DTO models.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Generated Client
      ↓
Consumer Reuse
```

The result is stable contract identity across service boundaries.