---
layout: default
title: Home
nav_order: 1
has_toc: false
---

# OpenAPI Generics — Keep Your API Contract Intact End-to-End
> Define your API once in Java.
> Preserve it across OpenAPI and generated clients — without duplication or drift.

---

## 📑 Contents

- [Why this exists](#why-this-exists)
- [What you actually do](#what-you-actually-do)
- [Quick Start](#quick-start)
- [Result](#result)
- [Compatibility Matrix](#compatibility-matrix)
- [Proof — Generated Client (Before vs After)](#proof--generated-client-before-vs-after)
- [What changed](#what-changed)
- [What is actually generated](#what-is-actually-generated)
- [How you actually use it](#how-you-actually-use-it)
- [What this gives you](#what-this-gives-you)
- [Why this matters](#why-this-matters)
- [Mental model](#mental-model)
- [Next steps](#next-steps)
- [References](#references--external-links)

---

## Why this exists

In most OpenAPI-based workflows:

* generics are flattened or lost
* response envelopes are regenerated per endpoint
* shared models are duplicated on the client side
* clients gradually drift from server-side contracts

Over time, this creates a gap between what your API **defines** and what your clients **consume**.

The result is not an immediate failure — but a slow erosion of contract integrity.

This platform removes that entire class of problems.

> Your Java contract remains the single source of truth — across all layers.

---

## What you actually do

You don’t configure OpenAPI.
You don’t maintain templates.
You don’t fight generator behavior.

You only do three things:

1. return your contract from controllers
2. optionally reuse your own shared contract models on the client side
3. generate clients from OpenAPI

That’s it.

The platform handles projection, generation, and contract alignment automatically.

---

## Quick Start

### 1. Server (producer)

Add the dependency:

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>0.9.0</version>
</dependency>
```

Return your contract:

```java
ServiceResponse<CustomerDto>
```

---

### 2. Client (consumer)

Inherit the parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.9.0</version>
</parent>
```

Optionally declare externally owned shared contract models:

```xml
 <!-- Map your DTOs to existing contract classes -->
<additionalProperties>
    <additionalProperty>openapiGenerics.responseContract.CustomerDto=io.github.blueprintplatform.contracts.customer.CustomerDto</additionalProperty>
</additionalProperties>
```

Generate the client:

```bash
mvn clean install
```

---

## Result

```java
ServiceResponse<CustomerDto>
```

The exact same contract shape flows from server to client.

* no duplicated envelope models
* generics preserved end-to-end
* external contract models reused instead of regenerated

---

## Compatibility Matrix

### Runtime (Server)

| Component         | Supported Versions          |
| ----------------- |-----------------------------|
| Java              | 17+                         |
| Spring Boot       | 3.4.x, 3.5.x, 4.x           |
| springdoc-openapi | 2.8.x, 3.x (WebMvc starter) |

### Build-time (Client Generation)

| Component         | Supported Versions |
| ----------------- | ------------------ |
| OpenAPI Generator | 7.x                |

---

## Proof — Generated Client (Before vs After)

### Before (default OpenAPI behavior)

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-before.png" width="700"/>
</p>

* duplicated envelope per endpoint
* generics flattened or lost
* unstable and verbose model graph

---

### After (contract-aligned generation)

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-after.png" width="700"/>
</p>

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

* no envelope duplication
* generics preserved end-to-end
* externally owned contract models reused directly

---

## What changed

Instead of generating new models from OpenAPI:

* the server projects the Java contract into OpenAPI deterministically
* external contract models can be explicitly mapped and reused
* wrappers are generated as thin type bindings
* the generator enforces contract alignment instead of passively materializing schemas

Result:

```text
Java Contract (SSOT)
        ↓
OpenAPI (projection, not authority)
        ↓
Generator (deterministic reconstruction)
        ↓
Client (contract-aligned types)
```

No reinterpretation.
No duplication.
No drift.

---

## What is actually generated

The client does **not** recreate your models.

If you provide shared contract models, they are reused directly via explicit mapping. If you do not, the platform still preserves the generic response structure and generates the required wrappers deterministically.

Instead, the generator produces **thin wrapper classes** that bind OpenAPI responses back to your canonical contract shape.

Example:

```java
public class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto> {
}
```

```java
public class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>> {
}
```

Key properties:

* no envelope duplication
* no structural redefinition
* no generic type loss
* shared contract reuse is supported, but not mandatory

These classes exist only to bridge OpenAPI → Java type system.

---

## How you actually use it

You never interact with generated wrappers directly.

Instead, you define an adapter boundary:

```java
public interface CustomerClientAdapter {

  ServiceResponse<CustomerDto> createCustomer(CustomerCreateRequest request);

  ServiceResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceResponse<Page<CustomerDto>> getCustomers();

}
```

Implementation delegates to the generated API:

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return api.getCustomers(null, null, 0, 5, "customerId", "ASC");
  }
}
```

If you choose to keep shared DTO ownership outside the generated client, this adapter boundary is also where you isolate generated request/response details from the rest of your application.

---

## What this gives you

At usage level, your application works with a single, stable contract shape:

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

You do **not** have to deal with common issues produced by default OpenAPI generation:

* duplicated envelope classes (one per endpoint)
* flattened generic types (loss of `Page<T>` / `ServiceResponse<T>` semantics)
* unstable model graphs (frequent diffs from minor changes)
* generator-specific model hierarchies (e.g., `InlineResponse200`, `CustomerDtoResponse`)

If you reuse shared contract DTOs, the system also avoids regenerating models you already own.

- No reinterpretation
- No duplication
- No drift

---

## Why this matters

Traditional OpenAPI generation often produces:

* duplicated response envelopes
* flattened generics
* unstable model graphs
* regenerated shared models that the client should not own

This approach guarantees:

* a single contract shape shared across all layers
* stable and predictable client generation
* optional reuse of shared external contract models
* zero drift between server and client semantics

---

## Mental model

Think of generated classes as:

> thin type adapters — not models

They exist because OpenAPI cannot express Java generics directly — not because your domain model requires duplication.

When shared contract models are provided, generated wrappers reference them directly. When they are not, the platform still preserves the success-envelope structure deterministically.

Your system always operates around:

```text
ServiceResponse<T>
```

Everything else is infrastructure.

---

## Next steps

* [Server Adoption](adoption/server-side-adoption.md)
* [Client Adoption](adoption/client-side-adoption.md)

---

## References & External Links

* 🌐 **GitHub Repository** — [openapi-generics](https://github.com/blueprint-platform/openapi-generics)
* 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

## Final note

If the contract stays consistent, everything stays consistent.

This system works by keeping that boundary intact.
