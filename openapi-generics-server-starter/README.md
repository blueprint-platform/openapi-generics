# openapi-generics-server-starter

> Deterministic OpenAPI projection for contract-first Spring Boot services

`openapi-generics-server-starter` is a Spring Boot starter that projects your Java API contract into a stable, generics-aware OpenAPI document.

It does not redefine the contract.
It publishes it as OpenAPI — deterministically and without schema drift.

The role of this module is precise:

> Take the canonical runtime contract and project it into OpenAPI without changing its meaning.

---

## Table of Contents

1. [What Problem It Solves](#what-problem-it-solves)
2. [What It Does](#what-it-does)
3. [How It Works](#how-it-works)
4. [Usage](#usage)
5. [Bring Your Own Envelope (BYOE)](#bring-your-own-envelope-byoe)
6. [Supported Contract Shapes](#supported-contract-shapes)
7. [What It Does NOT Do](#what-it-does-not-do)
8. [Compatibility](#compatibility)
9. [Determinism Guarantees](#determinism-guarantees)
10. [Failure Philosophy](#failure-philosophy)
11. [When To Use](#when-to-use)
12. [Mental Model](#mental-model)

---

## What Problem It Solves

In typical OpenAPI workflows:

* response envelopes are regenerated per endpoint
* generic structure is flattened or lost
* shared models are duplicated across layers
* generated clients drift from the actual server contract

Over time, this creates unstable OpenAPI output and fragile client generation.

This starter solves that by enforcing one rule:

> Java contract is the source of truth. OpenAPI is only its projection.

---

## What It Does

Once added, the starter:

* discovers supported response envelopes from controller return types
* projects deterministic wrapper schemas into OpenAPI
* preserves supported generic shapes
* adds vendor extensions required by the client codegen layer
* marks contract-owned infrastructure schemas to avoid duplicate generation downstream

No schema annotations.
No manual wrapper DTOs.
No patching OpenAPI by hand.

---

## How It Works

```text
Java Contract
    ↓
Runtime Projection Pipeline
    ↓
OpenAPI Document
```

For the default contract:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

the starter publishes deterministic wrapper schemas such as:

```text
ServiceResponseCustomerDto
ServiceResponsePageCustomerDto
```

If a custom envelope is configured, the same projection model applies, but the wrapper name is derived from that envelope type instead.

---

## Usage

### 1. Add dependency

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 2. Return your contract from controllers

Default envelope:

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(Long id) {
  return ResponseEntity.ok(ServiceResponse.of(customer));
}
```

Pagination:

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers() {
  return ResponseEntity.ok(ServiceResponse.of(page));
}
```

### 3. Expose OpenAPI through springdoc

```text
/v3/api-docs
/v3/api-docs.yaml
```

That is enough.

---

## Bring Your Own Envelope (BYOE)

The starter also supports custom success envelopes.

Example configuration:

```yaml
openapi-generics:
  envelope:
    type: io.example.ApiResponse
```

Example controller signature:

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(Long id) {
  return ResponseEntity.ok(ApiResponse.ok(customer));
}
```

Custom envelopes must satisfy these rules:

* must be a concrete class
* must declare exactly one type parameter
* must expose exactly one direct payload field of type `T`
* nested generic payloads are not supported

This means the starter supports:

```text
YourEnvelope<T>
```

but not:

```text
YourEnvelope<Page<T>>
```

The default platform envelope remains the only built-in path with container support.

---

## Supported Contract Shapes

### Default platform envelope

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

### Custom envelope (BYOE)

Supported:

* `YourEnvelope<T>`

Not supported:

* nested generic payloads in custom envelopes
* arbitrary collection wrappers
* map-based generic shapes
* multi-parameter envelope types

The scope is intentionally narrow to keep projection deterministic.

---

## What It Does NOT Do

This module does not:

* define the canonical contract library
* generate clients
* own DTOs
* change runtime HTTP behavior
* define service-specific error semantics

It only projects success-envelope contract structure into OpenAPI.

---

## Compatibility

| Component         | Supported Versions  |
| ----------------- | ------------------- |
| Java              | 17+                 |
| Spring Boot       | 3.4.x, 3.5.x, 4.x   |
| springdoc-openapi | 2.8.x, 3.x (WebMvc) |

---

## Determinism Guarantees

The starter is designed to provide:

* same contract → same OpenAPI output
* stable wrapper naming
* fixed projection behavior
* no scattered customizer ordering issues
* no contract reinterpretation

OpenAPI output is generated through a single runtime pipeline, then validated before completion.

---

## Failure Philosophy

If the contract shape is unsupported or the projected schema becomes inconsistent, the starter fails fast.

Typical failure mode:

```text
IllegalStateException
```

Principle:

> Incorrect projection is worse than no projection.

---

## When To Use

Use this module if:

* your service is Spring Boot based
* you publish OpenAPI with springdoc
* you want contract-first OpenAPI output
* you generate clients from published OpenAPI
* you want to avoid wrapper duplication and generic type loss

---

## Mental Model

Think of this module as:

> a deterministic projector from Java contract to OpenAPI

Not:

* a documentation helper
* a schema customization toolkit
* a model generator

---

## License

MIT — see [LICENSE](../LICENSE)
