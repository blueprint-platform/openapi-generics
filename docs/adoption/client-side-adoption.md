---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-Side Adoption

> Generate a Java client that reconstructs the published contract instead of redefining it.

This guide explains how to consume an OpenAPI Generics-enabled OpenAPI document and generate a contract-aligned Java client.

For server-side projection, see [Server-Side Adoption](./server-side-adoption.md).  
For internals, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [What the Client Does](#what-the-client-does)
- [BYOE](#byoe)
- [BYOC](#byoc)
- [Supported Output Shapes](#supported-output-shapes)
- [Fallback Mode](#fallback-mode)
- [Verification](#verification)
- [Usage Boundary](#usage-boundary)

---

## Quick Start

### 1. Inherit the codegen parent

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.1.0</version>
</parent>
```

### 2. Configure OpenAPI Generator

Use the OpenAPI Generator plugin as usual, but set the generator name to:

```xml
<generatorName>java-generics-contract</generatorName>
```

That is the OpenAPI Generics integration point.

Other options such as `library`, `apiPackage`, `modelPackage`, `invokerPackage`, and Spring Boot/Jackson configuration remain normal OpenAPI Generator choices.

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
                <inputSpec>${project.basedir}/src/main/resources/api-docs.yaml</inputSpec>

                <!-- Standard OpenAPI Generator choice. Use the client library that fits your project. -->
                <library>restclient</library>

                <!-- Standard generated package layout. Generic wrappers are emitted under modelPackage. -->
                <apiPackage>com.example.client.api</apiPackage>
                <modelPackage>com.example.client.dto</modelPackage>
                <invokerPackage>com.example.client.invoker</invokerPackage>

                <configOptions>
                    <!-- Choose the Spring/Jackson options that match your runtime. -->
                    <useSpringBoot3>true</useSpringBoot3>
                    <serializationLibrary>jackson</serializationLibrary>
                    <openApiNullable>false</openApiNullable>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Expected generated wrapper shape:

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

`Page<T>` is imported from `openapi-generics-contract`; the wrapper class itself is generated under your configured `modelPackage`.

### 3. Build

```bash
mvn clean install
```

Generated sources are added automatically from:

```text
target/generated-sources/openapi/src/gen/java
```

---

## What the Client Does

The client generation pipeline consumes an OpenAPI document and reconstructs contract-aligned Java wrapper types.

It uses OpenAPI Generics vendor extensions such as:

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-item: CustomerDto
x-ignore-model: true
```

The OpenAPI document is the input.

The Java contract remains the semantic target.

---

## BYOE

Use your own response envelope instead of the platform default `ServiceResponse<T>`.

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.envelope=com.example.contract.ApiResponse
    </additionalProperty>
</additionalProperties>
```

Generated wrappers then extend your envelope:

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {
}
```

Container payloads are also reconstructed when the OpenAPI document carries container metadata:

```java
public class ApiResponseListCustomerDto
    extends ApiResponse<List<CustomerDto>> {
}
```

```java
public class ApiResponsePageCustomerDto
    extends ApiResponse<Page<CustomerDto>> {
}
```

The configured envelope must be available on the client classpath.

---

## BYOC

Reuse DTOs you already own instead of regenerating them.

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
    </additionalProperty>
</additionalProperties>
```

Effect:

- `CustomerDto` is not generated
- generated wrappers import `com.example.contract.CustomerDto`
- the client reuses your contract-owned DTO

Add one mapping per externally owned OpenAPI model.

---

## Supported Output Shapes

Default platform envelope:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>
```

BYOE envelope:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>
```

`Page<T>` refers to:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
```

Generated wrappers are intentionally thin:

```java
public class ServiceResponseSetCustomerDto
    extends ServiceResponse<Set<CustomerDto>> {
}
```

They exist for transport binding and generic type preservation.

They are not domain models.

---

## Fallback Mode

To bypass OpenAPI Generics template patching and use stock OpenAPI Generator behavior:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Use this for:

- output comparison
- debugging
- incremental migration

Default:

```xml
<openapi.generics.skip>false</openapi.generics.skip>
```

---

## Verification

After generation, check:

### Wrappers should be thin

Expected:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

Not expected:

```java
public class ServiceResponsePageCustomerDto {
    private PageCustomerDto data;
    private Meta meta;
}
```

### Contract-owned models should not be regenerated

Generated DTO package should not contain:

```text
ServiceResponse
Meta
Sort
Page
```

Nor any DTO mapped through BYOC.

### BYOE and BYOC types must compile

If the build fails with `cannot find symbol`, the referenced contract module is missing from the client classpath.

---

## Usage Boundary

Generated APIs should stay behind an adapter boundary.

Application code should depend on contract types:

```java
ServiceResponse<CustomerDto>
```

or, with BYOE:

```java
ApiResponse<CustomerDto>
```

not on generated wrapper classes such as:

```java
ServiceResponseCustomerDto
ApiResponseCustomerDto
```

Recommended shape:

```java
public interface CustomerClient {

    ServiceResponse<CustomerDto> getCustomer(Long id);
}
```

Implementation delegates to the generated API.

This keeps generated code as a transport concern and prevents OpenAPI output from leaking into application logic.

---

## Further Reading

- [Server-Side Adoption](./server-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)