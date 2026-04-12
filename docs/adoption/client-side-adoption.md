---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-Side Adoption — Contract-First Client Integration

> Generate a Java client that **preserves contract semantics exactly as published** — with **progressive adoption**, **zero duplication**, and **no drift**.

This is **not a typical OpenAPI client setup**.

It defines a **controlled but optional build-time system** where:

* OpenAPI is treated as input (not authority)
* the contract is preserved (not regenerated)
* the output is deterministic when enabled
* and the system can be **selectively bypassed when needed**

This guide defines the **correct client-side integration model** for the platform.

It focuses on four things:

* consuming OpenAPI as input
* executing a controlled build pipeline
* optionally aligning with an external contract
* using the generated client safely

---

## 📑 Contents

- [60-second quick start](#-60-second-quick-start)
- [What the client actually does](#-what-the-client-actually-does)
- [Input: OpenAPI (not your contract)](#-input-openapi-not-your-contract)
- [Minimal setup](#-minimal-setup)
- [Progressive adoption modes (0.9.x)](#-progressive-adoption-modes-09x)
- [Build pipeline (what really happens)](#-build-pipeline-what-really-happens)
- [Output: what gets generated](#-output-what-gets-generated)
- [Usage: how the client enters your system](#-usage-how-the-client-enters-your-system)
- [Adapter boundary (strongly recommended)](#-adapter-boundary-strongly-recommended)
- [Quick verification](#-quick-verification)
- [Error handling](#-error-handling)
- [Mental model](#-mental-model)
- [Summary](#-summary)

---

## ⚡ 60-second quick start

You want:

* a type-safe client
* zero duplicated models
* preserved `ServiceResponse<T>` semantics

Do this:

### 1) Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.9.0</version>
</parent>
```

### 2) Provide OpenAPI

```text
/v3/api-docs.yaml
```

### 3) Build

```bash
mvn clean install
```

That’s it.

---

## 🎯 What the client actually does

The client has **one responsibility**:

> Convert an OpenAPI document into a **contract-aligned Java client** without redefining anything.

It does **not**:

* design models
* interpret business semantics
* introduce abstractions

It only executes a deterministic transformation:

```text
OpenAPI → deterministic Java client
```

---

## 📥 Input: OpenAPI (not your contract)

Client generation always starts from an **existing OpenAPI document**.

```bash
curl http://localhost:8084/.../v3/api-docs.yaml -o api.yaml
```

Critical distinction:

> OpenAPI is **input metadata**, not the contract itself.

Implication:

* structure comes from OpenAPI
* semantics come from contract types (shared or external)

---

## 📦 Minimal setup

You provide exactly two inputs. Everything else is handled by the platform.

---

### 1. Build-time orchestration (MANDATORY)

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.9.0</version>
</parent>
```

This is the **entry point of the system**.

It provides:

* generator binding (`java-generics-contract`)
* template pipeline (extract → patch → overlay)
* deterministic execution model
* generated sources registration

---

### 2. OpenAPI Generator plugin (USER INPUT ONLY)

You control the **input and integration surface only**.

At minimum:

* OpenAPI input (`inputSpec`)
* generator (`java-generics-contract`)
* HTTP client (`library`)
* package structure

---

### Full example configuration

<details>
<summary>Show complete plugin configuration</summary>

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <generatorName>java-generics-contract</generatorName>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>your-library-choice</library>

        <apiPackage>your.api.package</apiPackage>
        <modelPackage>your.model.package</modelPackage>
        <invokerPackage>your.invoker.package</invokerPackage>

        <configOptions>
          <!-- Choose ONE depending on your runtime -->
          <!-- Spring Boot 3 -->
          <useSpringBoot3>true</useSpringBoot3>

          <!-- Spring Boot 4 -->
          <!-- <useSpringBoot4>true</useSpringBoot4> -->

          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

        <!-- Optional: Bring Your Own Contract (external models) -->
        <!--
        <additionalProperties>
          <additionalProperty>
            openapiGenerics.responseContract.YourDto=your.package.YourDto
          </additionalProperty>
        </additionalProperties>
        -->

        <cleanupOutput>true</cleanupOutput>
        <skipValidateSpec>false</skipValidateSpec>

        <generateApiDocumentation>false</generateApiDocumentation>
        <generateApiTests>false</generateApiTests>
        <generateModelDocumentation>false</generateModelDocumentation>
        <generateModelTests>false</generateModelTests>

      </configuration>

    </execution>
  </executions>
</plugin>
```

</details>

---

### What you control here

* which OpenAPI spec is used
* which HTTP client is generated (`library`)
* package structure
* serialization strategy
* optional external contract mappings

---

### What you do NOT control

* generator internals
* template system
* contract semantics
* wrapper generation rules

---

### Reference implementations

For concrete, working setups:

```text
samples/
  spring-boot-3/customer-service-client
  spring-boot-4/customer-service-client
```

These show real configurations for both Spring Boot 3 and Spring Boot 4.

---

## 🧠 Progressive adoption modes (0.9.x)

The system is **not all-or-nothing anymore**.

It supports **three explicit modes**:

---

### 1. Full contract-aligned mode (default)

```xml
<openapi.generics.skip>false</openapi.generics.skip>
```

Behavior:

* deterministic generation enabled
* wrapper classes generated
* contract reused
* generics preserved

---

### 2. Compatibility mode (fallback)

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Behavior:

* falls back to default OpenAPI Generator
* no generics-aware processing
* no contract enforcement

Use this when:

* debugging generation differences
* comparing outputs
* gradual migration

---

### 3. Bring Your Own Contract (external models)

```xml
<additionalProperties>
  <additionalProperty>
    openapiGenerics.responseContract.CustomerDto=io.example.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Behavior:

* external models are reused
* no duplicate DTO generation
* wrappers import your existing classes

Implication:

> The platform does not own your contract — it aligns with it.

---

### Key idea

```text
You can opt in gradually:

Default → External models → Full contract alignment
```

This enables **progressive adoption without lock-in**.

---

## 🏗 Build pipeline (what really happens)

This system is a **controlled execution pipeline**.

```text
OpenAPI spec (input)
   ↓
Parent POM (orchestration)
   ↓
Template extraction (upstream)
   ↓
Template patch (api_wrapper injection)
   ↓
Template overlay (custom templates)
   ↓
Custom generator (java-generics-contract)
   ↓
Generated sources (contract-aligned)
```

> Each step is fixed and ordered.

---

### What the platform enforces

* contract models are NOT generated
* wrapper classes are deterministic
* generics are preserved
* OpenAPI is interpreted — not materialized

---

## 🧠 Output: what gets generated

From OpenAPI schema:

```text
ServiceResponseCustomerDto
```

Generated code:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

Properties:

* thin wrappers only
* no duplicated envelopes
* direct reuse of contract types

---

## 🚀 Usage: how the client enters your system

Generated sources:

```text
target/generated-sources/openapi
```

Usage:

```java
ServiceResponse<CustomerDto>
```

---

### Reference implementation

See sample consumer services:

```text
samples/
  spring-boot-3/customer-service-consumer
  spring-boot-4/customer-service-consumer
```

These show:

* how generated clients are used in real services
* how adapters isolate generated code
* how contract flows end-to-end

---

## 🧱 Adapter boundary (strongly recommended)

Do not expose generated APIs directly.

```java
public interface CustomerClient {
  ServiceResponse<CustomerDto> getCustomer(Long id);
}
```

Purpose:

* isolate generation details
* protect domain logic
* enable safe evolution

---

## 🔍 Quick verification

After generation:

* wrappers extend contract types
* no duplicate envelope classes exist
* generics are preserved

---

## ⚠️ Error handling

Errors follow:

```text
ProblemDetail (RFC 9457)
```

---

## 🧠 Mental model

Think of the client as:

> A deterministic build-time compiler
> that maps OpenAPI → contract-aligned Java code

But also:

> An optional layer that can be bypassed when needed

---

## 🧾 Summary

```text
Input   = OpenAPI
Modes   = optional + progressive
Process = controlled pipeline
Output  = thin wrappers over contract
```

The system works because:

* contract is never regenerated
* generation is deterministic when enabled
* adoption is progressive

---

🛡 MIT License
