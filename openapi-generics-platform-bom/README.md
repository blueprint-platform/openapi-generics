# openapi-generics-platform-bom

An **internal alignment bill of materials (BOM)**, not a consumer framework/tool version manager.
Applications normally start with the [server starter](../openapi-generics-server-starter/README.md),
[Maven parent](../openapi-generics-java-codegen-parent/README.md) or
[Gradle plugin](../openapi-generics-gradle-plugin/README.md) instead of importing this BOM directly.

## What it manages

| Dependency | Purpose |
| --- | --- |
| `openapi-generics-contract` | Align platform-owned shared Java types |
| `springdoc-openapi-starter-common` | Internal projection infrastructure baseline |

Platform modules import the same-release BOM. The build POM is the authority for exact values;
[compatibility](../docs/compatibility.md) distinguishes starter compile/test baselines from consumer runtime choices.

## What stays consumer-owned

- Boot, servlet/Jakarta, serialization and application dependency management.
- The producer's Springdoc UI/runtime selection within supported policy.
- Official OpenAPI Generator (OAG)/plugin versions and ordinary generator options.

Maven coherence lives in the codegen parent through `openapi-generator.version`; Gradle integrates
with the running consumer-selected official plugin. Adding tool/runtime pins here would cross those
ownership boundaries. The BOM neither defines contracts nor changes projection/reconstruction semantics.

MIT — [LICENSE](../LICENSE).
