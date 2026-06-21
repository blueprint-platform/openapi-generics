# openapi-generics-java-codegen

> Contract-aware Java generator extension for OpenAPI Generics

`openapi-generics-java-codegen` provides the custom OpenAPI Generator implementation used by OpenAPI Generics.

It defines the generator named:

```text
java-generics-contract
```

This module is build-time only.

In normal usage, consumers do not depend on it directly. It is wired through:

```text
openapi-generics-java-codegen-parent
```

---

## Contents

- [What It Does](#what-it-does)
- [Generation Pipeline](#generation-pipeline)
- [Wrapper Reconstruction](#wrapper-reconstruction)
- [BYOE](#byoe)
- [BYOC](#byoc)
- [Container Metadata](#container-metadata)
- [Model Filtering](#model-filtering)
- [Out of Scope](#out-of-scope)
- [Mental Model](#mental-model)

---

## What It Does

This module extends OpenAPI Generator’s Java client generation with OpenAPI Generics semantics.

Responsibilities:

- detect OpenAPI Generics wrapper models
- apply envelope metadata
- apply container metadata
- reuse external contract models
- filter ignored infrastructure models
- generate contract-aligned wrapper classes

It consumes metadata already present in the OpenAPI document.

It does not discover server-side Java contracts.

It does not orchestrate Maven template extraction or patching.

---

## Generation Pipeline

```text
OpenAPI Models
      ↓
Model Ownership Resolution
      ↓
Ignored Model Filtering
      ↓
Envelope Metadata Enrichment
      ↓
Container Metadata Enrichment
      ↓
External Import Resolution
      ↓
Wrapper Reconstruction
```

The generator operates on OpenAPI model metadata and produces Java client types.

---

## Wrapper Reconstruction

OpenAPI wrapper schemas are reconstructed as thin Java subclasses.

Example:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {
}
```

Container example:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

BYOE example:

```java
public class ApiResponseListCustomerDto
    extends ApiResponse<List<CustomerDto>> {
}
```

Generated wrappers intentionally contain no behavior.

Their role is to bind generic parameters to contract-owned envelope types.

---

## BYOE

BYOE is configured through generator additional properties:

```xml
<additionalProperty>
  openapi-generics.envelope=io.example.contract.ApiResponse
</additionalProperty>
```

The generator applies the configured envelope as the wrapper superclass.

Default envelope:

```java
ServiceResponse<T>
```

Configured envelope example:

```java
ApiResponse<T>
```

The envelope type must be available on the generated client classpath.

---

## BYOC

BYOC maps OpenAPI model names to externally owned Java types.

```xml
<additionalProperty>
  openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
</additionalProperty>
```

Effect:

- the mapped model is not generated
- wrapper classes import the external type
- generated clients reuse the contract-owned DTO

This keeps DTO ownership outside generated code.

---

## Container Metadata

The generator reconstructs supported container payloads using OpenAPI vendor extensions:

```yaml
x-data-container: List
x-data-item: CustomerDto
```

Built-in container mappings:

```java
List<T>

Set<T>

Page<T>
```

`Page<T>` refers to:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

Container imports and type names are applied during metadata enrichment.

Additional container mappings can be registered through:

```text
openapi-generics.data-container.<ContainerName>
```

---

## Model Filtering

Some schemas exist only as OpenAPI projection artifacts.

They must not become generated DTOs.

The generator filters models marked with:

```yaml
x-ignore-model: true
```

It also filters models registered through BYOC.

Ignored model imports are removed from generated wrapper models so wrappers do not reference non-generated classes.

---

## Out of Scope

This module does not:

- run Springdoc
- inspect controller return types
- create OpenAPI schemas
- orchestrate Maven plugin phases
- patch upstream templates
- own runtime application behavior

Those responsibilities belong to the server starter or the codegen parent.

---

## Mental Model

```text
OpenAPI + Vendor Extensions
        ↓
Contract-Aware Java Generation
        ↓
Thin Wrapper Types
```

This module does not try to generate more Java models.

It generates the minimum Java types required to preserve the original contract shape.