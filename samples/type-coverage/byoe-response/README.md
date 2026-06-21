# byoe-response-type-coverage

> End-to-end verification sample for OpenAPI projection, client generation, and runtime reconstruction using a custom response envelope.

This sample demonstrates the **Bring Your Own Envelope (BYOE)** capabilities of OpenAPI Generics.

Instead of using the platform-provided envelope implementation, the producer exposes endpoints using a completely user-owned generic response contract.

```java
ApiResponse<T>
```

The sample verifies that OpenAPI Generics can:

- project custom generic envelopes into OpenAPI
- reconstruct generic type information during code generation
- generate strongly typed clients
- deserialize responses back into the original contract shape
- support scalar, value, object, collection, and paged payloads

without requiring changes to the original response contract.

---

# Table of Contents

- [Purpose](#purpose)
- [Architecture](#architecture)
- [Modules](#modules)
- [Custom Envelope](#custom-envelope)
- [Supported Response Shapes](#supported-response-shapes)
- [Verification Matrix](#verification-matrix)
- [Running the Sample](#running-the-sample)
- [Verification Endpoints](#verification-endpoints)
- [BYOE Projection Flow](#byoe-projection-flow)
- [What This Sample Protects](#what-this-sample-protects)

---

# Purpose

The goal of this sample is to validate the complete BYOE lifecycle:

```text
User-Owned Contract
         ↓
Spring Endpoint
         ↓
OpenAPI Projection
         ↓
Generated Client
         ↓
Consumer Runtime
```

The consumer uses only generated client artifacts.

No DTO adapters.

No manual envelope reconstruction.

No custom deserialization code.

The generated client must reconstruct the original generic contract automatically.

---

# Architecture

```text
Producer
    │
    ▼
OpenAPI Projection
    │
    ▼
Generated Client
    │
    ▼
Consumer
```

The producer publishes endpoints using:

```java
ApiResponse<T>
```

The OpenAPI document contains projected wrapper schemas.

The OpenAPI Generics code generator reconstructs the original generic contract.

The generated client returns:

```java
ApiResponse<T>
```

instead of generated wrapper DTOs.

---

# Modules

```text
byoe-response-type-coverage
├── contract
├── producer
├── client
└── consumer
```

| Module | Responsibility |
|----------|----------|
| contract | User-owned ApiResponse contract |
| producer | Publishes ApiResponse-based endpoints |
| client | Generated Java client |
| consumer | Consumes generated client |
| consumer-api | Verification endpoints for runtime testing |

---

# Custom Envelope

The sample uses a completely user-owned envelope.

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

OpenAPI Generics only projects and reconstructs them.

The ownership of the contract remains entirely with the application.

---

# Supported Response Shapes

## Scalar Payloads

```java
ApiResponse<String>

ApiResponse<Boolean>

ApiResponse<Integer>

ApiResponse<Long>

ApiResponse<BigDecimal>
```

---

## Value Payloads

```java
ApiResponse<UUID>

ApiResponse<LocalDate>

ApiResponse<OffsetDateTime>

ApiResponse<CoverageStatus>
```

---

## Object Payloads

```java
ApiResponse<AddressDto>

ApiResponse<TypeProfileDto>
```

Including:

- nested DTOs
- enums
- collections
- maps
- temporal types

---

## Collection Payloads

```java
ApiResponse<List<TypeSummaryDto>>

ApiResponse<List<CoverageStatus>>

ApiResponse<Set<TypeSummaryDto>>

ApiResponse<Set<CoverageStatus>>
```

---

## Paged Payloads

```java
ApiResponse<Page<TypeSummaryDto>>

ApiResponse<Page<CoverageStatus>>
```

Using:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

---

# Verification Matrix

| Category | Verified |
|----------|----------|
| Scalar types | ✓ |
| UUID | ✓ |
| LocalDate | ✓ |
| OffsetDateTime | ✓ |
| Enum payloads | ✓ |
| DTO payloads | ✓ |
| Nested DTO graphs | ✓ |
| List payloads | ✓ |
| Set payloads | ✓ |
| Page payloads | ✓ |
| Generated client reconstruction | ✓ |
| Runtime deserialization | ✓ |
| Consumer integration | ✓ |
| User-owned envelope | ✓ |

---

# Running the Sample

## Start Producer

```bash
cd producer

mvn spring-boot:run
```

Producer URL:

```text
http://localhost:8076/type-coverage/byoe-response
```

Swagger UI:

```text
http://localhost:8076/type-coverage/byoe-response/swagger-ui/index.html
```

OpenAPI document:

```text
http://localhost:8076/type-coverage/byoe-response/v3/api-docs.yaml
```

---

## Start Consumer

```bash
cd consumer

mvn spring-boot:run
```

Consumer URL:

```text
http://localhost:8077/type-coverage/byoe-response-consumer
```

---

## Quick Smoke Test

After starting both producer and consumer, a small subset of endpoints can be called directly to verify that the generated client reconstructs the expected generic response types correctly.

The goal of these requests is not to validate business behavior.

They provide a fast end-to-end sanity check for:

- OpenAPI projection
- custom envelope projection
- generated client generation
- generic wrapper reconstruction
- runtime deserialization
- consumer integration

### Producer Verification

```bash
curl http://localhost:8076/type-coverage/byoe-response/types/scalars/string

curl http://localhost:8076/type-coverage/byoe-response/types/lists/summaries

curl http://localhost:8076/type-coverage/byoe-response/types/sets/statuses

curl http://localhost:8076/type-coverage/byoe-response/types/pages/summaries
```

Expected generic shapes:

```java
ApiResponse<String>

ApiResponse<List<TypeSummaryDto>>

ApiResponse<Set<CoverageStatus>>

ApiResponse<Page<TypeSummaryDto>>
```

### Consumer Verification

```bash
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/scalars/string

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/lists/summaries

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/sets/statuses

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/pages/summaries
```

The consumer uses only generated client artifacts.

No manual DTO mapping, custom deserialization, or envelope reconstruction exists in the consumer application.

Successful responses verify that generic type information survives the complete pipeline:

```text
User-Owned Contract
         ↓
OpenAPI Projection
         ↓
Generated Client
         ↓
Consumer Runtime
```

and is reconstructed back into the original contract shape:

```java
ApiResponse<T>
```

---

# Verification Endpoints

## Scalars

```text
/types/scalars/string

/types/scalars/boolean

/types/scalars/integer

/types/scalars/long

/types/scalars/decimal
```

---

## Values

```text
/types/values/uuid

/types/values/date

/types/values/datetime

/types/values/enum
```

---

## Objects

```text
/types/objects/address

/types/objects/profile
```

---

## Lists

```text
/types/lists/summaries

/types/lists/statuses
```

---

## Sets

```text
/types/sets/summaries

/types/sets/statuses
```

---

## Pages

```text
/types/pages/summaries

/types/pages/statuses
```

---

# BYOE Projection Flow

The producer starts with:

```java
ApiResponse<Page<TypeSummaryDto>>
```

Projection phase:

```text
ApiResponse<Page<TypeSummaryDto>>
            ↓
ApiResponsePageTypeSummaryDto
```

Generated client phase:

```text
ApiResponsePageTypeSummaryDto
            ↓
ApiResponse<Page<TypeSummaryDto>>
```

Consumer phase:

```java
ApiResponse<Page<TypeSummaryDto>>
```

The wrapper DTO exists only as a projection artifact.

The consumer works exclusively with the original generic contract.

---

# What This Sample Protects

This sample serves as a regression suite for BYOE support.

It validates that future changes do not break:

- custom envelope projection
- generic payload reconstruction
- collection reconstruction
- page reconstruction
- generated client typing
- runtime deserialization
- consumer integration

The sample is intentionally designed as an end-to-end verification pipeline rather than a unit-level feature demonstration.

Its purpose is to guarantee that user-owned response contracts continue to work without modification across future OpenAPI Generics releases.