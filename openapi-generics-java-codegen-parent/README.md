# openapi-generics-java-codegen-parent

> Build-time orchestration layer for deterministic, contract-aware OpenAPI client generation

`openapi-generics-java-codegen-parent` is the build-time orchestration layer of the OpenAPI Generics client-generation platform.

It assembles and controls the complete generation pipeline by combining:

* upstream OpenAPI Generator
* openapi-generics-java-codegen
* platform templates
* deterministic build orchestration

Its role is precise:

> Inherit one parent, provide your OpenAPI specification and standard generator configuration, and get deterministic, contract-aware Java client generation.

This is the primary integration surface for consumers.

---

## Table of Contents

1. [What Problem It Solves](#what-problem-it-solves)
2. [What It Provides](#what-it-provides)
3. [How It Works](#how-it-works)
4. [Usage](#usage)
5. [Optional Contract Alignment](#optional-contract-alignment)
6. [Compatibility Mode](#compatibility-mode)
7. [What Users Control](#what-users-control)
8. [What Users Should Not Override](#what-users-should-not-override)
9. [Compatibility](#compatibility)
10. [Determinism Guarantees](#determinism-guarantees)
11. [Failure Philosophy](#failure-philosophy)
12. [Mental Model](#mental-model)

---

## What Problem It Solves

Plain OpenAPI Generator setups tend to evolve differently across projects.

That often leads to:

* duplicated envelope models
* inconsistent template behavior
* fragile regeneration workflows
* contract drift between producers and consumers
* generation logic scattered across multiple build configurations

This parent removes that variability by centralizing the entire generation workflow.

---

## What It Provides

By inheriting this parent, a consumer gets:

* upstream template extraction
* deterministic template patching
* template overlay orchestration
* custom generator wiring (`java-generics-contract`)
* generated source registration
* fail-fast validation of generation assumptions
* optional contract alignment (BYOC / BYOE)

The consumer does not assemble or maintain the generation pipeline manually.

---

## How It Works

At build time, the parent orchestrates this flow:

```text
OpenAPI spec
   ↓
Extract upstream templates
   ↓
Validate template structure
   ↓
Patch wrapper insertion points
   ↓
Overlay platform templates
   ↓
Execute contract-aware generator
   ↓
Register generated sources
   ↓
Compile
```

The patch step is validated.

If the upstream template structure changes in a way that prevents wrapper integration, the build fails fast instead of generating potentially incorrect client code.

---

## Usage

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.1.0</version>
  <relativePath/>
</parent>
```

### 2. Configure OpenAPI Generator normally

Use the standard OpenAPI Generator plugin and configure your client as usual.

The parent supplies the orchestration layer and contract-aware generator infrastructure.

### 3. Build

```bash
mvn clean install
```

Generated sources are registered automatically.

---

## Optional Contract Alignment

### BYOC — Bring Your Own Contract Models

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Effect:

* externally owned DTOs are reused
* DTO regeneration is suppressed
* generated wrappers reference the contract type directly

### BYOE — Bring Your Own Envelope

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Effect:

* wrapper classes extend the configured envelope
* envelope metadata is injected into generation templates
* the envelope itself is not regenerated

---

## Compatibility Mode

Fallback mode can be enabled:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

When enabled, orchestration steps are skipped and generation falls back to standard OpenAPI Generator behavior.

Typical use cases:

* debugging
* migration validation
* output comparison

---

## What Users Control

Consumers should control only the normal integration surface:

* input specification
* library selection
* package names
* standard OpenAPI Generator configuration
* BYOC properties
* BYOE properties
* OpenAPI Generator version (within supported ranges)

The parent provides defaults but does not require consumers to use a specific OpenAPI Generator version.

---

## What Users Should Not Override

Consumers should not override platform-controlled orchestration steps:

* template extraction workflow
* template patching workflow
* wrapper insertion mechanism
* template overlay path
* generator registration strategy

These components collectively define the deterministic generation pipeline.

---

## Compatibility

| Component | Supported Versions |
|------------|-------------------|
| Java | 17+ |
| OpenAPI Generator | Supported 7.x releases |

Notes:

* the parent ships with a default OpenAPI Generator version
* consumers may override the generator version when required
* compatibility is validated against supported 7.x releases

---

## Determinism Guarantees

The parent is designed to provide:

* stable build phases
* repeatable template patching
* consistent wrapper generation
* deterministic source registration
* controlled generator wiring

Multiple client projects therefore receive the same contract-aware behavior rather than diverging through local customization.

---

## Failure Philosophy

The parent fails fast when structural assumptions break.

Typical causes:

* upstream template structure changes
* wrapper insertion markers cannot be applied
* platform generation assumptions no longer hold

Principle:

> Incorrect generation is worse than no generation.

---

## Mental Model

Think of this module as:

> the orchestration layer that turns OpenAPI Generator into a contract-aware client-generation pipeline

Not:

* a runtime library
* a template bundle by itself
* a general-purpose OpenAPI Generator replacement

---

## License

MIT — see [LICENSE](../LICENSE)
