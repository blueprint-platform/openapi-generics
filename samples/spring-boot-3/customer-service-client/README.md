# Boot 3 customer client library

A reusable RestClient library around the committed producer spec. Consumers call `CustomerClientAdapter`;
generated `CustomerControllerApi`, invoker setup and wrapper classes stay inside the library.

```text
Consumer service/client → CustomerClientAdapter
  → CustomerClientAdapterImpl / CustomerApiClientConfig
  → generated RestClient → producer
```

## What this sample actually does

- The Maven parent prepares `java-generics-contract` templates and generated-source compilation.
- Generation reads `../spec/customer-api-docs.yaml`; output is under
  `target/generated-sources/openapi/src/gen/java`.
- `CustomerDtoMapper` maps ordinary generated DTOs into the application-owned
  `io.github.blueprintplatform.contracts.customer.CustomerDto`. This sample's adapter therefore does
  perform payload mapping; the generated DTO is not declared the application contract.
- Request DTOs for CRUD operations are generated and mapped at the consumer's request boundary;
  API/invoker types and transport bindings are not consumer service dependencies.
- `CustomerApiClientConfig` owns HTTP configuration and ProblemDetail status handling.
  `ApiProblemException` is the stable error boundary, including empty/non-JSON/unparsable fallbacks.

The sample uses Boot 3 / Jackson 2 options. The POM is the source of exact library/options/dependency values.

## Build and use

Follow the [repository sample setup](../../README.md#repository-build-and-local-runtime-setup), then from
this module run `mvn clean verify`. Surefire and configured Failsafe client tests exercise JSON/error
responses against MockWebServer; this is not a claim that every sample transport runs against a live producer.

Use the stable adapter from the [consumer application](../customer-service-consumer/README.md). Its
read methods return shared envelope/page and domain DTO types; do not expose generated wrapper subclasses
as application contracts.

## Envelope ownership and optional DTO reuse

This producer uses `ServiceResponse`. If a producer selects BYOE (Bring Your Own Envelope), codegen reads its `x-api-wrapper-type`;
**do not configure `openapi-generics.envelope` on the client**. Supply the selected envelope dependency.

BYOC (Bring Your Own Contract) is optional: an existing DTO can instead be bound using its fully qualified Java class name (FQCN):
`openapi-generics.response-contract.CustomerDto=<FQCN>` plus its shared dependency. That is a different
reuse choice from the explicit mapper used here. See [client adoption](../../../docs/adoption/client-side-adoption.md#optional-reuse-existing-dtos-with-byoc)
and the [Gradle BYOC sample](../../gradle/customer-service/README.md).
