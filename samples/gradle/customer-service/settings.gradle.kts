pluginManagement {
    // Repository development bootstrap; published consumers use an explicit released plugin version.
    includeBuild("../../../openapi-generics-gradle-plugin")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "gradle-customer-service"
include("contract", "producer", "client", "consumer")
