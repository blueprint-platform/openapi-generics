plugins {
    java
}

repositories { mavenCentral() }

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
    // ProjectBuilder injects Gradle's compatibility interfaces into the test classloader.
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}
