---
layout: default
title: Home
nav_order: 1
description: "OpenAPI Generics for Spring Boot — keep your API contract intact end-to-end."
permalink: /
---

# OpenAPI Generics for Spring Boot

> Keep your Java API contract intact across OpenAPI projection and generated clients.

OpenAPI Generics is a contract-preserving OpenAPI generation platform for Spring Boot that keeps your Java contracts consistent from server implementation to generated client.

Instead of treating OpenAPI as the source of truth, OpenAPI Generics treats it as a deterministic projection of your Java contract.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Deterministic Client Reconstruction
```

The result is simple:

> Generated clients reconstruct your contract instead of redefining it.

[Get Started](#get-started) · [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

---

## Contents

- [The Problem](#the-problem)
- [Before vs After](#before-vs-after)
- [What's New in 1.2](#whats-new-in-12)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Get Started](#get-started)
- [Documentation](#documentation)
- [Compatibility](#compatibility)
- [Community](#community)

---

## The Problem

A typical Spring controller returns a generic contract such as:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers()
```

Default OpenAPI Generator usually materializes a new wrapper model:

```java
class ServiceResponsePageCustomerDto {
    PageCustomerDto data;
    Meta meta;
}
```

The envelope is duplicated, generics are flattened, and every service boundary introduces another copy of the same contract.

With OpenAPI Generics:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated wrapper becomes a thin type binding while the original contract remains the single source of truth.

---

## Before vs After

<table>
<tr>
<td align="center"><b>Default OpenAPI Generator</b></td>
<td align="center"><b>OpenAPI Generics</b></td>
</tr>
<tr>
<td><img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-before.png" width="450"/></td>
<td><img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-after.png" width="450"/></td>
</tr>
</table>

**Without OpenAPI Generics**

- duplicated wrapper models
- flattened generic types
- growing model graph
- additional mapping layers

**With OpenAPI Generics**

- shared contract envelope
- preserved generic structure
- reusable external DTOs (BYOC)
- deterministic generated clients

---

## What's New in 1.2

OpenAPI Generics 1.2 extends the platform beyond built-in containers.

Highlights include:

- Application-defined generic container support
- Java container identity preservation (`x-data-container-type`)
- Generated-source hygiene for cleaner Java clients
- Improved metadata validation
- Expanded regression coverage
- Full backward compatibility with 1.1.x

Built-in contracts remain fully supported:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

Applications can now register their own generic containers:

```yaml
openapi-generics:
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

---

## Key Features

| Feature | Description |
|----------|-------------|
| **Contract-first** | Java remains the source of truth. |
| **BYOE** | Reuse your own response envelope. |
| **BYOC** | Reuse externally owned DTOs. |
| **Application-defined containers** | Register custom generic containers. |
| **Container-aware reconstruction** | Built-in and configured containers share one deterministic pipeline. |
| **Deterministic generation** | Stable projection, validation, and generated clients. |
| **Generated-source hygiene** | Cleaner Java imports and generated artifacts. |

---

## How It Works

```text
Spring Boot
      ↓
openapi-generics-server-starter
      ↓
OpenAPI + Vendor Extensions
      ↓
openapi-generics-java-codegen-parent
      ↓
java-generics-contract
      ↓
Contract-Aligned Java Client
```

Projection publishes contract semantics through vendor extensions.

Client generation consumes those semantics and reconstructs the original Java contract instead of creating alternative models.

The generated OpenAPI document remains standard OpenAPI and is fully consumable by existing tooling.

---

## Get Started

### Producer

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Client

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.2.0</version>
</parent>
```

Configure:

```xml
<generatorName>java-generics-contract</generatorName>
```

Generate:

```bash
mvn clean install
```

---

## Documentation

- [Server-Side Adoption](adoption/server-side-adoption.md)
- [Client-Side Adoption](adoption/client-side-adoption.md)
- [Architecture](architecture/architecture.md)
- [Compatibility & Support Policy](compatibility.md)
- [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

---

## Samples

Repository samples cover:

- Spring Boot 3
- Spring Boot 4
- ServiceResponse
- BYOE
- Type coverage
- End-to-end producer → client → consumer validation

---

## Compatibility

Supported platforms:

- Java 17+
- Spring Boot 3.4.x / 3.5.x / 4.x
- Spring WebMvc
- springdoc-openapi
- OpenAPI Generator 7.x

See the full [Compatibility & Support Policy](compatibility.md).

---

## Community

- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
- [GitHub Issues](https://github.com/blueprint-platform/openapi-generics/issues)

OpenAPI Generics is released under the MIT License.