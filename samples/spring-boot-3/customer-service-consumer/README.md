# customer-service-consumer

> **Reference consumer: how a Spring Boot service consumes a contract-aligned, generics-aware client and exposes it safely**

---

## 📑 Table of Contents

* 🎯 [What this module shows](#-what-this-module-shows)
* 🧠 [Key idea](#-key-idea)
* 🏗️ [Structure](#-structure)
* 🔌 [Integration boundary (critical)](#-integration-boundary-critical)
* 🧩 [Adapter usage](#-adapter-usage)
* ⚖️ [Error handling](#-error-handling)
* 🔄 [Contract preservation](#-contract-preservation)
* ⚙️ [Configuration (important parts)](#-configuration-important-parts)
* 🧪 [Verify quickly](#-verify-quickly)
* 🔗 [Related modules](#-related-modules)
* 🧾 [Summary](#-summary)
* 🛡 [License](#-license)

---

## 🎯 What this module shows

This module demonstrates the **final stage of the pipeline**:

```text
Producer → OpenAPI → Generated Client → Adapter → Consumer Service
```

It answers one practical question:

> How do you actually use the generated client inside a real service?

---

## 🧠 Key idea

The consumer does NOT:

* regenerate models
* reinterpret responses
* unwrap contract types

It simply:

```text
Delegates → preserves contract → exposes it
```

---

## 🏗️ Structure

```text
Controller → Service → Client → Adapter → Generated API
```

### Flow

```text
HTTP Request
   ↓
Controller
   ↓
Service
   ↓
CustomerServiceClient (boundary)
   ↓
CustomerClientAdapter (generated client wrapper)
   ↓
Generated OpenAPI client
```

---

## 🔌 Integration boundary (critical)

The **real boundary** is here:

```java
CustomerServiceClient
```

This layer:

* isolates generated code
* handles exceptions
* maps requests if needed
* preserves `ServiceResponse<T>`

---

## 🧩 Adapter usage

Generated client is never used directly.

Instead:

```java
adapter.getCustomer(customerId)
```

This ensures:

* regeneration safety
* stable application code
* no coupling to generator internals

---

## ⚖️ Error handling

Errors follow:

```text
ProblemDetail (RFC 9457)
```

Handled via:

```java
ApiProblemException
```

Mapped into domain exceptions using:

```java
CustomerConsumerExceptionMapper
```

---

## 🔄 Contract preservation

End-to-end shape remains:

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

No:

* DTO duplication
* envelope rewriting
* mapping layers for correctness

---

## ⚙️ Configuration (important parts)

### Upstream API

```yaml
customer:
  api:
    base-url: http://localhost:8084/customer-service
```

### HTTP client behavior

* connection pooling
* timeouts
* explicit configuration

---

## 🧪 Verify quickly

Run consumer:

```bash
mvn spring-boot:run
```

Call:

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

Expected:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

---

## 🔗 Related modules

* **[customer-service](../customer-service/README.md)**
  Producer reference.

* **[customer-service-client](../customer-service-client/README.md)**
  Consumer example showing how the generated client is used.

* **[openapi-generics-java-codegen-parent](../../openapi-generics-java-codegen-parent/README.md)**
  Build-time orchestration.

---

## 🧾 Summary

```text
Generated client is not the boundary
Adapter is the boundary
Contract flows through unchanged
```

---

## 🛡 License

MIT License
