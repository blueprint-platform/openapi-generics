plugins {
    java
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
