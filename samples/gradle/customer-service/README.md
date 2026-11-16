# Gradle customer application stack

A canonical producer → reusable client library → consumer web application, following the
[Spring Boot 4 Maven stack](../../spring-boot-4/customer-service-client/README.md). References:
Spring Boot **4.1.2**, Java **25**, Springdoc **3.2.0**, OpenAPI Generator **7.27.0**, Gradle **9.9.1**.
These are sample verification inputs, not consumer support minimums.

## Modules and ownership

| Module | Responsibility |
| --- | --- |
| `contract` | Pure Java shared `CustomerDto`; owned by the application, with no framework dependency. |
| `producer` | Boot MVC web application: controller → customer service, serving an in-memory customer directory and projecting Java response contracts through the server starter. |
| `client` | Reusable Java library: stable `CustomerClientAdapter`, Spring `CustomerApiClientConfig`, adapter implementation and generated RestClient. Its public API returns shared contracts and exposes a stable `ApiProblemException`. |
| `consumer` | Boot MVC web application: controller → application service → consumer-owned remote client interface/implementation → library adapter. |

```text
HTTP caller
  → consumer CustomerConsumerController
  → CustomerConsumerService / CustomerConsumerServiceImpl
  → CustomerServiceClient / CustomerServiceClientImpl
  → client CustomerClientAdapter / CustomerClientAdapterImpl
  → generated CustomersApi / ApiClient
  → producer CustomerController → CustomerService
```

Both application `main` methods only call `SpringApplication.run(...)`. Both applications stay running
and expose HTTP endpoints. Generated API/invoker types are implementation details of `client`; the
consumer never imports them. The consumer discovers client configuration through Spring scanning
and supplies `customer.base-url`.

The read-only application exposes `GET /customers/1` and `GET /customers` on both web applications.
The in-memory directory contains customer `1` (`Ada`); unknown IDs return 404. Responses use shared
`ServiceResponse<CustomerDto>` and `ServiceResponse<Page<CustomerDto>>` types. BYOC (Bring Your Own Contract) already binds the
payload to the shared DTO, so there is no duplicate request DTO or artificial mapping layer for these
read-only operations. The service, remote-client and adapter boundaries follow the Maven example
without copying its larger CRUD/search API or transport coverage matrix.

## Client configuration and errors

Only shared contract dependencies are exported through Gradle `api`. The client library owns its
RestClient/Jackson 3 implementation dependencies; the consumer selects its own Boot web/runtime stack.
`CustomerApiClientConfig` clones the Boot-provided RestClient builder and installs a library-local
status handler. It does not modify the consumer's other RestClient builders.

Producer errors use RFC 9457 ProblemDetail responses; the producer spec documents the not-found
response and its ProblemDetail schema. Ordinary error-model generation remains upstream-owned. The client translates upstream HTTP failures to
`ApiProblemException`, retaining the HTTP status/title/detail for the sample's error contract. Empty or
malformed error bodies get a safe status-preserving fallback; transport/response-conversion failures
become 502. The consumer's `CustomerConsumerExceptionHandler` maps this stable exception to an
`application/problem+json` response. The consumer owns the outgoing problem instance URI. Invalid
consumer path parameters use Boot MVC's normal 400 ProblemDetail handling.

## Generation and reconstruction

```text
Producer Java contract → committed spec/customer-api-docs.json
  → client generateCustomerClient → normalized generated bindings → client library
```

The consumer build chooses the official generator plugin and normal generator options. The platform
plugin provisions its matching codegen, discovers upstream templates from the running official
plugin, prepares selected tasks, registers generated sources/compilation dependencies and normalizes
output before task snapshots. The sample uses a custom `GenerateTask`, `java-generics-contract`,
RestClient/Jackson 3, BYOC and non-default `src/gen/java`. There are no manual source-set, compilation,
classpath or template wiring workarounds.

Generated wrappers bind concrete parameters around shared Java `ServiceResponse`, `Page` and
`CustomerDto`; they do not define new contract authorities. Client generation reads only the committed
spec and never starts the producer. Generated Java remains under
`client/build/generated/customer/src/gen/java` and is not committed.

## Repository development bootstrap

Use JDK 25. From the repository root, install platform **1.3.0** from this checkout:

```bash
mvn -pl openapi-generics-server-starter,openapi-generics-java-codegen -am install -DskipTests
```

This checkout uses `includeBuild` for the plugin and restricted `mavenLocal()` for platform artifacts.
The shared DTO is a Gradle project dependency. These are repository development mechanisms, not
release-consumer prerequisites. Standalone consumers use explicit plugin versions and release
repositories as shown in the [plugin guide](../../../openapi-generics-gradle-plugin/README.md).

## Build and acceptance verification

From this directory:

```bash
./gradlew clean build --no-build-cache --configuration-cache
./gradlew clean build --no-build-cache --configuration-cache
```

The second identical command must report configuration-cache reuse. Both runs regenerate sources,
compile and run tests after clean; build cache is disabled to prevent restored output from replacing
execution. CI checks that application class outputs are initially absent, then runs the same path
and explicitly asserts cache reuse and snapshot stability. Both application `bootJar` tasks declare
their main classes: Boot's automatic discovery otherwise probes output-directory existence during
configuration, making a fresh first build invalidate its own configuration cache.

Verification covers:

- Producer projection equals the committed snapshot semantically. Drift fails without overwriting the
  snapshot; independently generated output is retained under `producer/build/openapi/`.
- Generated bindings compile automatically, reuse shared models and preserve normalized wrapper
  inheritance/imports. The adapter interface exposes exact shared direct/paged contracts.
- `CustomerHttpTest` starts real producer and consumer web servers on separate random ports and calls
  the **consumer HTTP endpoints**. Direct/page results traverse controller → service → remote client →
  adapter → generated RestClient → producer. Exact shared payload/page types, values and metadata are
  checked after both HTTP hops. A separate adapter-level assertion preserves proof that upstream
  deserialization used the generated generic wrapper subclasses.
- Unknown producer IDs traverse the complete chain and become consumer 404 ProblemDetail responses;
  invalid consumer IDs produce 400. Client tests check status-preserving fallback for empty/malformed
  upstream errors and 502 translation for transport failures. These targeted error tests use a mock
  HTTP exchange; the successful and not-found acceptance paths use both real applications.

The client library also builds independently, without either application running:

```bash
./gradlew :client:build
```

## Run the web applications

In separate terminals:

```bash
./gradlew :producer:bootRun
```

```bash
./gradlew :consumer:bootRun
```

Producer defaults to port **8080**; consumer defaults to **8081**, with
`customer.base-url=http://localhost:8080`. Override application properties through normal Boot flags,
for example `:consumer:bootRun --args='--server.port=8082 --customer.base-url=http://localhost:8080'`.

Call the consumer:

```bash
curl http://localhost:8081/customers/1
curl http://localhost:8081/customers
curl -i http://localhost:8081/customers/999
```

The first two return shared contract JSON; the last returns a 404 ProblemDetail. Stop each application
normally when finished. Tests close their application contexts automatically.

## Intentional producer contract changes

Review the independently generated producer spec before an explicit snapshot update:

```bash
./gradlew :producer:test -PupdateOpenApiSnapshot=true --rerun-tasks
./gradlew clean build --no-build-cache --configuration-cache
git diff -- spec/customer-api-docs.json
```

The update flag is never part of ordinary verification. Fixed producer OpenAPI server metadata keeps
the snapshot independent of test ports; the adapter uses the configured runtime base URL.

Application-owned envelopes, custom containers, List/Set/async, multipart/binary/form transport, multiple specs/generators and
arbitrary template coverage remain in their existing dedicated verification areas.
