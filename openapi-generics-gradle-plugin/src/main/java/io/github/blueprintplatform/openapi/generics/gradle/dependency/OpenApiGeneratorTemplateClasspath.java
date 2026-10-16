package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.nio.file.Path;

/**
 * Exposes the OpenAPI Generator runtime artifact used to read the upstream Java model template.
 *
 * <p>The official Gradle plugin already carries the matching {@code openapi-generator} runtime.
 * Reusing the artifact visible to the running plugin avoids a second dependency resolution through
 * the consumer project's repositories and guarantees that template preparation sees the same
 * upstream runtime that the official plugin loaded.
 */
public final class OpenApiGeneratorTemplateClasspath {

    private static final String UPSTREAM_JAVA_MODEL_TEMPLATE =
            "Java/model.mustache";

    private final OpenApiGeneratorTemplateArtifactLocator artifactLocator;

    public OpenApiGeneratorTemplateClasspath() {
        this(
                new OpenApiGeneratorTemplateArtifactLocator());
    }

    OpenApiGeneratorTemplateClasspath(
            OpenApiGeneratorTemplateArtifactLocator artifactLocator) {

        this.artifactLocator = artifactLocator;
    }

    public FileCollection create(Project project) {
        Path runtimeArtifact =
                artifactLocator.locate(
                        GenerateTask.class.getClassLoader(),
                        UPSTREAM_JAVA_MODEL_TEMPLATE);

        return project.files(
                runtimeArtifact.toFile());
    }
}
