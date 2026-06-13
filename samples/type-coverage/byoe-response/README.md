# byoe-response-type-coverage

> End-to-end reference sample for validating projection and client reconstruction using a custom user-defined response envelope.

This sample demonstrates the Bring Your Own Envelope (BYOE) capabilities of OpenAPI Generics.

Instead of using the platform-provided `ServiceResponse<T>` contract, the producer exposes endpoints using a completely custom response wrapper:

```java
ApiResponse<T>
```

The sample validates that OpenAPI Generics can project, reconstruct, and generate clients from a user-owned generic envelope without requiring changes to the original contract model.

---

## Architecture

The sample demonstrates the complete OpenAPI Generics BYOE flow:

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

The consumer does not manually map response payloads.

All responses are deserialized directly into the generated generic-aware client types produced by the platform.

---

## Modules

```text
byoe-response-type-coverage
├── contract
│   └── Custom ApiResponse<T> contract
│
├── producer
│   └── Exposes ApiResponse<T> endpoints
│
├── client
│   └── Generated Java client produced from the projected OpenAPI contract
│
└── consumer
    └── Uses the generated client to call the producer
```

---

## Custom Envelope

The sample uses a user-defined response contract:

```java
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private List<ApiError> errors;

}
```

with:

```java
public record ApiError(
    String code,
    String message
) {}
```

This contract is owned entirely by the application and is not part of OpenAPI Generics.

The platform only projects and reconstructs it.

---

## What this sample covers

The producer exposes endpoints using:

```java
ApiResponse<T>
```

and exercises multiple payload categories.

### Scalar payloads

```java
ApiResponse<String>
ApiResponse<Boolean>
ApiResponse<Integer>
ApiResponse<Long>
ApiResponse<BigDecimal>
```

### Value payloads

```java
ApiResponse<UUID>
ApiResponse<LocalDate>
ApiResponse<OffsetDateTime>
ApiResponse<CoverageStatus>
```

### Object payloads

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

> [!NOTE]
> BYOE type coverage intentionally covers `ApiResponse<T>` only.
>
> Nested container payloads such as:
>
> ```java
> ApiResponse<Page<T>>
> ```
>
> are outside the current BYOE support scope and are therefore not included in this sample.

---

## Quick Start

### Run producer

```bash
cd producer
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8076/type-coverage/byoe-response
```

### Run consumer

```bash
cd consumer
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8077/type-coverage/byoe-response-consumer
```

---

## Verify Producer

### OpenAPI

Swagger UI:

```text
http://localhost:8076/type-coverage/byoe-response/swagger-ui/index.html
```

YAML:

```text
http://localhost:8076/type-coverage/byoe-response/v3/api-docs.yaml
```

---

## Verify Consumer

The consumer calls the producer through the generated client and returns the deserialized response.

### Scalar payload

```bash
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/scalars/string
```

Expected shape:

```json
{
  "status": 200,
  "message": "OK",
  "data": "type-coverage",
  "errors": []
}
```

### Enum payload

```bash
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/values/enum
```

Expected shape:

```json
{
  "status": 200,
  "message": "OK",
  "data": "EXPERIMENTAL",
  "errors": []
}
```

### Complex DTO payload

```bash
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/objects/profile
```

Validates:

- nested DTOs
- collections
- maps
- enums
- LocalDate
- OffsetDateTime

---

## What is being validated

The sample verifies that OpenAPI Generics can correctly project and reconstruct a custom envelope containing:

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

### Custom Envelope

```java
ApiResponse<T>
```

including:

```java
status
message
data
errors
```

without requiring any modifications to the original contract implementation.

---

## Why this sample exists

Most organizations already have an established response envelope.

Replacing those contracts is often not feasible.

This sample demonstrates that OpenAPI Generics can operate on top of an existing response model and still provide:

- OpenAPI projection
- generic type reconstruction
- generated client compatibility
- wrapper generation
- vendor extension support
- contract ownership preservation

without forcing adoption of the platform's own envelope model.

---

## Mental Model

```text
User Contract
      ↓
ApiResponse<T>
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