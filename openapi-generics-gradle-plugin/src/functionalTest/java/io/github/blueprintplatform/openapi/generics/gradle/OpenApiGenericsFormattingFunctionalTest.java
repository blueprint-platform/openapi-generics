package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsFormattingFunctionalTest {

    private static final String PRINT_FORMATTING_CONTRACT = "printFormattingContract";

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);

        environment.writeBuildFile(
                ConsumerBuildFiles.officialJavaConsumer(
                                environment.openApiGeneratorVersion(),
                                environment.pluginVersion(),
                                environment.fixtureRepositoryUri())
                        + """

                        tasks.register("printFormattingContract") {
                            doLast {
                                configurations
                                    .getByName("openApiGenericsFormatter")
                                    .dependencies
                                    .forEach { dependency ->
                                        println(
                                            "OPENAPI_GENERICS_FORMATTER_DEPENDENCY="
                                                + "${dependency.group}:"
                                                + "${dependency.name}:"
                                                + "${dependency.version}"
                                        )
                                    }

                                println(
                                    "SPOTLESS_PLUGIN_APPLIED="
                                        + plugins.hasPlugin("com.diffplug.spotless")
                                )
                            }
                        }
                        """);
    }

    @Test
    void provisionsEmbeddedFormatterRuntimeWithoutApplyingSpotlessPlugin() {
        BuildResult result = environment.run(PRINT_FORMATTING_CONTRACT);

        assertTrue(
                result.getOutput()
                        .contains(
                                "OPENAPI_GENERICS_FORMATTER_DEPENDENCY="
                                        + "com.diffplug.spotless:spotless-lib:"),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(
                                "OPENAPI_GENERICS_FORMATTER_DEPENDENCY="
                                        + "io.github.solven-eu.cleanthat:java:"),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(
                                "OPENAPI_GENERICS_FORMATTER_DEPENDENCY="
                                        + "com.google.googlejavaformat:google-java-format:"),
                result::getOutput);

        assertTrue(
                result.getOutput().contains("SPOTLESS_PLUGIN_APPLIED=false"),
                result::getOutput);
    }
}
