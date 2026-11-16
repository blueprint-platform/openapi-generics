pluginManagement {
    repositories {
        exclusiveContent {
            forRepository { gradlePluginPortal() }
            filter {
                includeGroup("io.github.blueprint-platform.openapi-generics")
                includeGroup("org.openapi.generator")
                includeModule("org.openapitools", "openapi-generator-gradle-plugin")
            }
        }
        mavenCentral()
    }
}
rootProject.name = "first-client"
