plugins {
    base
    id("org.springframework.boot") version "4.1.2" apply false
    id("org.openapi.generator") version "7.27.0" apply false
}

subprojects {
    apply(plugin = "java-library")
    group = "io.github.blueprint-platform.samples.gradle"
    version = "1.3.1-SNAPSHOT"
    repositories {
        // Only needed for platform artifacts installed from this checkout for development.
        mavenLocal {
            content { includeGroup("io.github.blueprint-platform") }
        }
        mavenCentral()
    }
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
tasks.named("build") { dependsOn(subprojects.map { "${it.path}:build" }) }
tasks.named("check") { dependsOn(subprojects.map { "${it.path}:check" }) }
tasks.named("clean") { dependsOn(subprojects.map { "${it.path}:clean" }) }
