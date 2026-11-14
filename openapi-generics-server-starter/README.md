# openapi-generics-server-starter

Publish the Java envelope/container identity that a generated client needs to preserve generic contracts.
The starter runs during Springdoc document generation; normal MVC request handling and serialization
remain with your application.

```text
ServiceResponse<Page<CustomerDto>>
  → OpenAPI wrapper schema + envelope/container metadata
  → client generation can bind extends ServiceResponse<Page<CustomerDto>>
```

## Adopt in a service

Add `io.github.blueprint-platform:openapi-generics-server-starter:1.3.0` alongside your selected
Springdoc WebMVC starter. The built-in `ServiceResponse<T>` needs no envelope setting.
[Server adoption](../docs/adoption/server-side-adoption.md) contains the complete dependency/configuration
path and exact envelope/container requirements; [compatibility](../docs/compatibility.md) owns version references.

To keep an application-owned envelope, configure BYOE (Bring Your Own Envelope) on the producer:

```yaml
openapi-generics:
  envelope:
    type: com.example.contract.ApiResponse
```

This is envelope ownership, not a client DTO mapping. The producer emits `x-api-wrapper-type`;
the client derives the envelope identity from it without repeated configuration.

## Projection responsibilities

1. Discover supported MVC response signatures and unwrap supported synchronous/async wrappers.
2. Resolve the selected envelope and built-in/registered container descriptors.
3. Project concrete wrapper schemas, enrich metadata and mark duplicate infrastructure models for suppression.
4. Validate configured invariants and projected contract consistency.

`OpenApiPipelineOrchestrator` processes the same OpenAPI instance idempotently. This is projection-time
coordination, not a runtime request filter or a general schema cache. The resulting document remains
valid OpenAPI and can be consumed by tools that ignore the vendor extensions.

For a page wrapper, the protocol carries `x-api-wrapper`, `x-api-wrapper-type`,
`x-api-wrapper-datatype` and the container name/type/item trio. `x-ignore-model` retains an infrastructure
schema in OpenAPI while preventing duplicate Java model generation. The complete protocol and checks
are documented in [architecture](../docs/architecture/architecture.md#projection-protocol).

## Boundaries and verification

Java contracts remain authoritative. Built-in List/Set/Page and explicit application containers use
the same projection model. The starter does not generate clients, choose consumer runtimes, perform
business validation or infer arbitrary generic graphs. WebFlux is outside scope.

Check `/v3/api-docs` or `/v3/api-docs.yaml` for the expected wrapper metadata. A successful normal JSON
response alone does not prove OpenAPI projection or generated client correctness.
See [type coverage](../samples/type-coverage/README.md) for shape checks and the
[canonical web stack](../samples/gradle/customer-service/README.md) for real HTTP acceptance.
