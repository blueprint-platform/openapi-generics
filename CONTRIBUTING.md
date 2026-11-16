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

## Choose the smallest useful check

| Change | Start with |
| --- | --- |
| README, docs, sample README | Jekyll and internal links below; the Documentation workflow also compiles the public first-client example |
| Maven contract/server/codegen | Tests and `spotless:check` in the affected module; root install before sample verification |
| Gradle runtime integration | Plugin `./gradlew check`; `e2eTest` for real generation/compile changes |
| Gradle release build tooling | Plugin `./gradlew :buildSrc:test check`; no signing credentials or upload needed |

### Documentation

Use Ruby 3.3 and Bundler. From the repository root:

```bash
BUNDLE_GEMFILE=docs/Gemfile bundle install
BUNDLE_GEMFILE=docs/Gemfile bundle exec jekyll build --source docs --destination _site
BUNDLE_GEMFILE=docs/Gemfile bundle exec ruby scripts/test-docs-checks.rb
BUNDLE_GEMFILE=docs/Gemfile bundle exec ruby scripts/check-docs.rb --site _site
```

The link checker reads tracked Markdown, so stage new documentation files before running it. It checks
local files/anchors and the rendered site; external URL availability is deliberately outside the gate.
The Pages workflow still deploys only from main. The public-client fixture uses released dependencies,
not a reactor install: [quickstart instructions](quickstart/README.md).

### Java formatting

For handwritten core Java, select the affected Maven module(s); for example:

```bash
mvn -pl openapi-generics-contract,openapi-generics-server-starter,openapi-generics-java-codegen spotless:check
# Apply the configured fix, then review the diff:
mvn -pl openapi-generics-contract,openapi-generics-server-starter,openapi-generics-java-codegen spotless:apply
```

The samples aggregator owns handwritten sample formatting independently of their parent chains:

```bash
mvn -N -f samples/pom.xml spotless:check
mvn -N -f samples/pom.xml spotless:apply
```

Use JDK 21+ for the configured formatter; the complete samples build uses JDK 25. Samples currently
apply that formatting during `process-sources`, so inspect source diffs after builds. The independent
Gradle plugin has **no handwritten Spotless Gradle task**: preserve its existing style and run its
checks. Embedded generated-source normalization is a separate consumer lifecycle, not a contributor
formatter command. Do not hand-edit generated Java to fix source formatting.

## Prepare a review

Lead with the concrete change and outcome, then the evidence. Explain any contract/protocol or support
boundary impact. Current references are not consumer minima; an upstream promotion does not silently
change capability/negative edges. Keep template ownership narrow and verify ordinary upstream paths
when adding wrapper-specific behavior.

[Architecture](docs/architecture/architecture.md) explains invariants and lifecycle separation;
[sample catalog](samples/README.md) distinguishes application examples from coverage harnesses.
By contributing, you agree that contributions use the project's [MIT license](LICENSE).
