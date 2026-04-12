# customer-service-consumer (Spring Boot 4)

> **Reference consumer (SB4): consuming a contract-aligned, generics-aware client with an alternative configuration style (no BYOC mapping)**

---

## 📑 Table of Contents

* 🎯 [What this module shows](#-what-this-module-shows)
* 🧠 [Why this variant exists](#-why-this-variant-exists)
* 🏗️ [Structure](#-structure)
* 🔌 [Integration boundary (unchanged)](#-integration-boundary-unchanged)
* 🧩 [Adapter model](#-adapter-model)
* ⚖️ [Error handling](#-error-handling)
* 🔄 [Contract preservation](#-contract-preservation)
* ⚙️ [Configuration highlights](#-configuration-highlights)
* 🧪 [Verify quickly](#-verify-quickly)
* 🔑 [Key takeaway](#-key-takeaway)
* 🧾 [Summary](#-summary)
* 🛡 [License](#-license)

---

## 🎯 What this module shows

This module demonstrates the **same contract pipeline** as SB3, with one intentional difference:

> It shows a **pure contract reuse setup without explicit BYOC mapping**

```text
Producer → OpenAPI → Generated Client → Adapter → Consumer Service
```

---

## 🧠 Why this variant exists

In SB3 examples, you saw:

```xml
<additionalProperties>
  <additionalProperty>
    openapiGenerics.responseContract.CustomerDto=...
  </additionalProperty>
</additionalProperties>
```

In this SB4 version:

❌ This mapping is NOT used

This demonstrates that:

> Contract alignment can still work **without explicit BYOC configuration**, depending on setup and classpath alignment

---

## 🏗️ Structure

```text
Controller → Service → Client → Adapter → Generated API
```

Same architecture, same guarantees.

---

## 🔌 Integration boundary (unchanged)

The boundary remains:

```text
CustomerServiceClient
```

Responsibilities:

* isolates generated code
* maps requests if needed
* handles exceptions
* preserves `ServiceResponse<T>`

---

## 🧩 Adapter model

Generated client is still NOT used directly.

```java
adapter.getCustomer(customerId)
```

This ensures:

* regeneration safety
* no coupling to generator internals
* stable application layer

---

## ⚖️ Error handling

Same model:

```text
ProblemDetail (RFC 9457)
```

Handled via:

```java
ApiProblemException → mapped to domain exceptions
```

---

## 🔄 Contract preservation

End-to-end contract remains identical:

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

No:

* DTO duplication
* envelope rewriting
* semantic drift

---

## ⚙️ Configuration highlights

### Upstream API

```yaml
customer:
  api:
    base-url: http://localhost:8094/customer-service
```

### Differences vs SB3

* Uses **Spring Boot 4.x**
* Uses newer Springdoc version
* Demonstrates **versioned endpoints (V1)**
* Shows **contract reuse without explicit mapping**

---

## 🧪 Verify quickly

Run consumer:

```bash
mvn spring-boot:run
```

Call:

```bash
curl http://localhost:8095/customer-service-consumer/customers/1
```

Expected:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

---

## 🔑 Key takeaway

There are now **two valid integration styles** in the repo:

### 1. Explicit mapping (BYOC)

* full control
* explicit ownership

### 2. Implicit reuse (this module)

* simpler setup
* fewer config points

Both preserve:

```text
Contract identity → end-to-end
```

---

## 🧾 Summary

```text
Same pipeline
Same guarantees
Different configuration strategy
```

This module exists to show:

> The system is flexible in adoption — not tied to a single configuration path

---

## 🛡 License

MIT License
