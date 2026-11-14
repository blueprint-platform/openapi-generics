---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-side adoption

**Generate transport bindings around the Java contracts your application owns.**
Given producer metadata for `ServiceResponse<Page<CustomerDto>>`, the shared `java-generics-contract`
specialization emits:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The wrapper lives in your generated `modelPackage`; envelope and container types come from shared Java
contracts. Ordinary DTO/API/authentication/transport generation stays upstream-owned. `CustomerDto`
is generated normally unless you opt into BYOC (Bring Your Own Contract). Treat the wrapper as a transport binding: expose a
stable adapter API and keep generated API/invoker classes inside the client library.

## Before you start

You need the producer's [projected OpenAPI document](server-side-adoption.md#check-the-projected-document),
the shared envelope/container dependencies, and the dependencies required by your selected upstream
client library. Generation reads the spec; it does not need a running producer or Springdoc in the client.
An externally maintained spec can supply the same reconstruction metadata directly; Java contracts
retain authority.

Examples target prepared platform **1.3.0** artifacts and require them in your configured release
repositories. Public publication has not been performed by this checkout. Release consumers use
explicit versions; repository `includeBuild`/`mavenLocal` bootstrap is for development.

| Build | Platform entry point | Who selects upstream OpenAPI Generator (OAG)? |
| --- | --- | --- |
| [Maven](#maven) | `openapi-generics-java-codegen-parent` | Consumer `openapi-generator.version`; current default 7.27.0 |
| [Gradle](#gradle) | `io.github.blueprint-platform.openapi-generics` plugin | Consumer applies and versions the official `org.openapi.generator` plugin |

Both paths use the same reconstruction semantics. You retain spec, output, library, package and
ordinary generator options. [Supported lines and integration edges →](../compatibility.md)

## Maven

Inherit the parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.3.0</version>
  <relativePath/>
</parent>
```

Declare the official generator in `<build><plugins>`:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-client</id>
      <goals><goal>generate</goal></goals>
      <configuration>
        <generatorName>java-generics-contract</generatorName>
        <inputSpec>${project.basedir}/spec/customer.yaml</inputSpec>
        <library>restclient</library>
        <apiPackage>com.example.client.generated.api</apiPackage>
        <modelPackage>com.example.client.generated.model</modelPackage>
        <invokerPackage>com.example.client.generated.invoker</invokerPackage>
        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>jackson</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The parent prepares upstream templates and the wrapper overlay, registers sources, and normalizes
generated Java. Its `openapi-generator.version` keeps the official plugin, `openapi-generator-core`,
`openapi-generator` and upstream `Java/model.mustache` extraction aligned. Change that property rather
than independently versioning these pieces; the platform bill of materials (BOM) does not manage them.

With [client dependencies](#add-the-client-dependencies) in place, run `mvn clean verify`.
Default generated sources are under `target/generated-sources/openapi/src/gen/java`.
For lifecycle details and multiple outputs, see the [Maven parent](https://github.com/blueprint-platform/openapi-generics/blob/main/openapi-generics-java-codegen-parent/README.md)
and [multi-spec example](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/generation-coverage/multi-spec-maven-client/README.md).

## Gradle

Use the standard plugin repositories (`gradlePluginPortal()` / `mavenCentral()` in plugin management)
and apply both plugins in `build.gradle.kts`:

```kotlin
plugins {
    `java-library`
    id("org.openapi.generator") version "7.27.0"
    id("io.github.blueprint-platform.openapi-generics") version "1.3.0"
}
repositories { mavenCentral() }

openApiGenerate {
    generatorName.set("java-generics-contract")
    inputSpec.set(file("spec/customer.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/customer"))
    library.set("restclient")
    apiPackage.set("com.example.client.generated.api")
    modelPackage.set("com.example.client.generated.model")
    invokerPackage.set("com.example.client.generated.invoker")
    configOptions.set(mapOf(
        "sourceFolder" to "src/gen/java",
        "useSpringBoot4" to "true",
        "useJackson3" to "true",
        "serializationLibrary" to "jackson",
        "openApiNullable" to "false"
    ))
}
```

With [client dependencies](#add-the-client-dependencies) in place, run
`./gradlew build --configuration-cache`. With the Java plugin, selected `GenerateTask` outputs and
effective `sourceFolder` are registered automatically; compilation depends on generation. Custom and
multiple tasks get the same wiring, and non-selected tasks retain upstream behavior and consumer templates.

The platform integrates through `withPlugin`, discovers templates from the **running official plugin
classloader**, and provisions matching platform codegen. It does not apply/replace the official plugin
or resolve a second upstream template runtime. Effective upstream `configFile`/option precedence is
respected, and normalization runs before generation output snapshots; no consumer Spotless plugin is needed.

See the [complete Gradle consumer build](https://github.com/blueprint-platform/openapi-generics/blob/main/openapi-generics-gradle-plugin/README.md#standalone-consumer-example)
for settings/dependencies and [custom task configuration](https://github.com/blueprint-platform/openapi-generics/blob/main/openapi-generics-gradle-plugin/README.md#configuration-and-generated-source-wiring).

## Add the client dependencies

For both build tools, put shared `openapi-generics-contract:1.3.0` and application-owned envelope/container
contracts on the compile classpath. Add DTO contracts only when using BYOC or when your adapter requires them.

The Maven example uses **Boot 3.5 RestClient / Jackson 2**; the Gradle reference uses
**Boot 4 RestClient / Jackson 3**. Match dependencies to the selected generation profile.
The following are verified example sets, not platform-wide dependency requirements.

<details markdown="1">
<summary>Maven dependencies — Boot 3.5 / Jackson 2</summary>

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>3.5.16</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
<dependencies>
  <dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-contract</artifactId>
    <version>1.3.0</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
  </dependency>
  <dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
  </dependency>
  <dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
  </dependency>
  <dependency>
    <groupId>com.google.code.findbugs</groupId>
    <artifactId>jsr305</artifactId>
    <version>3.0.2</version>
  </dependency>
</dependencies>
```

</details>

<details markdown="1">
<summary>Gradle dependencies — Boot 4 / Jackson 3</summary>

```kotlin
dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    api("io.github.blueprint-platform:openapi-generics-contract:1.3.0")
    api("org.springframework:spring-web")
    api("tools.jackson.core:jackson-databind")
    implementation("jakarta.annotation:jakarta.annotation-api")
}
```

</details>

For the complete Boot 4/Jackson 3 application boundary and runtime verification,
see the [canonical Gradle stack](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/gradle/customer-service/README.md).

## Envelope and container identity come from the producer

**BYOE (Bring Your Own Envelope) is an envelope ownership choice**, configured on the [producer](server-side-adoption.md#choose-the-envelope).
For metadata `x-api-wrapper-type: com.example.contract.ApiResponse`, codegen reconstructs
`extends ApiResponse<Page<CustomerDto>>`; do not repeat an envelope setting on the client.

The same applies to registered application containers: `x-data-container-type` and item metadata select
the shared Java type. An `ApiResponse<Window<CustomerDto>>` binding requires those envelope/container
classes on the client classpath, not another client-side container registry.

## Optional: reuse existing DTOs with BYOC

If a DTO already belongs to a shared Java module, map its OpenAPI schema name to that type and declare
the actual shared dependency. This suppresses duplicate model generation and imports the shared type
into reconstructed wrappers. Without a mapping, ordinary DTO generation remains upstream-owned.

Maven, inside the generator execution's `<configuration>`:

```xml
<additionalProperties>
  <additionalProperty>openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto</additionalProperty>
</additionalProperties>
```

Gradle, inside the selected `GenerateTask`:

```kotlin
additionalProperties.put(
    "openapi-generics.response-contract.CustomerDto",
    "com.example.contract.CustomerDto"
)
```

The key is `openapi-generics.response-contract.<OpenAPI model name>`. A mapping does not provide the
classpath dependency and does not identify the envelope. BYOC and BYOE are independent choices.

## Keep generated types behind the client boundary

Follow the same layering for Maven and Gradle:

```text
Application controller → service → remote-client interface
  → client library adapter → generated API/invoker → HTTP producer
```

Expose a stable adapter API with shared contract types such as `ServiceResponse<Page<CustomerDto>>`.
Keep concrete generated wrappers and API/invoker setup inside the client module. If an ordinary
generated DTO should not cross that API, map it to an application-owned type there; BYOC can avoid
that translation when an existing shared DTO is already the right contract.

See the [Maven client adapter](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/spring-boot-4/customer-service-client/README.md) and
[canonical Gradle web stack](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/gradle/customer-service/README.md). Generated wrapper
inheritance enables transport/deserialization; it does not make generated source the contract owner.

## Verify the result

Check that wrappers extend the intended envelope and generic container, shared infrastructure/BYOC
models are not duplicated, and generated sources compile against the declared shared types. Run
application tests for actual HTTP/deserialization claims; successful generation or fixture-based
plugin tests alone do not establish runtime interoperability. See [verification layers](../compatibility.md#verification-evidence).

Invalid wrapper metadata and required template patch drift fail explicitly. Do not work around these
failures by inventing envelope/container identity or hand-editing generated source.

## Compare with stock generation

For Maven, `openapi.generics.skip=true` skips platform extraction/patch/overlay **and normalization**;
it does not change the generator name. A stock comparison also requires `generatorName=java`.
For Gradle, select upstream `java` on the task; Maven's skip property is not a Gradle opt-out API.
Normal upstream choices and template settings on non-selected Gradle tasks remain consumer-owned.

[Server adoption](server-side-adoption.md) · [Compatibility](../compatibility.md) ·
[Architecture](../architecture/architecture.md)
