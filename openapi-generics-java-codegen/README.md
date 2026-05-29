# openapi-generics-java-codegen

> Contract-aware OpenAPI Generator extension for deterministic Java client generation

`openapi-generics-java-codegen` is a custom OpenAPI Generator extension that augments the standard Java generator with contract-awareness.

Its responsibility is not orchestration, template management, or OpenAPI generation.

Its responsibility is precise:

> Apply contract-aware generation rules inside OpenAPI Generator so generated clients remain aligned with the published contract.

This module is build-time only and is typically consumed through `openapi-generics-java-codegen-parent`.

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
11. [Architectural Role](#architectural-role)
12. [Mental Model](#mental-model)

---

## What Problem It Solves

Default OpenAPI Generator behavior is schema-driven.

In contract-first systems this commonly causes:

* shared contract models to be regenerated
* wrapper semantics to be flattened or duplicated
* generated clients to drift from the server contract

This module prevents that.

It enforces a simple rule:

> If a type already belongs to the contract, generation should reference it rather than recreate it.

---

## What It Does

The extension builds on top of `JavaClientCodegen`.

It:

* suppresses generation of externally owned models
* suppresses generation of explicitly ignored models
* injects imports for externally mapped contract types
* injects envelope metadata for wrapper templates
* preserves wrapper semantics using OpenAPI vendor extensions

The result is generated code that stays aligned with the server-side projection model.

---

## How It Works

At a high level:

```text
OpenAPI
   ↓
GenericAwareJavaCodegen
   ↓
Contract-aware filtering
   ↓
Metadata enrichment
   ↓
Generated Java client
```

Internally the extension performs three core operations:

1. identify models that should not be generated
2. remove ignored models from generation graphs
3. enrich wrapper models with contract and envelope metadata

Templates then consume that metadata to generate contract-aligned wrapper types.

---

## BYOC — Bring Your Own Contract Models

Use BYOC when DTOs already exist in a contract module.

Configuration:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Effect:

* `CustomerDto` is treated as externally owned
* it is not generated again
* generated wrappers import and reuse it directly

Requirements:

* the key must match the OpenAPI model name
* the value must be a fully-qualified class name

---

## BYOE — Bring Your Own Envelope

Use BYOE when the contract already defines a custom response envelope.

Configuration:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Effect:

* wrapper templates receive the configured envelope metadata
* generated wrappers extend the configured envelope
* the envelope itself is not generated

This aligns client generation with the server-side BYOE projection model.

---

## What Gets Generated

The module generates thin wrapper types such as:

```java
public class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto> {}
public class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>> {}
public class ApiResponseCustomerDto extends ApiResponse<CustomerDto> {}
```

The module intentionally avoids generating:

* shared envelope classes
* externally owned DTOs
* duplicated contract models
* infrastructure ownership boundaries

---

## How It Is Used

This module is normally not consumed directly.

The intended integration point is:

```text
openapi-generics-java-codegen-parent
```

The parent module is responsible for:

* generator registration
* template extraction
* template patching and overlay
* OpenAPI Generator plugin integration
* generated source registration

This module provides generation behavior.

The parent provides execution orchestration.

---

## What It Does NOT Do

This module does not:

* define the canonical contract
* produce OpenAPI specifications
* own runtime behavior
* orchestrate the generation pipeline
* manage template extraction
* replace OpenAPI Generator
* act as a general-purpose generator fork

It is a focused contract-aware generator extension.

---

## Compatibility

| Component | Supported Versions |
|-----------|-------------------|
| Java | 17+ |
| OpenAPI Generator | 7.x |

This module is build-time only.

---

## Determinism Guarantees

The extension is designed to provide:

* stable model suppression
* deterministic wrapper generation
* explicit contract reuse
* repeatable generation results

Its behavior is deterministic because model ownership is explicit and wrapper semantics are carried through stable vendor extensions.

---

## Architectural Role

Within the generation stack:

```text
openapi-generics-java-codegen-parent
                ↓
      OpenAPI Generator
                ↓
 openapi-generics-java-codegen
                ↓
      Generated Java Client
```

This module sits inside the generator layer.

It determines:

* what should be generated
* what should be reused
* how wrapper metadata is propagated

It does not own build orchestration.

---

## Mental Model

Think of this module as:

> a contract-aware decision layer inside OpenAPI Generator

Not:

* a standalone SDK generator
* a template orchestration module
* a contract definition layer
* a runtime framework

---

## License

MIT — see [LICENSE](../LICENSE)
