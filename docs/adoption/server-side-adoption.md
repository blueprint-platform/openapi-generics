---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-side adoption

**Keep your controller contract; publish the Java identity needed by generated clients.**
The starter participates when Springdoc produces OpenAPI, leaving normal request handling and
serialization with Spring MVC and your application.

## What changes in the output?

Your endpoint still returns:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers() { ... }
```

Its projected wrapper schema carries:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

The [Java client generator](client-side-adoption.md) can then emit:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The Java contracts own behavior and fields. OpenAPI is their projection and the protocol between
producer and client reconstruction; the generated subclass binds a concrete transport response.

[Dependencies](#add-the-projection-dependencies) · [Envelope choice](#choose-the-envelope) ·
[Containers](#register-an-application-container) · [Verification](#check-the-projected-document)

## Add the projection dependencies

In an existing supported Spring MVC application, add:

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>1.3.0</version>
</dependency>
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.10.0</version>
</dependency>
```

The Springdoc pin above is the **Boot 3.5** reference. Use **3.2.0** for the Boot 4 reference.
Your application chooses its framework versions within the
[compatibility policy](../compatibility.md#supported-lines); the starter does not select a UI/runtime
for you. The platform dependency example uses **1.3.0**.

Keep supported controller methods unchanged. No OpenAPI Generics-specific controller annotation or
runtime response adapter is needed. `ServiceResponse<T>` is the default envelope and needs no setting.

## Choose the envelope

**BYOE — Bring Your Own Envelope** keeps an application-owned generic envelope authoritative instead
of requiring adoption of the platform's `ServiceResponse<T>`:

```yaml
openapi-generics:
  envelope:
    type: com.example.contract.ApiResponse
```

For an endpoint returning `ApiResponse<Page<CustomerDto>>`, the producer emits the fully qualified Java class name (FQCN) of the envelope in
`x-api-wrapper-type`. The client derives `extends ApiResponse<Page<CustomerDto>>` from it and needs
**no duplicate envelope configuration**. Supply the shared envelope dependency to the client.

The envelope must be a concrete class with exactly one type parameter and one direct payload field
of that parameter's type. Record envelopes, multiple type parameters and nested payload slots are
unsupported. Define `T data`, then use `ApiResponse<List<CustomerDto>>` at the endpoint; do not define
a payload slot such as `List<T> data` inside the envelope itself.

BYOC (Bring Your Own Contract) is a separate, optional [client-side DTO mapping](client-side-adoption.md#optional-reuse-existing-dtos-with-byoc).
It does not establish producer envelope identity.

## Supported response shapes

| Payload | Example |
| --- | --- |
| Direct value/object | `ServiceResponse<CustomerDto>` |
| Built-in collection | `ServiceResponse<List<CustomerDto>>`, `ServiceResponse<Set<CustomerDto>>` |
| Shared platform page | `ServiceResponse<Page<CustomerDto>>` |
| Registered application container | `ServiceResponse<Window<CustomerDto>>` |

BYOE supports the same payload shapes. Platform `Page<T>` is the shared Java contract, not Spring
Data's `Page<T>`. Supported MVC wrappers such as `ResponseEntity`, `CompletionStage`/`Future`,
`DeferredResult` and `WebAsyncTask` are unwrapped during introspection. Enum payloads should use
`@Schema(enumAsRef = true)`.

Component schema names must match the resolved Java simple type names; custom names such as
`@Schema(name = "...")` are outside scope. See the [full shape boundaries](../compatibility.md#contract-scope)
for the limits on nested generic graphs and non-MVC integrations.

## Register an application container

To project `ServiceResponse<Window<CustomerDto>>` or `ApiResponse<Window<CustomerDto>>`, register the
container type and its item collection property:

```yaml
openapi-generics:
  containers:
    - type: com.example.contract.Window
      item-property: items
```

The type must be a concrete class/record with one type parameter and a declared `List<T>` or `Set<T>`
item property using that parameter directly. Unregistered, inherited or nested generic item structures
are not inferred. Make this shared container type available on the generated client's classpath.

Container registration is independent of envelope selection. Metadata preserves the container FQCN
and item type, so reconstruction uses the same path as built-in containers rather than special-casing
`Window` or `Page`.

## Check the projected document

Start the producer and inspect `/v3/api-docs` or `/v3/api-docs.yaml`. Check the wrapper schema for the
endpoint you intend to generate; successful application startup alone does not establish shape coverage.

| Metadata | Check |
| --- | --- |
| `x-api-wrapper` | Boolean `true` identifies the generic wrapper |
| `x-api-wrapper-type` | FQCN of the selected Java envelope |
| `x-api-wrapper-datatype` | Projected payload name, e.g. `PageCustomerDto` |
| `x-data-container` | Semantic/simple container name, e.g. `Page` or `Window` |
| `x-data-container-type` | Shared Java container FQCN |
| `x-data-item` | Concrete container item type, e.g. `CustomerDto` |
| `x-ignore-model` | Retains a schema while suppressing duplicate Java model generation |

Container name/type/item metadata travel together; direct payloads need no container trio.
`x-envelope-import`, `x-envelope-type` and `x-extra-imports` are derived codegen metadata, not producer inputs.
Other OpenAPI tooling can ignore the vendor extensions and still consume the document.

Configured envelope/container invariants and projected contract inconsistencies fail explicitly through
the server's configuration, descriptor, projection or contract-validation exceptions. Some unresolved
introspection paths intentionally skip projection; check that the expected wrapper metadata is present.
These checks belong to contract publication, not ordinary HTTP request processing.

For maintained evidence, see [type/BYOE coverage](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/type-coverage/README.md) and the
[canonical web stack](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/gradle/customer-service/README.md). Review snapshot differences
before accepting producer contract changes; generated Java compilation and runtime HTTP behavior
are separate verification layers.

Next: [Maven or Gradle client adoption](client-side-adoption.md) ·
[Compatibility reference](../compatibility.md) · [Architecture](../architecture/architecture.md)
