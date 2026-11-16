# First working client — public 1.3.0 dependencies

Follow the [tutorial](../../docs/first-client.md). Choose `maven/` or `gradle/`; both read `spec.yaml`
and compile the same small API-use probe. JDK 21, Maven 3.9.x or Gradle 9.x is required.

```bash
mvn -f maven/pom.xml clean verify
# Or, with Gradle 9.x installed:
gradle -p gradle clean build
```

No root reactor install, live producer, composite build or local repository is required. All platform
inputs are released 1.3.0 artifacts. The Boot 4.1.2 / Jackson 3 RestClient dependency profile is one
consumer choice. This example proves generation and compilation, not an HTTP round trip.
