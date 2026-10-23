package io.github.blueprintplatform.openapi.generics.gradle.generation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.formatting.OpenApiGenericsFormattingSpec;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import io.github.blueprintplatform.openapi.generics.gradle.template.PrepareOpenApiGenericsTemplates;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsGenerateTaskConfigurerTest {

    @TempDir
    File temporaryDirectory;

    @Test
    void addsOwnedRuntimeClasspathsAfterTaskBecomesSelected() throws IOException {
        Project project = ProjectBuilder.builder().build();
        GenerateTask task = createGenerateTask(project, "generateClient");
        File codegenJar = createFile("openapi-generics-java-codegen-test.jar");
        File formatterJar = createFile("openapi-generics-formatter-test.jar");

        configure(
                project,
                task,
                project.files(codegenJar),
                project.files(formatterJar));

        task.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);

        task.getOutputDir()
                .set(new File(temporaryDirectory, "generated/client"));

        task.getConfigOptions().set(Map.of());

        assertTrue(task.getGeneratorClasspath().getFiles().contains(codegenJar));

        OpenApiGenericsFormattingSpec formatting =
                task.getExtensions()
                        .getByType(OpenApiGenericsFormattingSpec.class);

        assertTrue(
                formatting.getFormatterClasspath()
                        .getFiles()
                        .contains(formatterJar));

        assertTrue(
                formatting.getGeneratedJavaDirectory()
                        .getSingleFile()
                        .getPath()
                        .replace('\\', '/')
                        .endsWith("generated/client/src/main/java"));
    }

    @Test
    void doesNotAddOwnedRuntimeClasspathsToAnotherGenerator() throws IOException {
        Project project = ProjectBuilder.builder().build();
        GenerateTask task = createGenerateTask(project, "generateKotlin");
        File codegenJar = createFile("openapi-generics-java-codegen-test.jar");
        File formatterJar = createFile("openapi-generics-formatter-test.jar");

        configure(
                project,
                task,
                project.files(codegenJar),
                project.files(formatterJar));

        task.getGeneratorName().set("kotlin");

        task.getOutputDir()
                .set(new File(temporaryDirectory, "generated/kotlin"));

        task.getConfigOptions().set(Map.of());

        assertFalse(task.getGeneratorClasspath().getFiles().contains(codegenJar));

        OpenApiGenericsFormattingSpec formatting =
                task.getExtensions()
                        .getByType(OpenApiGenericsFormattingSpec.class);

        assertTrue(formatting.getFormatterClasspath().isEmpty());
    }

    private void configure(
            Project project,
            GenerateTask task,
            FileCollection generatorRuntimeClasspath,
            FileCollection formatterRuntimeClasspath) {

        OpenApiGenericsGenerateTaskConfigurer configurer =
                new OpenApiGenericsGenerateTaskConfigurer(
                        new OpenApiGenericsSourceLayout());

        configurer.configure(
                project,
                task,
                templateRuntime(project),
                generatorRuntimeClasspath,
                formatterRuntimeClasspath);
    }

    private OpenApiGenericsTemplateRuntime templateRuntime(Project project) {
        TaskProvider<PrepareOpenApiGenericsTemplates> preparationTask =
                project.getTasks()
                        .register(
                                "prepareOpenApiGenericsTemplates",
                                PrepareOpenApiGenericsTemplates.class);

        return new OpenApiGenericsTemplateRuntime(
                preparationTask,
                project.getLayout()
                        .getBuildDirectory()
                        .dir("openapi-generics/templates/effective"));
    }

    private GenerateTask createGenerateTask(
            Project project,
            String name) {

        return project.getTasks()
                .register(name, GenerateTask.class)
                .get();
    }

    private File createFile(String name) throws IOException {
        File file = new File(temporaryDirectory, name);
        assertTrue(file.createNewFile());
        return file;
    }
}