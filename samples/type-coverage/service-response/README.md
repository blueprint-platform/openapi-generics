# service-response-type-coverage

> End-to-end verification sample for OpenAPI projection, client generation, and runtime reconstruction of `ServiceResponse<T>` contracts.

This sample is intentionally focused on type coverage.

It verifies that response types published by a producer service can be projected into OpenAPI, reconstructed by the generated client, and consumed without losing generic type information.

---

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Modules](#modules)
- [Covered Response Shapes](#covered-response-shapes)
- [Running the Sample](#running-the-sample)
- [Verification Endpoints](#verification-endpoints)
- [Mental Model](#mental-model)

---

## What This Sample Validates

The sample exercises the complete OpenAPI Generics flow:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer
```

The generated client is used directly by the consumer application.

No manual DTO mapping or wrapper reconstruction exists inside the consumer.

---

## Modules

```text
service-response-type-coverage
├── producer
│
├── client
│
└── consumer
```

| Module | Responsibility |
|----------|----------------|
| producer | Publishes ServiceResponse-based endpoints |
| client | Generated Java client |
| consumer | Uses the generated client and exposes verification endpoints |

---

## Covered Response Shapes

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


### Page Payloads

```java
ServiceResponse<Page<TypeSummaryDto>>
ServiceResponse<Page<CoverageStatus>>
```

---

## Running the Sample

### Start Producer

```bash
cd producer
mvn spring-boot:run
```

Producer URLs:

```text
http://localhost:8074/type-coverage/service-response
```

Swagger UI:

```text
http://localhost:8074/type-coverage/service-response/swagger-ui/index.html
```

OpenAPI:

```text
http://localhost:8074/type-coverage/service-response/v3/api-docs.yaml
```

### Start Consumer

```bash
cd consumer
mvn spring-boot:run
```

Consumer URL:

```text
http://localhost:8075/type-coverage/service-response-consumer
```

---

## Quick Smoke Test

After starting both producer and consumer, a small subset of endpoints can be called directly to verify that the generated client reconstructs the expected generic response types correctly.

The goal of these requests is not to validate business behavior.

They provide a fast end-to-end sanity check for:

- OpenAPI projection
- generated client generation
- generic wrapper reconstruction
- runtime deserialization
- consumer integration

### Producer Verification

```bash
curl http://localhost:8074/type-coverage/service-response/types/scalars/string

curl http://localhost:8074/type-coverage/service-response/types/lists/summaries

curl http://localhost:8074/type-coverage/service-response/types/sets/statuses

curl http://localhost:8074/type-coverage/service-response/types/pages/summaries
```

Expected generic shapes:

```java
ServiceResponse<String>

ServiceResponse<List<TypeSummaryDto>>

ServiceResponse<Set<CoverageStatus>>

ServiceResponse<Page<TypeSummaryDto>>
```

### Consumer Verification

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/scalars/string

curl http://localhost:8075/type-coverage/service-response-consumer/types/lists/summaries

curl http://localhost:8075/type-coverage/service-response-consumer/types/sets/statuses

curl http://localhost:8075/type-coverage/service-response-consumer/types/pages/summaries
```

The consumer uses only generated client artifacts.

No manual DTO mapping or wrapper reconstruction exists in the consumer application.

Successful responses verify that generic type information survives the complete pipeline:

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

## Verification Endpoints

### Scalars

```text
/types/scalars/string
/types/scalars/boolean
/types/scalars/integer
/types/scalars/long
/types/scalars/decimal
```

### Values

```text
/types/values/uuid
/types/values/date
/types/values/datetime
/types/values/enum
```

### Objects

```text
/types/objects/address
/types/objects/profile
```

### Lists

```text
/types/lists/summaries
/types/lists/statuses
```

### Sets

```text
/types/sets/summaries
/types/sets/statuses
```

### Pages

```text
/types/pages/summaries
/types/pages/statuses
```

These endpoints validate that the generated client correctly reconstructs:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

for primitive, value, enum, DTO, list, set, and paged payload types.

---

## Mental Model

```text
ServiceResponse<T>
        ↓
OpenAPI Projection
        ↓
Generated Client
        ↓
Consumer Deserialization
```

The purpose of this sample is to verify that this flow remains stable across platform changes.