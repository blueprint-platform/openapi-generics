# Samples: choose an application or a verification surface

Start with an application stack to see the client-library boundary. Use coverage samples to inspect
specific projection, reconstruction or build behavior; they are not alternative application architectures.

| Goal | Sample | What it demonstrates |
| --- | --- | --- |
| Run a Gradle Boot 4 application | [Canonical customer stack](gradle/customer-service/README.md) | Two real web apps, reusable client adapter, shared BYOC (Bring Your Own Contract) DTO, direct/page HTTP, errors and cache replay |
| Use Maven with Boot 3 or Boot 4 | [Boot 3 client](spring-boot-3/customer-service-client/README.md) / [Boot 4 client](spring-boot-4/customer-service-client/README.md) | Producer → committed spec → client library/adapter → consumer service/controller |
| Inspect built-in envelope and container shapes | [ServiceResponse coverage](type-coverage/service-response/README.md) | Scalars/value/DTO/List/Set/Page/custom containers and representative MVC async projection |
| Keep an application-owned envelope | [BYOE (Bring Your Own Envelope) coverage](type-coverage/byoe-response/README.md) | `ApiResponse<T>` ownership, shared containers and generated binding compilation |
| Check multipart/binary/form interoperability | [Transport coverage](transport-coverage/README.md) | Ordinary upstream RestClient transport alongside generic JSON bindings |
| Generate from multiple specs in one Maven artifact | [Multi-spec generation](generation-coverage/multi-spec-maven-client/README.md) | Separate outputs/packages, execution-scoped BYOC and compile-time probe |

Generated wrappers bind generic parameters for transport. They are not application contracts.
Application stacks expose stable adapters/shared types; focused coverage harnesses may use generated
classes directly to probe their behavior. Do not copy harness boundaries into application code.

For a small first build using released artifacts and public repositories only, use the
[quickstart](../quickstart/README.md). This directory keeps the fuller application and verification
surfaces, including their repository development bootstrap.

## Repository build and local runtime setup

Use JDK 25 for the full Maven sample reactor (some modules target Java 21, others Java 25); platform
artifacts still target Java 17+. From the repository root:

```bash
mvn clean install
mvn -f samples/pom.xml clean verify
```

This runs Maven generation, compilation and configured tests. To run individual Maven apps afterward,
install the sample dependencies as well:

```bash
mvn -f samples/pom.xml install
```

The Gradle stack is a separate build; follow its [bootstrap and verification instructions](gradle/customer-service/README.md).
These local installs/composite builds are development setup, not public-consumer requirements.
For an independent project, use the [adoption guides](../docs/adoption/index.md).

## Run the Maven customer applications

| Stack | Producer base URL | Consumer base URL |
| --- | --- | --- |
| Boot 3 | `http://localhost:8084/customer-service` | `http://localhost:8085/customer-service-consumer` |
| Boot 4 | `http://localhost:8094/customer-service` | `http://localhost:8095/customer-service-consumer` |

From `samples/spring-boot-4` (or `spring-boot-3`), with Docker and Compose v2:

```bash
docker compose up --build -d
```

For Boot 4, inspect the producer spec and call the consumer:

```bash
curl http://localhost:8094/customer-service/v3/api-docs.yaml
curl -H 'API-Version: 1' http://localhost:8095/customer-service-consumer/customers/1
curl -H 'API-Version: 1' 'http://localhost:8095/customer-service-consumer/customers?page=0&size=5'
```

Boot 3 uses the corresponding 8084/8085 URLs without a version header. Swagger UI is at
`<producer-base>/swagger-ui/index.html`. Stop Compose with `docker compose down` in the same directory.
For local Boot execution and component responsibilities, follow each module README.

## What verification proves

- Producer snapshot tests regenerate committed specs; review diffs and use CI's drift check. A normal
  success JSON response is not proof that the OpenAPI metadata is correct.
- Generated compilation proves type/source compatibility. Type coverage has producer snapshot tests
  and client/consumer compilation; it does not automatically execute every HTTP route.
- Maven customer client tests exercise generated calls against MockWebServer. Canonical Gradle
  acceptance additionally starts both real apps and calls consumer HTTP endpoints through the full chain.
- Transport producer `TransportControllerIT` requires explicit selection; the default POM does not bind
  Failsafe for that class. See its README before interpreting `clean verify` as transport runtime proof.
- Plugin fixture-based wiring, real-codegen E2E and application HTTP tests are different evidence layers.

BYOE is producer envelope ownership; BYOC is optional client-side DTO reuse. The shared
[customer-contract](domain-contracts/README.md) used by Maven adapters and the Gradle BYOC DTO are
application-owned models, not platform envelope definitions.

[Compatibility](../docs/compatibility.md) · [Architecture](../docs/architecture/architecture.md)
