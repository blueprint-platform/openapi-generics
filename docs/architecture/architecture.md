---
layout: default
title: Architecture
nav_exclude: true
---

# Architecture

> Internal architecture of openapi-generics.

OpenAPI Generics preserves contract-owned generic response structures across the complete Java → OpenAPI → Client lifecycle.

The project is built around a single principle:

> Java contract is the source of truth.  
> OpenAPI is a projection.  
> Generated clients are deterministic reconstructions of that contract.

---

## Contents

- [Architecture Overview](#architecture-overview)
- [Projection Protocol](#projection-protocol)
- [Server Side](#server-side)
- [Client Generation](#client-generation)
- [Deterministic Generation](#deterministic-generation)
- [Supported Scope](#supported-scope)
- [Module Map](#module-map)
- [Mental Model](#mental-model)

---

## Architecture Overview

The platform consists of two independent parts connected only through the OpenAPI document.

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

The client generator consumes that metadata and reconstructs contract-aligned wrappers.

No runtime coupling exists between server and client generation.

---

## Projection Protocol

Generic semantics are carried through OpenAPI vendor extensions.

Examples:

```yaml
x-api-wrapper: true
x-data-container: Page
x-data-item: CustomerDto
x-ignore-model: true
```

These extensions allow the generator to distinguish:

- contract-owned wrappers
- container types
- payload types
- infrastructure models that should not be regenerated

---

## Server Side

`openapi-generics-server-starter` projects Java contracts into OpenAPI.

Responsibilities:

- Response contract discovery
- Generic type introspection
- Wrapper schema projection
- Container metadata enrichment
- Contract validation

Examples:

```java
ServiceResponse<CustomerDto>

ServiceResponse<Page<CustomerDto>>

ServiceResponse<List<CustomerDto>>

ServiceResponse<Set<CustomerDto>>
```

and equivalent BYOE envelopes.

---

## Client Generation

`GenericAwareJavaCodegen` extends OpenAPI Generator with contract awareness.

Responsibilities:

- Wrapper reconstruction
- External contract reuse (BYOC)
- Infrastructure model filtering
- Container-aware generation

Generated wrappers remain intentionally thin:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

---

## Deterministic Generation

The platform is designed around deterministic output.

```text
Java Contract
      ↓
OpenAPI
      ↓
Generated Client
```

Same contract produces the same OpenAPI document and the same generated client output.

The platform validates both projection and generation stages and prefers fail-fast behavior over potentially incorrect code generation.

---

## Supported Scope

Platform envelope:

```java
ServiceResponse<T>
```

Supported container types:

```java
Page<T>
List<T>
Set<T>
```

Custom envelopes are supported through BYOE and participate in the same projection and reconstruction pipeline.

---


## Module Map

For contributors navigating the codebase:

| Module | Responsibility |
|----------|----------|
| [`openapi-generics-contract`](../../README.md) | Shared platform contracts including `ServiceResponse<T>`, `Page<T>`, `Meta`, and `Sort`. |
| [`openapi-generics-server-starter`](../../openapi-generics-server-starter/README.md) | Spring Boot integration and OpenAPI projection pipeline. |
| [`openapi-generics-java-codegen`](../../openapi-generics-java-codegen/README.md) | Contract-aware OpenAPI Generator extension (`GenericAwareJavaCodegen`). |
| [`openapi-generics-java-codegen-parent`](../../openapi-generics-java-codegen-parent/README.md) | Build integration, template orchestration, and generator configuration. |
| [`openapi-generics-platform-bom`](../../openapi-generics-platform-bom/README.md) | Dependency alignment for platform modules and integrations. |


---

## Mental Model

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Contract-Aware Reconstruction
```

The platform does not redefine contracts.

It preserves them across the OpenAPI lifecycle.