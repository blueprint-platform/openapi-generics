# openapi-generics-platform-bom

> Internal dependency alignment for shared OpenAPI Generics platform components

`openapi-generics-platform-bom` is an internal Bill of Materials (BOM) used to align the small set of dependencies that must remain consistent across OpenAPI Generics platform modules.

The BOM is intentionally minimal.

Unlike traditional platform BOMs, it does not attempt to control application runtime stacks, Spring Boot versions, servlet APIs, or OpenAPI Generator versions used by consumers.

Its responsibility is limited to aligning platform-owned dependencies that must remain consistent across OpenAPI Generics modules.

---

## Table of Contents

1. [Purpose](#purpose)
2. [What It Controls](#what-it-controls)
3. [What It Does Not Control](#what-it-does-not-control)
4. [How It Is Used](#how-it-is-used)
5. [Why It Is Intentionally Small](#why-it-is-intentionally-small)
6. [Not Intended For Direct Consumption](#not-intended-for-direct-consumption)
7. [Architectural Role](#architectural-role)
8. [Design Constraints](#design-constraints)
9. [Versioning Strategy](#versioning-strategy)
10. [Mental Model](#mental-model)

---

## Purpose

OpenAPI Generics consists of several platform modules that collaborate to provide contract-aware OpenAPI projection and client generation.

Some dependencies are shared across platform modules and should remain aligned to avoid platform-level drift.

This BOM exists to:

* centralize versions for platform-owned dependencies
* keep contract and projection components aligned
* provide a stable dependency baseline across platform modules
* reduce internal version drift

The BOM is not responsible for application runtime decisions.

---

## What It Controls

The BOM manages only dependencies that are considered part of the OpenAPI Generics platform itself.

### Platform Modules

* `openapi-generics-contract`

### Projection Infrastructure

* `springdoc-openapi-starter-common`

These dependencies are shared by platform modules and benefit from centralized alignment.

---

## What It Does Not Control

The BOM intentionally does not manage:

* Spring Boot versions
* Jakarta versions
* servlet container versions
* application runtime dependencies
* consumer dependency management
* OpenAPI Generator versions
* OpenAPI Generator plugin versions

These decisions belong to the consuming application.

---

## How It Is Used

The BOM is imported by platform modules that require aligned platform dependencies.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.blueprint-platform</groupId>
            <artifactId>openapi-generics-platform-bom</artifactId>
            <version>${openapi-generics.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Current consumers include:

* `openapi-generics-server-starter`
* `openapi-generics-java-codegen-parent`

---

## Why It Is Intentionally Small

Earlier platform versions aligned a broader set of dependencies.

As the architecture evolved, OpenAPI Generator ownership moved into `openapi-generics-java-codegen-parent`, allowing consumers to override generator versions when needed.

The BOM now focuses only on dependencies that genuinely require platform-wide alignment.

---

## Not Intended For Direct Consumption

Application projects should normally consume:

* `openapi-generics-server-starter`
* `openapi-generics-java-codegen-parent`

rather than importing this BOM directly.

---

## Architectural Role

| Layer                           | Module                                 |
|---------------------------------|----------------------------------------|
| Contract Authority              | `openapi-generics-contract`            |
| OpenAPI Projection              | `openapi-generics-server-starter`      |
| Client Generation Orchestration | `openapi-generics-java-codegen-parent` |
| Dependency Alignment            | `openapi-generics-platform-bom`        |

The BOM exists only to provide dependency alignment.

---

## Design Constraints

The BOM should:

* manage only platform-owned dependencies
* avoid controlling application runtime choices
* avoid controlling framework versions
* avoid controlling generator execution versions
* remain small and predictable

---

## Versioning Strategy

The BOM version represents the dependency baseline associated with a particular OpenAPI Generics release.

Platform modules are expected to align with the BOM version of the same release.

---

## Mental Model

Think of this module as:

> the dependency alignment layer for platform-owned components

Not:

* a Spring Boot version manager
* an OpenAPI Generator version manager
* a runtime platform
* a feature module

---

## License

MIT — see [LICENSE](../LICENSE)