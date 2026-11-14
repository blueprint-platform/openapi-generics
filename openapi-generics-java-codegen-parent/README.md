# openapi-generics-java-codegen-parent

Use this Maven parent to generate and compile a contract-aware Java client without assembling the
platform template lifecycle yourself. You still configure the official OpenAPI Generator (OAG) execution and choose its
spec, library, packages and normal options. [Complete Maven adoption →](../docs/adoption/client-side-adoption.md#maven)

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.3.0</version>
  <relativePath/>
</parent>
```

In the official generator execution, select `java-generics-contract` and declare the shared-contract
and selected client-library dependencies. Run `mvn clean verify`.

## What the parent wires

| Stage | Responsibility |
| --- | --- |
| `generate-sources` | Extract selected upstream `Java/model.mustache`, patch wrapper dispatch, overlay platform templates and execute official generation |
| Source registration | Add the default generated Java root; official generator executions also contribute their selected roots |
| `process-sources` | Remove unused imports and normalize generated Java deterministically |

The default root is `target/generated-sources/openapi/src/gen/java`. For custom/multiple outputs,
review source registration **and** normalization includes: the default hygiene include does not
implicitly cover arbitrary output directories. The [multi-spec sample](../samples/generation-coverage/multi-spec-maven-client/README.md)
proves compilation/isolation, not normalization of every custom root.

## One selected upstream toolchain

`openapi-generator.version` aligns the official Maven plugin, `openapi-generator-core`,
`openapi-generator` and upstream template extraction. Override this property within supported policy,
not these pieces independently. The platform codegen version follows the platform release; the
internal bill of materials (BOM) does not pin consumer OAG or Boot runtimes. [Version roles →](../docs/compatibility.md)

BYOE (Bring Your Own Envelope) is producer envelope ownership; the client reads `x-api-wrapper-type` and supplies that shared
envelope dependency. Optional BYOC (Bring Your Own Contract) is a client mapping for existing DTOs. Neither changes ordinary
upstream API/transport ownership. Generated wrappers remain behind the client library adapter.

## Opt out for comparison

`openapi.generics.skip=true` skips platform extraction, patching, overlay and normalization.
It does not change `generatorName`; stock generation also requires choosing upstream `java`.
This property is Maven-specific, not a Gradle API.

## Scope

The parent orchestrates builds; it does not define Java contracts, inspect Spring controllers or
own application runtime behavior. Wrapper model docs are supplied by the shared codegen artifact;
RestClient-specific docs customization does not change ordinary model/enum ownership. Generating
upstream test sources does not by itself register their directory or supply JUnit dependencies.

For Gradle use the [official-plugin integration](../openapi-generics-gradle-plugin/README.md);
Maven parent settings are not Gradle configuration.
