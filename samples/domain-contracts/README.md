# Domain Contracts

This module defines **shared domain-level contract models** used across services.

It exists to separate **domain ownership** from:

* server-side OpenAPI projection
* client-side code generation

---

## Why this exists

In distributed systems, contract models are often:

* duplicated across services
* regenerated from OpenAPI
* gradually drifted over time

This module prevents that.

> Contracts are defined once, owned by the domain, and reused everywhere.

---

## Structure

```
domain-contracts/
  └── customer-contract/
```

Each submodule represents a **domain boundary**.

Example:

* `customer-contract` → Customer domain models

---

## Usage

### Server (producer)

Controllers return shared contract types:

```java
ServiceResponse<CustomerDto>
```

The server-side starter projects this into OpenAPI.

---

### Client (consumer)

Clients reuse the same contract dependency:

```xml
<dependency>
  <groupId>io.github.blueprint-platform.samples</groupId>
  <artifactId>customer-contract</artifactId>
</dependency>
```

The generator maps OpenAPI schemas to these types instead of regenerating them.

---

## Design principle

```text
Domain owns the contract
OpenAPI reflects it
Clients reuse it
```

---

## Scope

This module contains:

* domain DTOs (e.g. `CustomerDto`)
* no framework dependencies
* no OpenAPI annotations
* no transport concerns

It is intentionally:

* minimal
* stable
* framework-agnostic

---

## Relation to openapi-generics

This module works together with:

* `openapi-generics-server-starter` → projects contract to OpenAPI
* `openapi-generics-java-codegen` → reuses contract in generated clients

---

## Summary

```text
Define once → reuse everywhere → no duplication → no drift
```
