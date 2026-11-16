import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import java.io.File
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.plugin.compatibility.compatibility
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign
import com.gradle.publish.PublishTask
import io.github.blueprintplatform.release.CentralBundleTask
import java.nio.file.Files
import java.nio.file.Path

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.2.1"
    signing
    jacoco
}

group = "io.github.blueprint-platform"
version = "1.3.0"

// Explicit opt-in: ordinary verification and TestKit publication never need signing keys.
val releasePublication = providers.gradleProperty("releasePublication")
    .map(String::toBooleanStrict).orElse(false)
signing {
    isRequired = releasePublication.get()
}
tasks.withType<Sign>().configureEach {
    onlyIf("release publication is enabled") { releasePublication.get() }
}

val minimumOpenApiGeneratorApiCompileVersion = "7.25.0"
val currentOpenApiGeneratorVerificationVersion = "7.27.0"
val selectedOpenApiGeneratorTestRuntimeVersion =
    providers.gradleProperty("openApiGeneratorTestRuntimeVersion")
        .orElse(currentOpenApiGeneratorVerificationVersion)
val spotlessLibVersion = "4.10.4"
val jacocoVersion = "0.8.16" // Same reporting tool version as the Maven reactor.

val repositoryRoot = rootProject.projectDir.parentFile
val repositoryPom = repositoryRoot.resolve("pom.xml")
val codegenModule = repositoryRoot.resolve("openapi-generics-java-codegen")

val functionalTestPluginRepository =
    layout.buildDirectory.dir("functional-test-plugin-repository")

val functionalTestCodegenRepository =
    layout.buildDirectory.dir("functional-test-codegen-repository")

repositories {
    gradlePluginPortal()
    mavenCentral()
}

/*
 * Verification layers:
 *
 * test           -> plugin-owned unit contracts
 *                  (template patch, template IO, selection, source layout,
 *                  official extra-configuration compatibility)
 * functionalTest -> TestKit consumer contracts against a fake codegen jar
 *                  (apply, classpath, selection, source registration,
 *                  template preparation, configuration cache)
 * e2eTest        -> real java-generics-contract generation and compilation
 *
 * check runs test + functionalTest.
 * e2eTest stays explicit: it needs the Maven reactor codegen artifact.
 */
val functionalTest: SourceSet = sourceSets.create("functionalTest")
val e2eTest: SourceSet = sourceSets.create("e2eTest")
functionalTest.java.srcDir("src/testSupport/java")
e2eTest.java.srcDir("src/testSupport/java")
val combinedCoverage = providers.gradleProperty("combinedCoverage").map(String::toBoolean).orElse(false)
val nestedCoverageAgent = configurations.create("nestedCoverageAgent") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

/*
 * Production compiles against the public OpenAPI Generator Gradle plugin API,
 * at the minimum verified integration boundary, but does not publish or embed
 * the official plugin implementation. Consumer runtime selection is independent.
 */
val openApiGeneratorGradlePlugin =
    "org.openapi.generator:org.openapi.generator.gradle.plugin:$minimumOpenApiGeneratorApiCompileVersion"

configurations.named(sourceSets.test.get().implementationConfigurationName) {
    extendsFrom(configurations.compileOnly.get())
}

configurations.named(functionalTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(functionalTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations.named(e2eTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(e2eTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    add(nestedCoverageAgent.name, "org.jacoco:org.jacoco.agent:$jacocoVersion:runtime")
    add(functionalTest.implementationConfigurationName, "org.jacoco:org.jacoco.core:$jacocoVersion")
    add(e2eTest.implementationConfigurationName, "org.jacoco:org.jacoco.core:$jacocoVersion")
    /*
     * Declared once. testImplementation inherits compileOnly so component tests
     * can instantiate GenerateTask without repeating the same coordinate.
     */
    compileOnly(openApiGeneratorGradlePlugin)
    // Config-file binding uses the running official runtime's public settings model.
    compileOnly("org.openapitools:openapi-generator:$minimumOpenApiGeneratorApiCompileVersion")
    implementation("com.diffplug.spotless:spotless-lib:$spotlessLibVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("io.github.solven-eu.cleanthat:java:2.26")
    testRuntimeOnly("com.github.javaparser:javaparser-symbol-solver-core:3.28.2")
    testRuntimeOnly(
        "com.google.googlejavaformat:google-java-format:" +
            if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) "1.36.1" else "1.28.0"
    )

    /*
     * java-gradle-plugin already contributes Gradle TestKit to testImplementation.
     * The custom source sets inherit testImplementation above.
     */
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Unit coverage measures production plugin classes in this build's test JVM only.
// Opt-in nested collection is owned by the TestKit harness, not the outer Test JVM.
jacoco {
    toolVersion = jacocoVersion
}
tasks.withType<Test>().configureEach {
    val unitTest = name == "test"
    extensions.configure<JacocoTaskExtension> {
        isEnabled = unitTest
    }
    inputs.property("combinedCoverage", combinedCoverage)
    if (combinedCoverage.get()) {
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
    }
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    executionData.setFrom(layout.buildDirectory.file("jacoco/test.exec"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Implementation-Title" to "OpenAPI Generics Gradle Plugin",
            "Implementation-Version" to project.version.toString()
        )
    }
}

fun Test.configurePluginVerification() {
    useJUnitPlatform()
    if (combinedCoverage.get()) {
        maxParallelForks = 1
        systemProperty("junit.jupiter.execution.parallel.enabled", "false")
        val coverageOutput = layout.buildDirectory.dir("jacoco/nested/$name")
        outputs.dir(coverageOutput)
        doFirst {
            project.delete(coverageOutput)
            systemProperty("nestedCoverage.agent", nestedCoverageAgent.singleFile.absolutePath)
            systemProperty("nestedCoverage.output", coverageOutput.get().asFile.absolutePath)
            systemProperty("nestedCoverage.testKit", layout.buildDirectory.dir("coverage-testkit/$name").get().asFile.absolutePath)
            systemProperty("nestedCoverage.classes", layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath)
        }
    }

    systemProperty(
        "openApiGeneratorTestRuntimeVersion",
        selectedOpenApiGeneratorTestRuntimeVersion.get()
    )
    systemProperty(
        "functionalTestPluginRepository",
        functionalTestPluginRepository.get().asFile.absolutePath
    )
    systemProperty(
        "pluginUnderTestVersion",
        project.version.toString()
    )

    doFirst {
        logger.lifecycle(
            "{}: selected official OpenAPI Generator test runtime = {} (reference = {}, API compile baseline = {})",
            path,
            selectedOpenApiGeneratorTestRuntimeVersion.get(),
            currentOpenApiGeneratorVerificationVersion,
            minimumOpenApiGeneratorApiCompileVersion
        )
    }

    testLogging {
        showStandardStreams = true
    }
}

gradlePlugin {
    website = "https://github.com/blueprint-platform/openapi-generics"
    vcsUrl = "https://github.com/blueprint-platform/openapi-generics.git"
    plugins {
        create("openApiGenerics") {
            id = "io.github.blueprint-platform.openapi-generics"
            implementationClass =
                "io.github.blueprintplatform.openapi.generics.gradle.OpenApiGenericsGradlePlugin"
            displayName = "OpenAPI Generics Gradle Plugin"
            description =
                "Gradle integration for contract-aware Java client generation with OpenAPI Generics."
            tags.addAll("openapi", "code-generation", "java", "generics")
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

/*
 * TestKit consumer projects resolve the plugin marker and implementation
 * artifact from this isolated Maven repository.
 */
publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "OpenAPI Generics Gradle Plugin"
            description = "Gradle integration for contract-aware Java client generation with OpenAPI Generics."
            url = "https://github.com/blueprint-platform/openapi-generics"
            licenses {
                license {
                    name = "MIT License"
                    url = "https://opensource.org/licenses/MIT"
                    distribution = "repo"
                }
            }
            developers {
                developer {
                    id = "bsayli"
                    name = "Baris Sayli"
                    url = "https://github.com/bsayli"
                }
            }
            scm {
                url = "https://github.com/blueprint-platform/openapi-generics"
                connection = "scm:git:https://github.com/blueprint-platform/openapi-generics.git"
                developerConnection = "scm:git:ssh://git@github.com:blueprint-platform/openapi-generics.git"
                tag = "v1.3.0"
            }
        }
    }
    repositories {
        maven {
            name = "functionalTest"
            url = functionalTestPluginRepository.get().asFile.toURI()
        }
        maven {
            name = "releaseStaging"
            url = layout.buildDirectory.dir("release-staging").get().asFile.toURI()
        }
    }
}

// Maven Publish creates a task per publication/repository pair. Only the implementation
// belongs in the Central staging repository; Portal owns normal marker discovery.
tasks.withType<PublishToMavenRepository>().configureEach {
    if (name.endsWith("ToReleaseStagingRepository")) {
        onlyIf("only pluginMaven is staged for Central") { publication.name == "pluginMaven" }
        doFirst {
            check(releasePublication.get()) {
                "Release staging requires -PreleasePublication=true and a configured signing key."
            }
        }
    }
}

tasks.named<PublishTask>("publishPlugins") {
    doFirst {
        check(validateOnly.get() || releasePublication.get()) {
            "Portal upload requires -PreleasePublication=true and a configured signing key."
        }
    }
}

val publishFunctionalTestPlugin =
    tasks.named("publishAllPublicationsToFunctionalTestRepository")

// Repository-only, upload-free release tooling. No connection to check/TestKit/E2E.
val implementationPublication = providers.provider {
    publishing.publications.getByName("pluginMaven") as MavenPublication
}
tasks.withType<CentralBundleTask>().configureEach {
    group = "publishing"
    coordinates.set("${project.group}:${project.name}:${project.version}")
    releaseEnabled.set(releasePublication)
    validationOnly.set(providers.gradleProperty("releaseValidationOnly").map(String::toBooleanStrict).orElse(false))
    signingFingerprint.set(providers.gradleProperty("releaseSigningFingerprint").orElse(""))
    productionCommand.set(signingFingerprint.zip(validationOnly) { fingerprint, disposable ->
        "./gradlew -PreleasePublication=true -PreleaseSigningFingerprint=$fingerprint " +
            (if (disposable) "-PreleaseValidationOnly=true " else "") + "centralReleaseBundle"
    })
    gpgExecutable.set(providers.gradleProperty("releaseGpgExecutable").orElse("gpg"))
    gpgHome.set(providers.gradleProperty("releaseGpgHome").orElse(""))
    sourceRoot.set(repositoryRoot)
    stagingDirectory.set(layout.buildDirectory.dir("release-staging"))
    evidenceDirectory.set(layout.buildDirectory.dir("central-release"))
    publicationFiles.set(providers.provider {
        val publication = implementationPublication.get()
        val files = publication.artifacts.filter {
            it.extension != "asc" && !it.extension.endsWith(".asc")
        }.associate { artifact ->
            val classifier = artifact.classifier?.let { "-$it" }.orEmpty()
            "${publication.artifactId}-${publication.version}$classifier.${artifact.extension}" to artifact.file.absolutePath
        }.toMutableMap()
        files["${publication.artifactId}-${publication.version}.pom"] =
            layout.buildDirectory.file("publications/pluginMaven/pom-default.xml").get().asFile.absolutePath
        files["${publication.artifactId}-${publication.version}.module"] =
            layout.buildDirectory.file("publications/pluginMaven/module.json").get().asFile.absolutePath
        files
    })
}
val prepareCentralRelease = tasks.register<CentralBundleTask>("prepareCentralRelease") {
    description = "Captures source provenance and clears owned release staging/evidence (no upload)."
    phase.set("prepare")
}
tasks.withType<PublishToMavenRepository>().configureEach {
    if (name == "publishPluginMavenPublicationToReleaseStagingRepository") {
        dependsOn(prepareCentralRelease)
        // Maven Publish generates an artifact-level repository index. Central accepts
        // versioned components, not this local repository index. Remove only this known
        // generated index here; validation rejects any metadata or extra files remaining.
        val index = layout.buildDirectory.file(
            "release-staging/${project.group.toString().replace('.', '/')}/${project.name}/maven-metadata.xml"
        )
        doLast {
            val metadata = index.get().asFile.toPath()
            for (suffix in listOf("", ".md5", ".sha1", ".sha256", ".sha512")) {
                Files.deleteIfExists(Path.of(metadata.toString() + suffix))
            }
        }
    }
}
tasks.register<CentralBundleTask>("validateCentralReleaseStaging") {
    description = "Validates existing signed staging and writes external provenance/inventory; never restages or uploads."
    phase.set("validate")
}
tasks.register<CentralBundleTask>("centralReleaseBundle") {
    description = "Stages, validates and packages only the signed implementation for manual Central upload."
    phase.set("bundle")
    dependsOn("publishPluginMavenPublicationToReleaseStagingRepository")
}

fun resolveMavenExecutable(): String {
    val wrapperName =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "mvnw.cmd"
        } else {
            "mvnw"
        }

    val repositoryWrapper =
        repositoryRoot.resolve(wrapperName)

    if (repositoryWrapper.isFile) {
        return repositoryWrapper.absolutePath
    }

    val configuredExecutable =
        providers.gradleProperty("e2eMavenExecutable").orNull
            ?.takeIf(String::isNotBlank)
            ?: System.getenv("MAVEN_EXECUTABLE")
                ?.takeIf(String::isNotBlank)

    if (configuredExecutable != null) {
        val executable = file(configuredExecutable)

        if (!executable.isFile) {
            throw GradleException(
                "Configured Maven executable does not exist: " +
                    "'${executable.absolutePath}'."
            )
        }

        return executable.absolutePath
    }

    val executableName =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "mvn.cmd"
        } else {
            "mvn"
        }

    val pathEntries =
        System.getenv("PATH")
            .orEmpty()
            .split(File.pathSeparator)
            .filter(String::isNotBlank)

    val pathExecutable =
        pathEntries
            .asSequence()
            .map { File(it, executableName) }
            .firstOrNull(File::isFile)

    if (pathExecutable != null) {
        return pathExecutable.absolutePath
    }

    throw GradleException(
        "Maven executable could not be resolved for the real-codegen E2E setup. " +
            "Add a Maven wrapper to '${repositoryRoot.absolutePath}', expose Maven " +
            "through PATH, set MAVEN_EXECUTABLE, or pass " +
            "-Pe2eMavenExecutable=<absolute Maven executable path>."
    )
}

/*
 * Repository-level prerequisite for the real-codegen E2E.
 *
 * This task is deliberately separate from test and functionalTest. It runs only
 * when e2eTest is requested, builds the actual codegen module through the Maven
 * reactor, and installs the result into a Gradle-owned isolated repository.
 */
val prepareRealCodegenRepository =
    tasks.register<Exec>("prepareRealCodegenRepository") {
        group = "verification"
        description =
            "Builds the real OpenAPI Generics codegen artifact for e2eTest."

        inputs.file(repositoryPom)
        inputs.file(codegenModule.resolve("pom.xml"))
        inputs.dir(codegenModule.resolve("src"))

        outputs.dir(functionalTestCodegenRepository)

        workingDir(repositoryRoot)

        doFirst {
            if (!repositoryPom.isFile) {
                throw GradleException(
                    "OpenAPI Generics repository root could not be located at " +
                        "'${repositoryRoot.absolutePath}'."
                )
            }

            val codegenPom =
                codegenModule.resolve("pom.xml")

            if (!codegenPom.isFile) {
                throw GradleException(
                    "OpenAPI Generics codegen module could not be located at " +
                        "'${codegenModule.absolutePath}'."
                )
            }

            val isolatedRepository =
                functionalTestCodegenRepository.get().asFile

            isolatedRepository.mkdirs()

            commandLine(
                resolveMavenExecutable(),
                "--batch-mode",
                "--no-transfer-progress",
                "-pl",
                "openapi-generics-java-codegen",
                "-am",
                "install",
                "-DskipTests",
                "-Dmaven.repo.local=${isolatedRepository.absolutePath}"
            )
        }
    }

val functionalTestTask =
    tasks.register<Test>("functionalTest") {
        group = "verification"
        description = "Runs isolated Gradle TestKit functional tests."

        testClassesDirs = functionalTest.output.classesDirs
        classpath = functionalTest.runtimeClasspath

        configurePluginVerification()
        shouldRunAfter(tasks.named("test"))
        dependsOn(publishFunctionalTestPlugin)
    }

val e2eTestTask =
    tasks.register<Test>("e2eTest") {
        group = "verification"
        description =
            "Runs real OpenAPI Generics code generation and compilation."

        testClassesDirs = e2eTest.output.classesDirs
        classpath = e2eTest.runtimeClasspath

        configurePluginVerification()
        shouldRunAfter(functionalTestTask)

        dependsOn(publishFunctionalTestPlugin)
        dependsOn(prepareRealCodegenRepository)

        systemProperty(
            "functionalTestCodegenRepository",
            functionalTestCodegenRepository.get().asFile.absolutePath
        )
    }

tasks.named("check") {
    dependsOn(functionalTestTask)
    dependsOn(tasks.jacocoTestReport)
}

tasks.register<JacocoReport>("jacocoCombinedReport") {
    group = "verification"
    description = "Reports unit, nested TestKit and real-codegen plugin coverage (-PcombinedCoverage=true)."
    dependsOn(tasks.test, functionalTestTask, e2eTestTask)
    executionData.setFrom(layout.buildDirectory.file("jacoco/test.exec"),
        fileTree(layout.buildDirectory.dir("jacoco/nested")) { include("**/*.exec") })
    sourceSets(sourceSets.main.get())
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    doFirst {
        check(combinedCoverage.get()) { "Combined report requires -PcombinedCoverage=true; use jacocoTestReport for unit-only." }
        check(layout.buildDirectory.file("jacoco/test.exec").get().asFile.length() > 0) { "Missing unit execution data" }
        for (suite in listOf("functionalTest", "e2eTest")) {
            check(fileTree(layout.buildDirectory.dir("jacoco/nested/$suite")).matching { include("*.exec") }.files.any { it.length() > 0 }) {
                "Missing fresh nested execution data for $suite"
            }
        }
    }
}
