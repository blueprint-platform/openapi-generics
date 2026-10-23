package io.github.blueprintplatform.openapi.generics.gradle.integration;

import io.github.blueprintplatform.openapi.generics.gradle.dependency.OpenApiGenericsFormattingClasspath;
import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsGeneratedSourceRegistrar;
import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsGenerateTaskConfigurer;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateLifecycle;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Installs OpenAPI Generics behavior after the official OpenAPI Generator Gradle plugin is present.
 */
public final class OpenApiGeneratorIntegration {

    private final OpenApiGeneratorCompatibility compatibility;
    private final OpenApiGenericsTemplateLifecycle templateLifecycle;
    private final OpenApiGenericsFormattingClasspath formattingClasspath;
    private final OpenApiGenericsGeneratedSourceRegistrar generatedSourceRegistrar;
    private final OpenApiGenericsGenerateTaskConfigurer taskConfigurer;

    public OpenApiGeneratorIntegration(
            OpenApiGeneratorCompatibility compatibility,
            OpenApiGenericsTemplateLifecycle templateLifecycle,
            OpenApiGenericsFormattingClasspath formattingClasspath,
            OpenApiGenericsGeneratedSourceRegistrar generatedSourceRegistrar,
            OpenApiGenericsGenerateTaskConfigurer taskConfigurer) {

        this.compatibility = compatibility;
        this.templateLifecycle = templateLifecycle;
        this.formattingClasspath = formattingClasspath;
        this.generatedSourceRegistrar = generatedSourceRegistrar;
        this.taskConfigurer = taskConfigurer;
    }

    public void install(Project project) {
        Configuration generatorRuntimeClasspath =
                compatibility.requireGeneratorExtraConfiguration(project);

        OpenApiGenericsTemplateRuntime templateRuntime =
                templateLifecycle.register(
                        project,
                        generatorRuntimeClasspath);

        Configuration formatterRuntimeClasspath =
                formattingClasspath.register(project);

        /*
         * Project-level Java compilation wiring must be installed before entering
         * GenerateTask.configureEach callbacks. Gradle 9.x mutation guards reject configuring
         * another task provider from inside a task-container configuration callback.
         */
        generatedSourceRegistrar.install(project);

        project.getTasks()
                .withType(GenerateTask.class)
                .configureEach(
                        task ->
                                taskConfigurer.configure(
                                        project,
                                        task,
                                        templateRuntime,
                                        generatorRuntimeClasspath,
                                        formatterRuntimeClasspath));

        project.getLogger()
                .info(
                        "OpenAPI Generics integration installed with template task '{}' "
                                + "and embedded formatter configuration '{}'.",
                        templateRuntime.preparationTask().getName(),
                        OpenApiGenericsFormattingClasspath.CONFIGURATION_NAME);
    }
}
