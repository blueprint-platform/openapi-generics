# openapi-generics-java-codegen

> Generics-aware OpenAPI Generator extension for **contract-aligned Java clients**

`openapi-generics-java-codegen` is a **custom OpenAPI Generator extension** that enforces **contract-first client generation**.

It does not try to be smarter than your contract.
It ensures the generator **does not break it**.

> Prevent OpenAPI Generator from redefining platform-owned models and enforce contract-aligned output.

This module is **build-time only** and is typically consumed via `openapi-generics-java-codegen-parent`.

---

## 📑 Table of Contents

* 🎯 [Purpose](#-purpose)
* 🧠 [Core Idea](#-core-idea)
* ⚙️ [What It Does](#️-what-it-does)
* 🧱 [Result](#-result)
* 🧩 [External Model Mapping (BYOC)](#-external-model-mapping-byoc)
* 🧩 [Template Integration](#-template-integration)
* 🔗 [How It Is Used](#-how-it-is-used)
* 🚫 [Not Intended For Direct Use](#-not-intended-for-direct-use)
* 🔗 [Compatibility Matrix](#-compatibility-matrix)
* 🔒 [Determinism Guarantees](#-determinism-guarantees)
* ⚠️ [Design Constraints](#️-design-constraints)
* 🧠 [Mental Model](#-mental-model)
* 🔗 [Related Modules](#-related-modules)
* 📜 [License](#-license)

---

## 🎯 Purpose

Default OpenAPI Generator behavior:

* regenerates response envelopes per endpoint
* flattens or loses generic semantics
* creates model drift between server and client

This module prevents that.

It ensures that generated clients:

* reuse canonical contract types (`ServiceResponse`, `Meta`, `Page`, etc.)
* do NOT regenerate platform-owned models
* remain aligned with server-side contract semantics

It acts as an **enforcement layer** between OpenAPI and generated Java code.

---

## 🧠 Core Idea

> OpenAPI is a projection — not the source of truth

This generator enforces that rule at build time:

* platform-owned models must NOT be generated
* OpenAPI metadata must be interpreted, not materialized

If OpenAPI contains structure that already exists in the contract:

> it is mapped back — not regenerated

---

## ⚙️ What It Does

### 1) External model registry (BYOC)

Reads `additionalProperties` and registers mappings:

```text
openapiGenerics.responseContract.CustomerDto=io.example.contract.CustomerDto
```

Backed by: `ExternalModelRegistry`

Effect:

* marks models as **externally provided**
* prevents generation of those models
* enables import injection in wrappers

---

### 2) Ignore decision (dual source)

A model is ignored if:

* `x-ignore-model=true` is present in schema extensions
* OR it is registered as an external model

Backed by: `ModelIgnoreDecider`

---

### 3) 3-phase suppression strategy

#### Phase A — MARK

* during `fromModel`
* mark models as ignored

#### Phase B — LOCAL FILTER

* in `postProcessModels`
* remove ignored models from the current batch

#### Phase C — GLOBAL REMOVE

* in `postProcessAllModels`
* remove ignored models from the full graph

---

### 4) Import hygiene

* removes imports that reference ignored models
* injects correct imports for external types into wrapper models

Backed by: `ExternalImportResolver`

---

## 🧱 Result

Generated code:

* references `openapi-generics-contract`
* does NOT duplicate envelope types
* preserves generic semantics (`ServiceResponse<T>`, `Page<T>`)
* remains deterministic and stable

---

## 🧩 External Model Mapping (BYOC)

Optional but powerful.

### Configuration

```xml
<additionalProperties>
  <additionalProperty>
    openapiGenerics.responseContract.CustomerDto=
    io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

### Behavior

* prevents generation of `CustomerDto`
* injects correct import into wrappers
* reuses your domain model directly

### Important

* mapping key must match **OpenAPI model name**
* value must be **fully-qualified class name (FQCN)**

---

## 🧩 Template Integration

Templates live under:

```text
META-INF/openapi-generics/templates
```

### Core template: `api_wrapper.mustache`

Responsibilities:

* generate thin wrapper classes
* extend `ServiceResponse<T>`
* apply container semantics (`Page<T>`)

Example output:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

---

## 🔗 How It Is Used

This module is **not used directly**.

It is wired via:

```text
openapi-generics-java-codegen-parent
```

The parent POM:

* registers this generator (`java-generics-contract`)
* injects templates
* configures the OpenAPI Generator plugin

---

## 🚫 Not Intended For Direct Use

End users should NOT:

* reference this module directly
* configure it manually
* override generator behavior

Instead:

> Use the codegen parent — it orchestrates everything

---

## 🔗 Compatibility Matrix

| Component         | Supported Versions |
| ----------------- | ------------------ |
| Java              | 17+                |
| OpenAPI Generator | 7.x                |

Notes:

* `restclient` library requires **OpenAPI Generator ≥ 7.6.0**
* Module is build-time only (no Spring/runtime dependency)

---

## 🔒 Determinism Guarantees

* ✔ No duplication of contract models
* ✔ Stable model graph
* ✔ Consistent generation output
* ✔ Preservation of generic semantics

Mechanisms:

* explicit ignore rules
* controlled graph pruning
* deterministic template application

---

## ⚠️ Design Constraints

* depends on vendor extensions (`x-*` fields)
* assumes contract-first design
* tightly coupled to platform semantics

It is NOT a general-purpose generator.

---

## 🧠 Mental Model

> A guardrail inside OpenAPI Generator that prevents contract drift

Not:

* a standalone generator
* a user-facing tool

---

## 🔗 Related Modules

| Module                                 | Role                               |
| -------------------------------------- | ---------------------------------- |
| `openapi-generics-contract`            | Defines canonical models           |
| `openapi-generics-server-starter`      | Produces OpenAPI projection        |
| `openapi-generics-java-codegen`        | Enforces generation rules          |
| `openapi-generics-java-codegen-parent` | Orchestrates build-time generation |

---

## 📜 License

MIT License
