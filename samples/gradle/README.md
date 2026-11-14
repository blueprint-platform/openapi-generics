# Gradle adoption sample

[`customer-service`](customer-service/README.md) is the canonical Gradle-first Spring Boot 4 stack:

```text
producer → committed OpenAPI spec → generated RestClient → consumer
```

The four-module build contains producer and consumer Boot web applications plus a reusable client
library. Consumer HTTP controller → application service → remote client → library adapter → generated
RestClient → producer is verified over real HTTP for direct/paged responses and upstream errors.
Shared DTO reuse through BYOC (Bring Your Own Contract), automatic generated-source registration/normalization and configuration-cache
reuse remain part of the acceptance path.
The former standalone `customer-service-client` example is superseded by this stack's `client` module;
its custom GenerateTask and BYOC adoption path are preserved without a second independent example.

BYOE (Bring Your Own Envelope), custom containers, type/transport matrices, and multiple/mixed generator scenarios remain in
their existing dedicated samples and plugin regression tests.
