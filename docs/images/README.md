---
layout: default
title: Visual assets
nav_exclude: true
search_exclude: true
---

# Visual assets

| Purpose / reference | Editable source | Final render |
| --- | --- | --- |
| [README proof](https://github.com/blueprint-platform/openapi-generics/blob/main/README.md#see-the-proof): stock generated model versus shared generic binding | [proof/generic-binding-comparison.svg](proof/generic-binding-comparison.svg) | Same SVG, displayed directly |
| [Architecture](../architecture/architecture.md#architecture-overview): Java authority, projection/reconstruction and application boundaries | [architecture/openapi-generics-architecture.svg](architecture/openapi-generics-architecture.svg) | Same SVG, displayed directly |
| GitHub repository Social preview: product value at link-card size | [social/social-preview.svg](social/social-preview.svg) | **[social/social-preview.png](social/social-preview.png)**, 1280×640 |

The maintainer **manually uploads the social PNG to GitHub repository settings**. Git does not update
that setting. Keep the SVG and PNG together even though public Markdown does not embed the card.
After editing the social SVG, export it in a browser at its declared size and check full/half-size readability.

## Version lifecycle

Visuals contain no exact upstream versions. Social introduces the value; architecture explains
ownership; proof shows concrete output. Supported/current/minimum/negative versions belong in the
[compatibility reference](../compatibility.md), not the artwork.

After an upstream reference promotion, rerun verification and update the record below if the output
is unchanged. Do not edit SVGs or re-export PNGs merely for a patch release. Change artwork only when
its message, architecture or verified code excerpt changes. All SVGs are directly editable sources;
there is no separate HTML/draw.io source to synchronize.

## Proof verification

Last verified: **OpenAPI Generator 7.27.0**, **RestClient**, **Boot 4/Jackson 3**, no external DTO mappings.
Input: [canonical customer snapshot](https://github.com/blueprint-platform/openapi-generics/blob/main/samples/gradle/customer-service/spec/customer-api-docs.json).
Options: `useSpringBoot4=true`, `useJackson3=true`, `serializationLibrary=jackson`, `openApiNullable=false`.

Use the [standalone Gradle consumer example](https://github.com/blueprint-platform/openapi-generics/blob/main/openapi-generics-gradle-plugin/README.md#standalone-consumer-example)
with that input. Generate and compile once with upstream `java` and once with `java-generics-contract`,
keeping the same options and DTO reuse disabled. Stock `java` needs upstream source/task wiring;
the platform registers sources only for its selected generator. Compare `ServiceResponsePageCustomerDto.java`:
stock output owns `PageCustomerDto data` / `Meta meta`; the platform binding extends
`ServiceResponse<Page<CustomerDto>>`. `CustomerDto` remains ordinarily generated in both outputs.
The visual omits imports, annotations, accessors and other boilerplate; it is not a complete source listing.
