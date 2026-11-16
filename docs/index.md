---
layout: default
title: Home
nav_order: 1
description: "Adopt and verify contract-preserving Java client generation."
permalink: /
---

# OpenAPI Generics documentation

Preserve Java envelope and container identity across
`ServiceResponse<Page<CustomerDto>> → OpenAPI metadata → generated wrapper extends shared contract`.
For the problem, concrete before/after and project overview, start with the [README](https://github.com/blueprint-platform/openapi-generics/blob/main/README.md).

Consumer examples target the public **1.3.0** release. This site is built from `main`, currently on
**1.3.1-SNAPSHOT**, and may include later documentation improvements. For the release snapshot use
[tagged 1.3.0 documentation](https://github.com/blueprint-platform/openapi-generics/tree/v1.3.0/docs).

## What are you trying to do?

| Task | Start here | Result |
| --- | --- | --- |
| Generate and compile your first client | [First working client](first-client.md) | One payload, public dependencies, Maven or Gradle |
| Publish generic contracts from a Spring MVC service | [Server adoption](adoption/server-side-adoption.md) | Springdoc output carries envelope, container and payload metadata |
| Keep an existing application-owned envelope | [Choose the envelope](adoption/server-side-adoption.md#choose-the-envelope) | BYOE (Bring Your Own Envelope) preserves ownership without duplicate client configuration |
| Generate a Maven client | [Maven setup](adoption/client-side-adoption.md#maven) | Prepared templates, reconstructed wrappers and registered Java sources |
| Generate a Gradle client | [Gradle setup](adoption/client-side-adoption.md#gradle) | Official-plugin integration and automatic selected-task source wiring |
| Reuse existing Java DTOs | [Optional BYOC (Bring Your Own Contract) mappings](adoption/client-side-adoption.md#optional-reuse-existing-dtos-with-byoc) | Existing DTOs satisfy selected schemas instead of duplicate generation |
| Use an application-defined container | [Container registration](adoption/server-side-adoption.md#register-an-application-container) | Explicit Java identity survives projection and reconstruction |
| Check tool versions or supported shapes | [Compatibility reference](compatibility.md) | Supported lines, current references and integration-specific edges |

## Follow a working application

- [Canonical Gradle Boot 4 stack](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/gradle/customer-service/README.md): consumer HTTP endpoint →
  application service → remote client → library adapter → generated RestClient → producer.
- [Maven Boot 4 client library](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/spring-boot-4/customer-service-client/README.md): generated code
  behind an adapter, consumed by a separate Boot application.
- [Sample catalog](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/README.md): Boot 3/4, BYOE, type/container, transport and multi-spec verification.

Generated wrappers are transport bindings. Keep them and generated API/invoker classes behind the
[client library boundary](adoption/client-side-adoption.md#keep-generated-types-behind-the-client-boundary).
BYOC is optional; envelope/container preservation does not require sharing every DTO.

## Verify or understand the implementation

| Question | Reference |
| --- | --- |
| What metadata should the producer emit? | [Projection metadata and checks](adoption/server-side-adoption.md#check-the-projected-document) |
| What should a generated client preserve? | [Client verification](adoption/client-side-adoption.md#verify-the-result) |
| Which tests prove wiring, compilation or runtime interoperability? | [Verification layers](compatibility.md#verification-evidence) |
| How are projection, reconstruction and build integrations separated? | [Architecture](architecture/architecture.md) |
| Why preserve Java contract ownership? | [Design rationale](architecture/rationale.md) |
| What changed in 1.3.0? | [Changelog](https://github.com/blueprint-platform/openapi-generics/blob/main/CHANGELOG.md) |

Release consumer examples use explicit platform versions and release repositories; the repository
samples also document their separate development bootstrap. See [consumer prerequisites](adoption/client-side-adoption.md#before-you-start).

[Source and issues](https://github.com/blueprint-platform/openapi-generics) ·
[Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)

## Documentation measurement

The repository README and this home page include Scarf page-view pixels. The docs site configures
Google Analytics (`G-4NH21XZLBN`). These are documentation-visit measurements; the Java libraries and
Gradle plugin do not gain runtime telemetry from these settings. Page views are not a count of unique
users or verified production adoption.

<!-- Scarf tracking pixel - Docs Home -->
<img referrerpolicy="no-referrer-when-downgrade"
src="https://static.scarf.sh/a.png?x-pxid=6d755e89-9777-4da0-9e21-018c95a2755d&page=docs-home"
width="1" height="1" alt=""
style="display:none;" />
