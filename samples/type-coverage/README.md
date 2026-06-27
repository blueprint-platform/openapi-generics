# type-coverage

Focused end-to-end validation samples for OpenAPI Generics.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture, business workflows, or framework usage patterns.

Their purpose is to provide deterministic validation environments for verifying the core OpenAPI Generics pipeline and preventing regressions across future releases.

The type-coverage samples validate two distinct contract models:

- the platform-provided `ServiceResponse<T>` envelope
- a user-owned Bring Your Own Envelope (BYOE) contract using `ApiResponse<T>`

Each sample has its own validation scope. The two samples are related, but they do not validate the exact same response-shape matrix.

---

## Table of Contents

- [Purpose](#purpose)
- [Validation Model](#validation-model)
- [Design Principles](#design-principles)
- [Available Samples](#available-samples)
- [ServiceResponse Coverage](#serviceresponse-coverage)
- [BYOE Coverage](#byoe-coverage)
- [What Is Validated](#what-is-validated)
- [Regression Strategy](#regression-strategy)
- [Mental Model](#mental-model)

---

## Purpose

Each sample isolates a specific contract model and validates the complete lifecycle:

```text
Contract
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Runtime Deserialization
    ↓
Consumer Usage
```

The focus is not endpoint functionality.

The focus is preserving type fidelity across the entire projection and reconstruction process.

A successful type-coverage sample proves that the consumer receives the same generic contract shape that originally existed in the producer.

---

## Validation Model

The `type-coverage` directory contains focused validation suites for supported OpenAPI Generics contract shapes.

There are two separate coverage tracks.

### Platform Envelope Track

The `service-response` sample validates the canonical platform-provided envelope:

```java
ServiceResponse<T>
```

It verifies the built-in response shapes supported by the OpenAPI Generics platform:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

This sample does not validate application-owned generic containers such as `Paging<T>` or `Window<T>`.

### BYOE Track

The `byoe-response` sample validates a completely user-owned response envelope:

```java
ApiResponse<T>
```

It verifies BYOE support across built-in shapes:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

It also validates OpenAPI Generics 1.2 application-defined generic container support through user-owned container payloads:

```java
ApiResponse<Paging<T>>
ApiResponse<Window<T>>
```

This is the sample that exercises application-owned generic containers such as `Paging<T>` and `Window<T>`.

---

## Design Principles

Each sample follows the same structure:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer
```

The consumer uses only generated artifacts.

No manual DTO reconstruction exists.

No manual envelope reconstruction exists.

No manual generic container reconstruction exists.

No custom deserializers exist.

No adapter-level type conversion exists.

The generated client must reconstruct the original contract shape automatically.

---

## Available Samples

| Sample | Purpose |
|----------|----------|
| `service-response` | Validates the canonical platform-provided `ServiceResponse<T>` contract across scalar, value, object, enum, collection, set, and built-in page payloads. |
| `byoe-response` | Validates Bring Your Own Envelope (BYOE) support using a user-owned `ApiResponse<T>` contract, including built-in shapes and application-owned generic container payloads. |

---

## ServiceResponse Coverage

The `service-response` sample validates the platform-provided envelope:

```java
ServiceResponse<T>
```

This is the canonical OpenAPI Generics response contract.

The sample verifies that the generated client reconstructs the expected `ServiceResponse` shapes without redefining the envelope model.

### Scalar Payloads

```java
ServiceResponse<String>
ServiceResponse<Boolean>
ServiceResponse<Integer>
ServiceResponse<Long>
ServiceResponse<BigDecimal>
```

### Value Payloads

```java
ServiceResponse<UUID>
ServiceResponse<LocalDate>
ServiceResponse<OffsetDateTime>
ServiceResponse<CoverageStatus>
```

### Object Payloads

```java
ServiceResponse<AddressDto>
ServiceResponse<TypeProfileDto>
```

### List Payloads

```java
ServiceResponse<List<TypeSummaryDto>>
ServiceResponse<List<CoverageStatus>>
```

### Set Payloads

```java
ServiceResponse<Set<TypeSummaryDto>>
ServiceResponse<Set<CoverageStatus>>
```

### Built-in Page Payloads

```java
ServiceResponse<Page<TypeSummaryDto>>
ServiceResponse<Page<CoverageStatus>>
```

The purpose of this sample is to verify the baseline projection and reconstruction behavior of the canonical OpenAPI Generics contract.

It validates:

- `ServiceResponse<T>`
- `ServiceResponse<List<T>>`
- `ServiceResponse<Set<T>>`
- `ServiceResponse<Page<T>>`

It does not validate `ServiceResponse<Paging<T>>` or `ServiceResponse<Window<T>>`.

---

## BYOE Coverage

The `byoe-response` sample validates a completely user-owned response envelope:

```java
ApiResponse<T>
```

The response model is owned by the application, not by OpenAPI Generics.

Example shape:

```java
public class ApiResponse<T> {

    private int status;

    private String message;

    private T data;

    private List<ApiError> errors;

}
```

Error model:

```java
public record ApiError(
    String code,
    String message
) {}
```

These classes are not provided by OpenAPI Generics.

OpenAPI Generics projects and reconstructs them while preserving the original application contract.

### Scalar Payloads

```java
ApiResponse<String>
ApiResponse<Boolean>
ApiResponse<Integer>
ApiResponse<Long>
ApiResponse<BigDecimal>
```

### Value Payloads

```java
ApiResponse<UUID>
ApiResponse<LocalDate>
ApiResponse<OffsetDateTime>
ApiResponse<CoverageStatus>
```

### Object Payloads

```java
ApiResponse<AddressDto>
ApiResponse<TypeProfileDto>
```

### List Payloads

```java
ApiResponse<List<TypeSummaryDto>>
ApiResponse<List<CoverageStatus>>
```

### Set Payloads

```java
ApiResponse<Set<TypeSummaryDto>>
ApiResponse<Set<CoverageStatus>>
```

### Built-in Page Payloads

```java
ApiResponse<Page<TypeSummaryDto>>
ApiResponse<Page<CoverageStatus>>
```

### Application-Owned Generic Container Payloads

OpenAPI Generics 1.2 adds support for application-defined generic containers.

The `byoe-response` sample validates this capability using application-owned containers such as:

```java
Paging<T>
Window<T>
```

Covered response shapes include:

```java
ApiResponse<Paging<TypeSummaryDto>>
ApiResponse<Paging<CoverageStatus>>

ApiResponse<Window<TypeSummaryDto>>
ApiResponse<Window<CoverageStatus>>
```

These payloads verify that nested application-owned generic containers are projected into OpenAPI and reconstructed by the generated client without requiring custom serialization, manual mapping, adapter code, or client-side type repair.

They also verify that Java container identity is preserved through projection metadata, including `x-data-container-type`.

The purpose of this sample is to prove that OpenAPI Generics can preserve generic contract semantics even when the response envelope and nested generic containers are entirely application-owned.

---

## What Is Validated

The samples collectively verify:

- OpenAPI projection correctness
- generic response envelope reconstruction
- generated client type safety
- runtime deserialization behavior
- vendor extension processing
- ignored model handling
- external model handling
- scalar and value payload reconstruction
- DTO payload reconstruction
- enum payload reconstruction
- collection reconstruction
- built-in `Page<T>` reconstruction
- BYOE envelope reconstruction
- application-owned generic container reconstruction in the BYOE sample
- container identity preservation through `x-data-container-type`
- consumer compatibility
- backward compatibility
- deterministic generated-source hygiene
- regression protection

The important distinction is that application-owned generic containers are validated by the `byoe-response` sample.

The `service-response` sample remains focused on the canonical `ServiceResponse<T>` contract and its built-in supported shapes.

---

## Regression Strategy

These samples function as executable regression suites.

Their responsibility is to detect failures in:

- response introspection
- projection generation
- vendor extension processing
- `x-api-wrapper` generation
- `x-api-wrapper-datatype` generation
- `x-data-container` generation
- `x-data-container-type` generation
- `x-data-item` generation
- ignored model handling
- external model resolution
- generic wrapper reconstruction
- scalar payload reconstruction
- enum payload reconstruction
- collection reconstruction
- page reconstruction
- BYOE envelope reconstruction
- application-owned generic container reconstruction in BYOE flows
- generated client typing
- generated-source hygiene
- runtime deserialization
- consumer integration

before such failures reach real integration projects.

---

## Mental Model

OpenAPI Generics ultimately guarantees preservation of contract intent.

```text
Original Contract
         ↓
OpenAPI Projection
         ↓
Generated Sources
         ↓
Runtime Reconstruction
         ↓
Consumer Contract
```

For the platform-provided envelope:

```text
ServiceResponse<Page<TypeSummaryDto>>
                ↓
OpenAPI Projection
                ↓
Generated Client
                ↓
Consumer Deserialization
                ↓
ServiceResponse<Page<TypeSummaryDto>>
```

For a BYOE envelope with an application-owned generic container:

```text
ApiResponse<Paging<TypeSummaryDto>>
                ↓
OpenAPI Projection
                ↓
Generated Client
                ↓
Consumer Deserialization
                ↓
ApiResponse<Paging<TypeSummaryDto>>
```

The same reconstruction principle applies across the supported response shapes, but each sample has a specific validation boundary.

`service-response` validates the canonical platform envelope.

`byoe-response` validates a user-owned envelope and the application-owned generic container scenarios introduced in OpenAPI Generics 1.2.

A successful type-coverage sample proves that the consumer receives the same generic contract shape that originally existed in the producer.

This directory exists to continuously verify that guarantee.