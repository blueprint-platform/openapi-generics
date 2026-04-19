---
title: openapi-generics-platform — Architecture (0.9.x)
nav_exclude: true
---

# OpenAPI Generics Platform — Architecture (v1.0.0)

## Contents

1. [Purpose](#1-purpose)
2. [Core Principle](#2-core-principle)
3. [Problem Statement](#3-problem-statement)
4. [Solution Overview](#4-solution-overview)
5. [Supported Contract Shapes](#5-supported-contract-shapes)
6. [Server-Side Architecture](#6-server-side-architecture)
7. [Client-Side Architecture](#7-client-side-architecture)
8. [Build-Time Orchestration](#8-build-time-orchestration)
9. [Ownership Model](#9-ownership-model)
10. [Design Decisions](#10-design-decisions)
11. [System Boundaries](#11-system-boundaries)
12. [Summary](#12-summary)

## 1. Purpose

OpenAPI Generics is a **contract-first, generics-aware API platform** that ensures:

* A **single source of truth** for API contracts (Java)
* A **deterministic OpenAPI projection** (no semantic loss)
* **Client generation aligned with the contract** (no drift, no duplication)

This document defines the **architecture, responsibilities, and boundaries** of the platform.

---

## 2. Core Principle

> **OpenAPI is a projection. The Java contract is the authority.**

Implications:

* Contract models are **not re-generated** on the client
* OpenAPI contains **semantic hints**, not ownership
* Client generation reconstructs the **original contract abstraction**

---

## 3. Problem Statement

Standard OpenAPI workflows break down with generics:

* `ServiceResponse<T>` loses type semantics
* `Page<T>` becomes flattened or duplicated
* Envelope models are **re-generated per client**
* Contract ownership becomes ambiguous

Result:

* Drift between producer and consumer
* Model duplication
* Loss of abstraction

---

## 4. Solution Overview

The platform introduces a **two-phase architecture**:

### 4.1 Server: Semantic Projection

* Extracts real response types from runtime
* Interprets generic structures
* Enriches OpenAPI with **vendor extensions**
* Suppresses infrastructure models from generation

### 4.2 Client: Semantic Reconstruction

* Reads vendor extensions
* Reconstructs wrapper abstractions
* Reuses external contract models
* Avoids model duplication

---

## 5. Supported Contract Shapes

The platform intentionally supports a **restricted, deterministic set**:

### Default Envelope

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

### External Envelope (BYOE)

* `YourEnvelope<T>`

Constraints:

* Exactly **one generic parameter**
* Exactly **one direct payload field**
* No nested generics (`YourEnvelope<Page<T>>` is NOT supported)

Design intent:

> **Determinism over flexibility**

---

## 6. Server-Side Architecture

### 6.1 Entry Point

`OpenApiPipelineOrchestrator`

Pipeline execution:

1. Discover response types
2. Introspect generic structure
3. Generate wrapper semantics
4. Mark infrastructure schemas
5. Validate contract integrity

---

### 6.2 Response Discovery

`ResponseTypeDiscoveryStrategy`

* Framework-specific
* Default: Spring MVC (`RequestMappingHandlerMapping`)
* Produces: `ResolvableType`

---

### 6.3 Introspection Layer

`ResponseTypeIntrospector`

Responsibilities:

* Unwrap async / wrapper types

    * `ResponseEntity`
    * `CompletionStage`
    * `Future`
    * `DeferredResult`
* Detect contract envelope
* Extract payload structure

Output:

`ResponseTypeDescriptor`

---

### 6.4 Envelope Policy

`ResponseIntrospectionPolicyResolver`

Modes:

* Default → `ServiceResponse`
* External → configured via:

```
openapi-generics:
  envelope:
    type: com.example.ApiResponse
```

Validation rules:

* Must be a concrete class
* Must have exactly one type parameter
* Must expose exactly one payload field

---

### 6.5 Schema Enrichment

`WrapperSchemaProcessor` + `WrapperSchemaEnricher`

Adds semantic extensions:

* `x-api-wrapper`
* `x-api-wrapper-datatype`
* `x-data-container` (if container)
* `x-data-item`

Purpose:

> Encode generics semantics into OpenAPI

---

### 6.6 Generation Control

`SchemaGenerationControlMarker`

Marks schemas with:

* `x-ignore-model`

Used for:

* Envelope types
* Infrastructure models (`Meta`, `Sort`)
* Derived container schemas

Result:

> OpenAPI keeps structure, client avoids regeneration

---

### 6.7 Contract Validation

`OpenApiContractGuard`

Validates:

* Wrapper existence
* Required extensions
* Payload structure

Behavior:

> Fail-fast — no silent inconsistencies

---

## 7. Client-Side Architecture

### 7.1 Generator Extension

`GenericAwareJavaCodegen`

Extends:

* `JavaClientCodegen`

Responsibilities:

* Filter ignored models
* Inject external imports
* Apply envelope metadata
* Preserve wrapper semantics

---

### 7.2 External Model Reuse (BYOC)

Configuration:

```
openapi-generics.response-contract.CustomerDto=com.example.CustomerDto
```

Behavior:

* Prevents generation of DTO
* Injects correct import

---

### 7.3 Envelope Metadata

Configuration:

```
openapi-generics.envelope=com.example.ApiResponse
```

Injected into wrapper models:

* `x-envelope-import`
* `x-envelope-type`

Purpose:

> Ensure generated clients use correct envelope type

---

### 7.4 Template Strategy

* Patches upstream `model.mustache`
* Injects wrapper-aware template (`api_wrapper.mustache`)
* Verifies patch integrity at build-time

Guarantee:

> Deterministic generation — upstream changes cannot silently break behavior

---

## 8. Build-Time Orchestration

`openapi-generics-java-codegen-parent`

Pipeline:

1. Extract upstream templates
2. Extract custom templates
3. Patch model template
4. Validate patch
5. Overlay templates
6. Run generator
7. Register generated sources

Control:

```
openapi.generics.skip=true
```

---

## 9. Ownership Model

### Server

* Owns contract
* Defines response shapes

### Client

* Reconstructs contract
* Owns adapter usage

### Platform

* Defines transformation rules
* Enforces determinism

---

## 10. Design Decisions

### Narrow Generic Support

Trade-off:

* (+) Deterministic
* (+) Predictable codegen
* (-) Limited flexibility

---

### Vendor Extensions as DSL

Trade-off:

* (+) No OpenAPI spec violation
* (+) Full semantic control
* (-) Requires custom generator

---

### Fail-Fast Validation

Trade-off:

* (+) Early detection
* (+) No silent corruption
* (-) Strict adoption curve

---

## 11. System Boundaries

The platform does NOT:

* Define business logic
* Enforce API design patterns
* Handle runtime behavior

It ONLY:

> Preserves contract semantics across server → OpenAPI → client

---

## 12. Summary

OpenAPI Generics provides:

* Contract-first architecture
* Deterministic OpenAPI projection
* Generics-aware client generation
* External contract reuse

Key outcome:

> **No drift. No duplication. No semantic loss.**
