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
- [Mental model](#-mental-model)
- [Summary](#-summary)

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
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>0.9.0</version>
</dependency>
```

### 2) Return contract types

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
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

There is **one canonical success envelope**:

```text
ServiceResponse<T>
```

Supported shapes:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

This constraint enables:

* deterministic schema naming
* stable projection
* type-safe client reconstruction

---

## 📦 Minimal dependencies

No configuration required.

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

Requirements:

* Spring Boot (WebMVC)
* OpenAPI endpoint (`/v3/api-docs`) via springdoc

---

## ✍️ What you actually write

You write **only your domain contract**.

### Example

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
  return ResponseEntity.ok(ServiceResponse.of(dto));
}
```

### Pagination

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...) {
  return ResponseEntity.ok(ServiceResponse.of(page));
}
```

That’s it.

No:

* annotations
* schema config
* wrapper DTOs

---

## 🧠 What gets published to OpenAPI

From:

```java
ServiceResponse<CustomerDto>
```

The system produces:

```text
ServiceResponseCustomerDto
```

Characteristics:

* deterministic naming
* `allOf` composition
* vendor extensions for generation

Example extensions:

```text
x-api-wrapper
x-data-container
x-data-item
x-ignore-model
```

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
Contract-aware introspection
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

### Key properties

* single orchestrator (no ordering issues)
* no patching / no overrides
* deterministic output

---

## ⚠️ Rules (do NOT break these)

These are **hard architectural constraints**.

### 1. Only use the canonical envelope

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

---

### 2. Do NOT replace the envelope

❌ Wrong:

```text
CustomerResponse
ApiResponse
PagedResult
```

Breaks determinism and cross-layer alignment.

---

### 3. Payload is fully flexible

✔ Valid:

```text
ServiceResponse<CustomerDto>
ServiceResponse<Anything>
```

The system constrains structure — not domain.

---

### 4. Errors are NOT wrapped

```text
ProblemDetail (RFC 9457)
```

Separate protocol.

---

### 5. Do NOT customize OpenAPI

No:

* manual schemas
* annotations
* overrides

The starter owns projection.

---

## 🔍 Quick verification

```bash
curl http://localhost:8084/.../v1/.../1
```

Expected:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

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

## 🧠 Mental model

Think of the server as:

> A deterministic compiler from runtime contract → OpenAPI

Not:

* a schema designer
* a customization layer

---

## 🧾 Summary

```text
Input   = Java contract
Process = projection pipeline
Output  = deterministic OpenAPI
```

The system works because:

* contract is never redefined
* projection is deterministic
* downstream generation is predictable

---

🛡 MIT License