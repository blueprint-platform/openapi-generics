package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsCompatibilityFunctionalTest {

    @TempDir
    Path projectDirectory;

    @Test
    void rejectsOfficialPluginWithoutRequiredCapability() throws IOException {
        FunctionalTestEnvironment environment =
                FunctionalTestEnvironment.create(projectDirectory);

        environment.writeBuildFile(
                ConsumerBuildFiles.officialJavaConsumer(
                        FunctionalTestEnvironment.UNSUPPORTED_OPENAPI_GENERATOR_VERSION,
                        environment.pluginVersion(),
                        environment.fixtureRepositoryUri()));

        BuildResult result = environment.runAndFail("help");

        assertTrue(
                result.getOutput()
                        .contains(
                                "OpenAPI Generics Gradle Plugin requires "
                                        + "org.openapi.generator Gradle Plugin "
                                        + "7.25.0 or later"),
                result::getOutput);
    }

    @Test
    void supportsMinimumOfficialPluginVersion() throws IOException {
        FunctionalTestEnvironment environment =
                FunctionalTestEnvironment.create(
                        projectDirectory,
                        FunctionalTestEnvironment.MINIMUM_SUPPORTED_OPENAPI_GENERATOR_VERSION);

        environment.writeOfficialJavaConsumer();

        BuildResult result =
                environment.run(
                        ConsumerBuildFiles.PRINT_EXTRA_DEPENDENCIES,
                        ConsumerBuildFiles.PRINT_GENERATOR_CLASSPATHS,
                        ConsumerBuildFiles.PRINT_TEMPLATE_DIRS,
                        ConsumerBuildFiles.PRINT_TEMPLATE_DEPENDENCIES,
                        ConsumerBuildFiles.PRINT_MAIN_JAVA_SRC_DIRS);

        assertTrue(
                result.getOutput()
                        .contains(
                                "OPENAPI_GENERATOR_EXTRA_DEPENDENCY="
                                        + environment.expectedCodegenDependency()),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.startsWith(
                                                        "GENERATOR_CLASSPATH_"
                                                                + ConsumerBuildFiles.OFFICIAL_GENERATE_TASK
                                                                + "=")
                                                && line.contains(
                                                        "openapi-generics-java-codegen")),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.startsWith(
                                                        "TEMPLATE_DIR_"
                                                                + ConsumerBuildFiles.OFFICIAL_GENERATE_TASK
                                                                + "=")
                                                && line.contains(
                                                        "openapi-generics/templates/effective/Java")),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(
                                "TEMPLATE_DEPENDENCY_"
                                        + ConsumerBuildFiles.OFFICIAL_GENERATE_TASK
                                        + "=true"),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.startsWith("MAIN_JAVA_SRC=")
                                                && line.contains("generated/openapi")
                                                && line.contains("src/gen/java")),
                result::getOutput);
    }
}
