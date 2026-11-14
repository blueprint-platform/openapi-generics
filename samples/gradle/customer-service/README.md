# Gradle-first customer service

A Spring Boot **4.1.2**, Java **25** application adoption stack using OpenAPI Generator **7.27.0**,
Springdoc **3.2.0** and Gradle **9.9.1**. These are this sample's verified references, not consumer support minimums.

```text
contract (pure Java CustomerDto)
    ↓                        ↘
producer → spec/customer-api-docs.json → client → consumer
```

- `producer`: Boot MVC application exposing direct and paged customer responses through the server starter.
- `contract`: application-owned shared DTO; no framework dependency.
- `client`: client library containing official `GenerateTask` generation, RestClient/Jackson 3 setup,
  BYOC mapping and the stable `CustomerClientAdapter` interface. Generated API/invoker types are used
  only inside its adapter implementation/configuration.
- `consumer`: Boot application using its own `CustomerServiceClient` interface/implementation, which
  delegates to `CustomerClientAdapter`. Public methods return shared `ServiceResponse<CustomerDto>`
  and `ServiceResponse<Page<CustomerDto>>` contracts.

The official generator version and ordinary generator options belong to the consumer build. The
OpenAPI Generics plugin provides its aligned codegen, templates, generated-source registration,
compilation dependency and normalization. There is no manual source-set, generator classpath or
template wiring. The sample uses a custom `generateCustomerClient` task and non-default source folder.

## Client library boundary

The separation follows the [Maven Boot 4 stack](../../spring-boot-4/customer-service-client/README.md):

```text
ConsumerApplication
  → consumer.service.client.CustomerServiceClient
  → CustomerServiceClientImpl
  → client.adapter.CustomerClientAdapter
  → CustomerClientAdapterImpl
  → generated CustomersApi / ApiClient
  → producer HTTP
```

`CustomerApiClientConfig` lives in `client` and builds the generated transport behind the adapter.
The consumer discovers client configuration through Spring scanning and supplies `customer.base-url`;
it neither creates nor imports generated API/invoker types. Only shared contract dependencies are
exported through the client library's Gradle `api` dependencies; RestClient and its Boot BOM are
implementation dependencies. The consumer selects its own Boot runtime dependencies.

## Repository development bootstrap

Use JDK 25. From the repository root, install the platform 1.3.0 artifacts from this checkout:

```bash
mvn -pl openapi-generics-server-starter,openapi-generics-java-codegen -am install -DskipTests
```

This sample uses the repository plugin through `includeBuild`, and restricts `mavenLocal()` to platform
artifacts. Its shared DTO is a Gradle project dependency, not a Maven sample prerequisite. Release
consumers replace `includeBuild` with the released OpenAPI Generics plugin version and resolve released
platform artifacts from their configured repository; they still select the official generator version.

For a standalone release consumer, use the [Gradle plugin guide](../../../openapi-generics-gradle-plugin/README.md). Its explicit plugin versions and standard repositories replace this checkout's development-only composite/local repository setup. The 1.3.0 artifacts must be published before public resolution can succeed.

## Build and verify

From this directory:

```bash
./gradlew clean build --no-build-cache --configuration-cache
./gradlew clean build --no-build-cache --configuration-cache
```

The second identical command must report configuration-cache reuse. Disabling the build cache here
ensures compilation, generation and tests actually execute after each clean, rather than being restored.
The ordinary `./gradlew build` also works with this sample's enabled configuration/build caches.

`build` checks four acceptance cases:

- Producer projection is compared semantically with the committed JSON snapshot; its independently
  generated document is retained under `producer/build/openapi/`. Drift fails verification and never
  silently overwrites the committed snapshot.
- Generated contract bindings compile automatically, reuse shared types without duplicate models, and
  have normalized wrapper declarations/imports.
- The client adapter interface exposes exact shared direct/paged contract types without generated types.
- A random-port Boot producer receives real HTTP calls from a separately configured consumer context
  through the consumer-owned service-client interface and library adapter to the generated RestClient.
  Direct and paged payloads deserialize to the exact shared `CustomerDto`,
  `Page`, and `ServiceResponse` hierarchy; metadata and page values are checked. No mocked HTTP or manual
  JSON parsing substitutes for this test.

Client generation reads only the committed spec; it never starts the producer:

```bash
./gradlew :client:build
```

Generated Java stays under `client/build/generated/customer/src/gen/java` and is not committed.

## Intentional contract changes

After reviewing a producer contract change, explicitly regenerate its snapshot:

```bash
./gradlew :producer:test -PupdateOpenApiSnapshot=true --rerun-tasks
./gradlew clean build --no-build-cache --configuration-cache
git diff -- spec/customer-api-docs.json
```

The first command is an explicit snapshot update, not the default verification behavior. Review and
commit intentional spec changes with the corresponding contract/producer change. Fixed OpenAPI server
metadata keeps the snapshot independent of test ports; the consumer configures the actual base URL.

## Run applications separately

In one terminal:

```bash
./gradlew :producer:bootRun
```

In another:

```bash
./gradlew :consumer:bootRun --args='--customer.base-url=http://localhost:8080'
```

The consumer prints the direct and paged responses and exits. Runtime tests start and close both
application contexts automatically and use a random server port.

This is an adoption example, not another coverage matrix. BYOE, custom containers, List/Set, async,
transport variants, multiple specs/generators and arbitrary templates are intentionally left to their
existing canonical verification areas.
