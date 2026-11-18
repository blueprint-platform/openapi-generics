import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `java-library`
    id("org.openapi.generator")
    id("io.github.blueprint-platform.openapi-generics")
}
val platformVersion = providers.gradleProperty("platformVersion").get()
val generatedPackage = "io.github.blueprintplatform.samples.gradle.customer.generated"
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    api(project(":contract"))
    api("io.github.blueprint-platform:openapi-generics-contract:$platformVersion")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("jakarta.annotation:jakarta.annotation-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<GenerateTask>("generateCustomerClient") {
    group = "code generation"
    description = "Generates the Java client from the customer service OpenAPI document."
    generatorName.set("java-generics-contract")
    inputSpec.set(file("../spec/customer-api-docs.json").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/customer"))

    library.set("restclient")

    apiPackage.set("$generatedPackage.api")
    modelPackage.set("$generatedPackage.dto")
    invokerPackage.set("$generatedPackage.invoker")

    configOptions.set(
        mapOf(
            "sourceFolder" to "src/gen/java",
            "useSpringBoot4" to "true",
            "serializationLibrary" to "jackson",
            "openApiNullable" to "false",
            "useJackson3" to "true"
        )
    )

    // BYOC: bind the generated CustomerDto schema to the shared domain contract.
    additionalProperties.set(
        mapOf(
            "openapi-generics.response-contract.CustomerDto" to
                    "io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto"
        )
    )

    cleanupOutput.set(true)
    skipValidateSpec.set(false)

    generateApiDocumentation.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateModelTests.set(false)
}

val generatedJava = layout.buildDirectory.dir("generated/customer/src/gen/java")
tasks.test {
    inputs.dir(generatedJava)
    systemProperty("sample.generatedJava", generatedJava.get().asFile.absolutePath)
}
