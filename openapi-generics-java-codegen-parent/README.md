# openapi-generics-java-codegen-parent

> Build-time orchestration for deterministic, contract-aligned OpenAPI client generation

`openapi-generics-java-codegen-parent` is the parent POM that wires the full client-generation pipeline for `openapi-generics`.

It turns OpenAPI client generation from ad hoc plugin configuration into a controlled build process.

Its role is precise:

> Inherit one parent, provide your OpenAPI spec and normal client settings, and get contract-aligned Java client generation.

This is the primary integration surface for consumers.

---

## Table of Contents

1. [What Problem It Solves](#what-problem-it-solves)
2. [What It Provides](#what-it-provides)
3. [How It Works](#how-it-works)
4. [Usage](#usage)
5. [Optional Contract Alignment](#optional-contract-alignment)
6. [Compatibility Mode](#compatibility-mode)
7. [What Users Control](#what-users-control)
8. [What Users Should Not Override](#what-users-should-not-override)
9. [Compatibility](#compatibility)
10. [Determinism Guarantees](#determinism-guarantees)
11. [Failure Philosophy](#failure-philosophy)
12. [Mental Model](#mental-model)
13. [Related Modules](#related-modules)
14. [License](#license)

---

## What Problem It Solves

Plain OpenAPI Generator setups usually become inconsistent across projects.

That leads to:

* duplicated envelope models
* different template behavior between consumers
* fragile regeneration
* drift between published contract and generated client

This parent removes that variability by centralizing the build pipeline.

---

## What It Provides

By inheriting this parent, a consumer gets:

* custom generator wiring (`java-generics-contract`)
* template extraction from upstream OpenAPI Generator
* deterministic template patching for wrapper support
* local template overlay
* generated source registration in the Maven build

This means the consumer does not assemble the pipeline manually.

---

## How It Works

At build time, the parent orchestrates this flow:

```text
OpenAPI spec
   ↓
Extract upstream model.mustache
   ↓
Patch wrapper insertion hook
   ↓
Overlay openapi-generics templates
   ↓
Run custom generator
   ↓
Add generated sources to compilation
```

The patch step is validated.
If the upstream `model.mustache` structure changes in a way that breaks wrapper insertion, the build fails fast.

---

## Usage

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.0.0</version>
  <relativePath/>
</parent>
```

### 2. Configure the OpenAPI Generator plugin normally

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

        <apiPackage>com.example.generated.api</apiPackage>
        <modelPackage>com.example.generated.dto</modelPackage>
        <invokerPackage>com.example.generated.invoker</invokerPackage>

        <configOptions>
          <!-- Spring Boot 4 -->
          <!-- <useSpringBoot4>true</useSpringBoot4> -->
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

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

### 3. Build

```bash
mvn clean install
```

Generated sources are added automatically.

---

## Optional Contract Alignment

The parent supports optional alignment with externally owned contract types.

### BYOC — reuse external DTOs

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

### BYOE — use your own envelope

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Notes:

* `openapi-generics.envelope` is not needed for the default `ServiceResponse<T>` path
* if you configure a custom envelope, that envelope dependency must already exist on the client classpath
* DTO mappings are optional and only needed when models come from an external/shared contract module

---

## Compatibility Mode

The parent also supports fallback mode.

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

When enabled, the orchestration steps are skipped and the build falls back to standard OpenAPI Generator behavior.

Use this for:

* debugging generation differences
* comparing outputs
* incremental migration

---

## What Users Control

Consumers should control only the normal integration surface:

* `inputSpec`
* `library`
* package names
* standard generator config options
* optional BYOC / BYOE properties
* optional OpenAPI Generator version within supported range

These are safe customization points.

---

## What Users Should Not Override

Consumers should not override the platform-controlled execution path, including:

* generator name
* template directory
* patch pipeline behavior
* local wrapper template wiring

Those parts are what make the generation deterministic and contract-aware.

---

## Compatibility

| Component         | Supported Versions |
| ----------------- | ------------------ |
| Java              | 17+                |
| OpenAPI Generator | 7.x                |

Notes:

* the parent defaults to OpenAPI Generator `7.21.0`
* `restclient` requires OpenAPI Generator `7.6.0+`

---

## Determinism Guarantees

The parent is designed to provide:

* stable build phases
* stable wrapper template injection
* repeatable generated source layout
* controlled generator wiring across consumers

This is how multiple client projects get the same contract-aligned behavior instead of drifting through local configuration.

---

## Failure Philosophy

The parent fails fast when structural assumptions break.

Typical reasons:

* upstream template structure changed
* wrapper patch marker could not be inserted
* generation path no longer matches platform expectations

Principle:

> Incorrect generation is worse than no generation.

---

## Mental Model

Think of this module as:

> the build-time orchestrator for contract-safe Java client generation

Not:

* a runtime library
* a template bundle by itself
* a convenience wrapper around OpenAPI Generator

---

## Related Modules

| Module                                 | Role                                   |
| -------------------------------------- | -------------------------------------- |
| `openapi-generics-contract`            | Canonical contract authority           |
| `openapi-generics-server-starter`      | Runtime OpenAPI projection             |
| `openapi-generics-java-codegen`        | Contract-aware generator extension     |
| `openapi-generics-java-codegen-parent` | Consumer entry point and orchestration |

---

## License

MIT License
