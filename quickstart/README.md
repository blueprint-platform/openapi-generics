# Quickstart: your first working client

Follow the [tutorial](../docs/first-client.md). Choose `maven/` or `gradle/`; both read `spec.yaml`
and compile the same small API-use probe. Use JDK 21 and internet access for the first build.
The included wrappers download Maven 3.9.9 or Gradle 9.9.1 and verify the distribution checksum.
These are the example's reproducible build-tool versions.

From `quickstart/`, choose one:

```bash
(cd maven && ./mvnw clean verify)
(cd gradle && ./gradlew clean build)
```

On Windows, use `.\mvnw.cmd clean verify` inside `maven/` or `.\gradlew.bat clean build` inside `gradle/`.

No root reactor install, live producer, composite build or local repository is required. All platform
inputs are released 1.3.0 artifacts. The Boot 4.1.2 / Jackson 3 RestClient dependency profile is one
consumer choice. This example proves generation and compilation, not an HTTP round trip.

For application architecture, real HTTP verification and coverage harnesses, use the
[sample catalog](../samples/README.md). Those repository development builds have their own bootstrap.
