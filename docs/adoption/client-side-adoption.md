---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-Side Adoption

> Generate Java clients that reconstruct your published contract instead of redefining it.

This guide explains how to generate contract-aligned Java clients from an OpenAPI Generics document.

For server-side projection, see [Server-Side Adoption](./server-side-adoption.md).  
For architecture details, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [BYOE — Bring Your Own Envelope](#byoe)
- [BYOC — Bring Your Own Contract](#byoc)
- [Supported Contracts](#supported-contracts)
- [Fallback Mode](#fallback-mode)
- [Verification](#verification)
- [Usage Boundary](#usage-boundary)
- [Further Reading](#further-reading)

---

## Quick Start

### 1. Inherit the codegen parent

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.2.0</version>
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


### 3. Generate

```bash
mvn clean install
```

Generated sources are added automatically under:

```text
target/generated-sources/openapi/src/gen/java
```

---

## BYOE

Reuse your existing response envelope instead of `ServiceResponse<T>`.

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.envelope=com.example.contract.ApiResponse
    </additionalProperty>
</additionalProperties>
```

Generated wrappers extend your envelope while preserving the published contract semantics.

---

## BYOC

Reuse existing DTOs instead of generating duplicates.

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
    </additionalProperty>
</additionalProperties>
```

Mapped models are imported directly from your shared contract module.

---

## Supported Contracts

Built-in contracts:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

BYOE envelopes support the same response shapes.

Application-defined generic containers (for example `Paging<T>` or `Window<T>`) participate in the same projection and reconstruction pipeline when published through OpenAPI Generics metadata.

---

## Fallback Mode

Disable OpenAPI Generics template patching:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

To return completely to standard OpenAPI Generator behavior, use:

```xml
<generatorName>java</generatorName>
```

---

## Verification

After generation, verify that:

- wrappers extend existing contracts rather than redefining them
- contract-owned infrastructure models are not regenerated
- BYOE and BYOC types resolve successfully
- configured generic containers are reconstructed identically to built-in containers

---

## Usage Boundary

Generated wrappers are transport bindings, not application contracts.

Application code should depend on shared contract types such as:

```java
ServiceResponse<CustomerDto>
```

or

```java
ApiResponse<CustomerDto>
```

rather than generated wrapper classes.

Keep generated clients behind an adapter boundary so application code remains independent of generated artifacts.

---

## Further Reading

- [Server-Side Adoption](./server-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)