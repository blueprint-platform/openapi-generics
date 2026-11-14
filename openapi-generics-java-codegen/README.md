# openapi-generics-java-codegen

The shared **build-time** `java-generics-contract` generator reconstructs Java generic bindings from
OpenAPI metadata. Use it through the [Maven parent](../openapi-generics-java-codegen-parent/README.md)
or [Gradle plugin](../openapi-generics-gradle-plugin/README.md), not as an application runtime dependency.

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated subclass is a transport binding. Shared Java envelopes/containers own their fields and
behavior; a client adapter should expose those shared contracts to application code.

## What this specialization changes

`GenericAwareJavaCodegen` extends upstream `JavaClientCodegen`. It validates wrapper metadata, derives
envelope imports/types, reconstructs container bindings, applies optional BYOC (Bring Your Own Contract) payload imports and
filters suppressed/reused models. Ordinary DTO/API/authentication/transport generation remains
upstream-owned; this generator does not produce only wrappers or eliminate all generated DTOs.

| Input | Effect |
| --- | --- |
| `x-api-wrapper` + `x-api-wrapper-type` + datatype | Validate wrapper identity and bind the shared Java envelope |
| Container name/type/item trio | Bind List/Set/Page or a registered application container using the same template path |
| `x-ignore-model` | Keep the OpenAPI schema, suppress duplicate Java model generation and clean ignored model imports |
| `openapi-generics.response-contract.<Model>=<FQCN>` | Optional client-side BYOC mapping; reuse a Java DTO already supplied on the classpath |

`FQCN` means the fully qualified Java class name of the existing contract.

BYOE (Bring Your Own Envelope) envelope identity comes from the **producer** in `x-api-wrapper-type`; there is no second client
envelope declaration. BYOC is separate and optional. Unmapped ordinary DTOs continue through upstream
generation. Derived `x-envelope-import`, `x-envelope-type` and `x-extra-imports` are internal codegen
metadata, not new producer requirements.

The generator consumes OpenAPI; it does not inspect controllers, run Springdoc or guess Java types
at runtime. [Client adoption](../docs/adoption/client-side-adoption.md) gives complete configuration;
[server adoption](../docs/adoption/server-side-adoption.md) defines the producer protocol.

## Template and build ownership

The artifact supplies the small wrapper overlay used by both integrations. Maven extracts upstream
`Java/model.mustache` from the selected OpenAPI Generator (OAG) version and patches its wrapper dispatch; Gradle discovers
that upstream template through the running official plugin classloader. Both overlay this artifact's
resources. They do not redefine its reconstruction semantics.

Invalid wrapper invariants and required template drift fail explicitly. Existing warn-and-skip,
`Optional.empty` and no-op cases are not blanket fail-fast guarantees. The internal bill of materials (BOM) does not select
the consumer's upstream OAG runtime. See [compatibility](../docs/compatibility.md) for supported/version boundaries.

## Generated model documentation

With `generateModelDocumentation=true`, the **RestClient-scoped** wrapper doc overlay shows the actual
Java generic binding and shared envelope/container/BYOC identities without links to suppressed local
model `.md` files. It reuses the existing Java wrapper partial; it does not invent a second binding algorithm.

Ordinary RestClient model/enum docs still use upstream partials, and other libraries retain upstream
documentation behavior. This is not a general model-doc fork or a promise that upstream API-documentation
Java examples are copy/paste-ready. Generated test behavior is unchanged: tests are scaffolding, and
consumer test dependencies/source-root registration remain separate from generating files.

## Verify this module

From the repository root:

```bash
mvn -pl openapi-generics-java-codegen -am test
```

Component tests cover metadata/reuse/filtering; real-generator doc regressions cover direct/Page/BYOC/BYOE
bindings, ordinary model/enum preservation and non-RestClient isolation. Maven samples and Gradle E2E
provide separate generation/compilation evidence; application tests establish HTTP interoperability.
