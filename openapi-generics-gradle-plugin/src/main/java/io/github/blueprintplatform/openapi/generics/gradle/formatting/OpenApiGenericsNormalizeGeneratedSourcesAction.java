package io.github.blueprintplatform.openapi.generics.gradle.formatting;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

/**
 * Normalizes the final Java output of a selected OpenAPI Generics GenerateTask.
 *
 * <p>All execution-time state is supplied during task configuration. The action therefore does not
 * access task extensions or project state while the task is executing, preserving configuration
 * cache compatibility.
 */
public final class OpenApiGenericsNormalizeGeneratedSourcesAction
        implements Action<Task>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logging.getLogger(OpenApiGenericsNormalizeGeneratedSourcesAction.class);

    private final Provider<Boolean> selected;
    private final Provider<Set<FileSystemLocation>> formatterClasspathElements;
    private final Provider<Set<FileSystemLocation>> generatedJavaDirectoryElements;
    private final String taskPath;

    public OpenApiGenericsNormalizeGeneratedSourcesAction(
            Provider<Boolean> selected,
            Provider<Set<FileSystemLocation>> formatterClasspathElements,
            Provider<Set<FileSystemLocation>> generatedJavaDirectoryElements,
            String taskPath) {

        this.selected = selected;
        this.formatterClasspathElements = formatterClasspathElements;
        this.generatedJavaDirectoryElements = generatedJavaDirectoryElements;
        this.taskPath = taskPath;
    }

    @Override
    public void execute(Task ignored) {
        if (!selected.get()) {
            return;
        }

        Set<File> formatterClasspath =
                formatterClasspathElements
                        .get()
                        .stream()
                        .map(FileSystemLocation::getAsFile)
                        .sorted(
                                (left, right) ->
                                        left.getAbsolutePath()
                                                .compareTo(right.getAbsolutePath()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<FileSystemLocation> generatedDirectories =
                generatedJavaDirectoryElements.get();

        if (generatedDirectories.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one generated Java directory for OpenAPI Generics task '%s', "
                            + "but found %d."
                            .formatted(
                                    taskPath,
                                    generatedDirectories.size()));
        }

        File generatedJavaDirectory =
                generatedDirectories
                        .iterator()
                        .next()
                        .getAsFile();

        OpenApiGenericsGeneratedSourceNormalizer.create(formatterClasspath)
                .normalize(generatedJavaDirectory.toPath());

        LOGGER.info(
                "OpenAPI Generics normalized generated Java sources for '{}': {}",
                taskPath,
                generatedJavaDirectory.getAbsolutePath());
    }
}