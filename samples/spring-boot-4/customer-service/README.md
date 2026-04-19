# customer-service

> Minimal reference producer for **contract-first, generics-aware OpenAPI**

This sample shows only one thing:

> How to expose a Spring Boot API using `ServiceResponse<T>` so that OpenAPI stays deterministic and clients stay aligned.

---

## 🚀 Quick Start

### 1. Add dependency

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

### 2. Use contract in controllers

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

### 3. Run

```bash
mvn spring-boot:run
```

---

## 📌 Rules (only these matter)

✔ Use only:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

✔ `T` is yours (any DTO)

❌ Do NOT:

* create custom envelopes (unless explicitly configured)
* wrap errors → use `ProblemDetail`
* customize OpenAPI manually

---

## 🔍 Verify

```bash
curl http://localhost:8084/customer-service/customers/1
```

Expected shape:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

If this is correct → everything downstream works (OpenAPI → client).

---

## 🌐 OpenAPI

* Swagger UI
  [http://localhost:8094/customer-service/swagger-ui/index.html](http://localhost:8094/customer-service/swagger-ui/index.html)

* YAML
  [http://localhost:8094/customer-service/v3/api-docs.yaml](http://localhost:8094/customer-service/v3/api-docs.yaml)

---

## ⚙️ Config (minimal)

```yaml
server:
  port: 8094
  servlet:
    context-path: /customer-service

spring:
  mvc:
    problemdetails:
      enabled: true

springdoc:
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

---

## 🧩 BYOE (optional)

You can replace the default envelope:

```yaml
openapi-generics:
  envelope:
    type: io.example.ApiResponse
```

Requirements:

* must be generic (`<T>`)
* must have a single payload field
* must be available as a dependency

If not configured → `ServiceResponse<T>` is used.

---

## 🧠 Mental Model

```text
Controller → ServiceResponse<T> → OpenAPI (projection)
```

You define the contract.
The starter projects it deterministically.

---

## 📜 License

MIT
