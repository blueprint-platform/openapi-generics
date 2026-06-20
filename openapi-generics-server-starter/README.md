# openapi-generics-server-starter

> Deterministic OpenAPI projection for contract-first Spring Boot services

`openapi-generics-server-starter` projects Java response contracts into a stable, generics-aware OpenAPI document.

It does not define the contract.  
It does not generate clients.  
It does one thing:

> Discover supported Java response shapes and project them into OpenAPI without losing generic semantics.

---

## Table of Contents

- [What It Solves](#what-it-solves)
- [Supported Shapes](#supported-shapes)
- [How It Works](#how-it-works)
- [Generated OpenAPI Metadata](#generated-openapi-metadata)
- [Schema Enrichment](#schema-enrichment)
- [Usage](#usage)
- [BYOE](#byoe)
- [What This Module Does Not Do](#what-this-module-does-not-do)
- [Determinism Guarantees](#determinism-guarantees)
- [Mental Model](#mental-model)
- [License](#license)

---

## What It Solves

Standard OpenAPI generation often loses or flattens generic response structure.

Typical problems:

- duplicated response wrapper models
- lost generic payload information
- unstable generated client types
- drift between Java contracts and OpenAPI output

This starter keeps the Java contract as the source of truth and treats OpenAPI as a projection.

---

## Supported Shapes

Default platform envelope:

```java
ServiceResponse<T>
ServiceResponse<Page<T>>
ServiceResponse<List<T>>
```

Custom envelope support:

```java
YourEnvelope<T>
```

Custom envelopes must:

- be a concrete class
- declare exactly one type parameter
- expose exactly one direct payload field of type `T`

Nested generics inside custom envelopes are intentionally not supported.

---

## How It Works

```text
Controller return types
    ↓
Response type discovery
    ↓
Contract introspection
    ↓
Wrapper schema projection
    ↓
Container-aware enrichment
    ↓
Generation-control markers
    ↓
OpenAPI contract validation
```

The pipeline is coordinated by a single `OpenApiPipelineOrchestrator` and runs once per OpenAPI instance.

---

## Generated OpenAPI Metadata

The starter adds vendor extensions used by the generics-aware client codegen layer:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: ListCustomerDto
x-data-container: List
x-data-item: CustomerDto
x-ignore-model: true
```

These extensions form the projection protocol between server OpenAPI output and client generation.

---

## Schema Enrichment

Container handling is strategy-based.

Built-in strategies:

- `Page<T>` via direct schema resolution
- `List<T>` via wrapper-embedded array resolution

This keeps container extraction extensible without hard-coding all behavior inside the wrapper enricher.

---

## Usage

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```

Example:

```java
@GetMapping
public ResponseEntity<ServiceResponse<List<CustomerDto>>> listCustomers() {
  return ResponseEntity.ok(ServiceResponse.of(customers));
}
```

Springdoc exposes the projected OpenAPI document as usual:

```text
/v3/api-docs
/v3/api-docs.yaml
```

---

## BYOE

Custom envelope configuration:

```yaml
openapi-generics:
  envelope:
    type: io.example.ApiResponse
```

Example:

```java
ResponseEntity<ApiResponse<CustomerDto>>
```

BYOE is intentionally limited to simple payload shapes to keep projection deterministic.

---

## What This Module Does Not Do

This starter does not:

- define `ServiceResponse`, `Page`, or shared contract models
- generate Java clients
- own DTOs
- change runtime HTTP behavior
- implement business validation
- support arbitrary nested generic graphs

---

## Determinism Guarantees

The starter is designed around:

- same Java contract → same OpenAPI output
- stable wrapper naming
- explicit vendor metadata
- fail-fast validation
- no hidden schema reinterpretation

If the projected OpenAPI document becomes inconsistent, the pipeline fails before invalid clients are generated.

---

## Mental Model

Think of this module as:

> a deterministic projector from Java response contracts to OpenAPI metadata-rich schemas

Not as:

- a documentation helper
- a DTO generator
- a general OpenAPI customization toolkit

---

## License

MIT — see [LICENSE](../LICENSE)