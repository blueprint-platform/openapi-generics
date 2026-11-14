# Contributing

Start with a reproducible issue or focused change. Use [Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
for design/adoption questions and [Issues](https://github.com/blueprint-platform/openapi-generics/issues)
for bugs. Follow the [Code of Conduct](CODE_OF_CONDUCT.md); report vulnerabilities privately through
[SECURITY.md](SECURITY.md).

## Choose the owning layer

| Change | Owner |
| --- | --- |
| Built-in Java contract semantics | `openapi-generics-contract` |
| MVC discovery, projection, protocol validation | `openapi-generics-server-starter` |
| Java reconstruction, optional DTO reuse and wrapper overlay | `openapi-generics-java-codegen` |
| Selected Maven toolchain/template/source lifecycle | `openapi-generics-java-codegen-parent` |
| Official Gradle plugin integration / lazy selected-task wiring | Independent `openapi-generics-gradle-plugin` build |
| Internal dependency alignment | `openapi-generics-platform-bom` |

Java contracts are authoritative, OpenAPI is their projection/protocol, and generated wrappers are
transport bindings. Fix the owning source rather than generated output. BYOE (Bring Your Own Envelope) keeps envelope ownership
with the application; BYOC (Bring Your Own Contract) is optional client DTO reuse. Ordinary upstream API/model/transport behavior
is not platform-owned merely because a sample exercises it.

## Development setup

Use JDK 21 for core/plugin verification and JDK 25 for the complete sample suite. Platform bytecode
targets Java 17+. Use a Maven version compatible with the selected official generator plugin (local verification uses
3.9.9), and repository Gradle wrappers; exact compatibility roles are in
[compatibility](docs/compatibility.md).

Create a focused branch, update tests/documentation at the affected layer, and avoid unrelated refactors.
Use the repository's Java formatting standard. Existing commit categories include `feature:`,
`bugfix:`, `docs:`, `refactor:`, `test:` and `ci:`; prioritize a concrete, reviewable description.

## Testing

Run affected checks; the complete repository path is:

```bash
# JDK 21, repository root
mvn clean install
(cd openapi-generics-gradle-plugin && ./gradlew check && ./gradlew e2eTest)
# Switch to JDK 25
mvn -f samples/pom.xml clean verify
(cd samples/gradle/customer-service && ./gradlew clean build --no-build-cache --configuration-cache)
(cd samples/gradle/customer-service && ./gradlew clean build --no-build-cache --configuration-cache)
```

The second stack build must reuse configuration cache with generation/compilation/tests executing
after clean. Complete Maven installs before measuring that replay; replacing a local codegen artifact
correctly invalidates its cache inputs. The root Maven reactor does not run the independent Gradle builds.

Unit tests, fixture-based TestKit, real-codegen compilation and application HTTP tests establish
different things. Report the commands actually executed, not just the existence of test files.
Committed OpenAPI snapshots are reviewed artifacts: inspect diffs and retain CI drift detection.
Generated Java, target/build output and local investigation plans are not source authority.

## Prepare a review

Lead with the concrete change and outcome, then the evidence. Explain any contract/protocol or support
boundary impact. Current references are not consumer minima; an upstream promotion does not silently
change capability/negative edges. Keep template ownership narrow and verify ordinary upstream paths
when adding wrapper-specific behavior.

[Architecture](docs/architecture/architecture.md) explains invariants and lifecycle separation;
[sample catalog](samples/README.md) distinguishes application examples from coverage harnesses.
By contributing, you agree that contributions use the project's [MIT license](LICENSE).
