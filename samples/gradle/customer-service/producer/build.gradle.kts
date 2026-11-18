plugins {
    `java-library`
    id("org.springframework.boot")
}
val platformVersion = providers.gradleProperty("platformVersion").get()

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    implementation(project(":contract"))
    implementation("io.github.blueprint-platform:openapi-generics-server-starter:$platformVersion")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Springdoc 3.2.0 is the verified reference for this Boot 4 sample.
    @Suppress("kotlin:S6624")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
val snapshot = rootProject.layout.projectDirectory.file("spec/customer-api-docs.json")
val generatedSnapshot = layout.buildDirectory.file("openapi/customer-api-docs.json")
val updateSnapshot = providers.gradleProperty("updateOpenApiSnapshot").orElse("false")
tasks.test {
    inputs.file(snapshot)
    inputs.property("updateOpenApiSnapshot", updateSnapshot)
    outputs.file(generatedSnapshot)
    systemProperty("sample.snapshot", snapshot.asFile.absolutePath)
    systemProperty("sample.generatedSnapshot", generatedSnapshot.get().asFile.absolutePath)
    systemProperty("sample.updateSnapshot", updateSnapshot.get())
}

// Avoid main-class discovery probing class output existence during cache configuration.
tasks.bootJar {
    mainClass.set("io.github.blueprintplatform.samples.gradle.customer.producer.ProducerApplication")
}
