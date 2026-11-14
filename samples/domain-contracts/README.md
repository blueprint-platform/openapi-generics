# Application-owned customer contracts

`customer-contract` contains the framework-independent `CustomerDto` shared by the Maven customer
producer and its client library's stable adapter API. It is a sample application contract, not the
platform's envelope/container authority.

The Maven client currently generates an ordinary transport DTO and maps it with `CustomerDtoMapper`
into this shared type. Adding the dependency alone does **not** tell codegen to reuse a DTO. Optional
BYOC (Bring Your Own Contract) additionally requires an explicit schema-name → Java-type mapping; the multi-spec Maven and
canonical Gradle examples demonstrate that path.

```xml
<dependency>
  <groupId>io.github.blueprint-platform.samples</groupId>
  <artifactId>customer-contract</artifactId>
  <version>1.3.0</version>
</dependency>
```

Build/install it as part of the [Maven samples](../README.md#repository-build-and-local-runtime-setup).
Do not copy these sample coordinates as a public platform dependency; supply your own contract module.

[Optional BYOC configuration](../../docs/adoption/client-side-adoption.md#optional-reuse-existing-dtos-with-byoc) ·
[Client library example](../spring-boot-4/customer-service-client/README.md)
