package io.github.blueprintplatform.openapi.generics.gradle.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsSourceLayoutTest {

    @TempDir
    Path directory;

    static Stream<Arguments> configFiles() {
        return Stream.of(
                Arguments.of("config.json", "{\"sourceFolder\":\"src/custom/java\"}"),
                Arguments.of("config.json", "{\"additionalProperties\":{\"sourceFolder\":\"src/custom/java\"}}"),
                Arguments.of("config.yaml", "sourceFolder: src/custom/java\n"),
                Arguments.of("config.yml", "additionalProperties:\n  sourceFolder: src/custom/java\n"));
    }

    @ParameterizedTest
    @MethodSource("configFiles")
    void resolvesUpstreamConfigFileSourceFolder(String name, String content) throws IOException {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", null);
        Path config = directory.resolve(name);
        Files.writeString(config, content);
        task.getConfigFile().fileValue(config.toFile());

        assertEquals(new File(project.getLayout().getBuildDirectory().get().getAsFile(), "generated/client/src/custom/java"),
                new OpenApiGenericsSourceLayout().generatedJavaDirectory(
                        project, task, OpenApiGenericsTaskSelection.from(task)).get());
    }

    @Test
    void preservesOfficialOptionPrecedence() throws IOException {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", null);
        Path config = directory.resolve("config.json");
        Files.writeString(config, "{\"sourceFolder\":\"src/file/java\"}");
        task.getConfigFile().fileValue(config.toFile());
        task.getAdditionalProperties().put("sourceFolder", "src/additional/java");
        var root = new OpenApiGenericsSourceLayout().generatedJavaDirectory(
                project, task, OpenApiGenericsTaskSelection.from(task));

        assertEquals(new File(project.getLayout().getBuildDirectory().get().getAsFile(), "generated/client/src/additional/java"), root.get());
        task.getConfigOptions().put("sourceFolder", "src/options/java");
        assertEquals(new File(project.getLayout().getBuildDirectory().get().getAsFile(), "generated/client/src/options/java"), root.get());
    }

    @Test
    void derivesConfiguredSourceFolder() {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", "src/gen/java");

        File generatedJavaDirectory =
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .get();

        assertEquals(
                project.getLayout()
                        .getBuildDirectory()
                        .dir("generated/client/src/gen/java")
                        .get()
                        .getAsFile(),
                generatedJavaDirectory);
    }

    @Test
    void usesDefaultJavaSourceFolderWhenUnconfigured() {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", null);

        File generatedJavaDirectory =
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .get();

        assertEquals(
                project.getLayout()
                        .getBuildDirectory()
                        .dir("generated/client/" + OpenApiGenericsSourceLayout.DEFAULT_JAVA_SOURCE_FOLDER)
                        .get()
                        .getAsFile(),
                generatedJavaDirectory);
    }

    @Test
    void returnsAbsentProviderForAnotherGenerator() {
        Project project = createProject();
        GenerateTask task = createGenerateTask(project, "generateKotlin");
        task.getGeneratorName().set("kotlin");
        task.getOutputDir()
                .set(
                        project.getLayout()
                                .getBuildDirectory()
                                .dir("generated/kotlin")
                                .get()
                                .getAsFile());
        task.getConfigOptions().set(Map.of());

        assertFalse(
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .isPresent());
    }

    @Test
    void rejectsMalformedConfigWithItsPathAndParserCauseInsteadOfFallingBack() throws Exception {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", null);
        var config = project.getLayout().getProjectDirectory().file("invalid-generator.json").getAsFile();
        java.nio.file.Files.writeString(config.toPath(), "{ invalid JSON");
        task.getConfigFile().set(config);
        var directory = new OpenApiGenericsSourceLayout()
                .generatedJavaDirectory(project, task, OpenApiGenericsTaskSelection.from(task));

        var exception = assertThrows(IllegalStateException.class, directory::get);
        assertTrue(exception.getMessage().contains(config.toString()));
        assertInstanceOf(java.io.IOException.class, exception.getCause());
    }

    private GenerateTask selectedTask(
            Project project, String outputDirectory, String sourceFolder) {

        GenerateTask task = createGenerateTask(project, "generateClient");
        task.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);
        task.getOutputDir()
                .set(
                        project.getLayout()
                                .getBuildDirectory()
                                .dir(outputDirectory)
                                .get()
                                .getAsFile());
        task.getConfigOptions()
                .set(
                        sourceFolder == null
                                ? Map.of()
                                : Map.of(
                                        OpenApiGenericsSourceLayout.SOURCE_FOLDER_OPTION,
                                        sourceFolder));
        return task;
    }

    private Project createProject() {
        return ProjectBuilder.builder().build();
    }

    private GenerateTask createGenerateTask(Project project, String taskName) {
        return project.getTasks().register(taskName, GenerateTask.class).get();
    }
}
