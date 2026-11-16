---
layout: default
title: Compatibility & Support
nav_order: 4
---

# Compatibility & support reference

Use this page for supported lines, exact verification inputs and ownership boundaries.
For configuration, use [server](adoption/server-side-adoption.md) or [client](adoption/client-side-adoption.md) adoption.

[Supported lines](#supported-lines) · [Current references](#current-references-and-integration-edges) ·
[Contract scope](#contract-scope) · [Verification](#verification-evidence)

## Supported lines

Published platform artifacts target **Java 17+**. Choose a JDK supported by your framework and build-tool
version; the repository's verification JDKs are listed separately below.

| Projection surface | Spring Boot | Springdoc | Integration |
| --- | --- | --- | --- |
| Boot 3.5 | 3.5.x | 2.8.x–2.10.x | Spring MVC |
| Boot 4 | 4.x | 3.x | Spring MVC |

| Reconstruction surface | OpenAPI Generator | Build line |
| --- | --- | --- |
| Maven | **7.x** | Maven integration |
| Gradle | **7.x**, subject to the integration capability edge below | **9.x** |

Support covers documented platform-owned behavior within these lines. Representative tests/samples
maintain that support; not every patch combination has a separate test stack. Absence from the
reference table does not make a version unsupported.

## Current references and integration edges

**Current/reference** means the repository's exact verification/default input, not a consumer requirement
or support minimum. **Minimum edge** and **negative boundary** describe a specific integration.

| Server reference | Verification Java | Spring Boot | Springdoc |
| --- | --- | --- | --- |
| Boot 3.5 compatibility/type coverage | 21 | 3.5.16 | **2.10.0** |
| Boot 4 Maven and canonical Gradle stack | 25 | 4.1.2 | **3.2.0** |

The public Gradle consumer example and canonical application reference use Boot 4/Jackson 3.
Boot 3.5 compatibility remains verified through Maven application/type-coverage builds. Model-only
Gradle E2E verifies reconstruction/compilation, not a Spring Boot runtime; its Jakarta annotation
selection does not establish framework compatibility.

| Tool surface | Supported line | Minimum edge | Current/reference | Negative boundary |
| --- | --- | --- | --- | --- |
| Maven/core OpenAPI Generator (OAG) | 7.x | No additional platform-wide numeric minimum declared here | **7.27.0** | No platform-wide numeric rejection declared here |
| Gradle OAG integration | 7.x with required capability | **7.25.0** API/verified capability baseline | **7.27.0** verification runtime | **7.24.0**, this integration only |
| Gradle distribution | 9.x | **9.0.0** minimum tested distribution | **9.9.1** wrapper/reference | No additional negative point declared here |

The Gradle integration requires resolvable, non-consumable `openApiGeneratorExtra`; it is a capability
boundary, not a platform-wide numeric version gate. Production official API dependencies remain
`compileOnly` at the minimum OAG API baseline; test runtime selection is independent. Gradle API
compilation follows the build distribution, not a separate minimum-distribution compile target.

A new upstream release is a temporary candidate until verified, then replaces the current reference.
Previous currents are not accumulated in a permanent matrix. Moving current does not move minimum or
negative boundaries. In particular, the Gradle OAG edge does **not** narrow platform-wide 7.x support.

## Version ownership and build coherence

| Surface | Consumer chooses | Platform maintains |
| --- | --- | --- |
| Maven | `openapi-generator.version`, spec, library, packages and normal options | One selected version across official Maven plugin, core, generator and upstream `Java/model.mustache` extraction |
| Gradle | Applies/versions official `org.openapi.generator`; selects tasks and normal options | `withPlugin` integration, templates from the running official classloader, matching platform codegen and selected-task wiring |
| Framework/runtime | Boot, Springdoc, serialization and client-library stack within support policy | Projection/reconstruction interoperability within the documented scope |

The internal platform bill of materials (BOM) does not manage consumer Boot/OAG/plugin versions. Sample pins and starter
compile/test baselines are not universal runtime constraints. Gradle does not implicitly apply, replace
or override the official plugin. Maven and Gradle share reconstruction semantics, not lifecycle settings.

Keep producer and codegen components aligned around the published metadata protocol, especially
`x-api-wrapper-type`. **1.3.0 preserves 1.2.x runtime contracts; no 1.2 contract migration is required.**

Contract backward compatibility is separate from security maintenance. Preserving 1.2.x contracts does
not promise security backports to 1.2.x binaries; see the [security maintenance table](https://github.com/blueprint-platform/openapi-generics/blob/main/SECURITY.md#supported-versions).

## Contract scope

| Supported capability | Boundary |
| --- | --- |
| Built-in envelope | Pure Java shared `ServiceResponse<T>` |
| BYOE (Bring Your Own Envelope) envelope ownership | Concrete application class, one generic parameter, one direct payload field of that parameter's type; no record/multi-parameter/nested payload-slot envelope |
| Built-in containers | `List<T>`, `Set<T>`, shared platform `Page<T>` |
| Registered application containers | Concrete class/record, one type parameter, declared `List<T>`/`Set<T>` item property using it directly; shared type available to the client |
| Optional BYOC (Bring Your Own Contract) reuse | Client schema-name → external Java contract mappings; dependency supplied separately; does not identify the envelope |
| MVC wrapper discovery | Supported synchronous and async wrappers; no arbitrary generic-graph inference |

Java contracts are authoritative; OpenAPI is their projection and the producer/client protocol.
Generated wrappers are concrete transport/deserialization bindings. Keep generated API/invoker and
wrapper classes behind a client adapter rather than making them application contracts.

Out of scope: WebFlux, non-Java server frameworks or reconstruction, arbitrary nested generic graphs,
unregistered container inference, and component names different from resolved Java simple names
(including custom `@Schema(name = "...")`). See [server shape requirements](adoption/server-side-adoption.md#supported-response-shapes).

## Ownership boundary

| OpenAPI Generics owns | Surrounding ecosystem/application owns |
| --- | --- |
| Generic projection protocol and envelope/container identity | Ordinary OAG API/model/authentication/transport generation |
| Metadata validation and contract-aware Java reconstruction, including BYOE/BYOC | HTTP library internals and serialization customization |
| Reconstruction template preparation, required patch-drift detection and generated-source hygiene | Framework configuration, runtime/library choice and application architecture |

Interoperability tests exercise surrounding behavior without transferring its ownership to the platform.
OpenAPI tooling that does not understand these vendor extensions can still consume the document.

## Verification evidence

| Layer | Establishes | Does not establish |
| --- | --- | --- |
| Unit/context tests | Deterministic contract, projection, metadata and codegen component behavior | Every framework/tool patch combination |
| Fixture-based Gradle TestKit | Plugin classpath, task/source wiring, templates and configuration-cache integration | Real codegen or HTTP interoperability |
| Real-codegen E2E / Maven generation samples | Generated-source compilation, suppression/reuse and affected cache paths | Runtime HTTP interoperability for every shape |
| Application/runtime tests | Specific exercised HTTP/deserialization paths; canonical Gradle tests call consumer endpoints through to the producer | Exhaustive type/transport combinations |
| Committed spec drift checks | Reviewed projection stability against snapshots | Runtime client correctness by themselves |

[Samples](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/README.md) separate type/container, multi-spec and transport checks; ordinary Java
RestClient multipart/binary/form scenarios remain upstream-owned. Build/test definitions and executed
results determine coverage; a test file's existence alone does not prove execution.
