package io.github.blueprintplatform.openapi.generics.gradle.generation;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.openapitools.codegen.config.DynamicSettings;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Derives the generated Java source root from official GenerateTask properties.
 */
public final class OpenApiGenericsSourceLayout {

    public static final String SOURCE_FOLDER_OPTION =
            "sourceFolder";

    public static final String DEFAULT_JAVA_SOURCE_FOLDER =
            "src/main/java";

    public Provider<File> generatedJavaDirectory(
            Project project,
            GenerateTask task,
            OpenApiGenericsTaskSelection selection) {

        Provider<String> configFileSourceFolder =
                project.getProviders()
                        .fileContents(task.getConfigFile())
                        .getAsText()
                        .zip(task.getConfigFile(),
                                (content, file) -> sourceFolderFromConfigFile(file.getAsFile(), content))
                        .orElse(DEFAULT_JAVA_SOURCE_FOLDER);

        Provider<String> sourceFolder =
                task.getConfigOptions().orElse(Map.of())
                        .zip(task.getAdditionalProperties().orElse(Map.of()),
                                (options, properties) -> options.containsKey(SOURCE_FOLDER_OPTION)
                                        ? options.get(SOURCE_FOLDER_OPTION)
                                        : propertySourceFolder(properties))
                        .orElse(configFileSourceFolder);

        // Source registration needs the location before generation produces the task outputs.
        Provider<File> directory = task.getOutputDir().getLocationOnly()
                .zip(sourceFolder, (output, folder) -> new File(output.getAsFile(), folder));
        Provider<File> absent = project.getProviders().provider(() -> null);

        return selection.isSelected()
                .flatMap(
                        selected ->
                                selected
                                        ? directory
                                        : absent);
    }

    private String sourceFolderFromConfigFile(File file, String content) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        try {
            // Use upstream's config binding, including top-level dynamic options.
            DynamicSettings settings = (name.endsWith(".yaml") || name.endsWith(".yml")
                    ? Yaml.mapper() : Json.mapper())
                    .copy()
                    .registerModule(new GuavaModule())
                    .readValue(content, DynamicSettings.class);
            return propertySourceFolder(settings.getGeneratorSettings().getAdditionalProperties());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read generator sourceFolder from " + file, exception);
        }
    }

    private String propertySourceFolder(Map<String, ?> properties) {
        Object value = properties.get(SOURCE_FOLDER_OPTION);
        return value == null ? null : value.toString();
    }
}
