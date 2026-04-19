# OpenAPI Generics for Spring Boot — Keep Your API Contract Intact End-to-End

[![Build](https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml/badge.svg)](https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml)
[![CodeQL](https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml/badge.svg)](https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml)
[![Release](https://img.shields.io/github/v/release/blueprint-platform/openapi-generics?label=release&logo=github)](https://github.com/blueprint-platform/openapi-generics/releases/latest)

[![Java](https://img.shields.io/badge/Java-17%2B-lightgrey?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x%20%E2%86%92%204.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.x-blue?logo=openapiinitiative)](https://openapi-generator.tech/)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p align="center">
  <img src="docs/images/cover/cover.png" alt="Generics-Aware OpenAPI Contract Lifecycle" width="720"/>
</p>

---

## Table of Contents

* [Why this exists (practical impact)](#why-this-exists-practical-impact)
* [Key features in 1.0.0 (GA)](#key-features-in-100-ga)
* [Real usage (what you actually do)](#real-usage-what-you-actually-do)
* [Quick start (2 minutes)](#quick-start-2-minutes)
* [Compatibility](#compatibility)
* [Contract lifecycle model](#contract-lifecycle-model)
* [Core idea](#core-idea)
* [System Architecture Overview](#system-architecture-overview)
* [Proof — before vs after](#proof--before-vs-after)
* [Design guarantees](#design-guarantees)
* [Modules](#modules)
* [References](#references)
* [Contributing](#contributing)
* [License](#license)

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

## Key features in 1.0.0 (GA)

This is no longer a template-level customization.

It is now a **contract-aligned generation system with progressive adoption** — designed to adapt to existing architectures instead of forcing new ones.

---

### 1. Bring Your Own Envelope (BYOE)

Use your **existing response envelope** without migrating to `ServiceResponse`.

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Result:

* no forced migration to a new envelope type
* your response model remains intact
* existing contracts continue to work as-is

Behavior:

* If not configured → `ServiceResponse<T>` is used (default)
* If configured → your envelope type becomes the base of generated wrappers

This removes the most common adoption blocker:

> "Do we need to change our response model to use this?"

Answer: **No.**

---

#### How it works

The platform does not generate your envelope.

It **reuses it as a contract dependency** — both on the server and client side.

Two usage paths are supported:

**1. Springdoc-based (automatic)**

* Server starter detects your envelope
* OpenAPI is enriched with required semantics automatically
* No manual schema work is required

**2. Spec-first / manual OpenAPI**

* Teams can define wrapper schemas directly in OpenAPI
* The wrapper expresses the relationship between the envelope and the payload
* Minimal semantics are added to indicate that the schema represents a generic wrapper
* Client generation reconstructs the correct generic structure from this definition

Example (simplified):

```yaml
ApiResponseLicenseAccessResponse:
  type: object
  properties:
    data:
      $ref: "#/components/schemas/LicenseAccessResponse"
  x-api-wrapper: true
  x-api-wrapper-datatype: LicenseAccessResponse
```

Optional: internal envelope-related models can be marked to avoid regeneration:

```yaml
ApiError:
  type: object
  properties:
    errorCode:
      type: string
    message:
      type: string
  x-ignore-model: true
```

> Springdoc is the easiest path — not the only one.

In both approaches, the outcome is the same:

* the envelope remains your contract
* OpenAPI acts as a projection
* generated clients preserve the original type structure

---

### 2. Bring Your Own Contract (BYOC)

Reuse your own domain models instead of generating them:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Result:

* no duplicated DTOs
* full control over model ownership

---

### 3. Progressive adoption (client-side only)

Switch generation modes through the client build configuration:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

| Mode              | Behavior                    |
| ----------------- | --------------------------- |
| `false` (default) | Contract-aware generation   |
| `true`            | Standard OpenAPI generation |

---

### 4. Deterministic build pipeline

Client generation is a **controlled execution system**:

* upstream templates are patched safely
* contract semantics are injected
* upstream drift fails the build early

---

### 5. End-to-end samples (Spring Boot 3 & 4)

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
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Client (consumer)

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.0</version>
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

- No duplicated envelope
- Generics preserved
- Contract reused end-to-end

---

> Note: Equivalent pipelines exist under `samples/spring-boot-4/...` for Spring Boot 4.

---

## Compatibility

OpenAPI Generics is currently verified with:

- **Java:** 17+
- **Spring Boot:** 3.4.x, 3.5.x, 4.x
- **springdoc-openapi:** 2.8.x (Spring Boot 3.x), 3.x (Spring Boot 4.x)
- **OpenAPI Generator:** 7.x
- **Server scope:** Spring WebMvc (`springdoc-openapi-starter-webmvc-ui`)

See the full compatibility matrix and support policy: [Compatibility & Support Policy](docs/compatibility.md)

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
YourEnvelope<T>
```

> `ServiceResponse<T>` is the **default contract provided by the platform** — not a restriction.

The system is designed around a simple principle:

> Define your contract once. Preserve it end-to-end.

---

### What this means in practice

* The response envelope is **not regenerated per endpoint**
* The same contract is reused across:

  * server responses
  * OpenAPI projection
  * generated clients
* Client models **extend the contract**, instead of redefining it

Result:

* no envelope duplication
* no drift between server and client
* a stable, predictable type system

---

### Supported shapes (deterministic scope)

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
YourEnvelope<T>
```

These shapes are **explicitly supported and enforced** to guarantee:

* deterministic OpenAPI generation
* predictable client reconstruction
* zero ambiguity in generic resolution

> **Note:** BYOE supports envelopes with a single direct generic payload <br>
> (e.g. `YourEnvelope<T>`). <br>
> Nested payloads like `YourEnvelope<Page<T>>` are out of scope <br>
> and fail fast at application startup.

---

### Important: default ≠ mandatory

While `ServiceResponse<T>` is the canonical default, the platform does **not require you to use it**.

With BYOE (Bring Your Own Envelope):

```text
YourEnvelope<T>
```

can be used instead, without changing the overall model.

The behavior remains the same:

* OpenAPI is still a projection
* Generics are still preserved
* Clients still reconstruct the contract shape

---

## System Architecture Overview

<p align="center">
  <img src="docs/images/architecture/openapi-generics-architecture.svg"
       alt="OpenAPI Generics contract-first architecture flow"
       style="max-width:900px; width:100%;"/>
</p>

For internal architecture and design decisions, see [Architecture](docs/architecture/platform.md).

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
* ✔ Contract ownership is preserved (including the response envelope)
* ✔ Generics are preserved (within supported scope)
* ✔ Client generation is deterministic
* ✔ No contract duplication (external models are reused, not regenerated)
* ✔ Upstream drift is detected early

---

## Modules

* [openapi-generics-contract](openapi-generics-contract/README.md)
* [openapi-generics-server-starter](openapi-generics-server-starter/README.md)
* [openapi-generics-java-codegen](openapi-generics-java-codegen/README.md)
* [openapi-generics-java-codegen-parent](openapi-generics-java-codegen-parent/README.md)
* [openapi-generics-platform-bom](openapi-generics-platform-bom/README.md)

---

## References

- **Adoption Guide (GitHub Pages)**  
  [Spring Boot OpenAPI Generics — Adoption Guide](https://blueprint-platform.github.io/openapi-generics/)

- **Medium Article**  
  [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

- **RFC 9457**  
  [Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)

---

## Contributing

Contributions are welcome — especially:

* architectural discussions
* real-world usage feedback
* edge cases and integration insights

If you're evaluating or using the project, your perspective is valuable.

- Open an issue: https://github.com/blueprint-platform/openapi-generics/issues
- Start a discussion: https://github.com/blueprint-platform/openapi-generics/discussions

---

## License

MIT — see [LICENSE](LICENSE)

---

**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)
