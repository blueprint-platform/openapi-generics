# Quickstart: your first working client

Follow the [tutorial](../docs/first-client.md). Choose `maven/` or `gradle/`; both read `spec.yaml`
and compile the same small API-use probe. Use JDK 21 and either Maven (verified with 3.9.9) or
Gradle 9.x (verified with 9.9.1); the verified Maven version is not a declared support minimum.

```bash
mvn -f maven/pom.xml clean verify
# Or, with Gradle 9.x installed:
gradle -p gradle clean build
```

No root reactor install, live producer, composite build or local repository is required. All platform
inputs are released 1.3.0 artifacts. The Boot 4.1.2 / Jackson 3 RestClient dependency profile is one
consumer choice. This example proves generation and compilation, not an HTTP round trip.

For application architecture, real HTTP verification and coverage harnesses, use the
[sample catalog](../samples/README.md). Those repository development builds have their own bootstrap.
