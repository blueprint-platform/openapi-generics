package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// SonarQube for IDE can classify this custom Gradle test source set as production.
@SuppressWarnings("java:S5960")
class OpenApiGenericsTaskWiringFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeMixedGeneratorConsumer();
    }

    @Test
    void preservesOrdinaryExtensionTemplateDuringRealGeneration() throws IOException {
        Files.writeString(projectDirectory.resolve("spec.yaml"), """
                openapi: 3.0.3
                info: {title: Ordinary template regression, version: "1"}
                paths: {}
                components:
                  schemas:
                    Pet:
                      type: object
                      properties:
                        name: {type: string}
                """);
        Path templates = Files.createDirectories(projectDirectory.resolve("templates"));
        Files.writeString(templates.resolve("model.mustache"), """
                // ORDINARY_CONSUMER_TEMPLATE
                package {{package}};
                {{#models}}{{#model}}public class {{classname}} {}{{/model}}{{/models}}
                """);
        environment.writeBuildFile(
                ConsumerBuildFiles.ordinaryJavaConsumerWithTemplate(
                        environment.officialPluginRuntimeVersion(),
                        environment.pluginVersion(),
                        environment.fixtureRepositoryUri()));

        BuildResult result = environment.run(
                ConsumerBuildFiles.PRINT_TEMPLATE_DIRS, "openApiGenerate");

        assertTrue(result.getOutput().contains(
                "TEMPLATE_DIR_openApiGenerate=" + templates.toRealPath().toString().replace('\\', '/')),
                result::getOutput);
        assertTrue(Files.readString(projectDirectory.resolve(
                "build/generated/ordinary/src/main/java/review/model/Pet.java"))
                .contains("ORDINARY_CONSUMER_TEMPLATE"), result::getOutput);
        assertTrue(result.task(":prepareOpenApiGenericsTemplates") == null, result::getOutput);
    }

    @Test
    void preservesConsumerGeneratorSelection() {
        BuildResult result = environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DIRS);

        assertTrue(
                result.getOutput()
                        .contains(
                                "GENERATOR_NAME_"
                                        + ConsumerBuildFiles.GENERICS_GENERATE_TASK
                                        + "=java-generics-contract"),
                result::getOutput);
        assertTrue(
                result.getOutput()
                        .contains(
                                "GENERATOR_NAME_"
                                        + ConsumerBuildFiles.KOTLIN_GENERATE_TASK
                                        + "=kotlin"),
                result::getOutput);
    }

    @Test
    void wiresTemplatePreparationOnlyToGenericsTask() {
        BuildResult generics =
                environment.run(
                        ConsumerBuildFiles.GENERICS_GENERATE_TASK,
                        "--dry-run");
        BuildResult kotlin =
                environment.run(
                        ConsumerBuildFiles.KOTLIN_GENERATE_TASK,
                        "--dry-run");

        assertTrue(
                generics.getOutput().contains(":prepareOpenApiGenericsTemplates"),
                generics::getOutput);
        assertTrue(
                kotlin.getOutput()
                        .lines()
                        .noneMatch(line -> line.contains(":prepareOpenApiGenericsTemplates")),
                kotlin::getOutput);
    }

    @Test
    void overridesTemplateDirOnlyForSelectedTask() {
        BuildResult result = environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DIRS);
        String output = result.getOutput();

        assertTrue(
                output.contains(
                        "TEMPLATE_DIR_"
                                + ConsumerBuildFiles.GENERICS_GENERATE_TASK
                                + "="),
                result::getOutput);
        assertTrue(
                output.contains("openapi-generics/templates/effective/Java"),
                result::getOutput);

        String kotlinLine =
                output.lines()
                        .filter(
                                line ->
                                        line.startsWith(
                                                "TEMPLATE_DIR_"
                                                        + ConsumerBuildFiles
                                                                .KOTLIN_GENERATE_TASK
                                                        + "="))
                        .findFirst()
                        .orElse("");

        assertFalse(
                kotlinLine.contains("openapi-generics/templates/effective/Java"),
                () -> "Kotlin task should keep the official templateDir:\n" + output);
    }
}
