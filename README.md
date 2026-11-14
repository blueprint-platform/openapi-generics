<h1 align="center">OpenAPI Generics for Spring Boot</h1>

<p align="center">
  <a href="https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml"><img src="https://github.com/blueprint-platform/openapi-generics/actions/workflows/build.yml/badge.svg" alt="Build" /></a>
  <a href="https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml"><img src="https://github.com/blueprint-platform/openapi-generics/actions/workflows/codeql.yml/badge.svg" alt="CodeQL" /></a>
  <a href="https://codecov.io/gh/blueprint-platform/openapi-generics"><img src="https://codecov.io/gh/blueprint-platform/openapi-generics/branch/main/graph/badge.svg" alt="Codecov" /></a>
  <br />
  <a href="https://github.com/blueprint-platform/openapi-generics/releases/latest"><img src="https://img.shields.io/github/v/release/blueprint-platform/openapi-generics" alt="Release" /></a>
  <a href="https://central.sonatype.com/artifact/io.github.blueprint-platform/openapi-generics-server-starter"><img src="https://img.shields.io/maven-central/v/io.github.blueprint-platform/openapi-generics-server-starter" alt="Maven Central" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT" /></a>
  <br />
  <a href="docs/compatibility.md"><img src="https://img.shields.io/badge/Java-17%2B-lightgrey?logo=openjdk" alt="Java: 17+" /></a>
  <a href="docs/compatibility.md"><img src="https://img.shields.io/badge/Spring%20Boot-3.5.x%20%E2%86%92%204.x-6DB33F?logo=springboot&amp;logoColor=white" alt="Spring Boot: 3.5.x → 4.x" /></a>
  <a href="docs/compatibility.md"><img src="https://img.shields.io/badge/OpenAPI%20Generator-7.x-blue?logo=openapiinitiative" alt="OpenAPI Generator: 7.x" /></a>
</p>

A generated client can match your JSON and still replace your shared Java response types.
**Keep your envelope and container contracts intact in generated Java clients.**
OpenAPI Generics carries their identity through OpenAPI metadata, then specializes OpenAPI Generator's
Java generation to bind concrete payloads back to those contracts.

## The transformation in 30 seconds

Your Spring MVC endpoint returns a shared Java contract:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers() { ... }
```

The server starter projects the **envelope, container and payload identities into OpenAPI**.
The `java-generics-contract` generator reads that metadata and binds the concrete response back to
the shared Java types:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

`ServiceResponse<T>` and `Page<T>` still own their fields and behavior. The generated subclass is a
**transport binding**, not a new application contract. Generation reads the spec; it needs no running producer.

<details>
<summary>Inspect the OpenAPI identity metadata</summary>

```yaml
# ServiceResponsePageCustomerDto schema
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

OpenAPI is the protocol between server projection and client reconstruction. These extensions carry
Java identity without making OpenAPI the contract authority. Other tooling can ignore them.
[Metadata requirements and checks →](docs/adoption/server-side-adoption.md#check-the-projected-document)

</details>

## See the proof

![For the same response schema, stock Java generates PageCustomerDto data and Meta fields; java-generics-contract generates a subclass extending the shared ServiceResponse of Page of CustomerDto.](docs/images/proof/generic-binding-comparison.svg)

Excerpts from generated sources compiled with the **same spec and client options**, with optional DTO
reuse disabled. `CustomerDto` remains upstream-generated in both outputs; the binding preserves the
shared `ServiceResponse` and `Page` identities.
[Proof source and reproduction →](docs/images/README.md)

## Start with your project

The adoption examples use explicit **1.3.0** platform versions. Configure Maven Central for platform
dependencies and Gradle Plugin Portal for plugin ID resolution. Repository samples have a separate
[development setup](samples/README.md#repository-build-and-local-runtime-setup);
local installs and composite builds are not release-consumer requirements.

### 1. Project the contract from your service

Add `io.github.blueprint-platform:openapi-generics-server-starter:1.3.0` alongside your selected Springdoc
WebMVC dependency. Keep supported controller signatures unchanged; the built-in `ServiceResponse<T>`
needs no envelope configuration. [Server setup →](docs/adoption/server-side-adoption.md)

### 2. Generate the client with your build tool

| Maven | Gradle |
| --- | --- |
| Use the Maven codegen parent with your official generator execution. | Apply the official OpenAPI Generator plugin together with the platform Gradle plugin. |
| **[Maven setup →](docs/adoption/client-side-adoption.md#maven)** | **[Gradle setup →](docs/adoption/client-side-adoption.md#gradle)** |

Both select `java-generics-contract` and use the same Java reconstruction semantics. You choose the
upstream generator version, spec, library, packages and ordinary options, and supply shared-contract
and client-library dependencies. The integrations prepare templates and wire generated Java into compilation;
see each guide for its lifecycle and source-layout details.

## Follow a complete application

The **[canonical Gradle Boot 4 stack](samples/gradle/customer-service/README.md)** is a producer web app,
a reusable client library and a consumer web app. Its acceptance tests start both real servers and
call the consumer's HTTP endpoints through the full chain:

```text
Consumer controller → service → CustomerServiceClient
  → client-library adapter → generated RestClient → producer HTTP endpoint
```

Tests verify direct/paged shared contracts, actual generic-wrapper deserialization and error handling
across both HTTP hops. Producer snapshot equality and repeated clean-build configuration-cache reuse
are checked separately. [Build, run and inspect the HTTP acceptance →](samples/gradle/customer-service/README.md#build-and-acceptance-verification)

The boundary matters: application code calls a stable client API; generated API/invoker classes and
wrapper bindings stay inside the client implementation. The **[Boot 4 Maven client library](samples/spring-boot-4/customer-service-client/README.md)**
shows the same separation with explicit payload mapping and a [consumer web app](samples/spring-boot-4/customer-service-consumer/README.md).
Its client HTTP tests use MockWebServer; the full two-application acceptance path above is separate evidence.

For focused checks, see [response-shape coverage](samples/type-coverage/README.md),
[transport interoperability](samples/transport-coverage/README.md) and
[multi-spec generation](samples/generation-coverage/multi-spec-maven-client/README.md).
[All samples and run instructions →](samples/README.md)

## What the platform owns

Java contracts are authoritative. OpenAPI Generics owns their envelope/container projection,
metadata validation and generic wrapper reconstruction. The generator specializes upstream
`JavaClientCodegen`; it is not a fork.

**Ordinary DTO, API, authentication and transport generation remain upstream-owned.** Your application
chooses its HTTP library, serialization configuration and runtime versions. The server starter runs
during Springdoc projection, not request handling; it does not change response serialization.
Interoperability tests exercise those surrounding systems without transferring their ownership.

## Choose your envelope; reuse DTOs when needed

**BYOE — Bring Your Own Envelope** is a core envelope ownership choice. Use the platform's
`ServiceResponse<T>` or keep your application's `ApiResponse<T>`. Configure the latter on the producer:

```yaml
openapi-generics:
  envelope:
    type: com.example.contract.ApiResponse
```

The client reads envelope identity from the projected metadata; no duplicate client envelope setting
is needed. Supply that shared Java type on the client classpath.
[Envelope requirements →](docs/adoption/server-side-adoption.md#choose-the-envelope)

**BYOC — Bring Your Own Contract** is separate, optional client-side DTO reuse. Map a schema name to an
existing Java type, and supply its dependency, instead of generating that model. It does not select
the envelope. Without BYOC, ordinary DTOs are still generated; map them at the adapter boundary when
application code needs its own types.
[DTO reuse mappings →](docs/adoption/client-side-adoption.md#optional-reuse-existing-dtos-with-byoc)

Built-in containers are `List<T>`, `Set<T>` and the platform's `Page<T>` (distinct from Spring Data's
`Page<T>`). Explicitly registered application containers use the same metadata-driven reconstruction.
[Container registration and limits →](docs/adoption/server-side-adoption.md#register-an-application-container)

## Compatibility and 1.3.0

Supported lines are **Java 17+**, Spring MVC on **Boot 3.5.x / 4.x**, **OpenAPI Generator 7.x**, Maven and
**Gradle 9.x**. Exact framework/tool references, supported shapes and integration-specific edges belong
in the [compatibility reference](docs/compatibility.md); current references are not consumer-wide pins.

1.3.0 adds first-class Gradle integration, stricter wrapper metadata validation and broader
async/multi-spec verification. Existing 1.2.x runtime contracts require no migration.
[Changelog →](CHANGELOG.md)

## Implementation and further reading

Find configuration and verification tasks in the [documentation hub](docs/index.md). Read
[architecture](docs/architecture/architecture.md) for projection, reconstruction and build boundaries,
or [design rationale](docs/architecture/rationale.md) for the trade-offs.

<details>
<summary>Implementation modules</summary>

| Module | Role |
| --- | --- |
| [contract](openapi-generics-contract/README.md) | Pure Java shared envelope, paging and metadata types |
| [server starter](openapi-generics-server-starter/README.md) | Spring MVC contract projection |
| [Java codegen](openapi-generics-java-codegen/README.md) | Shared Java reconstruction semantics and wrapper templates |
| [Maven parent](openapi-generics-java-codegen-parent/README.md) | Selected-toolchain coherence and Maven generation lifecycle |
| [Gradle plugin](openapi-generics-gradle-plugin/README.md) | Official-plugin integration and selected-task wiring |
| [platform bill of materials (BOM)](openapi-generics-platform-bom/README.md) | Internal platform dependency alignment |

</details>

[External BYOE adoption example](https://github.com/bsayli/licensing)

## Contributing and license

[Contributing](CONTRIBUTING.md) · [Issues](https://github.com/blueprint-platform/openapi-generics/issues) ·
[Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)

MIT — see [LICENSE](LICENSE). **Barış Saylı** · [GitHub](https://github.com/bsayli)

<!-- Scarf tracking pixel - README -->
<img referrerpolicy="no-referrer-when-downgrade"
src="https://static.scarf.sh/a.png?x-pxid=c623cace-dbdd-4882-96c2-ade255cfad1b&page=README.md"
width="1" height="1" alt=""
style="display:none;" />
