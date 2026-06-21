---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-Side Adoption

> Publish a generics-aware OpenAPI document from your Spring Boot service without changing runtime behavior.

This guide explains what changes in a producer service when `openapi-generics-server-starter` is added.

For client generation, see [Client-Side Adoption](./client-side-adoption.md).  
For internals, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [What the Starter Does](#what-the-starter-does)
- [Supported Controller Shapes](#supported-controller-shapes)
- [BYOE](#byoe)
- [What Is Left Untouched](#what-is-left-untouched)
- [Error Responses](#error-responses)
- [Verification](#verification)

---

## Quick Start

Add the starter:

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

Write controller methods normally:

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(@PathVariable Long id) {
    return ResponseEntity.ok(ServiceResponse.of(service.findById(id)));
}
```

Container payloads are supported:

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers() {
    return ResponseEntity.ok(ServiceResponse.of(service.findAll()));
}
```

```java
@GetMapping("/list")
public ResponseEntity<ServiceResponse<List<CustomerDto>>> listCustomers() {
    return ResponseEntity.ok(ServiceResponse.of(service.list()));
}
```

```java
@GetMapping("/set")
public ResponseEntity<ServiceResponse<Set<CustomerDto>>> customerSet() {
    return ResponseEntity.ok(ServiceResponse.of(service.set()));
}
```

`Page<T>` refers to:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

---

## What the Starter Does

The starter runs when Springdoc generates the OpenAPI document:

```text
/v3/api-docs
/v3/api-docs.yaml
```

It does not intercept HTTP requests.

It does not change serialization.

It does not change controller runtime behavior.

It only projects supported Java response shapes into OpenAPI with metadata that the client generator can later reconstruct.

Pipeline summary:

```text
Controller return type
        ↓
Response introspection
        ↓
Wrapper schema metadata
        ↓
Container metadata
        ↓
Generation-control markers
        ↓
OpenAPI validation
```

---

## Supported Controller Shapes

Default platform envelope:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

Supported examples:

```java
ResponseEntity<ServiceResponse<CustomerDto>>

ResponseEntity<ServiceResponse<List<CustomerDto>>>

ResponseEntity<ServiceResponse<Set<CustomerDto>>>

ResponseEntity<ServiceResponse<Page<CustomerDto>>>
```

Async wrappers are unwrapped automatically:

```java
CompletionStage<ServiceResponse<CustomerDto>>

Future<ServiceResponse<CustomerDto>>

DeferredResult<ServiceResponse<CustomerDto>>

WebAsyncTask<ServiceResponse<CustomerDto>>
```

Enums are supported when published as reusable schema components:

```java
@Schema(enumAsRef = true)
public enum CoverageStatus {
    ACTIVE,
    PASSIVE,
    EXPERIMENTAL
}
```

---

## BYOE

Use a custom envelope by configuring it on the producer:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

Then use it in controller return types:

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
}
```

BYOE supports the same container model:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>
```

The envelope class must:

- be a concrete class
- declare exactly one type parameter
- expose exactly one direct payload field of type `T`

Valid example:

```java
public final class ApiResponse<T> {

    private int status;

    private String message;

    private T data;
}
```

Invalid envelope shapes fail at startup with a clear `IllegalStateException`.

---

## What Is Left Untouched

The starter only acts on supported envelope-shaped responses.

These remain normal Springdoc output:

```java
CustomerDto

List<CustomerDto>

Map<String, Object>

ResponseEntity<Void>
```

Reactive WebFlux types such as `Mono<T>` and `Flux<T>` are outside the current scope.

---

## Error Responses

The starter projects success response envelopes.

It does not define your error model.

Choose one service-wide pattern:

```text
Success → ServiceResponse<T>
Error   → ProblemDetail
```

or:

```text
Success → YourEnvelope<T>
Error   → YourEnvelope<Void>
```

Keep the pattern consistent across the service.

The client generator preserves the response contract; it does not decide error semantics.

---

## Verification

### 1. Check runtime response

```bash
curl -s http://localhost:8084/customer-service/customers/1
```

The JSON should match your actual envelope serialization.

If runtime JSON is wrong, fix the envelope/Jackson contract. The starter does not change runtime serialization.

### 2. Check OpenAPI metadata

```bash
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml | grep -A4 "x-api-wrapper"
```

Expected metadata:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: CustomerDto
```

For container payloads:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-item: CustomerDto
```

### 3. Check ignored infrastructure models

The OpenAPI document should mark contract-owned infrastructure schemas with:

```yaml
x-ignore-model: true
```

Typical ignored models:

```text
ServiceResponse
Meta
Sort
Page
```

or your BYOE envelope.

If these markers are present, the server-side projection is ready for client generation.

---

## Further Reading

- [Client-Side Adoption](./client-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)