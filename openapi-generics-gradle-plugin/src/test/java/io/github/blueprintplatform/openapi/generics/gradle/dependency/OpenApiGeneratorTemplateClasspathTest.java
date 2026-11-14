package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.jar.JarFile;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class OpenApiGeneratorTemplateClasspathTest {
    @Test
    void usesRunningOfficialRuntimeWithoutResolvingAnotherConsumerArtifact() throws Exception {
        var project = ProjectBuilder.builder().build();
        assertTrue(project.getRepositories().isEmpty());
        var artifact = new OpenApiGeneratorTemplateClasspath().create(project).getSingleFile();
        try (var jar = new JarFile(artifact)) {
            assertNotNull(jar.getJarEntry("Java/model.mustache"));
        }
        assertTrue(project.getConfigurations().isEmpty());
        assertTrue(project.getRepositories().isEmpty());
    }
}
