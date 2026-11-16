---
layout: default
title: Troubleshooting
parent: Adoption Guides
nav_order: 3
---

# Troubleshoot a generation build

Keep the failing command, platform/official generator versions, selected library/options and the
smallest relevant spec/build fragment. Remove credentials before sharing logs. Report an issue with
that reproduction when the checks below do not explain the failure.

| Symptom | Check | Corrective action |
| --- | --- | --- |
| Gradle cannot find the platform plugin | Plugin ID/version and `pluginManagement.repositories` | Resolve plugin IDs through `gradlePluginPortal()`; project `mavenCentral()` alone does not configure plugin discovery. Apply the official OAG plugin explicitly too. |
| Codegen/contract cannot resolve | Project repositories and matching platform versions | Use Maven Central for the released platform dependencies. Do not copy repository-only `includeBuild` or `mavenLocal()` bootstrap into a public consumer. |
| `OpenApiGenericsCodegenVersionConflictException` | A direct codegen dependency differs from the plugin manifest version | Remove the independent declaration; the plugin provisions its matching codegen. Align platform components. |
| `OpenApiGeneratorCompatibilityException` | The running official plugin lacks the required `openApiGeneratorExtra` capability | Check the [integration edge](../compatibility.md#current-references-and-integration-edges); it is not a platform-wide OAG exclusion. |
| Wrapper metadata is rejected | Wrapper/envelope identity and the container name/type/item triple in the actual input spec | Correct the producer projection or spec; follow [metadata checks](server-side-adoption.md#check-the-projected-document). Do not infer identity from generated names. |
| `TemplatePreparationException` or required patch-anchor drift | Running official generator version and full diagnostic | Keep the supported version selection coherent and report the minimal reproduction. Do not bypass a required patch guard or substitute another upstream runtime. |
| Shared Java envelope/container/DTO type cannot compile | Compile classpath and optional BYOC mapping | Add the actual shared contract dependency. A mapping supplies identity, not the Java classes. |
| Spring/Jackson classes cannot compile | Selected generator library and Boot/Jackson option/dependency profile | Use one coherent [client dependency set](client-side-adoption.md#add-the-client-dependencies); Boot/Jackson choices are consumer-owned. |
| Generated files exist but are not compiled | Maven parent inheritance or Gradle selected task and effective source folder | Use the [Maven lifecycle](client-side-adoption.md#maven), or Java plugin plus `java-generics-contract` tasks in Gradle. Check JSON/YAML `configFile` and option precedence before adding manual roots. |

[Quickstart](../first-client.md) · [Client adoption](client-side-adoption.md) ·
[Server adoption](server-side-adoption.md)
