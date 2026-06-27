---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page defines the officially supported runtime and build-time scope for **OpenAPI Generics 1.2.0**.

It describes what the platform tests, publishes, and treats as supported behavior.

---

## Contents

- [Supported Scope](#supported-scope)
- [Compatibility Matrix](#compatibility-matrix)
- [Verified Capabilities](#verified-capabilities)
- [Support Policy](#support-policy)
- [Out of Scope](#out-of-scope)
- [Further Reading](#further-reading)

---

## Supported Scope

OpenAPI Generics currently supports:

- Java 17+
- Spring Boot WebMvc applications
- springdoc-openapi WebMvc starter
- OpenAPI Generator 7.x
- Maven-based client generation

The supported built-in contract model includes:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

BYOE envelopes participate in the same reconstruction model:

```java
YourEnvelope<T>

YourEnvelope<List<T>>

YourEnvelope<Set<T>>

YourEnvelope<Page<T>>
```

`Page<T>` refers to:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

Applications may also register their own generic container contracts.

Example:

```yaml
openapi-generics:
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

Configured containers participate in the same projection, metadata enrichment, and deterministic reconstruction pipeline as the built-in container types.

---

## Compatibility Matrix

### Runtime — Server-Side Projection

| Java | Spring Boot | springdoc-openapi | Scope  | Status    |
|------|-------------|-------------------|--------|-----------|
| 17+  | 3.4.x       | 2.8.x             | WebMvc | Supported |
| 17+  | 3.5.x       | 2.8.x             | WebMvc | Supported |
| 17+  | 4.x         | 3.x               | WebMvc | Supported |

### Build-Time — Client Generation

| Java | OpenAPI Generator | Build Tool | Status    |
|------|-------------------|------------|-----------|
| 17+  | 7.x               | Maven      | Supported |

The client generation parent provides a tested default OpenAPI Generator version, but consumers may override `openapi-generator.version` within the supported 7.x line.

---

## Verified Capabilities

The supported matrix is validated through repository samples and dedicated type-coverage modules.

Verified capabilities include:

- default `ServiceResponse<T>` projection
- `Page<T>`, `List<T>`, and `Set<T>` container projection
- application-defined generic container registration
- configurable container projection and reconstruction
- preservation of Java container identity through `x-data-container-type`
- BYOE envelope projection
- BYOE container reconstruction
- BYOC external DTO reuse
- generated wrapper reconstruction
- ignored infrastructure model filtering
- generated-source hygiene and deterministic Java client output
- end-to-end producer → OpenAPI → generated client → consumer validation

Reference samples:

```text
samples/spring-boot-3
samples/spring-boot-4
samples/type-coverage/service-response
samples/type-coverage/byoe-response
```

Sample modules may use Java 21, but published platform artifacts target Java 17+.

---

## Support Policy

A combination is officially supported when:

- it appears in the compatibility matrix
- it stays within the documented platform scope
- it is covered by samples or maintained verification paths

Inside that boundary, OpenAPI Generics owns:

- generics-aware OpenAPI projection
- vendor extension metadata
- application-defined generic container registration
- contract-aware Java client generation
- wrapper reconstruction
- container-aware reconstruction
- BYOE and BYOC resolution
- generated-source hygiene
- fail-fast detection of template patch drift

Outside that boundary, behavior belongs to the surrounding ecosystem, such as:

- HTTP client library internals
- Jackson customization
- unrelated Spring configuration
- downstream application architecture

---

## Out of Scope

The following are not currently supported:

- Spring WebFlux
- Gradle-native client generation
- non-Java server frameworks
- non-Java client reconstruction
- arbitrary nested generic graphs
- automatic inference of unregistered custom generic containers

Standard OpenAPI tooling can still consume the generated OpenAPI document.

Tools that do not understand OpenAPI Generics metadata simply ignore the vendor extensions.

---

## Further Reading

- [Adoption Guides](index.md)
- [Architecture](architecture/architecture.md)
- [README](../README.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)