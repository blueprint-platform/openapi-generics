---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-Side Adoption — Contract-First OpenAPI Publication

> Publish a **deterministic, generics-aware OpenAPI** from Spring Boot with **one contract and zero duplication**.

This is a **contract-first projection system**, not a documentation tool.

You define your contract in Java.  
The platform guarantees a **stable, generator-ready OpenAPI projection**.

---

## Contents

- [60-second quick start](#-60-second-quick-start)
- [What the server actually does](#-what-the-server-actually-does)
- [The only rule that matters](#-the-only-rule-that-matters)
- [Minimal dependencies](#-minimal-dependencies)
- [What you actually write](#-what-you-actually-write)
- [What gets published to OpenAPI](#-what-gets-published-to-openapi)
- [Projection pipeline (what really happens)](#-projection-pipeline-what-really-happens)
- [Rules (do NOT break these)](#-rules-do-not-break-these)
- [Quick verification](#-quick-verification)
- [Samples](#-samples-recommended)

---

## ⚡ 60-second quick start

You want:

* deterministic OpenAPI output
* no envelope duplication
* generics preserved in generated clients

Do this:

### 1) Add dependency

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 2) Return contract types

Default envelope:

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

Or bring your own envelope (BYOE):

```java
ApiResponse<CustomerDto>
```

(Optional configuration for BYOE)

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

### 3) Expose OpenAPI

```text
/v3/api-docs.yaml
```

Done.

---

## 🎯 What the server actually does

The server has **one responsibility**:

> Project the runtime Java contract into a **deterministic OpenAPI representation**.

It does **not**:

* generate clients
* define alternative models
* adapt output for specific generators

It only performs:

```text
Java Contract → OpenAPI (projection)
```

Everything else happens downstream.

---

## 🧩 The only rule that matters

There is **one canonical success model** — but not a single fixed type.

```text
YourEnvelope<T>
```

> `ServiceResponse<T>` is the **default envelope provided by the platform**.

Supported shapes (deterministic scope):

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
YourEnvelope<T>
```

Constraints:

* exactly **one direct generic payload** must exist
* nested generic payloads in custom envelopes are **not supported**

This constraint enables:

* deterministic schema naming
* stable projection
* type-safe client reconstruction

---

## 📦 Minimal dependencies

No configuration required.

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

Requirements:

* Spring Boot (WebMVC)
* OpenAPI endpoint (`/v3/api-docs`) via springdoc

---

## ✍️ What you actually write

You write **only your domain contract**.

### Default (platform-provided envelope)

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
  return ResponseEntity.ok(ServiceResponse.of(dto));
}
```

### Pagination (default envelope)

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...) {
  return ResponseEntity.ok(ServiceResponse.of(page));
}
```

---

### Using your own envelope (BYOE)

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(...) {
  return ResponseEntity.ok(ApiResponse.success(dto));
}
```

Configuration:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

---

That’s it.

No:

* annotations
* schema config
* wrapper DTOs

The platform detects the envelope and projects it into OpenAPI deterministically.

---

## 🧠 What gets published to OpenAPI

From:

```java
ServiceResponse<CustomerDto>
```

or (BYOE):

```java
ApiResponse<CustomerDto>
```

The system produces a **deterministic wrapper schema**:

```text
ServiceResponseCustomerDto
ApiResponseCustomerDto
```

Characteristics:

* deterministic naming (based on envelope + payload type)
* `allOf` composition
* vendor extensions for generation

Example extensions:

```text
x-api-wrapper
x-api-wrapper-datatype
x-data-container
x-data-item
x-ignore-model
```

Notes:

* The **envelope itself is NOT generated** — it is treated as an external contract
* Wrapper schemas exist only to **bind OpenAPI responses to the generic contract shape**
* The same mechanism applies to both default and custom envelopes

> OpenAPI is a **projection artifact**, not the source of truth.

---

## 🔁 Projection pipeline (what really happens)

The server is not "configurable".
It is a **fixed execution pipeline**.

```text
Controller return types
        ↓
Response type discovery
        ↓
Contract-aware introspection (envelope + payload)
        ↓
Base schema registration
        ↓
Wrapper schema generation
        ↓
Container enrichment
        ↓
Duplicate model marking
        ↓
Contract validation (fail-fast)
        ↓
OpenAPI output
```
---

### What changed with BYOE

* The pipeline dynamically resolves the **envelope type** (default or external)
* Payload slot is discovered via **introspection rules** (single direct generic)
* The same projection logic applies regardless of envelope origin

---

### Key properties

* single orchestrator (no ordering issues)
* no patching / no overrides
* deterministic output
* envelope-agnostic (default or BYOE uses the same pipeline)

---

## ⚠️ Rules (do NOT break these)

These are **hard architectural constraints**.

### 1. Use a single generic envelope model

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
YourEnvelope<T>
```

Constraints:

* exactly **one direct generic payload** must exist
* nested generic payloads in custom envelopes are **not supported**

---

### 2. You MAY use your own envelope (BYOE)

✔ Valid:

```text
ApiResponse<T>
CustomEnvelope<T>
```

Conditions:

* must be a **concrete class**
* must declare **exactly one type parameter**
* must contain **exactly one direct payload field of type T**

Violations (invalid):

```text
ApiResponse<Page<T>>      ❌ nested generic payload
ApiResponse<T, M>         ❌ multiple type parameters
WrapperWithoutPayload     ❌ no payload field
```

---

### 3. Payload is fully flexible

✔ Valid:

```text
ServiceResponse<CustomerDto>
YourEnvelope<Anything>
```

The system constrains structure — not domain.

---

### 4. Error model is NOT enforced by the platform

Error handling is **not dictated by the starter**.

It depends entirely on the **contract model defined by the service**.

Two common patterns:

#### 1. Separate error protocol (recommended default)

```text
Success → ServiceResponse<T>
Error   → ProblemDetail (RFC 9457)
```

#### 2. Envelope-based error model (custom / BYOE)

```text
Success → YourEnvelope<T>
Error   → YourEnvelope<T> (e.g. errors field)
```

#### Key point

```text
The platform enforces success envelope structure,
but does not define or restrict error semantics.
```

---

### 5. Do NOT customize OpenAPI

No:

* manual schemas (when using Springdoc mode)
* annotations for schema shaping
* overrides of generated structure

The starter owns projection.

---

## 🔍 Quick verification

```bash
curl http://localhost:8084/.../v1/.../1
```

Expected structure:

```json
{
  "<payload-field>": { ... }
}
```

Examples:

Default envelope (`ServiceResponse`):

```json
{
  "data": { ... },
  "meta": { ... }
}
```

Custom envelope (BYOE):

```json
{
  "data": { ... },
  "status": 200,
  "message": "OK"
}
```

Key check:

* exactly **one payload field** carries the response data
* envelope structure matches your contract (default or custom)

If correct:

```text
Server → OpenAPI → Client is aligned
```

---

## 📦 Samples (recommended)

Full end-to-end examples are provided:

* Spring Boot 3 samples
* Spring Boot 4 samples
* client generation examples
* consumer services

These demonstrate:

* how the contract is produced
* how the client is generated
* how it is consumed safely

> If anything is unclear, inspect samples instead of guessing.

---

🛡 MIT License