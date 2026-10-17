import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.openapi.generator") version "7.26.0"
    id("io.github.blueprint-platform.openapi-generics")
}

group = "io.github.blueprint-platform.samples"
version = "1.3.0-SNAPSHOT"

val platformVersion = "1.3.0-SNAPSHOT"
val springBootVersion = "3.5.16"
val generatedPackage = "io.github.blueprintplatform.samples.gradle.customer.generated"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    implementation("io.github.blueprint-platform:openapi-generics-contract:$platformVersion")
    implementation("io.github.blueprint-platform.samples:customer-contract:$platformVersion")

    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("org.apache.httpcomponents.client5:httpclient5")
    compileOnly("org.slf4j:slf4j-api")
}

tasks.register<GenerateTask>("generateCustomerClient") {
    generatorName.set("java-generics-contract")
    inputSpec.set(file("../../spring-boot-3/spec/customer-api-docs.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/customer"))

    library.set("restclient")

    apiPackage.set("$generatedPackage.api")
    modelPackage.set("$generatedPackage.dto")
    invokerPackage.set("$generatedPackage.invoker")

    configOptions.set(
        mapOf(
            "sourceFolder" to "src/gen/java",
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
            "openApiNullable" to "false"
        )
    )

    // BYOC: bind the generated CustomerDto schema to the shared domain contract.
    additionalProperties.set(
        mapOf(
            "openapi-generics.response-contract.CustomerDto" to
                    "io.github.blueprintplatform.contracts.customer.CustomerDto"
        )
    )

    cleanupOutput.set(true)
    skipValidateSpec.set(false)

    generateApiDocumentation.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateModelTests.set(false)
}
