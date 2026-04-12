# openapi-generics Server Side — Summary

## What this module is now
`openapi-generics-server-starter` is no longer just a Springdoc helper.

It is now a **Spring Boot starter that installs a deterministic, contract-aware OpenAPI projection pipeline**.

Its job is to project Java contract semantics into OpenAPI while preserving supported generic response shapes.

---

## Core architectural role
The server side is built around a **single pipeline**:

1. Register canonical base schemas
2. Discover controller response types
3. Introspect supported contract-aware shapes
4. Build wrapper schemas deterministically
5. Mark infrastructure/container schemas to avoid duplicate generation
6. Validate final OpenAPI output fail-fast

This means:
- no scattered customizers
- no ordering tricks
- no patch-based schema fixing
- one explicit execution path

---

## Main components

### `OpenApiGenericsAutoConfiguration`
Wires the whole system.

Creates:
- response type discovery strategy
- response type introspector
- base schema registrar
- schema generation control marker
- wrapper schema enricher
- wrapper schema processor
- contract guard
- pipeline orchestrator
- single `OpenApiCustomizer` entry point

---

### `OpenApiPipelineOrchestrator`
The central execution coordinator.

Responsibilities:
- runs the pipeline once per `OpenAPI` instance
- enforces deterministic execution order
- delegates all actual work to focused components

---

### `ResponseTypeDiscoveryStrategy`
Framework-specific discovery abstraction.

Current implementation:
- `MvcResponseTypeDiscoveryStrategy`
- scans Spring MVC handler mappings
- collects method return types as `ResolvableType`

This isolates framework scanning from core contract logic.

---

### `ResponseTypeIntrospector`
Framework-independent type analysis.

Supported contract-aware shapes:
- `ServiceResponse<T>`
- `ServiceResponse<Page<T>>`

It explicitly does **not** own:
- `ServiceResponse<List<T>>`
- arbitrary nested generics
- maps
- generic combinations outside the guaranteed scope

It also unwraps common outer wrappers such as:
- `ResponseEntity`
- `CompletionStage`
- `Future`
- `DeferredResult`
- `WebAsyncTask`

---

## Base schema model
`BaseSchemaRegistrar` ensures the canonical base schemas exist:

- `Sort`
- `Meta`
- `ServiceResponse`
- `ServiceResponseVoid`

Properties:
- idempotent
- non-invasive
- contract-aligned

So base schemas are treated as part of the projection engine, not as accidental Springdoc output.

---

## Wrapper schema generation
`WrapperSchemaProcessor` + `ServiceResponseSchemaFactory` handle wrapper creation.

Behavior:
- wrapper schemas are **authoritatively rebuilt**
- existing schemas are replaced, not patched
- wrapper structure is always normalized from contract rules

Wrapper model:
- base `ServiceResponse`
- composed with `allOf`
- `data` field overridden with concrete target schema

Examples:
- `ServiceResponseCustomerDto`
- `ServiceResponsePageCustomerDto`

---

## Vendor extension model
The server side now defines a clear OpenAPI semantic layer using vendor extensions:

- `x-api-wrapper`
- `x-api-wrapper-datatype`
- `x-class-extra-annotation`
- `x-data-container`
- `x-data-item`
- `x-ignore-model`

This is effectively a **custom projection DSL** used by client generation.

---

## Container enrichment
`WrapperSchemaEnricher` adds semantic metadata for supported containers.

Current default container:
- `Page`

It enriches wrappers with:
- `x-data-container`
- `x-data-item`

This makes `Page<T>` semantics visible to downstream generators without relying on ad hoc logic.

---

## Duplicate model prevention
`SchemaGenerationControlMarker` marks schemas with `x-ignore-model`.

Ignored schemas include:
- `ServiceResponse`
- `ServiceResponseVoid`
- `Meta`
- `Sort`
- derived container schemas such as `PageCustomerDto`

Purpose:
- keep schemas in OpenAPI
- prevent code generators from re-generating infrastructure/shared models

This directly supports the project’s anti-duplication goal.

---

## Validation model
`OpenApiContractGuard` performs final fail-fast validation.

It checks:
- required base schemas exist
- wrapper schemas contain required extensions
- wrapper schemas use `allOf`
- wrapper schemas define `data`

This protects only the critical invariants of the contract-aware projection model.

---

## Starter behavior
The module is packaged as a reusable Spring Boot starter:
- Java 17 baseline
- Spring Boot 3.5.13
- Spring WebMVC 6.2.17
- BOM-managed dependency alignment

It also includes a fallback auto-configuration:
- if Springdoc is missing, the starter stays inactive
- logs a warning instead of failing startup

So the starter is safe and non-intrusive.

---

## Current identity of the server side
The server module is now best understood as:

> A deterministic server-side OpenAPI projection engine for `ServiceResponse<T>` and `ServiceResponse<Page<T>>`.

Not just a customization helper.

---

## Final synthesis
The server side now does the following:

- owns the OpenAPI projection pipeline
- preserves only explicitly supported generic contract shapes
- builds canonical wrapper schemas deterministically
- exposes semantics through stable vendor extensions
- suppresses duplicate infrastructure model generation
- validates final contract integrity fail-fast

In short:

> The server side has evolved from “Springdoc customization” into the **core projection engine** of openapi-generics.