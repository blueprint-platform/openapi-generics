# OpenAPI Generics for Spring Boot — Keep Your API Contract Intact End-to-End

[![Build](https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml/badge.svg)](https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/blueprint-platform/openapi-generics?label=release\&logo=github)](https://github.com/blueprint-platform/openapi-generics/releases/latest)
[![CodeQL](https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml/badge.svg)](https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/blueprint-platform/openapi-generics/branch/main/graph/badge.svg)](https://codecov.io/gh/blueprint-platform/openapi-generics)
[![Java](https://img.shields.io/badge/Java-17%2B-red?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x%20%7C%203.5.x%20%7C%204.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.x-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p align="center">
  <img src="docs/images/cover/cover.png" alt="Generics-Aware OpenAPI Contract Lifecycle" width="720"/>
  <br/>
  <strong>
    OpenAPI shouldn’t redefine your contract.
  </strong>
  <br/>
  <em>
   Preserve it end-to-end.
  </em>
</p>

---

## 📑 Table of Contents

* ⚡ [Why this exists (practical impact)](#why-this-exists-practical-impact)
* 🆕 [What’s new in 0.9.x](#whats-new-in-09x)
* ⚡ [Real usage (what you actually do)](#real-usage-what-you-actually-do)
* ⚡ [Quick start (2 minutes)](#quick-start-2-minutes)
* 🔁 [Contract lifecycle model](#contract-lifecycle-model)
* 💡 [Core idea](#core-idea)
* 🏗 [System Architecture Overview](#system-architecture-overview)
* 🔎 [Proof — before vs after](#proof--before-vs-after)
* 🧠 [Design guarantees](#design-guarantees)
* 📦 [Modules](#modules)
* 📘 [Adoption guides](#adoption-guides)
* 🔗 [References](#references)
* 🤝 [Contributing](#contributing)
* 🛡 [License](#license)

---

## Why this exists (practical impact)

In most OpenAPI-based systems:

* generics are flattened or lost
* response envelopes are regenerated per endpoint
* client models drift from server contracts over time

This creates **hidden long-term cost**:

* duplicated DTO hierarchies
* fragile client regeneration
* broken assumptions across services

This project removes that entire class of problems.

> **Define your contract once in Java — reuse it everywhere without drift.**

---

## What’s new in 0.9.x

This is no longer a “template tweak”.

It is now a **contract-aligned generation system with progressive adoption**.

### 1. Bring Your Own Contract (BYOC)

Reuse your own domain models instead of generating them:

```xml
<additionalProperties>
  <additionalProperty>
    openapiGenerics.responseContract.CustomerDto=
    io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Result:

* no duplicated DTOs
* full control over model ownership

---

### 2. Progressive adoption (no lock-in)

Switch between modes safely:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

| Mode              | Behavior                    |
| ----------------- | --------------------------- |
| `false` (default) | Contract-aware generation   |
| `true`            | Standard OpenAPI generation |

> Adopt incrementally — not all-or-nothing.

---

### 3. Deterministic build pipeline

Client generation is a **controlled execution system**:

* upstream templates are patched safely
* contract semantics are injected
* upstream drift fails the build early

---

### 4. End-to-end samples (SB3 + SB4)

Full pipelines are included:

* Spring Boot 3
* Spring Boot 4
* producer → client → consumer

Browse:

* [samples/](samples/)

---

## Real usage (what you actually do)

You do **NOT** copy code from this repo.

You only add two building blocks.

### Server (producer)

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>0.9.x</version>
</dependency>
```

### Client (consumer)

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.9.x</version>
</parent>
```

Optional:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

---

## Quick start (2 minutes)

1. Run a sample producer (Spring Boot 3 or 4):

```bash
cd samples/spring-boot-3/customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

Verify:

* Swagger UI: [http://localhost:8084/customer-service/swagger-ui/index.html](http://localhost:8084/customer-service/swagger-ui/index.html)
* OpenAPI: [http://localhost:8084/customer-service/v3/api-docs.yaml](http://localhost:8084/customer-service/v3/api-docs.yaml)

---

2. Generate a client from the same pipeline:

```bash
cd samples/spring-boot-3/customer-service-client
mvn clean install
```

---

3. Inspect generated output:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

✔ No duplicated envelope
✔ Generics preserved
✔ Contract reused end-to-end

---

> Note: Equivalent pipelines exist under `samples/spring-boot-4/...` for Spring Boot 4.

---

## Contract lifecycle model

```text
Java Contract (SSOT)
        ↓
OpenAPI (projection)
        ↓
Generator (enforcement)
        ↓
Client (contract-aligned)
```

> OpenAPI is a projection — not the source of truth.

---

## Core idea

The response envelope is a **shared contract**, not a generated model.

```text
ServiceResponse<T>
```

Supported:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

Everything else falls back to default OpenAPI behavior.

---

## System Architecture Overview

<p align="center">
  <img src="docs/images/architecture/openapi-generics-architecture.svg"
       alt="OpenAPI Generics contract-first architecture flow"
       style="max-width:900px; width:100%;"/>
</p>

---

## Proof — before vs after

### Before

<p align="center">
  <img src="docs/images/proof/generated-client-wrapper-before.png" width="820"/>
</p>

```java
class ServiceResponsePageCustomerDto {
  PageCustomerDto data;
  Meta meta;
}
```

### After

<p align="center">
  <img src="docs/images/proof/generated-client-wrapper-after.png" width="820"/>
</p>

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

---

## Design guarantees

* ✔ Contract identity is preserved
* ✔ Generics are preserved (within supported scope)
* ✔ Client generation is deterministic
* ✔ External models are reusable
* ✔ Upstream drift is detected early

---

## Modules

* [openapi-generics-contract](openapi-generics-contract/README.md)
* [openapi-generics-server-starter](openapi-generics-server-starter/README.md)
* [openapi-generics-java-codegen](openapi-generics-java-codegen/README.md)
* [openapi-generics-java-codegen-parent](openapi-generics-java-codegen-parent/README.md)

---

## Adoption guides

* [Server-Side Adoption](docs/adoption/server-side-adoption.md)
* [Client-Side Adoption](docs/adoption/client-side-adoption.md)

---

## References

- 📘 **Adoption Guide (GitHub Pages)**  
  [Spring Boot OpenAPI Generics — Adoption Guide](https://blueprint-platform.github.io/openapi-generics/)

- ✍️ **Medium Article**  
  [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

- 📄 **RFC 9457**  
  [Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)

---

## Contributing

Contributions are welcome — especially:

* architectural discussions
* real-world usage feedback
* edge cases and integration insights

If you're evaluating or using the project, your perspective is valuable.

👉 Open an issue: https://github.com/blueprint-platform/openapi-generics/issues  
👉 Start a discussion: https://github.com/blueprint-platform/openapi-generics/discussions

---

## License

MIT — see [LICENSE](LICENSE)

---

**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)
