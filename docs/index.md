---
layout: default
title: Home
nav_order: 1
description: "OpenAPI Generics for Spring Boot — keep your API contract intact end-to-end."
permalink: /
---

# OpenAPI Generics for Spring Boot

> Keep your Java API contract intact across OpenAPI projection and generated clients.

OpenAPI Generics is a contract-preserving OpenAPI generation platform for Spring Boot.

It treats:

```text
Java Contract → OpenAPI Projection → Generated Java Client
```

as one deterministic lifecycle.

The goal is simple:

> Generated clients should reconstruct your contract, not redefine it.

[Get started](#get-started) · [View on GitHub](https://github.com/blueprint-platform/openapi-generics)

---

## Contents

- [The Problem](#the-problem)
- [Proof — generated client, before vs after](#proof--generated-client-before-vs-after)
- [What's New in 1.1](#whats-new-in-11)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Get Started](#get-started)
- [Documentation](#documentation)
- [Samples](#samples)
- [Compatibility](#compatibility)
- [Community](#community)

## The Problem

You write a Spring controller with a generic response contract:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...)
```

Default OpenAPI Generator usually turns this into generated wrapper models:

```java
class ServiceResponsePageCustomerDto {
    private PageCustomerDto data;
    private Meta meta;
}
```

The envelope is duplicated.

The generic structure is flattened.

The client no longer uses the same contract shape as the server.

---

## What OpenAPI Generics Generates

With OpenAPI Generics, the generated wrapper stays contract-aligned:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

The wrapper is only a type binding.

The envelope remains contract-owned.

The payload type remains reusable.

The generic structure survives the OpenAPI lifecycle.

---

## Proof — generated client, before vs after

The two screenshots below are from a real generated client module — same OpenAPI document, same generator version, only the contract-aware pipeline is toggled.

### Before — default OpenAPI Generator

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-before.png" width="700" alt="Generated client without openapi-generics — duplicated envelope models, flattened generics"/>
</p>

- envelope materialized as a full class per endpoint
- `<T>` flattened — `getData()` returns a fused type that needs casting
- model graph grows linearly with the number of endpoints

### After — with openapi-generics

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-after.png" width="700" alt="Generated client with openapi-generics — thin wrappers extending the contract envelope"/>
</p>

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

- one shared envelope, imported from the contract module
- generics preserved end-to-end — `ServiceResponse<Page<CustomerDto>>` survives intact
- externally owned DTOs reused directly via BYOC — no duplication

---

## What’s New in 1.1

Version 1.1 adds container-aware reconstruction.

Supported response shapes now include:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

The same container model also works with BYOE envelopes:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>
```

`Page<T>` refers to the paging contract provided by:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

---

## Key Features

| Feature | Description |
|---|---|
| Contract-first projection | Java contracts remain the source of truth. |
| Generic reconstruction | Supported generic response shapes are reconstructed in generated Java clients. |
| BYOE | Use your own response envelope instead of `ServiceResponse<T>`. |
| BYOC | Reuse externally owned DTOs instead of regenerating them. |
| Container-aware generation | Built-in support for `Page<T>`, `List<T>`, and `Set<T>`. |
| Deterministic output | Same contract and configuration produce the same generated client. |
| Fail-fast behavior | Invalid projection or generation assumptions fail early. |

---

## How It Works

```text
Spring Boot Service
        ↓
openapi-generics-server-starter
        ↓
OpenAPI + vendor extensions
        ↓
openapi-generics-java-codegen-parent
        ↓
java-generics-contract
        ↓
Generated Java Client
```

The server starter projects supported Java response contracts into OpenAPI.

The codegen layer reads OpenAPI Generics metadata and reconstructs thin Java wrapper types.

The generated OpenAPI document remains valid OpenAPI.

Standard OpenAPI tooling can still consume it.

---

## Get Started

### Producer

Add the server starter:

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

The starter runs when Springdoc generates the OpenAPI document:

```text
/v3/api-docs
/v3/api-docs.yaml
```

It does not intercept requests or change runtime HTTP behavior.

---

### Client

Use the codegen parent:

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.1.0</version>
</parent>
```

Configure OpenAPI Generator with:

```xml
<generatorName>java-generics-contract</generatorName>
```

Then run:

```bash
mvn clean install
```

Generated wrappers extend your contract types directly.

---

## Documentation

- [Server-Side Adoption](adoption/server-side-adoption.md)
- [Client-Side Adoption](adoption/client-side-adoption.md)
- [Architecture](architecture/architecture.md)
- [Compatibility & Support Policy](compatibility.md)
- [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

---

## Samples

Runnable examples are available in the repository:

```text
samples/spring-boot-3
samples/spring-boot-4
samples/type-coverage/service-response
samples/type-coverage/byoe-response
```

The type-coverage samples validate scalar, value, object, list, set, and page response shapes end-to-end.

---

## Compatibility

- Java 17+
- Spring Boot 3.4.x, 3.5.x, 4.x
- Spring WebMvc
- springdoc-openapi
- OpenAPI Generator 7.x

See [Compatibility & Support Policy](compatibility.md) for details.

---

## Community

- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
- [GitHub Issues](https://github.com/blueprint-platform/openapi-generics/issues)

OpenAPI Generics is open source under the MIT license.