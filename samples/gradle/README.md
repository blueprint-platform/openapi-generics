# Gradle adoption sample

[`customer-service`](customer-service/README.md) is the canonical Gradle-first Spring Boot 4 stack:

```text
producer → committed OpenAPI spec → generated RestClient → consumer
```

The four-module build demonstrates shared Java contracts, direct and paged responses, real HTTP
deserialization, automatic generated-source registration/normalization, and configuration-cache reuse.
The former standalone `customer-service-client` example is superseded by this stack's `client` module;
its custom GenerateTask and BYOC adoption path are preserved without a second independent example.

BYOE, custom containers, type/transport matrices, and multiple/mixed generator scenarios remain in
their existing dedicated samples and plugin regression tests.
