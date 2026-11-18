package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.OpenApiGenericsCodegenVersionConflictException;
import io.github.blueprintplatform.openapi.generics.gradle.exception.PluginVersionResolutionException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OpenApiGenericsCodegenClasspathTest {
    private static final String MODULE = "io.github.blueprint-platform:openapi-generics-java-codegen";
    @TempDir Path directory;

    @Test
    void provisionsManifestAlignedCodegenOnceWithoutChangingOtherDependencies() throws Exception {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        Dependency otherModule = project.getDependencies().add(extra.getName(), "io.github.blueprint-platform:other:9.0");
        Dependency otherGroup = project.getDependencies().add(extra.getName(), "example:openapi-generics-java-codegen:9.0");
        try (URLClassLoader loader = versionedMarker("1.3.1-SNAPSHOT")) {
            var provisioning = new OpenApiGenericsCodegenClasspath(loader.loadClass(PluginMarker.class.getName()));
            Dependency first = provisioning.register(project, extra);
            assertEquals("1.3.1-SNAPSHOT", first.getVersion());
            assertEquals(OpenApiGenericsCodegenClasspath.OPENAPI_GENERICS_GROUP, first.getGroup());
            assertEquals(OpenApiGenericsCodegenClasspath.OPENAPI_GENERICS_CODEGEN_MODULE, first.getName());
            assertSame(first, provisioning.register(project, extra));
            assertEquals(3, extra.getDependencies().size());
            assertTrue(extra.getDependencies().contains(otherModule));
            assertTrue(extra.getDependencies().contains(otherGroup));
        }
    }

    @Test
    void reusesMatchingConsumerDeclarationAndAcceptsLaterMatchingDeclarations() throws Exception {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        Dependency existing = project.getDependencies().add(extra.getName(), MODULE + ":1.3.1-SNAPSHOT");
        try (URLClassLoader loader = versionedMarker("1.3.1-SNAPSHOT")) {
            var provisioning = new OpenApiGenericsCodegenClasspath(loader.loadClass(PluginMarker.class.getName()));
            assertSame(existing, provisioning.register(project, extra));
            assertDoesNotThrow(() -> project.getDependencies().add(extra.getName(), MODULE + ":1.3.1-SNAPSHOT"));
            assertEquals(1, extra.getDependencies().size());
        }
    }

    @ParameterizedTest(name = "conflicting dependency present before registration: {0}")
    @ValueSource(booleans = {true, false})
    void rejectsConflictingVersionRegardlessOfDeclarationOrder(boolean before) throws Exception {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        try (URLClassLoader loader = versionedMarker("1.3.1-SNAPSHOT")) {
            var provisioning = new OpenApiGenericsCodegenClasspath(loader.loadClass(PluginMarker.class.getName()));
            if (before) project.getDependencies().add(extra.getName(), MODULE + ":1.2.1");
            else provisioning.register(project, extra);
            var dependencies = project.getDependencies();
            String configurationName = extra.getName();
            var exception = before
                    ? assertThrows(OpenApiGenericsCodegenVersionConflictException.class,
                            () -> provisioning.register(project, extra))
                    : assertThrows(OpenApiGenericsCodegenVersionConflictException.class,
                            () -> dependencies.add(configurationName, MODULE + ":1.2.1"));
            assertTrue(exception.getMessage().contains(MODULE + ":1.3.1-SNAPSHOT"));
            assertTrue(exception.getMessage().contains("1.2.1"));
            assertTrue(exception.getMessage().contains(extra.getName()));
        }
    }

    @Test
    void rejectsVersionlessCodegenInsteadOfAllowingIndependentResolution() throws Exception {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        project.getDependencies().add(extra.getName(), MODULE);
        try (URLClassLoader loader = versionedMarker("1.3.1-SNAPSHOT")) {
            var provisioning = new OpenApiGenericsCodegenClasspath(loader.loadClass(PluginMarker.class.getName()));
            var exception = assertThrows(OpenApiGenericsCodegenVersionConflictException.class,
                    () -> provisioning.register(project, extra));
            assertTrue(exception.getMessage().contains("declared version 'null'"));
        }
    }

    @Test
    void failsExplicitlyWithoutImplementationVersionInsteadOfGuessingPlatformVersion() {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        var provisioning = new OpenApiGenericsCodegenClasspath(PluginMarker.class);
        assertThrows(PluginVersionResolutionException.class, () -> provisioning.register(project, extra));
        assertTrue(extra.getDependencies().isEmpty());
    }

    @Test
    void rejectsBlankManifestImplementationVersion() throws Exception {
        Project project = ProjectBuilder.builder().build();
        Configuration extra = project.getConfigurations().create("openApiGeneratorExtra");
        try (URLClassLoader loader = versionedMarker(" ")) {
            var provisioning = new OpenApiGenericsCodegenClasspath(loader.loadClass(PluginMarker.class.getName()));
            assertThrows(PluginVersionResolutionException.class, () -> provisioning.register(project, extra));
            assertTrue(extra.getDependencies().isEmpty());
        }
    }

    private URLClassLoader versionedMarker(String version) throws IOException {
        Path jar = directory.resolve("plugin.jar");
        String resource = PluginMarker.class.getName().replace('.', '/') + ".class";
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        try (var output = new JarOutputStream(Files.newOutputStream(jar), manifest);
             var bytes = PluginMarker.class.getResourceAsStream("/" + resource)) {
            assertNotNull(bytes);
            output.putNextEntry(new JarEntry(resource));
            bytes.transferTo(output);
            output.closeEntry();
        }
        return new URLClassLoader(new URL[] {jar.toUri().toURL()}, null);
    }

    // A resource loaded from a real manifest-bearing JAR, without a second Gradle/plugin runtime.
    public interface PluginMarker {}
}
