# OpenAPI Generics Gradle Plugin

Generate Java clients that preserve shared Java envelope and container contracts. The plugin connects the official OpenAPI Generator Gradle plugin to the shared `java-generics-contract` specialization; it manages the reconstruction build lifecycle, while ordinary API/model/authentication/transport generation remains upstream-owned.

For a response such as `ServiceResponse<Page<CustomerDto>>`, the client binds a generated subclass to
the shared generic contract. Keep generated API/invoker/wrapper classes behind a library adapter; the
[canonical web stack](../samples/gradle/customer-service/README.md) shows the application boundary.
Ordinary DTO/API/transport generation stays upstream-owned; DTO reuse through BYOC (Bring Your Own Contract) is optional.

The standalone consumer example uses platform **1.3.0**. Configure Gradle Plugin Portal for plugin ID resolution and Maven Central for the matching codegen/contract dependencies.

## Prerequisites and version roles

- Java 17+ for platform artifacts. Choose a JDK supported by your Gradle distribution and application stack; repository plugin verification uses JDK 21, and the Boot 4 sample uses JDK 25.
- Gradle **9.x**; minimum tested distribution **9.0.0**, current wrapper/reference **9.9.1**.
- Apply the official `org.openapi.generator` plugin yourself. The platform never applies, replaces or overrides its version.
- General platform OpenAPI Generator support is **7.x**. This Gradle integration requires the `openApiGeneratorExtra` capability: its minimum API/verified capability baseline is **7.25.0**, current reference **7.27.0**, explicit negative regression **7.24.0**. These integration edges do not narrow Maven/platform-wide 7.x support.
- Put shared envelope/container types and any BYOC contracts on the generated client's compile classpath. Add the dependencies required by the selected upstream library/serialization options.

Current references are verification inputs, not universal consumer pins. See the [compatibility policy](../docs/compatibility.md). Production official API dependencies are `compileOnly` at the minimum baseline; functional/E2E runtime selection is independent.

## Standalone consumer example

The Gradle plugin supports the platform’s documented Spring Boot compatibility range, including
**Boot 3.5.x and 4.x**. Boot 4/Jackson 3 below is the canonical/reference example, not a Gradle support limit.

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "customer-client"
```

`build.gradle.kts` (Boot 4 RestClient/Jackson 3 reference example):

```kotlin
plugins {
    `java-library`
    id("org.openapi.generator") version "7.27.0"
    id("io.github.blueprint-platform.openapi-generics") version "1.3.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    api("io.github.blueprint-platform:openapi-generics-contract:1.3.0")
    api("org.springframework:spring-web")
    api("tools.jackson.core:jackson-databind")
    implementation("jakarta.annotation:jakarta.annotation-api")
}

openApiGenerate {
    generatorName.set("java-generics-contract")
    inputSpec.set(layout.projectDirectory.file("spec/customer.yaml").asFile.absolutePath)
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

This profile generates `jakarta.annotation.Generated`, `Nonnull` and `Nullable`, supplied by
`jakarta.annotation-api` above.

Place the producer's projected OpenAPI document at `spec/customer.yaml`, then run:

```bash
./gradlew build --configuration-cache
```

The library, dependency platform and other dependencies above are one application choice, not platform-wide requirements. The matching Boot 4/Jackson 3 application architecture is demonstrated by the [canonical stack](../samples/gradle/customer-service/README.md). The producer selects Springdoc: Boot 3.5 reference **2.10.0**, Boot 4 reference **3.2.0**. A client generation build does not need Springdoc or a running producer.

## Configuration and generated-source wiring

Consumers own the spec, generator name, output, library, packages and ordinary options. The platform provisions only the matching `openapi-generics-java-codegen` artifact, using its own plugin version. Do not add a second codegen version; conflicting direct declarations fail explicitly.

For every `GenerateTask` selecting `java-generics-contract`, the plugin:

1. Discovers upstream `Java/model.mustache` through the running official plugin classloader, patches required dispatch anchors and overlays the platform wrapper template. Missing/ambiguous ownership or patch drift fails explicitly.
2. Wires the selected task's generator classpath and preparation dependency, including custom tasks.
3. With the Java plugin, registers the task's output/effective `sourceFolder` in main Java sources and wires compilation to generation.
4. Normalizes generated Java as the generation task's final action, before output snapshots. Consumers do not need to apply Spotless.

No manual `sourceSets`, `compileJava.dependsOn`, template extraction or generator classpath workaround is required. Upstream `configFile` binding is respected: `configOptions` overrides task `additionalProperties`, then config-file values, then defaults. File contents are tracked for configuration-cache invalidation. Generation, registration and normalization share the same source root.

Custom tasks use the official task type and the same selection rule:

```kotlin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

// Place the import at the top of build.gradle.kts.
tasks.register<GenerateTask>("generateOrders") {
    generatorName.set("java-generics-contract")
    inputSpec.set(file("spec/orders.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/orders"))
    library.set("restclient")
    apiPackage.set("com.example.orders.api")
    modelPackage.set("com.example.orders.model")
    invokerPackage.set("com.example.orders.invoker")
}
```

Use separate outputs/packages for multiple tasks. Non-selected tasks retain ordinary upstream behavior and consumer `templateDir` settings. The platform owns effective templates for selected tasks. To use stock generation, select upstream `java`; Maven's `openapi.generics.skip` is not a Gradle opt-out API.

When `generateModelDocumentation` is enabled, the shared codegen artifact supplies a RestClient-only
wrapper doc overlay: it shows the Java generic binding and shared identities without broken local
links to suppressed models. Ordinary model/enum docs remain upstream-owned. API doc examples and
generated tests are not rewritten by this feature; generating tests does not register a test source root.

## Envelope ownership, optional DTO reuse and contract authority

Java contracts are authoritative. OpenAPI is their projection and the protocol between producer and codegen. A generated wrapper such as `ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>>` binds concrete generic parameters for transport/deserialization; it is not another contract definition.

**BYOE (Bring Your Own Envelope):** The producer configures `openapi-generics.envelope.type` and emits `x-api-wrapper-type`. The client derives envelope identity/imports from that metadata; there is no duplicate client envelope setting. Supply the application-owned envelope/container dependency to the client. BYOE requires a concrete class with one generic parameter and one direct payload field of that parameter's type.

**BYOC:** Map schema names to shared Java models through generator `additionalProperties`, independently of envelope identity:

```kotlin
// Inside the selected GenerateTask configuration:
additionalProperties.put(
    "openapi-generics.response-contract.CustomerDto",
    "com.example.contract.CustomerDto"
)
```

Also declare your actual shared contract dependency, for example `api(project(":customer-contract"))` in a multi-project consumer build. Mapping alone does not supply a classpath dependency. Built-in List/Set/Page and explicitly registered application containers use the same metadata-driven reconstruction semantics as Maven. See [client adoption](../docs/adoption/client-side-adoption.md) and [server adoption](../docs/adoption/server-side-adoption.md) for supported shapes and configuration.

## Repository verification and development

From this plugin directory:

```bash
./gradlew check
./gradlew e2eTest
```

`check` also creates unit-only JaCoCo reports: XML at
`build/reports/jacoco/test/jacocoTestReport.xml` and HTML at
`build/reports/jacoco/test/html/index.html`. Run `./gradlew test jacocoTestReport` for that report alone.
All production plugin classes are included. For combined unit + nested Functional/TestKit +
real-codegen E2E coverage, run:

```bash
./gradlew check e2eTest jacocoCombinedReport -PcombinedCoverage=true
```

CI uploads `build/reports/jacoco/jacocoCombinedReport/jacocoCombinedReport.xml` under
`gradle-plugin`; HTML is beside it in `html/index.html`. The opt-in test harness instruments
only plugin classes in nested Gradle JVMs and explicitly collects their execution data over
loopback sockets, including after expected failures. Collection runs serially and retains
configuration-cache assertions. The canonical Boot stack remains outside coverage. Without
the property, verification and the unit-only report need no nested agent or socket.
Codecov tracks this flag against its own automatic baseline with the existing 2% tolerance. The
repository-wide aggregate and patch checks still include all reported production code; existing
thresholds are unchanged. Coverage combines executed lines; each layer's assertions still
establish its own behavioral evidence.

| Layer | Evidence |
| --- | --- |
| Unit | Plugin-owned selection, source layout/precedence, compatibility/version guards, template ownership/patching and normalization |
| Functional/TestKit | Gradle application/lifecycle, classpath/source/task wiring, mixed/multiple tasks and configuration cache |
| Real-codegen E2E | Actual shared reconstruction artifact, generation, compilation and cache behavior; model-focused, with no Boot runtime |
| [Canonical Boot 4 stack](../samples/gradle/customer-service/README.md) | Real producer/consumer applications, direct/page HTTP deserialization, errors, snapshot equality and clean-build cache reuse |

The model-only E2E fixtures select Jakarta annotations with `useJakartaEe`, not a Spring Boot runtime.
A `useSpringBoot3` generation option alone would not prove Boot 3.5 compatibility. Boot 3.5 remains
verified by the [Maven Boot 3.5 stack](../samples/spring-boot-3/customer-service-client/README.md) and type-coverage builds;
it is not a second Gradle default application stack. The public Gradle path above and canonical
application reference use Boot 4/Jackson 3; those example choices do not change supported version lines.

The repository sample uses `includeBuild` and restricted `mavenLocal()` to test this checkout. Those are development mechanisms, not release-consumer prerequisites. Standalone consumers use explicit plugin versions, Gradle Plugin Portal for plugin discovery, and Maven Central for project dependencies as above.
