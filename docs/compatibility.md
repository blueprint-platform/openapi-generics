---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page defines the officially supported runtime and build-time combinations for **OpenAPI Generics 1.0.2**.

It is the authoritative compatibility reference for the platform — the contract between what we test, what we publish, and what consumers can rely on.

---

## Contents

- [1. Supported scope](#1-supported-scope)
- [2. Compatibility matrix](#2-compatibility-matrix)
  - [2.1 Runtime — server-side OpenAPI projection](#21-runtime--server-side-openapi-projection)
  - [2.2 Build-time — client generation](#22-build-time--client-generation)
- [3. Verification basis](#3-verification-basis)
- [4. Support policy](#4-support-policy)
- [5. Scope clarifications](#5-scope-clarifications)
- [6. Notes](#6-notes)

---

## 1. Supported scope

OpenAPI Generics currently supports:

- **Java 17+**
- **Spring Boot** (WebMvc-based applications)
- **springdoc-openapi** WebMvc starter
- **OpenAPI Generator 7.x**
- **Maven**-based client generation

The platform scope is intentionally explicit. Each item above is tested against samples, documented in adoption guides, and covered by the compatibility matrix below.

Out of scope:

- WebFlux (reactive Spring)
- Gradle-native build support
- non-Java server frameworks
- non-WebMvc springdoc variants

> Out-of-scope items are not "future work" promises. They are deliberate exclusions — see [Architecture — design decisions and their trade-offs](architecture/architecture.md#design-decisions-and-their-trade-offs) for the reasoning behind each one.

---

## 2. Compatibility matrix

### 2.1 Runtime — server-side OpenAPI projection

| Java | Spring Boot | springdoc-openapi | Scope          | Status     |
| ---- | ----------- | ----------------- | -------------- | ---------- |
| 17+  | 3.4.x       | 2.8.x             | WebMvc starter | Supported  |
| 17+  | 3.5.x       | 2.8.x             | WebMvc starter | Supported  |
| 17+  | 4.x         | 3.x               | WebMvc starter | Supported  |

A combination is **supported** when:

- it is verified end-to-end through repository samples
- the projection pipeline runs without runtime workarounds
- the published artifacts resolve cleanly under the listed Spring Boot baseline

### 2.2 Build-time — client generation

| Java | OpenAPI Generator | Build tool | Status     |
| ---- | ----------------- | ---------- | ---------- |
| 17+  | 7.x               | Maven      | Supported  |

#### Upstream template governance — the zero-drift guarantee

The platform does **not** rely on static template forks or manual template overrides for the OpenAPI Generator.

To ensure long-term compatibility with the `7.x` line, the client generation pipeline applies a **build-time surgical patch**:

- the generator's core upstream `model.mustache` is dynamically extracted during the build
- a semantic, non-destructive patch injects the wrapper-aware branch into the foundational schema loop
- the platform's `api_wrapper.mustache` partial is overlaid on top
- the patched output drives the generator via `<templateDirectory>` — **not** a frozen snapshot

**Fail-fast safety.** The pipeline verifies the patch signature immediately after applying it. If an upstream OpenAPI Generator upgrade alters the foundational template structure, the system does not produce silent errors or corrupted clients — the build fails fast and explicitly:

```
OpenAPI template patch FAILED — upstream model.mustache structure changed.
```

This guarantees that the generated client is always correctly aligned with the upstream generator version you choose to run, and that upstream drift is detected at **build time**, not at **runtime**.

For pipeline internals: [Architecture — client-side build pipeline](architecture/architecture.md#client-side-the-build-pipeline).

---

## 3. Verification basis

The supported combinations above are validated through the sample pipelines included in this repository.

Current sample coverage:

- Spring Boot 3 producer / client / consumer
- Spring Boot 4 producer / client / consumer

Reference sample modules:

```text
samples/
  spring-boot-3/customer-service
  spring-boot-3/customer-service-client
  spring-boot-3/customer-service-consumer
  spring-boot-4/customer-service
  spring-boot-4/customer-service-client
  spring-boot-4/customer-service-consumer
```

These samples verify the platform at the level it owns:

- server-side OpenAPI projection (envelope detection, vendor extension stamping, contract integrity)
- client-side contract-aware generation (template patch, wrapper materialization, BYOE/BYOC resolution)
- end-to-end contract alignment (producer → spec → client → consumer)

Each sample stack is runnable with a single `docker compose up --build -d` from its directory. See [samples/README](../samples/README.md) for the full run-and-verify flow.

> Sample build configurations target Java 21 by default — this is the **sample baseline**, not the platform baseline. The platform itself supports Java 17+, and the published artifacts compile against the Java 17 baseline.

---

## 4. Support policy

### Officially supported

A combination is officially supported when **all three** conditions hold:

- it falls within the compatibility matrix above
- it is covered by repository samples or maintained verification paths
- it stays within the documented platform scope

If any of the three is missing, the combination is best-effort — it may work, but is not part of the platform contract.

### Platform boundary

OpenAPI Generics supports the platform boundary it owns. Inside that boundary:

- generics-aware OpenAPI projection
- contract-aware client generation
- deterministic wrapper reconstruction
- BYOE / BYOC resolution
- fail-fast detection of upstream template drift

Outside that boundary, behavior remains the responsibility of the surrounding ecosystem:

- underlying OpenAPI Generator internals (e.g. how the `restclient` library generates HTTP code)
- library-specific runtime behavior (Jackson configuration, HTTP client tuning)
- framework or library interactions unrelated to platform logic

**Example.** Spring Boot 4.x + OpenAPI Generator 7.x is supported as a platform combination — the projection pipeline runs, the client patch holds, and wrappers extend the contract correctly. Lower-level interoperability concerns between unrelated libraries (e.g. a Jackson module conflicting with a custom HTTP filter) remain outside the platform contract unless explicitly documented here.

### What "supported" does NOT mean

- it does not mean every conceivable downstream library combination is tested
- it does not mean upstream OpenAPI Generator regressions are absorbed silently — they fail the build
- it does not mean migration between Spring Boot major versions is automatic — see runtime matrix above

---

## 5. Scope clarifications

### Spring WebMvc only

Server-side support currently targets:

```xml
<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
```

This is the officially supported publication model. The MVC discovery strategy (`MvcResponseTypeDiscoveryStrategy`) is the structural reason WebFlux is not currently supported — see [Architecture — wiring](architecture/architecture.md#wiring) for what would need to change to add reactive support.

### Maven only

The contract-aware client generation pipeline is currently packaged and supported through the Maven parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.2</version>
</parent>
```

Gradle support is currently out of scope. The five-stage pipeline (extract → patch → overlay → generate → register sources) is implemented as Maven plugin orchestration; a Gradle equivalent would require a parallel implementation, not a port.

### Java-and-Spring-only by design

Cross-language parity is an explicit non-goal. The platform's value proposition — preserving Java generics across the OpenAPI projection — does not translate uniformly to TypeScript, Python, or Go generators. Other-language clients consuming a Java-server-emitted spec see plain OpenAPI without contract awareness — they do not break, they simply do not benefit from the generic-preservation layer.

---

## 6. Notes

- `ServiceResponse<T>` is the default contract envelope, shipped via `openapi-generics-contract`. BYOE replaces it with your own envelope without leaving the supported matrix.
- BYOE and BYOC operate within the same supported runtime/build matrix — they are configuration inputs, not separate compatibility tracks.
- Support is defined by **tested platform behavior**, not by every possible downstream library combination. If your scenario is inside the matrix and the samples reproduce it, you are supported. If your scenario is outside the matrix, [open a discussion](https://github.com/blueprint-platform/openapi-generics/discussions) — the boundary is documented, but it can move with evidence.
- Questions about specific version pairings not listed above belong in [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions) rather than issues, until they are formally added to the matrix.

---

## Further reading

- [Adoption Guides](index.md) — server-side and client-side integration walkthroughs
- [Architecture](architecture/architecture.md) — pipeline internals and design decisions
- [README](../README.md) — project overview, problem statement, value proposition