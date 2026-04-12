---
title: openapi-generics-client-side — Summary (0.9.x)
nav_exclude: true
---

# openapi-generics Client Side — Summary

## What the client side is now
The client side is no longer just a template tweak.

It is now a **deterministic Java client generation layer** built on top of OpenAPI Generator, designed to keep generated clients aligned with the external contract model.

Its job is to ensure that shared contract models are **reused**, not regenerated, while wrapper semantics remain visible and usable in generated client code.

---

## Client-side architecture
The client side is split into **two distinct layers**:

### 1. `openapi-generics-java-codegen`
This is the **custom generator extension**.

It extends `JavaClientCodegen` and adds generics-aware behavior:
- registers external contract models
- ignores models that must not be generated
- injects required imports for external types
- removes ignored models from the generation graph

So this module is the **codegen behavior layer**.

---

### 2. `openapi-generics-java-codegen-parent`
This is the **build orchestration layer**.

It standardizes how generation is executed:
- extracts upstream OpenAPI Generator templates
- patches `model.mustache`
- overlays local wrapper templates
- wires the custom generator into Maven
- adds generated sources to compilation automatically

So this module is the **generation infrastructure layer**.

---

## Core generator behavior

### `GenericAwareJavaCodegen`
This is the main custom generator.

Responsibilities:
- initialize external model registry from `additionalProperties`
- detect models that should be ignored
- remove ignored models from generated output
- inject external imports into wrapper models
- clean invalid/self imports

This means the generator is no longer passive.
It actively enforces the contract-aligned generation model.

---

### `ExternalModelRegistry`
This holds mappings like:

`openapiGenerics.externalModel.CustomerDto=io.example.contract.CustomerDto`

Purpose:
- declare that a schema name maps to an already existing Java class
- treat that model as externally provided
- allow wrappers to import that real class instead of generating duplicates

---

### `ModelIgnoreDecider`
This decides whether a model must be excluded from generation.

A model is ignored if:
- `x-ignore-model=true` exists
- or the model is registered as external

This is the client-side continuation of the server-side anti-duplication contract.

---

### `ExternalImportResolver`
This handles wrapper imports.

Flow:
- detect wrapper model via `x-api-wrapper`
- resolve inner type from:
    - `x-data-item`
    - or `x-api-wrapper-datatype`
- look up external FQCN in the registry
- inject it into `x-extra-imports` for template usage

This is what allows generated wrappers to **reference external contract classes correctly**.

---

## Wrapper generation model
The actual wrapper class generation is completed by the custom template layer, especially `api_wrapper.mustache`.

That template:
- imports externally mapped model types via `x-extra-imports`
- imports canonical shared contract types such as `ServiceResponse`
- imports container types such as `Page` when `x-data-container` exists
- optionally applies extra annotations via `x-class-extra-annotation`
- generates the wrapper as a thin subclass of the canonical envelope

Effective result:

- plain shape:
    - `ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>`

- container-aware shape:
    - `ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>>`

So the wrapper is **not a duplicated structural model**.
It becomes a **thin typed extension of the shared canonical contract**.

This is a critical part of the design:
- structure lives in the shared contract
- wrapper typing lives in generated code
- external DTO ownership remains external

---

## Template strategy
The parent POM builds a controlled generation pipeline:

1. unpack upstream `model.mustache`
2. unpack local openapi-generics templates
3. patch upstream template to recognize `x-api-wrapper`
4. overlay local wrapper template files
5. run OpenAPI Generator with the custom generator
6. add generated Java sources to the Maven compile path

This means:
- upstream OpenAPI Generator is still used
- but wrapper handling is inserted in a controlled, verifiable way

---

## Safety model
The parent build includes a structural safety check.

After patching `model.mustache`, it verifies that the patch marker exists.
If upstream template structure changes, the build fails.

That means template customization is not “best effort”.
It is **guarded against silent upstream breakage**.

---

## How a sample client uses it
A sample client project inherits from:

`openapi-generics-java-codegen-parent`

Then it configures the OpenAPI plugin with:

- `generatorName=java-generics-contract`
- input OpenAPI spec
- normal Java client package settings
- `library=restclient`
- external model mappings via `additionalProperties`

Example:
- `CustomerDto` is declared as external
- generator does not regenerate it
- generated wrappers import and use the contract-provided `CustomerDto`

So the sample proves the intended usage model:
- spec is generated from server side
- client generation uses generics-aware codegen
- shared DTOs come from contract dependency
- wrapper classes are generated as thin typed extensions
- duplicated infrastructure/shared models are suppressed

---

## What this client side achieves
The client side now guarantees:

- external shared models are reused, not duplicated
- wrapper semantics survive generation
- wrapper classes extend the canonical shared contract types
- generated client code stays aligned with the published contract
- template behavior is deterministic and build-controlled
- upstream generator drift is detected early

---

## Current identity of the client side
The client side is best understood as:

> A deterministic Java code generation layer that turns vendor-extended OpenAPI into contract-aligned clients by reusing shared contract models and generating thin typed wrapper subclasses.

---

## Final synthesis
The client side now does the following:

- consumes the semantic extensions produced by the server side
- suppresses generation of shared/infrastructure models
- injects imports for externally owned contract types
- generates wrapper classes as thin extensions of canonical contract envelopes
- preserves container semantics such as `Page<T>`
- standardizes generation through a reusable parent POM
- protects template customization against upstream breakage

In short:

> The client side has evolved from “template customization” into the **contract-aligned Java generation engine** of openapi-generics.