package io.github.blueprintplatform.openapi.generics.gradle.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsTaskSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsNormalizeGeneratedSourcesActionTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void normalizesSelectedGeneratorOutput() throws IOException {
        Project project =
                ProjectBuilder.builder()
                        .withProjectDir(temporaryDirectory.toFile())
                        .build();

        GenerateTask selected =
                project.getTasks()
                        .register("generateSelected", GenerateTask.class)
                        .get();

        selected.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);

        Path generatedDirectory =
                temporaryDirectory.resolve("selected/src/gen/java");

        OpenApiGenericsFormattingSpec formatting =
                configureFormatting(selected, generatedDirectory);

        Path selectedJava =
                generatedDirectory.resolve("Selected.java");

        Files.createDirectories(selectedJava.getParent());

        Files.writeString(
                selectedJava,
                "package example; import java.util.Set; public class Selected{}",
                StandardCharsets.UTF_8);

        OpenApiGenericsTaskSelection selection =
                OpenApiGenericsTaskSelection.from(selected);

        new OpenApiGenericsNormalizeGeneratedSourcesAction(
                selection.isSelected(),
                formatting.getFormatterClasspath().getElements(),
                formatting.getGeneratedJavaDirectory().getElements(),
                selection.getTaskPath())
                .execute(selected);

        String normalized =
                Files.readString(
                        selectedJava,
                        StandardCharsets.UTF_8);

        assertFalse(normalized.contains("java.util.Set"));
        assertTrue(normalized.contains("public class Selected {}"));
    }

    @Test
    void leavesAnotherGeneratorUntouchedWithoutResolvingFormattingState()
            throws IOException {

        Project project =
                ProjectBuilder.builder()
                        .withProjectDir(temporaryDirectory.toFile())
                        .build();

        GenerateTask other =
                project.getTasks()
                        .register("generateOther", GenerateTask.class)
                        .get();

        other.getGeneratorName().set("kotlin");

        Path otherJava =
                temporaryDirectory.resolve(
                        "other/src/gen/java/Other.java");

        Files.createDirectories(otherJava.getParent());

        String rawOther =
                "package example; import java.util.Set; public class Other{}";

        Files.writeString(
                otherJava,
                rawOther,
                StandardCharsets.UTF_8);

        OpenApiGenericsTaskSelection selection =
                OpenApiGenericsTaskSelection.from(other);

        Provider<Set<FileSystemLocation>> unresolvedFormatterClasspath =
                project.getProviders()
                        .provider(
                                () -> {
                                    throw new AssertionError(
                                            "Formatter classpath must not be resolved "
                                                    + "for a non-selected generator.");
                                });

        Provider<Set<FileSystemLocation>> unresolvedGeneratedDirectory =
                project.getProviders()
                        .provider(
                                () -> {
                                    throw new AssertionError(
                                            "Generated Java directory must not be resolved "
                                                    + "for a non-selected generator.");
                                });

        new OpenApiGenericsNormalizeGeneratedSourcesAction(
                selection.isSelected(),
                unresolvedFormatterClasspath,
                unresolvedGeneratedDirectory,
                selection.getTaskPath())
                .execute(other);

        assertEquals(
                rawOther,
                Files.readString(
                        otherJava,
                        StandardCharsets.UTF_8));
    }

    private OpenApiGenericsFormattingSpec configureFormatting(
            GenerateTask task,
            Path generatedDirectory) {

        OpenApiGenericsFormattingSpec formatting =
                task.getExtensions()
                        .create(
                                OpenApiGenericsFormattingSpec.EXTENSION_NAME,
                                OpenApiGenericsFormattingSpec.class);

        formatting.getFormatterClasspath()
                .from(testRuntimeClasspath());

        formatting.getGeneratedJavaDirectory()
                .from(generatedDirectory.toFile());

        return formatting;
    }

    private Set<File> testRuntimeClasspath() {
        return Arrays.stream(
                        System.getProperty("java.class.path")
                                .split(File.pathSeparator))
                .map(File::new)
                .filter(File::isFile)
                .sorted(
                        Comparator.comparing(File::getAbsolutePath))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}