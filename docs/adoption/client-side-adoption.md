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

## Contents

- [60-second quick start](#60-second-quick-start)
- [What the client actually does](#what-the-client-actually-does)
- [Input: OpenAPI (not your contract)](#input-openapi-not-your-contract)
- [Minimal setup](#minimal-setup)
- [Progressive adoption modes](#progressive-adoption-modes)
- [Build pipeline (what really happens)](#build-pipeline-what-really-happens)
- [Output: what gets generated](#output-what-gets-generated)
- [Usage: how the client enters your system](#usage-how-the-client-enters-your-system)
- [Quick verification](#quick-verification)
- [Error handling](#error-handling)

---

## 60-second quick start

You want:

* a type-safe client
* zero duplicated models
* preserved generic response semantics

Do this:

### 1) Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.0</version>
</parent>
```

### 2) Provide OpenAPI

```text
/v3/api-docs.yaml
```

### 3) (Optional) Align with your contract

Use your own envelope (BYOE):

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Reuse your existing models:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

### 4) Build

```bash
mvn clean install
```

That's it.

---

### Result

Default envelope:

```java
ServiceResponse<CustomerDto>
```

Custom envelope (BYOE):

```java
ApiResponse<CustomerDto>
```

The generated client preserves the same contract shape published by the server.

---

## What the client actually does

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

## Input: OpenAPI (not your contract)

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

## Minimal setup

You provide exactly two inputs. Everything else is handled by the platform.

### 1. Build-time orchestration (MANDATORY)

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.0</version>
</parent>
```

This is the **entry point of the system**.

It provides:

* generator binding (`java-generics-contract`)
* template pipeline (extract → patch → overlay)
* deterministic execution model
* generated sources registration

### 2. OpenAPI Generator plugin (USER INPUT ONLY)

You control the **input and integration surface only**.

At minimum:

* OpenAPI input (`inputSpec`)
* generator (`java-generics-contract`)
* HTTP client (`library`)
* package structure

Everything else (templates, contract behavior, wrapper logic) is handled by the platform.

### Full example configuration

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
            openapi-generics.response-contract.YourDto=your.package.YourDto
          </additionalProperty>
        </additionalProperties>
        -->

        <!-- Optional: Bring Your Own Envelope (BYOE) -->
        <!--
        <additionalProperties>
          <additionalProperty>
            openapi-generics.envelope=your.package.YourEnvelope
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

### What you control here

* which OpenAPI spec is used
* which HTTP client is generated (`library`)
* package structure
* serialization strategy
* optional external contract mappings (BYOC)
* optional envelope override (BYOE)

### What you do NOT control

* generator internals
* template system
* contract semantics
* wrapper generation rules

> The generator is **contract-driven**, not schema-driven.

### Reference implementations

For concrete, working setups:

```text
samples/
  spring-boot-3/customer-service-client
  spring-boot-4/customer-service-client
```

These show real configurations for both Spring Boot 3 and Spring Boot 4.

---

## Progressive adoption modes

The system defines **explicit build-time modes** and **separate alignment inputs**.

These are **not the same concern**.

* Modes control **how the generator behaves**
* Alignment inputs define **how the contract is resolved**

### 1. Build-time modes (execution behavior)

These control whether the contract-aware system is active.

#### 1.1 Contract-aligned mode (default)

```xml
<openapi.generics.skip>false</openapi.generics.skip>
```

Behavior:

* contract-aware generation is enabled
* wrapper classes are generated as **type bindings**
* generic structure is preserved
* OpenAPI is **interpreted**, not materialized

This is the **standard operating mode**.

#### 1.2 Compatibility mode (fallback)

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Behavior:

* falls back to standard OpenAPI Generator
* no contract-aware processing
* models are generated directly from schemas
* generics may be flattened or lost

Use this when:

* comparing outputs
* debugging generation issues
* migrating incrementally

### 2. Contract alignment inputs (orthogonal to modes)

These do **not change the execution mode**.

They define how the **contract is resolved during generation**.

#### 2.1 BYOE — Bring Your Own Envelope (PRIMARY)

Define which envelope type represents your contract.

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Behavior:

* the generator resolves the **envelope type explicitly**
* generated wrappers extend your envelope
* no envelope class is generated
* the system becomes **envelope-agnostic**

Example output:

```java
class ApiResponseCustomerDto extends ApiResponse<CustomerDto>
```

Constraints:

* must be a concrete class
* must declare exactly one type parameter
* must expose a single direct payload of type `T`

#### 2.2 BYOC — Bring Your Own Contract Models

Reuse externally owned DTOs instead of generating them.

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Behavior:

* DTOs are resolved from your classpath
* no duplicate model generation
* generated wrappers import your types directly

Example output:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

### 3. How they work together

These mechanisms are **composable**.

Typical production setup:

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Result:

```text
Envelope  → external (BYOE)
DTO       → external (BYOC)
Wrappers  → generated (thin bindings)
```

### 4. Summary

```text
Mode controls execution    →  openapi.generics.skip = ON/OFF
Inputs control alignment   →  openapi-generics.envelope / response-contract.*
```

The system works because:

* execution is explicitly controlled
* contract ownership is externalizable
* generation remains deterministic
* adoption is fully reversible

The generator does not invent models. It resolves which envelope to use (BYOE), which DTOs to reuse (BYOC), and how to bind them deterministically.

---

## Build pipeline (what really happens)

This system is a **controlled, contract-aware execution pipeline**.

It does not simply generate code from OpenAPI. It **reconstructs the contract shape deterministically**.

```text
OpenAPI spec (input)
   ↓
Parent POM (orchestration)
   ↓
Template extraction (upstream)
   ↓
Template patch (contract semantics injection)
   ↓
Template overlay (custom templates)
   ↓
Custom generator (java-generics-contract)
   ↓
Contract-aware model resolution (BYOC / BYOE)
   ↓
Generated sources (contract-aligned)
```

### What actually changes vs standard OpenAPI generation

Standard OpenAPI Generator materializes schemas into models directly — every schema becomes a class, generics collapse into flat wrappers, and envelope types are duplicated per endpoint.

The contract-aware pipeline changes this:

* models are **not blindly materialized**
* contract semantics are **interpreted and enforced**
* wrappers are generated as **type bindings**, not structures
* external contract models are resolved from the classpath, not regenerated

### Contract-aware steps (the core difference)

The platform modifies upstream templates to:

* detect wrapper schemas (`x-api-wrapper`)
* inject required imports
* bind models to contract types

### Output characteristics

Generated code is:

* deterministic
* contract-aligned
* minimal (thin wrappers only)

Example:

```java
class ApiResponseCustomerDto extends ApiResponse<CustomerDto>
```

No:

* envelope duplication
* DTO regeneration (if BYOC used)
* structural reinterpretation

### Key properties of the pipeline

* single orchestrator (no ordering issues)
* fixed execution flow (no runtime branching)
* deterministic output
* contract-driven, not schema-driven
* envelope-agnostic (default and BYOE use the same pipeline)

---

## Output: what gets generated

The system does **not generate models** in the traditional sense.

It generates **thin, contract-aligned wrapper types** that bind OpenAPI responses back to your canonical contract.

### From OpenAPI schema

```text
ServiceResponseCustomerDto
ApiResponseCustomerDto
```

### Generated code

Default envelope:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

Custom envelope (BYOE):

```java
class ApiResponseCustomerDto extends ApiResponse<CustomerDto>
```

### Deterministic naming

Wrapper class names are derived from:

```text
envelope + payload type
```

Examples:

```text
ServiceResponseCustomerDto
ServiceResponsePageCustomerDto
ApiResponseCustomerDto
```

This guarantees:

* stable diffs
* predictable generation
* no naming collisions

---

## Usage: how the client enters your system

Generated sources are added to your project automatically:

```text
target/generated-sources/openapi
```

These sources are **not your domain layer**.

They are an **integration boundary**.

### What you actually use

You never work with generated wrapper classes directly.

You work with your **contract shape**:

```java
ServiceResponse<CustomerDto>
```

Or with BYOE:

```java
ApiResponse<CustomerDto>
```

This is the only type your application should depend on.

### How the client enters your system

Generated APIs are consumed through a **controlled adapter boundary**.

```java
public interface CustomerClient {

  ServiceResponse<CustomerDto> getCustomer(Long id);

}
```

Implementation delegates to generated client:

```java
@Service
public class CustomerClientImpl implements CustomerClient {

  private final CustomerControllerApi api;

  public CustomerClientImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Long id) {
    return api.getCustomer(id);
  }
}
```

### Why this boundary matters

Generated code is a **transport concern**, not a domain concern.
The adapter is where you translate between them.

Without this boundary, generated types leak into your application —
binding your domain to OpenAPI output, generator conventions, and the transport itself.

With the adapter in place:

* **Domain purity** — your app depends on contract types, not generated artifacts
* **Testability** — business logic is tested against the contract, not the HTTP client
* **Contract stability** — generator updates do not ripple into domain code
* **Replaceability** — the underlying transport can change without touching the app

The adapter is not boilerplate.
It is the seam between *what your system means* and *how it talks to the outside*.

This is **not optional in real systems**.

### What flows through your system

```text
Controller → Contract → Adapter → Generated Client → HTTP
```

Not:

```text
Controller → Generated Models → ???
```

### Reference implementation

See sample consumer services:

```text
samples/
  spring-boot-3/customer-service-consumer
  spring-boot-4/customer-service-consumer
```

These demonstrate:

* adapter-based integration
* contract-first usage
* safe isolation of generated code

---

## Quick verification

After generation, verify the following:

* wrapper classes extend the correct contract type
* no duplicate envelope classes are generated
* generics are preserved (`<T>` is not flattened)

Examples:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
class ApiResponseCustomerDto extends ApiResponse<CustomerDto>
```

If these conditions hold:

```text
OpenAPI → Client → Contract alignment is correct
```

---

## Error handling

Error handling is **not enforced by the client generator**.

It depends on the **contract model used by the service**.

Two common patterns:

### 1. Separate error protocol (recommended default)

```text
Success → ServiceResponse<T>
Error   → ProblemDetail (RFC 9457)
```

### 2. Envelope-based error model (custom / BYOE)

```text
Success → YourEnvelope<T>
Error   → YourEnvelope<T> (e.g. errors field)
```

### Key point

```text
The generator does not define error semantics.
It preserves whatever contract the service exposes.
```