---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page defines the officially supported runtime and build-time combinations for **OpenAPI Generics 1.0.0**.

It is the authoritative compatibility reference for the platform.

---

## Contents

- [1. Supported Scope](#1-supported-scope)
- [2. Compatibility Matrix](#2-compatibility-matrix)
    - [2.1 Runtime (Server-Side OpenAPI Projection)](#21-runtime-server-side-openapi-projection)
    - [2.2 Build-Time (Client Generation)](#22-build-time-client-generation)
- [3. Verification Basis](#3-verification-basis)
- [4. Support Policy](#4-support-policy)
- [5. Scope Clarifications](#5-scope-clarifications)
- [6. Notes](#6-notes)

---

## 1. Supported Scope

OpenAPI Generics currently supports:

* **Java 17+**
* **Spring Boot (WebMvc-based applications)**
* **springdoc-openapi WebMvc starter**
* **OpenAPI Generator 7.x**
* **Maven-based client generation**

The platform scope is intentionally explicit.

Out of scope:

* WebFlux
* Gradle-native build support
* non-Java server frameworks
* non-WebMvc springdoc variants

---

## 2. Compatibility Matrix

### 2.1 Runtime (Server-Side OpenAPI Projection)

| Java | Spring Boot | springdoc-openapi | Scope          | Status    |
| ---- | ----------- | ----------------- | -------------- | --------- |
| 17+  | 3.4.x       | 2.8.x             | WebMvc starter | Supported |
| 17+  | 3.5.x       | 2.8.x             | WebMvc starter | Supported |
| 17+  | 4.x         | 3.x               | WebMvc starter | Supported |

### 2.2 Build-Time (Client Generation)

| Java | OpenAPI Generator | Build Tool | Status    |
| ---- | ----------------- | ---------- | --------- |
| 17+  | 7.x               | Maven      | Supported |

#### Upstream Template Governance (Zero-Drift Guarantee)

The platform does **not** rely on static template forks or brittle manual overrides for the OpenAPI Generator.

To ensure long-term compatibility with the `7.x` line, the client generation pipeline employs a **build-time surgical patch**:
* The generator's core upstream templates are dynamically extracted during the build.
* A semantic, non-destructive patch is applied to inject wrapper logic into the foundational schema loop.
* **Fail-Fast Safety:** The pipeline verifies the patch signature immediately. If an upstream OpenAPI Generator upgrade alters the foundational template structure, the system does not produce silent errors or corrupted clients — **the build fails fast and explicitly.**

This guarantees that the generated client is always correctly aligned with the upstream generator version you choose to run.

---

## 3. Verification Basis

The supported combinations above are validated through the sample pipelines included in this repository.

Current sample coverage includes:

* Spring Boot 3 producer / client / consumer
* Spring Boot 4 producer / client / consumer

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

* server-side OpenAPI projection
* client-side contract-aware generation
* end-to-end contract alignment

---

## 4. Support Policy

### Officially supported

A combination is considered officially supported when:

* it falls within the compatibility matrix above
* it is covered by repository samples or maintained verification paths
* it stays within the documented platform scope

### Platform boundary

OpenAPI Generics supports the platform boundary it owns.

This includes:

* generics-aware OpenAPI projection
* contract-aware client generation
* deterministic wrapper reconstruction

Behavior outside that boundary remains the responsibility of the surrounding ecosystem, including:

* underlying OpenAPI Generator internals
* library-specific runtime behavior
* framework/library interactions unrelated to platform logic

Example:

* Spring Boot 4.x + OpenAPI Generator 7.x is supported as a platform combination
* lower-level interoperability details between third-party libraries remain outside the platform contract unless explicitly documented here

---

## 5. Scope Clarifications

### Spring WebMvc only

Server-side support currently targets:

```xml
<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
```

This is the officially supported publication model.

### Maven only

The contract-aware client generation pipeline is currently packaged and supported through the Maven parent:

* `openapi-generics-java-codegen-parent`

Gradle support is currently out of scope.

---

## 6. Notes

* `ServiceResponse<T>` remains the default contract model
* BYOE and BYOC operate within the same supported runtime/build matrix
* Support is defined by tested platform behavior, not by every possible downstream library combination
