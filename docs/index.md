---
layout: default
title: Home
nav_order: 1
description: "OpenAPI Generics for Spring Boot — keep your API contract intact end-to-end."
permalink: /
---

# OpenAPI Generics for Spring Boot

> Keep your API contract intact end-to-end — Java types, OpenAPI document, generated clients.

OpenAPI Generics is a deterministic, generics-aware API platform for Spring Boot. It treats your Java code as the source of truth for the contract, and OpenAPI as a faithful projection of it. The result: generic response shapes survive intact across the server, the spec, and every generated client — without duplicated envelope models, lost type parameters, or drift between layers.

[Get started](#get-started) · [View on GitHub](https://github.com/blueprint-platform/openapi-generics)

---

## The problem in 30 seconds

You write a clean controller:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...)
```

Springdoc projects it into OpenAPI. OpenAPI Generator produces a Java client. And then this happens:

```java
// Generated client — generics gone, envelope duplicated per endpoint
public class ServiceResponsePageCustomerDto {
  private List<CustomerDto> data;
  private Meta meta;
  // getters, setters, @JsonProperty, full envelope re-implementation...
}

public class ServiceResponsePageOrderDto {
  private List<OrderDto> data;        // same shape
  private Meta meta;                  // same Meta
  // ...regenerated again, per endpoint
}
```

Every endpoint gets its own copy of the envelope. The `<T>` is gone. Your client team writes the same wrapper logic on every project. The contract you thought was unified is now scattered across dozens of generated classes.

This is what OpenAPI Generics fixes.

---

## What you get instead

The same controller, with openapi-generics:

```java
// Generated client — generics preserved, envelope shared
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}

public class ServiceResponsePageOrderDto
    extends ServiceResponse<Page<OrderDto>> {}
```

One envelope, one `Meta`, one `Page`. Wrappers are thin type bindings. Your DTOs come from your existing classpath, not from regeneration. The contract on the server, in the OpenAPI document, and in every generated wrapper is the same shape.

---

## Key features

| Feature | What it does |
|---|---|
| **Contract-first projection** | Your Java types are the source of truth; OpenAPI is a faithful projection, not a separate contract you maintain by hand. |
| **Generics preserved** | `ServiceResponse<Page<CustomerDto>>` survives intact through the OpenAPI document and into every generated client. |
| **BYOE — Bring Your Own Envelope** | Use `ServiceResponse<T>` out of the box, or plug in your own envelope (`ApiResponse<T>`, `Result<T>`, …). Validated at startup, no silent degradation. |
| **BYOC — Bring Your Own Contract** | Reuse externally owned DTOs from a shared contract module instead of regenerating them client-side. Zero duplication. |
| **Deterministic & fail-fast** | Same input + same configuration → byte-identical output across builds. Misconfiguration fails at boot or build time, never silently. |
| **Progressive adoption** | Single switch (`openapi.generics.skip`) flips between contract-aware and stock OpenAPI Generator behavior. No fork to unwind. |
| **Zero-drift template patching** | The codegen pipeline patches the upstream `model.mustache` at build time — no frozen template snapshots, no silent drift across generator versions. |

---

## How it works

Two halves of a single contract:

```
        ┌──────────────────────────────────┐
        │     Java contract (source        │
        │     of truth — your code)        │
        └────────────┬─────────────────────┘
                     │
            projects (server)
                     ▼
        ┌──────────────────────────────────┐
        │   OpenAPI document with vendor   │
        │   extensions:                    │
        │     x-api-wrapper                │
        │     x-data-container             │
        │     x-data-item                  │
        │     x-ignore-model               │
        └────────────┬─────────────────────┘
                     │
            reconstructs (client)
                     ▼
        ┌──────────────────────────────────┐
        │   Generated Java client —        │
        │   thin wrappers extending the    │
        │   contract, generics preserved   │
        └──────────────────────────────────┘
```

**On the server**, a Springdoc customizer inspects controller return types, identifies envelope shapes, and stamps the OpenAPI document with vendor extensions that describe the original generic structure. Envelope and infrastructure schemas (`ServiceResponse`, `Page`, `Meta`) are marked `x-ignore-model` so the client knows not to regenerate them.

**On the client**, a Maven parent POM orchestrates a five-stage build pipeline (extract → patch → overlay → generate → register sources). The `java-generics-contract` generator reads the vendor extensions and reconstructs the original generic shape — emitting wrappers as thin `extends` bindings rather than duplicated full classes.

For the full pipeline mechanics, see [Architecture](architecture/architecture.md).

---

## Get started

### Producer service (server-side)

Add the starter to your Spring Boot service:

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.0.1</version>
</dependency>
```

Write your controllers normally — `ResponseEntity<ServiceResponse<CustomerDto>>`, `ResponseEntity<ServiceResponse<Page<CustomerDto>>>`, async variants — and the projection runs automatically. No annotations, no customizer registration, no OpenAPI hand-editing.

→ [Server-Side Adoption Guide](adoption/server-side-adoption.md)

### Client module (client-side)

Inherit the codegen parent in your client module:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.1</version>
</parent>
```

Configure the OpenAPI Generator plugin with `<generatorName>java-generics-contract</generatorName>`, point `inputSpec` at your producer's OpenAPI document, and run `mvn clean install`. Generated wrappers extend your contract directly.

→ [Client-Side Adoption Guide](adoption/client-side-adoption.md)

### Working samples

The repository ships runnable end-to-end stacks for both Spring Boot 3 and Spring Boot 4:

```
samples/spring-boot-3/    ← producer + client + consumer (BYOC enabled)
samples/spring-boot-4/    ← producer + client + consumer (zero-config default flow)
```

Each stack runs with `docker compose up --build -d` from its directory. The two stacks intentionally differ — they show that BYOE and BYOC are *optional alignment inputs*, not requirements. See [`samples/README.md`](https://github.com/blueprint-platform/openapi-generics/tree/main/samples) for the full run-and-verify flow.

---

## Compatibility

- **Java 17+** (samples use Java 21)
- **Spring Boot 3.4.x, 3.5.x, 4.x** — WebMvc only
- **springdoc-openapi WebMvc starter** — 2.8.x for Boot 3, 3.x for Boot 4
- **OpenAPI Generator 7.x** — Maven-based client generation

WebFlux, Gradle, and non-Java server frameworks are deliberately out of scope. The platform boundary is documented in detail.

→ [Compatibility & Support Policy](compatibility.md)

---

## Relationship to OpenAPI Generator

OpenAPI Generics **is not a fork** of OpenAPI Generator. It uses the upstream `openapi-generator-maven-plugin` 7.x as its execution engine and patches the upstream `model.mustache` at build time — surgically, with a single regex insertion that injects the generic-aware branch into the foundational schema loop.

If the upstream template structure ever changes in a way that the patch can't apply, the build fails immediately with a clear error. There is no frozen template snapshot, no parallel generator implementation, no maintenance burden compounding with each upstream release.

What stays upstream:
- the generator itself
- the HTTP client libraries (`restclient`, `webclient`, `resttemplate`, …)
- API operation generation
- Mustache template foundations

What this platform adds:
- vendor extension protocol (`x-api-wrapper`, `x-data-container`, `x-data-item`, `x-ignore-model`)
- `GenericAwareJavaCodegen` — a thin extension of `JavaClientCodegen`
- contract-aware wrapper templates (overlaid on patched upstream)
- BYOE / BYOC resolution at generation time
- the build pipeline that orchestrates extract → patch → overlay → generate → register

---

## Documentation

- [**Server-Side Adoption**](adoption/server-side-adoption.md) — what changes in your producer service
- [**Client-Side Adoption**](adoption/client-side-adoption.md) — generate a contract-aligned Java client
- [**Architecture**](architecture/architecture.md) — pipeline internals, vendor extension protocol, design decisions
- [**Compatibility & Support Policy**](compatibility.md) — supported version matrix and platform boundary
- [**README**](https://github.com/blueprint-platform/openapi-generics) — project overview and source

---

## Community

- 💬 [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions) — design questions, edge cases, OAS 3.1 compliance
- 🐛 [GitHub Issues](https://github.com/blueprint-platform/openapi-generics/issues) — bug reports, feature requests
- ⭐ Star the repo if openapi-generics solves a problem you've had

The project is open source under MIT license. Contributions, feedback, and adoption stories are all welcome.