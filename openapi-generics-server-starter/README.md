# openapi-generics-server-starter

> Contract-aware OpenAPI projection for Spring Boot services

`openapi-generics-server-starter` projects Java response contracts into OpenAPI while preserving supported generic response semantics.

It does not define contracts.

It does not generate clients.

Its responsibility is:

> Discover supported response contracts and project them into OpenAPI in a form that can be deterministically reconstructed by client generators.

---

## Contents

- [What It Does](#what-it-does)
- [Supported Response Shapes](#supported-response-shapes)
- [Projection Pipeline](#projection-pipeline)
- [Generated Metadata](#generated-metadata)
- [Usage](#usage)
- [BYOE](#byoe)
- [Out of Scope](#out-of-scope)
- [Mental Model](#mental-model)

---

## What It Does

The starter keeps Java contracts as the source of truth and treats OpenAPI as a projection layer.

Responsibilities:

- response contract discovery
- generic type introspection
- wrapper schema projection
- container-aware metadata enrichment
- OpenAPI contract validation

The generated OpenAPI document remains valid OpenAPI and can be consumed by standard tooling.

---

## Supported Response Shapes

Platform envelope:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

BYOE envelopes using user-owned response contracts:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>
```

`ApiResponse<T>` is only an example of a user-owned envelope. Any configured BYOE envelope that satisfies the structural requirements participates in the same projection pipeline.

`Page<T>` refers to the paging contract provided by `openapi-generics-contract`:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

Supported container types:

```java
List<T>

Set<T>

Page<T>
```

Nested container graphs remain outside the supported scope:

```java
ServiceResponse<List<List<T>>>

ServiceResponse<Page<List<T>>>

ApiResponse<List<List<T>>>

ApiResponse<Page<List<T>>>
```

---

## Projection Pipeline

```text
Controller Return Types
        ↓
Contract Discovery
        ↓
Contract Introspection
        ↓
Wrapper Projection
        ↓
Container Enrichment
        ↓
Generation-Control Marking
        ↓
OpenAPI Validation
```

The pipeline executes when Springdoc generates the OpenAPI document:

```text
/v3/api-docs

/v3/api-docs.yaml
```

It does not affect request handling or runtime application behavior.

---

## Generated Metadata

The starter enriches OpenAPI schemas with vendor extensions used by the client generation layer.

Simple wrapper example:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: CustomerDto
```

Container wrapper example:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: ListCustomerDto
x-data-container: List
x-data-item: CustomerDto
```

Generation-control marker:

```yaml
x-ignore-model: true
```

These extensions form the projection protocol between server OpenAPI output and client reconstruction.

---

## Usage

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

Example platform envelope endpoints:

```java
ResponseEntity<ServiceResponse<List<CustomerDto>>>

ResponseEntity<ServiceResponse<Set<CustomerDto>>>

ResponseEntity<ServiceResponse<Page<CustomerDto>>>
```

---

## BYOE

Configure an existing user-owned response envelope:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

Example BYOE endpoints:

```java
ResponseEntity<ApiResponse<CustomerDto>>

ResponseEntity<ApiResponse<List<CustomerDto>>>

ResponseEntity<ApiResponse<Set<CustomerDto>>>

ResponseEntity<ApiResponse<Page<CustomerDto>>>
```

The custom envelope must:

- be a concrete class
- declare exactly one type parameter
- expose exactly one direct payload field of type `T`

The payload field must be direct at the envelope definition level.

This is valid:

```java
class ApiResponse<T> {
    private T data;
}
```

This is not valid:

```java
class ApiResponse<T> {
    private Wrapper<T> data;
}
```

Container payloads are supported at endpoint usage level through the configured envelope:

```java
ApiResponse<List<CustomerDto>>

ApiResponse<Set<CustomerDto>>

ApiResponse<Page<CustomerDto>>
```

---

## Out of Scope

This module does not:

- define shared contract types
- generate Java clients
- own DTO models
- modify runtime HTTP behavior
- perform business validation
- support arbitrary nested generic graphs

---

## Mental Model

```text
Java Contract
      ↓
OpenAPI Projection
```

The responsibility of this module ends once the OpenAPI document has been produced.