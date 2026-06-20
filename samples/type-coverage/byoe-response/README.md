# byoe-response-type-coverage

> End-to-end verification sample for OpenAPI projection, client generation, and runtime reconstruction using a custom response envelope.

This sample demonstrates the Bring Your Own Envelope (BYOE) capabilities of OpenAPI Generics.

Instead of the platform-provided `ServiceResponse<T>`, the producer exposes endpoints using a user-owned generic envelope:

```java
ApiResponse<T>
```

The sample verifies that OpenAPI Generics can project, reconstruct, and generate clients from an existing response contract without requiring changes to the original model.

---

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Modules](#modules)
- [Custom Envelope](#custom-envelope)
- [Covered Response Shapes](#covered-response-shapes)
- [Running the Sample](#running-the-sample)
- [Verification Endpoints](#verification-endpoints)
- [Mental Model](#mental-model)

---

## What This Sample Validates

The sample exercises the complete BYOE flow:

```text
Custom Contract
       ↓
Producer
       ↓
OpenAPI Projection
       ↓
Generated Client
       ↓
Consumer
```

The generated client is used directly by the consumer application.

No manual wrapper reconstruction or DTO mapping exists inside the consumer.

---

## Modules

```text
byoe-response-type-coverage
├── contract
├── producer
├── client
└── consumer
```

| Module | Responsibility |
|----------|----------------|
| contract | User-owned ApiResponse contract |
| producer | Publishes ApiResponse-based endpoints |
| client | Generated Java client |
| consumer | Uses the generated client |

---

## Custom Envelope

The sample uses a completely user-owned response contract:

```java
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private List<ApiError> errors;

}
```

```java
public record ApiError(
    String code,
    String message
) {}
```

This contract is not provided by OpenAPI Generics.

The platform only projects and reconstructs it.

---

## Covered Response Shapes

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

Including:

- nested DTOs
- collections
- maps
- enums
- temporal types

> [!NOTE]
>
> BYOE currently supports:
>
> ```java
> ApiResponse<T>
> ```
>
> Nested container payloads such as:
>
> ```java
> ApiResponse<Page<T>>
> ApiResponse<List<T>>
> ```
>
> are intentionally outside the current BYOE support scope.

---

## Running the Sample

### Start Producer

```bash
cd producer
mvn spring-boot:run
```

Producer URLs:

```text
http://localhost:8076/type-coverage/byoe-response
```

Swagger UI:

```text
http://localhost:8076/type-coverage/byoe-response/swagger-ui/index.html
```

OpenAPI:

```text
http://localhost:8076/type-coverage/byoe-response/v3/api-docs.yaml
```

### Start Consumer

```bash
cd consumer
mvn spring-boot:run
```

Consumer URL:

```text
http://localhost:8077/type-coverage/byoe-response-consumer
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

These endpoints validate that the generated client correctly reconstructs:

```java
ApiResponse<T>
```

for primitive, value, enum, and DTO payload types.

---

## Mental Model

```text
ApiResponse<T>
        ↓
OpenAPI Projection
        ↓
Generated Client
        ↓
Consumer Deserialization
```

The purpose of this sample is to verify that BYOE support remains stable across platform changes.