plugins {
    java
    id("org.openapi.generator") version "7.27.0"
    id("io.github.blueprint-platform.openapi-generics") version "1.3.0"
}
repositories { mavenCentral() }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    implementation("io.github.blueprint-platform:openapi-generics-contract:1.3.0")
    implementation("org.springframework:spring-web")
    implementation("tools.jackson.core:jackson-databind")
    implementation("jakarta.annotation:jakarta.annotation-api")
}
openApiGenerate {
    generatorName.set("java-generics-contract")
    inputSpec.set(file("../spec.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/openapi"))
    library.set("restclient")
    apiPackage.set("example.generated.api")
    modelPackage.set("example.generated.model")
    invokerPackage.set("example.generated.invoker")
    configOptions.set(mapOf(
        "sourceFolder" to "src/gen/java",
        "useSpringBoot4" to "true",
        "useJackson3" to "true",
        "serializationLibrary" to "jackson",
        "openApiNullable" to "false"
    ))
}
