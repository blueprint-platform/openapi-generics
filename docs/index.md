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

## Contents

- [Why this exists](#why-this-exists)
- [What you actually do](#what-you-actually-do)
- [Setup](#setup)
- [Compatibility](#compatibility)
- [Result](#result)
- [Proof — Generated Client (Before vs After)](#proof--generated-client-before-vs-after)
- [What is actually generated](#what-is-actually-generated)
- [How you actually use it](#how-you-actually-use-it)
- [What this gives you](#what-this-gives-you)
- [Mental model](#mental-model)
- [Next steps](#next-steps)
- [References](#references)

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

You don't configure OpenAPI.
You don't maintain templates.
You don't fight generator behavior.

You only do three things:

1. return your contract from controllers
2. optionally reuse your own shared contract models on the client side
3. generate clients from OpenAPI

That's it.

The platform handles projection, generation, and contract alignment automatically.

---

## Setup

### 1. Server (producer)

Add the dependency:

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.0.1</version>
</dependency>
```

Return your contract:

```java
ServiceResponse<CustomerDto>
```

Optional: bring your own response envelope (BYOE)

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

---

### 2. Client (consumer)

Inherit the parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.1</version>
</parent>
```

If using a custom envelope, declare it for client generation:

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.envelope=io.example.contract.ApiResponse
    </additionalProperty>
</additionalProperties>
```

Optionally declare externally owned shared contract models:

```xml
<!-- Map your DTOs to existing contract classes -->
<additionalProperties>
    <additionalProperty>
        openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
    </additionalProperty>
</additionalProperties>
```

Generate the client:

```bash
mvn clean install
```

---

## Compatibility

OpenAPI Generics is currently verified with:

- **Java:** 17+
- **Spring Boot:** 3.4.x, 3.5.x, 4.x
- **springdoc-openapi:** 2.8.x (Spring Boot 3.x), 3.x (Spring Boot 4.x)
- **OpenAPI Generator:** 7.x
- **Server scope:** Spring WebMvc (`springdoc-openapi-starter-webmvc-ui`)

See the full compatibility matrix and support policy: [Compatibility & Support Policy](compatibility.md)

---

## Result

```java
ServiceResponse<CustomerDto>
```

Or with a custom envelope:

```java
ApiResponse<CustomerDto>
```

The exact same contract shape flows from server to client.

* no duplicated envelope models
* generics preserved end-to-end
* external contract models reused instead of regenerated
* existing response envelopes can be used without migration

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

### What changed

Instead of generating new models from OpenAPI:

* the server projects the Java contract into OpenAPI deterministically
* external contract models can be explicitly mapped and reused
* wrappers are generated as thin type bindings
* the generator enforces contract alignment instead of passively materializing schemas

```text
Java Contract (SSOT)
        ↓
OpenAPI (projection, not authority)
        ↓
Generator (deterministic reconstruction)
        ↓
Client (contract-aligned types)
```

No reinterpretation. No duplication. No drift.

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

Or, when using BYOE:

```java
YourEnvelope<CustomerDto>
```

You do **not** have to deal with common issues produced by default OpenAPI generation:

* duplicated envelope classes (one per endpoint)
* flattened generic types (loss of `Page<T>` / `ServiceResponse<T>` semantics)
* unstable model graphs (frequent diffs from minor changes)
* generator-specific model hierarchies (e.g., `InlineResponse200`, `CustomerDtoResponse`)

If you reuse shared contract DTOs, the system also avoids regenerating models you already own.

The result is a consistent, predictable contract model — whether you use the default envelope or your own.

---

## Mental model

Think of generated classes as:

> thin type adapters — not models

They exist because OpenAPI cannot express Java generics directly — not because your domain model requires duplication.

When shared contract models are provided, generated wrappers reference them directly. When they are not, the platform still preserves the response-envelope structure deterministically.

Your system always operates around a single envelope contract:

```text
YourEnvelope<T>
```

`ServiceResponse<T>` is the default platform contract.
Your own envelope can be used as well.

Everything else is infrastructure.

---

## Next steps

* [Server Adoption](adoption/server-side-adoption.md)
* [Client Adoption](adoption/client-side-adoption.md)

If the contract stays consistent, everything stays consistent.
This system works by keeping that boundary intact.

---

## References

* **GitHub Repository** — [openapi-generics](https://github.com/blueprint-platform/openapi-generics)
* **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)