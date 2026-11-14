plugins {
    `java-library`
    id("org.springframework.boot")
}
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
    implementation(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    testImplementation(project(":producer"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Avoid main-class discovery probing class output existence during cache configuration.
tasks.bootJar {
    mainClass.set("io.github.blueprintplatform.samples.gradle.customer.consumer.ConsumerApplication")
}
