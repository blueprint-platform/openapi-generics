# openapi-generics-java-codegen

> Contract-aware Java code generation for OpenAPI Generics

`openapi-generics-java-codegen` extends OpenAPI Generator with contract-awareness.

Its purpose is simple:

> Generate wrapper models while reusing contract-owned types instead of recreating them.

This module is build-time only and is typically consumed through `openapi-generics-java-codegen-parent`.

---

## Table of Contents

- [What It Does](#what-it-does)
- [Generation Flow](#generation-flow)
- [BYOC — Bring Your Own Contract Models](#byoc--bring-your-own-contract-models)
- [BYOE — Bring Your Own Envelope](#byoe--bring-your-own-envelope)
- [Container Support](#container-support)
- [Generated Wrappers](#generated-wrappers)
- [What Is Not Generated](#what-is-not-generated)
- [Architectural Role](#architectural-role)
- [Determinism](#determinism)
- [Mental Model](#mental-model)
- [License](#license)

---

## What It Does

The generator augments standard Java generation with:

- external model reuse (BYOC)
- custom envelope support (BYOE)
- container metadata support
- model ownership enforcement
- deterministic wrapper generation

The result is a generated client that stays aligned with the published OpenAPI Generics contract.

---

## Generation Flow

```text
OpenAPI
   ↓
Ownership Resolution
   ↓
Model Filtering
   ↓
Metadata Enrichment
   ↓
Wrapper Generation
   ↓
Generated Java Client
```

The generator never reinterprets the contract.

It consumes metadata already projected into OpenAPI by the server layer.

---

## BYOC — Bring Your Own Contract Models

Existing contract models can be reused directly.

```xml
<additionalProperty>
  openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
</additionalProperty>
```

Effect:

- model generation is skipped
- generated wrappers import the existing type
- contract ownership remains explicit

---

## BYOE — Bring Your Own Envelope

Custom response envelopes are supported.

```xml
<additionalProperty>
  openapi-generics.envelope=io.example.contract.ApiResponse
</additionalProperty>
```

Generated wrappers extend the configured envelope instead of the default platform envelope.

Example:

```java
public class ApiResponseCustomerDto extends ApiResponse<CustomerDto> {}
```

---

## Container Support

Built-in container mappings:

```text
List<T>
Page<T>
```

The generator resolves container imports and type metadata automatically.

Additional containers may be registered through generator properties.

---

## Generated Wrappers

Examples:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {
}
```

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {
}
```

Generated wrappers are intentionally thin.

Their purpose is type preservation, not behavior.

---

## What Is Not Generated

The generator intentionally does not generate:

- external contract DTOs
- shared envelope types
- shared paging types
- ignored infrastructure models

Ownership remains with the contract module.

---

## Architectural Role

```text
openapi-generics-server-starter
                ↓
           OpenAPI
                ↓
openapi-generics-java-codegen
                ↓
      Generated Client
```

The server side publishes contract metadata.

This generator consumes that metadata and reconstructs the correct Java type hierarchy.

---

## Determinism

The generator guarantees:

- stable ownership rules
- stable filtering behavior
- stable wrapper generation
- repeatable output from identical OpenAPI input

Same OpenAPI contract → same generated wrappers.

---

## Mental Model

Think of this module as:

> a contract-aware layer on top of OpenAPI Generator

Its responsibility is not generating more models.

Its responsibility is generating fewer, but correct, models.

---

## License

MIT — see [LICENSE](../LICENSE)