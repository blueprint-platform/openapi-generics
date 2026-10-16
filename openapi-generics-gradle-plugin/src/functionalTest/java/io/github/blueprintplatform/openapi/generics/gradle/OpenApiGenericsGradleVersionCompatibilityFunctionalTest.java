package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OpenApiGenericsGradleVersionCompatibilityFunctionalTest {

    @TempDir
    Path projectDirectory;

    @ParameterizedTest(name = "supports Gradle {0}")
    @ValueSource(strings = {"9.0.0", "9.8.1"})
    void supportsGradleNineLine(String gradleVersion) throws IOException {
        FunctionalTestEnvironment environment =
                FunctionalTestEnvironment.create(projectDirectory);
        environment.writeOfficialJavaConsumer();

        BuildResult sourceRegistration =
                environment.runWithGradleVersion(
                        gradleVersion,
                        ConsumerBuildFiles.PRINT_MAIN_JAVA_SRC_DIRS);

        assertTrue(
                sourceRegistration.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        (line.startsWith("MAIN_JAVA_SRC=")
                                                        || line.startsWith(
                                                                "MAIN_JAVA_SOURCE_DIR="))
                                                && line.contains("generated/openapi")
                                                && line.contains("src/gen/java")),
                sourceRegistration::getOutput);

        BuildResult first =
                environment.runWithGradleVersionAndConfigurationCache(
                        gradleVersion,
                        "compileJava",
                        "--dry-run");

        assertTrue(
                first.getOutput()
                        .contains(":" + ConsumerBuildFiles.OFFICIAL_GENERATE_TASK),
                first::getOutput);
        assertTrue(
                first.getOutput().contains(":prepareOpenApiGenericsTemplates"),
                first::getOutput);
        assertTrue(
                first.getOutput().contains("Configuration cache entry stored")
                        || first.getOutput().contains(
                                "Calculating task graph as no cached"),
                first::getOutput);

        BuildResult second =
                environment.runWithGradleVersionAndConfigurationCache(
                        gradleVersion,
                        "compileJava",
                        "--dry-run");

        assertTrue(
                second.getOutput().contains("Reusing configuration cache"),
                second::getOutput);
    }
}
