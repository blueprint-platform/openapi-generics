# openapi-generics-java-codegen

> Generics-aware OpenAPI Generator extension for contract-aligned Java clients

`openapi-generics-java-codegen` is a custom OpenAPI Generator extension that keeps generated Java clients aligned with the published contract.

It does not invent a new model.
It prevents the generator from duplicating or drifting away from the existing one.

Its role is precise:

> Reuse contract-owned models, preserve wrapper semantics, and generate thin contract-aligned client output.

This module is build-time only and is typically used through `openapi-generics-java-codegen-parent`.

---

## Table of Contents

1. [What Problem It Solves](#what-problem-it-solves)
2. [What It Does](#what-it-does)
3. [How It Works](#how-it-works)
4. [BYOC — Bring Your Own Contract Models](#byoc--bring-your-own-contract-models)
5. [BYOE — Bring Your Own Envelope](#byoe--bring-your-own-envelope)
6. [What Gets Generated](#what-gets-generated)
7. [How It Is Used](#how-it-is-used)
8. [What It Does NOT Do](#what-it-does-not-do)
9. [Compatibility](#compatibility)
10. [Determinism Guarantees](#determinism-guarantees)
11. [Mental Model](#mental-model)
12. [Related Modules](#related-modules)
13. [License](#license)

---

## What Problem It Solves

Default OpenAPI Generator behavior is schema-driven.
That usually causes three problems in contract-first systems:

* shared models get regenerated
* wrapper semantics become flattened or duplicated
* generated clients drift from the server contract

This module prevents that.

It enforces one rule:

> If the contract already owns the type, the generator must reuse it, not recreate it.

---

## What It Does

The generator extension adds contract-aware behavior on top of `JavaClientCodegen`.

It:

* suppresses generation of ignored or externally provided models
* injects imports for externally mapped DTOs
* injects envelope metadata for wrapper templates
* preserves wrapper semantics using vendor extensions already present in OpenAPI

This keeps generated clients aligned with the server-side projection.

---

## How It Works

At a high level:

```text
OpenAPI
   ↓
Custom generator
   ↓
Contract-aware filtering + metadata injection
   ↓
Generated Java client
```

Internally, the extension applies three core steps:

1. mark ignored models during model creation
2. remove ignored models from local and global generation graphs
3. enrich wrapper models with external import and envelope metadata

The result is thin generated code that references the real contract instead of duplicating it.

---

## BYOC — Bring Your Own Contract Models

Use BYOC when DTOs already exist in your own contract module.

Configuration:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Effect:

* `CustomerDto` is treated as externally provided
* it is not generated again
* wrapper classes import and reuse it directly

Important:

* the property key must match the OpenAPI model name exactly
* the value must be a fully-qualified class name

---

## BYOE — Bring Your Own Envelope

Use BYOE when the service contract uses a custom success envelope.

Configuration:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Effect:

* wrapper templates receive the correct envelope import
* generated wrappers extend your configured envelope type
* the envelope itself is not generated

This works together with the server-side BYOE projection model.

---

## What Gets Generated

This module does not generate full contract structures.

It generates thin wrapper classes such as:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>>
class ApiResponseCustomerDto extends ApiResponse<CustomerDto>
```

What it avoids generating:

* shared envelope classes
* externally owned DTOs
* duplicated infrastructure models

---

## How It Is Used

This module is normally not consumed directly.

It is wired by:

```text
openapi-generics-java-codegen-parent
```

The parent POM handles:

* generator registration
* template extraction and overlay
* OpenAPI Generator plugin integration
* generated source registration

That is the intended integration path.

---

## What It Does NOT Do

This module does not:

* define the canonical contract
* produce OpenAPI
* own runtime behavior
* replace the parent orchestration layer
* act as a general-purpose OpenAPI Generator fork

It is a contract-aware generation extension, nothing more.

---

## Compatibility

| Component         | Supported Versions |
| ----------------- | ------------------ |
| Java              | 17+                |
| OpenAPI Generator | 7.x                |

This module is build-time only.

---

## Determinism Guarantees

The extension is designed to provide:

* stable model suppression
* repeatable wrapper generation
* explicit contract reuse
* no accidental regeneration of owned types

Its behavior is deterministic because model ownership is explicit and wrapper semantics are carried by stable vendor extensions.

---

## Mental Model

Think of this module as:

> a contract guardrail inside OpenAPI Generator

Not:

* a standalone SDK generator
* a template hack
* a new model definition layer

---

## Related Modules

| Module                                 | Role                               |
| -------------------------------------- | ---------------------------------- |
| `openapi-generics-contract`            | Canonical contract authority       |
| `openapi-generics-server-starter`      | Runtime OpenAPI projection         |
| `openapi-generics-java-codegen`        | Contract-aware generator extension |
| `openapi-generics-java-codegen-parent` | Build-time orchestration           |

---

## License

MIT License
