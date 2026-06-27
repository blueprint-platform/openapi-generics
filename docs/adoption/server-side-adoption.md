---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-Side Adoption

> Publish a generics-aware OpenAPI document from your Spring Boot service without changing runtime behavior.

This guide explains how to enable OpenAPI Generics on the producer side.

For client generation, see [Client-Side Adoption](./client-side-adoption.md).  
For implementation details, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [What the Starter Does](#what-the-starter-does)
- [Supported Controller Shapes](#supported-controller-shapes)
- [BYOE — Bring Your Own Envelope](#byoe)
- [Verification](#verification)
- [Further Reading](#further-reading)

---

## Quick Start

Add the starter:

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

Write controller methods normally:

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
    return ResponseEntity.ok(ServiceResponse.of(service.findById(id)));
}
```

Container payloads are supported as well:

```java
ServiceResponse<List<CustomerDto>>
ServiceResponse<Set<CustomerDto>>
ServiceResponse<Page<CustomerDto>>
```

Application-defined generic containers are also supported when registered through the optional `openapi-generics.containers` configuration.

---

## What the Starter Does

The starter runs only when Springdoc generates an OpenAPI document, for example:

```text
/v3/api-docs
/v3/api-docs.yaml
```

It:

- discovers supported generic response contracts
- projects wrapper and container metadata into OpenAPI
- publishes OpenAPI Generics vendor extensions
- validates the generated contract metadata

It does **not**:

- intercept HTTP requests
- change runtime serialization
- modify controller behavior

Projection pipeline:

```text
Java Contract
      ↓
Response Introspection
      ↓
OpenAPI Projection
      ↓
Vendor Extensions
      ↓
Validated OpenAPI Document
```

---

## Supported Controller Shapes

Built-in contracts:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

BYOE envelopes support the same response shapes.

Application-defined generic containers (for example `Paging<T>` or `Window<T>`) participate in the same projection pipeline when registered through configuration.

Asynchronous wrappers are unwrapped automatically, including:

```java
CompletionStage<T>
Future<T>
DeferredResult<T>
WebAsyncTask<T>
```

---

## BYOE — Bring Your Own Envelope

Use your own response envelope instead of `ServiceResponse<T>`.

Configure the envelope and, optionally, register application-defined generic containers:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse

  # Optional
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

The configured envelope becomes the published contract while preserving the same projection model.

Configured generic containers are optional. When registered, they participate in the same projection pipeline as the built-in `List<T>`, `Set<T>`, and platform-provided `Page<T>` container types.

This allows generated clients to reconstruct both built-in and application-defined generic container contracts through the same deterministic model.

---

## Verification

After starting the application, verify that the generated OpenAPI document contains OpenAPI Generics metadata.

Built-in container example:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

Application-defined container example:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: PagingCustomerDto
x-data-container: Paging
x-data-container-type: io.example.contract.Paging
x-data-item: CustomerDto
```

Infrastructure schemas that are contract-owned or externally provided should also be marked with:

```yaml
x-ignore-model: true
```

These metadata allow the client generator to reconstruct the original Java contract deterministically.

---

## Further Reading

- [Client-Side Adoption](./client-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)