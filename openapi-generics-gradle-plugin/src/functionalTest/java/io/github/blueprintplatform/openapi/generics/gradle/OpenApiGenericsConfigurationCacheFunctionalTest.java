package io.github.blueprintplatform.openapi.generics.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsConfigurationCacheFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    private static void assertSuccessfulPrepare(BuildResult result) {
        BuildTask task = result.task(":prepareOpenApiGenericsTemplates");
        assertNotNull(task, result::getOutput);
        assertTrue(
                task.getOutcome() == SUCCESS || task.getOutcome() == UP_TO_DATE,
                result::getOutput);
    }

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeOfficialJavaConsumer();
    }

    @Test
    void reusesConfigurationCacheForTemplatePreparation() {
        BuildResult first =
                environment.runWithConfigurationCache("prepareOpenApiGenericsTemplates");
        assertSuccessfulPrepare(first);
        assertTrue(
                first.getOutput().contains("Configuration cache entry stored")
                        || first.getOutput().contains("Calculating task graph as no cached"),
                first::getOutput);

        BuildResult second =
                environment.runWithConfigurationCache("prepareOpenApiGenericsTemplates");
        assertSuccessfulPrepare(second);
        assertTrue(
                second.getOutput().contains("Reusing configuration cache"),
                second::getOutput);
    }

    @Test
    void reusesConfigurationCacheForCompileJavaTaskGraph() {
        BuildResult first =
                environment.runWithConfigurationCache(
                        "compileJava",
                        "--dry-run");

        assertTrue(
                first.getOutput().contains("Configuration cache entry stored")
                        || first.getOutput().contains("Calculating task graph as no cached"),
                first::getOutput);

        assertTrue(
                first.getOutput().contains(":openApiGenerate"),
                first::getOutput);

        assertTrue(
                first.getOutput().contains(":prepareOpenApiGenericsTemplates"),
                first::getOutput);

        BuildResult second =
                environment.runWithConfigurationCache(
                        "compileJava",
                        "--dry-run");

        assertTrue(
                second.getOutput().contains("Reusing configuration cache"),
                second::getOutput);

        assertTrue(
                second.getOutput().contains(":openApiGenerate"),
                second::getOutput);
    }
}
