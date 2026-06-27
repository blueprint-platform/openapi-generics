# type-coverage

Focused end-to-end validation samples for OpenAPI Generics.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture, business workflows, or framework usage patterns.

Their purpose is to provide deterministic validation environments for verifying the core OpenAPI Generics pipeline and preventing regressions across future releases.

The samples validate that generic response contracts can move through the complete lifecycle without losing their original contract shape:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Runtime
```

---

## Table of Contents

- [Purpose](#purpose)
- [Available Samples](#available-samples)
- [Sample Boundaries](#sample-boundaries)
- [Validation Scope](#validation-scope)
- [Regression Role](#regression-role)
- [Mental Model](#mental-model)

---

## Purpose

The `type-coverage` samples exist to verify contract preservation across the OpenAPI Generics pipeline.

They focus on:

- OpenAPI projection correctness
- vendor extension processing
- generated client reconstruction
- runtime deserialization
- consumer-side type compatibility
- regression protection across future releases

The focus is not endpoint functionality.

The focus is preserving type fidelity from producer contract to consumer usage.

---

## Available Samples

| Sample                                           | Purpose                                                                                 |
|--------------------------------------------------|-----------------------------------------------------------------------------------------|
| [`service-response`](service-response/README.md) | Validates the canonical platform-provided `ServiceResponse<T>` envelope.                |
| [`byoe-response`](byoe-response/README.md)       | Validates Bring Your Own Envelope support using a user-owned `ApiResponse<T>` contract. |

Each sample contains its own producer, generated client, and consumer modules.

For the full validation matrix, endpoint list, and smoke-test commands, see the individual sample documentation.

---

## Sample Boundaries

The two samples are related, but they do not validate the exact same response-shape matrix.

### `service-response`

The `service-response` sample validates the canonical OpenAPI Generics envelope:

```java
ServiceResponse<T>
```

It covers the built-in supported response shapes:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

This sample is the baseline validation path for the platform-provided contract.

It does not validate application-owned generic containers such as `Paging<T>` or `Window<T>`.

### `byoe-response`

The `byoe-response` sample validates a completely user-owned response envelope:

```java
ApiResponse<T>
```

It covers BYOE reconstruction for built-in shapes:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

It also validates OpenAPI Generics 1.2 application-defined generic container support through user-owned container payloads such as:

```java
ApiResponse<Paging<T>>
ApiResponse<Window<T>>
```

This sample proves that OpenAPI Generics can preserve generic contract semantics even when the response envelope and nested generic containers are owned by the application.

---

## Validation Scope

Across the available samples, the type-coverage suite verifies:

- generic envelope reconstruction
- scalar payload handling
- value payload handling
- enum payload handling
- DTO payload handling
- collection payload reconstruction
- built-in `Page<T>` reconstruction
- BYOE envelope reconstruction
- application-owned generic container reconstruction in BYOE flows
- ignored model handling
- external model handling
- generated client type safety
- runtime deserialization behavior
- consumer compatibility

The exact covered shapes differ by sample.

Use the sample-specific README files for detailed coverage.

---

## Regression Role

These samples act as executable regression suites.

They are intended to detect failures in:

- response introspection
- OpenAPI metadata generation
- vendor extension consistency
- generic wrapper reconstruction
- generated Java client typing
- generated-source hygiene
- runtime deserialization
- producer → client → consumer compatibility

before such failures reach real integration projects.

---

## Mental Model

OpenAPI Generics preserves contract intent across the full lifecycle.

```text
Original Java Contract
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
Consumer Runtime
          ↓
Same Contract Shape
```

For the platform envelope, this means:

```text
ServiceResponse<Page<TypeSummaryDto>>
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
ServiceResponse<Page<TypeSummaryDto>>
```

For a BYOE envelope with an application-owned generic container, this means:

```text
ApiResponse<Paging<TypeSummaryDto>>
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
ApiResponse<Paging<TypeSummaryDto>>
```

A successful type-coverage sample proves that the consumer receives the same generic contract shape that originally existed in the producer.

This directory exists to continuously verify that guarantee.