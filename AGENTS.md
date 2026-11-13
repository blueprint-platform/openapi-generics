# Repository operating contract

Preserve Java contract ownership, projection/reconstruction boundaries, and deterministic integration behavior.

## Contract authority and module boundaries

- **Java contracts are authoritative.** `openapi-generics-contract` owns platform shared types such as `ServiceResponse<T>` and `Page<T>`; application-owned BYOC/BYOE Java contracts retain authority in their own domains.
- OpenAPI is their projection and the protocol boundary between server projection and client reconstruction. Generated wrappers bind concrete generic parameters for transport/deserialization; they are not new contract authorities. Externally/shared-owned models are reused where the architecture requires it, not regenerated.
- The platform contract module stays pure Java, without Spring, Springdoc, or runtime framework dependencies.

| Module | Ownership boundary |
|---|---|
| `openapi-generics-server-starter` | Spring MVC projection; keep `OpenApiPipelineOrchestrator` idempotent |
| `openapi-generics-java-codegen` | Shared `java-generics-contract` reconstruction semantics and template overlay |
| `openapi-generics-java-codegen-parent` | Maven generation lifecycle and selected-toolchain coherence |
| `openapi-generics-gradle-plugin` | Official Gradle plugin integration; independent of the Maven reactor |
| `openapi-generics-platform-bom` | Internal platform dependency alignment, not consumer framework/tool version selection |

`java-generics-contract` specializes upstream `JavaClientCodegen`; it is not a fork. Maven and Gradle must not redefine its contract semantics. Ordinary OpenAPI Generator model/API/authentication/transport behavior remains upstream-owned; interoperability tests do not transfer that ownership to this platform.

## Compatibility and version ownership

Consumers choose concrete framework/tool versions within supported policy. The platform owns its integration coherence, not that choice.

| Role | Meaning |
|---|---|
| Supported line | Declared framework/tool support policy |
| Minimum compatibility edge | Integration-specific lower API/capability or tested runtime edge |
| Current verified/reference | Single current verification/default input for a surface; neither consumer requirement nor support minimum |
| Negative boundary | Explicit unsupported capability regression case for an integration, not automatically a platform-wide exclusion |
| Temporary candidate | Verification stage before reference promotion, not permanent repository state |

- Verify a new upstream release against the existing implementation as a candidate. Pass replaces the current reference; failure requires investigating the concrete break.
- Do not accumulate previous currents in the permanent matrix. Removal from the matrix does not make a version unsupported. Change minimum/negative boundaries only when the actual compatibility boundary changes.
- Read exact role values from build configuration and executable tests. Independently maintained surfaces may have different references. Sample pins, starter compile/test baselines, internal BOM values and wrapper versions are not universal consumer constraints; representative verification is not exhaustive support-line testing.
- General OpenAPI Generator support is **7.x**; Gradle support is **9.x**. The Gradle integration's OAG minimum/negative boundary must not narrow platform-wide 7.x support.

| Spring Boot supported line | springdoc-openapi supported lines |
|---|---|
| 3.4.x | 2.8.x |
| 3.5.x | 2.8.x / 2.9.x |
| 4.x | 3.x |

### Maven coherence

The consumer-selected `openapi-generator.version` stays aligned across the official Maven plugin, `openapi-generator-core`, `openapi-generator`, and upstream `Java/model.mustache` extraction. The platform BOM does not manage these versions or pin consumer runtimes.

`openapi.generics.skip=true` skips Maven extraction/patching/overlay/normalization. It does not change `generatorName`; stock generation also requires choosing upstream `java`. It is not a Gradle opt-out API.

### Gradle ownership and lifecycle

- Integrate through `withPlugin("org.openapi.generator", ...)`; never implicitly apply, replace, or override the official plugin/version. Generator name, spec, library, packages and ordinary options remain consumer-owned.
- Discover upstream templates from the running official plugin classloader, not a second independently resolved upstream runtime in consumer repositories. Missing/ambiguous template ownership and required template patch-anchor drift fail explicitly.
- Provision only the platform-version-aligned `openapi-generics-java-codegen` artifact; conflicting direct codegen declarations fail.
- Production official API dependencies remain `compileOnly` at the minimum OAG API baseline. Functional/E2E runtime selection is independent (`-PopenApiGeneratorTestRuntimeVersion=...`). This is distinct from the minimum tested Gradle distribution edge; Gradle API compilation follows the build distribution, and the wrapper is a reference, not a support minimum.
- `openApiGeneratorExtra` is a resolvable, non-consumable capability boundary, not a platform-wide numeric version gate.
- Apply templates, classpath wiring, preparation dependencies, source registration and normalization only to `GenerateTask` instances selecting `java-generics-contract`, including custom/multiple tasks. Custom tasks do not inherit the default task's worker classpath. Preserve non-selected tasks, especially consumer `templateDir` settings; an absent provider assigned with `set` can erase them.
- With the Java plugin, register roots and compilation dependencies from each selected task's output and effective `sourceFolder`, including upstream `configFile` binding/option precedence. Generation, registration and normalization must use the same root; consumers must not need manual source-set or compile dependency workarounds.
- Keep providers lazy/configuration-cache safe. Install project-level Java/source-set wiring outside `GenerateTask.configureEach` mutation scope. Track config-file contents; avoid output-provider dependencies that create task cycles.
- Capture normalization state during configuration; do not access Project/task extensions during execution. Normalize as the generation task's final action, before output snapshots. Consumers do not need the Spotless Gradle plugin.

## Projection protocol and supported shapes

BYOE identifies an application-owned envelope on the **producer** (`openapi-generics.envelope.type`). The producer emits `x-api-wrapper-type`; the client derives envelope imports/types from it, without redundant client envelope configuration. BYOC separately maps model names to external Java contracts through generator `additionalProperties` (`openapi-generics.response-contract.<Model>=<FQCN>`); it does not establish envelope identity.

| Canonical server → client extension | Meaning |
|---|---|
| `x-api-wrapper` | Boolean wrapper marker |
| `x-api-wrapper-type` | Fully qualified Java envelope identity |
| `x-api-wrapper-datatype` | Payload type |
| `x-data-container` | Semantic/simple container name |
| `x-data-container-type` | Fully qualified container identity |
| `x-data-item` | Item type |
| `x-ignore-model` | Retain schema, suppress duplicate model generation |

`x-envelope-import`, `x-envelope-type`, and `x-extra-imports` are derived codegen-internal metadata, not producer requirements. `x-payload-property` is not an active canonical extension. Validate wrapper metadata before reconstruction; container name/type/item metadata travel together.

- Container semantics are metadata-driven, not Page-specific branching. Built-ins are `List<T>`, `Set<T>`, and `Page<T>`. Explicit application containers require a concrete class/record with one type parameter and a declared `List<T>`/`Set<T>` item property using it directly; their shared Java type must be on the client classpath.
- BYOE requires a concrete class with one generic parameter and one direct payload field of that parameter's type. Record envelopes, multiple generic parameters and nested generic payload slots are unsupported; supply containers as the envelope's type argument at usage sites.
- Runtime projection supports Spring MVC synchronous and supported async wrappers. WebFlux, arbitrary generic graph reconstruction, non-Java reconstruction and runtime client-side reflection are out of scope.

## Failure semantics and evidence

- Fail explicitly for invalid platform-owned invariants (envelope/container/metadata inconsistency, reconstruction errors, required template drift), using existing module exceptions or `IllegalStateException` as appropriate. Do not add hidden fallback behavior or runtime type guessing.
- Preserve intentional `Optional.empty`, warn-and-skip and no-op outcomes. A general fail-fast preference is not a reason to harden/rewrite them without a concrete requirement or defect.
- Implementation, build configuration and executable test results are primary evidence. Check docs/CHANGELOG for consistency; stale documentation must not override verified implementation. Test existence alone does not prove execution or coverage.
- Verify at the affected layer. Unit tests establish deterministic component behavior; fixture-based TestKit tests establish integration wiring, not real codegen. Real-codegen E2E establishes generation/compilation/cache behavior; it does not imply runtime HTTP interoperability. Use application runtime tests for that claim.
- Generated/build outputs are not source authority and are not committed by default. Committed sample OpenAPI snapshots are deliberate validation artifacts: review intentional updates and make drift visible in verification/CI.

## Verification commands

Published platform artifacts target Java 17+; Boot 3 samples use Java 21 and Boot 4 samples Java 25. Use the appropriate JDK; do not infer a tested runtime matrix from the compile target.

From repository root:

```bash
mvn clean install
mvn -pl openapi-generics-server-starter test
mvn -f samples/pom.xml clean verify  # JDK 25
mvn spotless:apply
```

Maven release publication uses `mvn clean deploy -P central-publish`; this is publication, not routine verification.

From `openapi-generics-gradle-plugin/`:

```bash
./gradlew test
./gradlew functionalTest
./gradlew check
./gradlew e2eTest
```

`check` includes unit + functional TestKit tests. `e2eTest` is explicit and builds real Maven codegen into an isolated repository. The root Maven reactor does not run Gradle plugin/sample verification.

Canonical Gradle adoption lives at `samples/gradle/customer-service/` (JDK 25). Follow its README for platform snapshot bootstrap; shared CustomerDto is a Gradle project dependency. Run twice there:

```bash
./gradlew clean build --no-build-cache --configuration-cache
```

Confirm configuration-cache reuse and actual generation/compile/test execution. The client reads the committed spec without a live producer; acceptance tests start a real producer for direct/page HTTP deserialization. Keep this adoption stack separate from canonical type/transport/multi-spec/mixed-generator coverage.
