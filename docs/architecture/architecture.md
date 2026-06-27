---
layout: default
title: Architecture
nav_exclude: true
---

# Architecture

> Internal architecture of OpenAPI Generics.

OpenAPI Generics preserves contract semantics across the complete Java → OpenAPI → Client lifecycle.

Its architecture is based on a simple principle:

> Java contracts are the source of truth.  
> OpenAPI is a deterministic projection.  
> Generated clients reconstruct the original contract.

---

## Contents

- [Architecture Overview](#architecture-overview)
- [Projection Protocol](#projection-protocol)
- [Projection](#projection)
- [Client Generation](#client-generation)
- [Deterministic Pipeline](#deterministic-pipeline)
- [Supported Scope](#supported-scope)
- [Module Map](#module-map)

---

## Architecture Overview

OpenAPI Generics consists of two independent phases connected only by the generated OpenAPI document.

```text
Spring Boot Service
        │
        ▼
openapi-generics-server-starter
        │
        ▼
OpenAPI + Vendor Extensions
        │
        ▼
openapi-generics-java-codegen
        │
        ▼
Generated Java Client
```

The server publishes contract metadata.

The client generator consumes that metadata to reconstruct contract-aligned Java types.

---

## Projection Protocol

Contract semantics are projected through OpenAPI vendor extensions.

Example:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
x-ignore-model: true
```

These extensions describe wrapper semantics, container identity, payload type, and generation behavior while remaining valid OpenAPI.

---

## Projection

`openapi-generics-server-starter` analyzes Java response contracts and projects them into OpenAPI.

Responsibilities:

- response contract discovery
- generic type introspection
- wrapper schema projection
- metadata enrichment
- contract validation

Supported contracts include:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

Applications may also register their own generic container contracts (for example `Paging<T>` or `Window<T>`), which participate in the same projection pipeline.

---

## Client Generation

`GenericAwareJavaCodegen` extends OpenAPI Generator with contract awareness.

Responsibilities:

- wrapper reconstruction
- container-aware reconstruction
- BYOE support
- BYOC support
- infrastructure model filtering
- generated-source hygiene

Generated wrappers intentionally remain thin:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

---

## Deterministic Pipeline

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Deterministic Client Reconstruction
```

The same contract and configuration always produce the same OpenAPI document and the same generated client.

Projection and generation are validated with fail-fast verification to prevent contract drift.

---

## Supported Scope

Built-in platform contracts:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

Supported capabilities:

- BYOE (Bring Your Own Envelope)
- BYOC (Bring Your Own Contract)
- application-defined generic containers
- deterministic OpenAPI metadata
- contract-aware Java client generation

---

## Module Map

| Module                                                                                         | Responsibility |
|------------------------------------------------------------------------------------------------|----------------|
| [`openapi-generics-contract`](../../openapi-generics-contract/README.md)                                                    | Shared contracts and platform types |
| [`openapi-generics-server-starter`](../../openapi-generics-server-starter/README.md)           | Spring Boot projection pipeline |
| [`openapi-generics-java-codegen`](../../openapi-generics-java-codegen/README.md)               | Contract-aware OpenAPI Generator extension |
| [`openapi-generics-java-codegen-parent`](../../openapi-generics-java-codegen-parent/README.md) | Generator orchestration and template lifecycle |
| [`openapi-generics-platform-bom`](../../openapi-generics-platform-bom/README.md)               | Dependency alignment |

---

## Mental Model

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Contract Reconstruction
```

OpenAPI Generics does not redefine contracts.

It preserves them.