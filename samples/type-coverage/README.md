# type-coverage

Focused end-to-end validation samples for OpenAPI Generics.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture, business workflows, or framework usage patterns.

Their purpose is to provide deterministic validation environments for verifying the core OpenAPI Generics pipeline and preventing regressions across future releases.

The type-coverage samples validate both the platform-provided `ServiceResponse<T>` envelope and user-owned Bring Your Own Envelope (BYOE) contracts, including scalar payloads, value payloads, DTO payloads, collections, built-in pages, and application-owned generic containers.

---

## Table of Contents

- [Purpose](#purpose)
- [What Is Validated](#what-is-validated)
- [Design Principles](#design-principles)
- [Available Samples](#available-samples)
- [Covered Contract Shapes](#covered-contract-shapes)
- [Scalar Payloads](#scalar-payloads)
- [Value Payloads](#value-payloads)
- [Object Payloads](#object-payloads)
- [Collection Payloads](#collection-payloads)
- [Built-in Page Payloads](#built-in-page-payloads)
- [Application-Owned Generic Container Payloads](#application-owned-generic-container-payloads)
- [BYOE Coverage](#byoe-coverage)
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

## What Is Validated

The samples collectively verify:

- OpenAPI projection correctness
- generic response envelope reconstruction
- generated client type safety
- runtime deserialization behavior
- vendor extension processing
- external model handling
- ignored model handling
- envelope reconstruction
- scalar and value payload reconstruction
- DTO payload reconstruction
- enum payload reconstruction
- collection reconstruction
- built-in `Page<T>` reconstruction
- application-owned generic container reconstruction
- container identity preservation through `x-data-container-type`
- consumer compatibility
- backward compatibility
- deterministic generated-source hygiene
- regression protection

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
| `service-response` | Validates the canonical platform-provided `ServiceResponse<T>` contract across scalar, value, object, enum, collection, page, and application-owned generic container payloads. |
| `byoe-response` | Validates Bring Your Own Envelope (BYOE) support using a completely user-owned `ApiResponse<T>` contract across the same response shape matrix. |

---

## Covered Contract Shapes

The type-coverage samples validate the same reconstruction model across both built-in and user-owned envelopes.

### Platform Envelope

```java
ServiceResponse<T>
```

### User-Owned Envelope

```java
ApiResponse<T>
```

Both envelopes are validated against the following payload categories.

---

## Scalar Payloads

```java
ServiceResponse<String>
ServiceResponse<Boolean>
ServiceResponse<Integer>
ServiceResponse<Long>
ServiceResponse<BigDecimal>

ApiResponse<String>
ApiResponse<Boolean>
ApiResponse<Integer>
ApiResponse<Long>
ApiResponse<BigDecimal>
```

These scenarios validate that simple scalar values are projected and reconstructed without losing the generic response envelope.

---

## Value Payloads

```java
ServiceResponse<UUID>
ServiceResponse<LocalDate>
ServiceResponse<OffsetDateTime>
ServiceResponse<CoverageStatus>

ApiResponse<UUID>
ApiResponse<LocalDate>
ApiResponse<OffsetDateTime>
ApiResponse<CoverageStatus>
```

These scenarios validate value-object style payloads, date/time payloads, and enum payloads.

Enum coverage also protects the reusable OpenAPI component requirement for generic enum payload reconstruction.

---

## Object Payloads

```java
ServiceResponse<AddressDto>
ServiceResponse<TypeProfileDto>

ApiResponse<AddressDto>
ApiResponse<TypeProfileDto>
```

These scenarios validate DTO payload reconstruction and external model handling.

The generated client must reuse or reconstruct the expected DTO types without introducing unnecessary mapping layers.

---

## Collection Payloads

```java
ServiceResponse<List<TypeSummaryDto>>
ServiceResponse<List<CoverageStatus>>

ServiceResponse<Set<TypeSummaryDto>>
ServiceResponse<Set<CoverageStatus>>

ApiResponse<List<TypeSummaryDto>>
ApiResponse<List<CoverageStatus>>

ApiResponse<Set<TypeSummaryDto>>
ApiResponse<Set<CoverageStatus>>
```

These scenarios validate collection-aware projection and reconstruction.

The generated client must preserve both the response envelope and the collection payload type.

---

## Built-in Page Payloads

```java
ServiceResponse<Page<TypeSummaryDto>>
ServiceResponse<Page<CoverageStatus>>

ApiResponse<Page<TypeSummaryDto>>
ApiResponse<Page<CoverageStatus>>
```

These scenarios validate reconstruction of the built-in OpenAPI Generics `Page<T>` contract.

The generated client must preserve the page container instead of flattening or redefining the paged response structure.

---

## Application-Owned Generic Container Payloads

OpenAPI Generics 1.2 adds support for application-defined generic containers.

The type-coverage samples validate nested application-owned containers such as:

```java
Paging<T>
Window<T>
```

Covered response shapes include:

```java
ServiceResponse<Paging<TypeSummaryDto>>
ServiceResponse<Paging<CoverageStatus>>

ServiceResponse<Window<TypeSummaryDto>>
ServiceResponse<Window<CoverageStatus>>

ApiResponse<Paging<TypeSummaryDto>>
ApiResponse<Paging<CoverageStatus>>

ApiResponse<Window<TypeSummaryDto>>
ApiResponse<Window<CoverageStatus>>
```

These scenarios verify that application-owned generic containers are projected into OpenAPI and reconstructed by the generated client without custom serialization, mapping, adapter code, or client-side type repair.

They also verify that Java container identity is preserved through projection metadata, including `x-data-container-type`.

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

OpenAPI Generics must project and reconstruct this contract without requiring changes to the original response model.

The sample verifies BYOE reconstruction across:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
ApiResponse<Paging<T>>
ApiResponse<Window<T>>
```

The purpose is to prove that OpenAPI Generics can preserve generic contract semantics even when the response envelope and nested generic containers are entirely application-owned.

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
- application-owned generic container reconstruction
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

For example:

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

The same reconstruction flow applies to every supported response shape, including platform-owned envelopes, user-owned envelopes, built-in containers, collections, pages, and application-owned generic payload containers.

A successful type-coverage sample proves that the consumer receives the same generic contract shape that originally existed in the producer.

This directory exists to continuously verify that guarantee.