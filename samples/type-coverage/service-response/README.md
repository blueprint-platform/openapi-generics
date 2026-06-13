# service-response-type-coverage

> End-to-end reference sample for validating projection and client reconstruction of the canonical `ServiceResponse<T>` contract.

This sample exists to verify how different payload types are projected into OpenAPI and reconstructed by the OpenAPI Generics platform.

It is intentionally not a business application.

Its purpose is to provide a deterministic environment for validating OpenAPI projection, generated client behavior, wrapper reconstruction, vendor extensions, and regression scenarios.

---

## Architecture

The sample demonstrates the complete OpenAPI Generics flow:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer
```

The consumer does not manually map response payloads.

All responses are deserialized directly into the generated generic-aware client types produced by the platform.

---

## Modules

```text
service-response-type-coverage
├── producer
│   └── Exposes ServiceResponse<T> endpoints
│
├── client
│   └── Generated Java client produced from the projected OpenAPI contract
│
└── consumer
    └── Uses the generated client to call the producer
```

---

## What this sample covers

The producer exposes endpoints using the canonical response shapes:

```java
ServiceResponse<T>

ServiceResponse<Page<T>>
```

and exercises multiple payload categories.

### Scalar payloads

```java
ServiceResponse<String>
ServiceResponse<Boolean>
ServiceResponse<Integer>
ServiceResponse<Long>
ServiceResponse<BigDecimal>
```

### Value payloads

```java
ServiceResponse<UUID>
ServiceResponse<LocalDate>
ServiceResponse<OffsetDateTime>
ServiceResponse<CoverageStatus>
```

### Object payloads

```java
ServiceResponse<AddressDto>
ServiceResponse<TypeProfileDto>
```

Including:

- nested DTOs
- collections
- maps
- enums
- temporal types

### Paged payloads

```java
ServiceResponse<Page<TypeSummaryDto>>

ServiceResponse<Page<CoverageStatus>>
```

Including:

- DTO item types
- enum item types
- nested generic reconstruction

---

## Sample Payload Categories

### Scalars

```text
GET /types/scalars/string
GET /types/scalars/boolean
GET /types/scalars/integer
GET /types/scalars/long
GET /types/scalars/decimal
```

### Values

```text
GET /types/values/uuid
GET /types/values/date
GET /types/values/datetime
GET /types/values/enum
```

### Objects

```text
GET /types/objects/address
GET /types/objects/profile
```

### Pages

```text
GET /types/pages/summaries
GET /types/pages/statuses
```

---

## Quick Start

### Run producer

```bash
cd producer
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8074/type-coverage/service-response
```

### Run consumer

```bash
cd consumer
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8075/type-coverage/service-response-consumer
```

---

## Verify Producer

### OpenAPI

Swagger UI:

```text
http://localhost:8074/type-coverage/service-response/swagger-ui/index.html
```

YAML:

```text
http://localhost:8074/type-coverage/service-response/v3/api-docs.yaml
```

---

## Verify Consumer

The consumer calls the producer through the generated client and returns the deserialized response.

### Scalar payload

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/scalars/string
```

Expected shape:

```json
{
  "data": "type-coverage",
  "meta": {}
}
```

### Enum payload

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/values/enum
```

Expected shape:

```json
{
  "data": "EXPERIMENTAL",
  "meta": {}
}
```

### Complex DTO payload

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/objects/profile
```

Validates:

- nested DTOs
- collections
- maps
- enums
- LocalDate
- OffsetDateTime

### Paged DTO payload

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/pages/summaries
```

Validates:

```java
ServiceResponse<Page<TypeSummaryDto>>
```

### Paged enum payload

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/pages/statuses
```

Validates:

```java
ServiceResponse<Page<CoverageStatus>>
```

This is one of the most important scenarios because it exercises:

```java
ServiceResponse<T>

Page<T>

Enum
```

simultaneously.

---

## What is being validated

The sample verifies that OpenAPI Generics can correctly project and reconstruct:

### Scalars

```java
String
Boolean
Integer
Long
BigDecimal
```

### Value Types

```java
UUID
LocalDate
OffsetDateTime
Enum
```

### Objects

```java
DTO
Nested DTO
Collection
Map
```

### Nested Generics

```java
ServiceResponse<Page<DTO>>

ServiceResponse<Page<Enum>>
```

---

## Why this sample exists

When investigating projection behavior or generated client output, it is useful to isolate payload handling from business concerns.

This sample provides a reproducible environment that can be used to:

- inspect generated OpenAPI schemas
- validate vendor extensions
- verify wrapper generation
- verify enum handling
- validate generic reconstruction
- reproduce code generation issues
- validate platform regressions
- test generated client interoperability

---

## Mental Model

```text
Contract Type
      ↓
ServiceResponse<T>
      ↓
OpenAPI Projection
      ↓
Vendor Extensions
      ↓
Generated Client
      ↓
Consumer Deserialization
```

The sample focuses exclusively on validating this flow.