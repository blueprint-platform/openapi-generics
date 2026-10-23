package io.github.blueprintplatform.openapi.generics.gradle.generation;

import io.github.blueprintplatform.openapi.generics.gradle.formatting.OpenApiGenericsFormattingSpec;
import io.github.blueprintplatform.openapi.generics.gradle.formatting.OpenApiGenericsNormalizeGeneratedSourcesAction;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import java.util.concurrent.Callable;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Adds OpenAPI Generics-owned template, runtime classpath, and generated-source normalization wiring
 * to official generation tasks.
 *
 * <p>The configurer never assigns or overrides {@code generatorName}. Generator selection remains
 * an explicit consumer decision.
 */
public final class OpenApiGenericsGenerateTaskConfigurer {

    private final OpenApiGenericsSourceLayout sourceLayout;

    public OpenApiGenericsGenerateTaskConfigurer(
            OpenApiGenericsSourceLayout sourceLayout) {

        this.sourceLayout = sourceLayout;
    }

    public void configure(
            Project project,
            GenerateTask task,
            OpenApiGenericsTemplateRuntime templateRuntime,
            FileCollection generatorRuntimeClasspath,
            FileCollection formatterRuntimeClasspath) {

        OpenApiGenericsTaskSelection selection =
                OpenApiGenericsTaskSelection.from(task);

        Provider<Directory> effectiveJavaTemplateDirectory =
                selection.isSelected()
                        .flatMap(
                                selected ->
                                        selected
                                                ? templateRuntime
                                                .effectiveTemplateDirectory()
                                                .map(directory -> directory.dir("Java"))
                                                : project.getProviders()
                                                .provider(() -> null));

        Provider<FileCollection> selectedGeneratorRuntimeClasspath =
                selection.isSelected()
                        .map(
                                selected ->
                                        selected
                                                ? generatorRuntimeClasspath
                                                : project.files());

        Provider<FileCollection> selectedFormatterRuntimeClasspath =
                selection.isSelected()
                        .map(
                                selected ->
                                        selected
                                                ? formatterRuntimeClasspath
                                                : project.files());

        /*
         * The official OpenAPI Generator plugin has already connected
         * GenerateTask.templateDir with its extension property using set(provider).
         * A convention added here therefore does not reliably become the effective
         * value. Selected OpenAPI Generics tasks must explicitly use the prepared
         * Java template root.
         */
        task.getTemplateDir()
                .set(effectiveJavaTemplateDirectory);

        /*
         * Custom GenerateTask instances do not inherit the default openApiGenerate
         * task's generator worker classpath. The selected task therefore receives
         * the official openApiGeneratorExtra configuration explicitly.
         *
         * The provider remains lazy so generatorName can still be configured by
         * the consumer after this callback runs.
         */
        task.getGeneratorClasspath()
                .from(selectedGeneratorRuntimeClasspath);

        task.dependsOn(
                (Callable<Object>)
                        () ->
                                selection.isSelected().get()
                                        ? templateRuntime.preparationTask()
                                        : null);

        OpenApiGenericsFormattingSpec formatting =
                task.getExtensions()
                        .create(
                                OpenApiGenericsFormattingSpec.EXTENSION_NAME,
                                OpenApiGenericsFormattingSpec.class);

        formatting.getFormatterClasspath()
                .from(selectedFormatterRuntimeClasspath);

        formatting.getGeneratedJavaDirectory()
                .from(
                        sourceLayout.generatedJavaDirectory(
                                project,
                                task,
                                selection));

        /*
         * The formatter runtime participates in the GenerateTask input fingerprint.
         * Normalization itself runs as the final action of the same GenerateTask so
         * Gradle snapshots the already-clean generated output for up-to-date/build-cache
         * purposes instead of observing a later task mutate another task's outputs.
         */
        task.getInputs()
                .files(formatting.getFormatterClasspath())
                .withPropertyName("openApiGenericsFormatterClasspath")
                .withNormalizer(ClasspathNormalizer.class);

        /*
         * Capture every piece of execution-time state during configuration.
         *
         * The action must not look up task extensions while GenerateTask is executing:
         * Gradle configuration cache explicitly prohibits Task.extensions access at
         * execution time.
         */
        task.doLast(
                "normalizeOpenApiGenericsGeneratedSources",
                new OpenApiGenericsNormalizeGeneratedSourcesAction(
                        selection.isSelected(),
                        formatting.getFormatterClasspath().getElements(),
                        formatting.getGeneratedJavaDirectory().getElements(),
                        selection.getTaskPath()));

        task.doFirst(
                ignored ->
                        reportSelection(
                                task,
                                selection));
    }

    private void reportSelection(
            GenerateTask task,
            OpenApiGenericsTaskSelection selection) {

        if (selection.isSelected().get()) {
            task.getLogger()
                    .info(
                            "OpenAPI Generics selected GenerateTask '{}' "
                                    + "with generatorName '{}'.",
                            selection.getTaskPath(),
                            selection.getDisplayGeneratorName());

            task.getLogger()
                    .info(
                            "OpenAPI Generics effective templateDir for '{}': {}",
                            task.getPath(),
                            task.getTemplateDir()
                                    .get()
                                    .getAsFile()
                                    .getAbsolutePath());

            return;
        }

        task.getLogger()
                .info(
                        "OpenAPI Generics skipped GenerateTask '{}' "
                                + "because generatorName is '{}'.",
                        selection.getTaskPath(),
                        selection.getDisplayGeneratorName());
    }
}