package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGeneratorTemplateArtifactLocatorTest {

    private static final String RESOURCE_PATH =
            "Java/model.mustache";

    private final OpenApiGeneratorTemplateArtifactLocator locator =
            new OpenApiGeneratorTemplateArtifactLocator();

    @TempDir
    Path tempDir;

    @Test
    void locatesDirectoryClasspathRoot() throws Exception {
        Path classpathRoot =
                tempDir.resolve("classes");

        Path resource =
                classpathRoot.resolve(RESOURCE_PATH);

        Files.createDirectories(resource.getParent());
        Files.writeString(
                resource,
                "model-template",
                StandardCharsets.UTF_8);

        try (URLClassLoader classLoader =
                isolatedClassLoader(
                        classpathRoot.toUri().toURL())) {

            assertEquals(
                    classpathRoot.toAbsolutePath().normalize(),
                    locator.locate(
                            classLoader,
                            RESOURCE_PATH));
        }
    }

    @Test
    void locatesOwningJar() throws Exception {
        Path jar =
                createTemplateJar(
                        tempDir.resolve("openapi-generator-test.jar"));

        try (URLClassLoader classLoader =
                isolatedClassLoader(
                        jar.toUri().toURL())) {

            assertEquals(
                    jar.toAbsolutePath().normalize(),
                    locator.locate(
                            classLoader,
                            RESOURCE_PATH));
        }
    }

    @Test
    void rejectsMissingTemplateResource() throws Exception {
        try (URLClassLoader classLoader =
                isolatedClassLoader()) {

            TemplatePreparationException exception =
                    assertThrows(
                            TemplatePreparationException.class,
                            () ->
                                    locator.locate(
                                            classLoader,
                                            RESOURCE_PATH));

            assertTrue(
                    exception.getMessage()
                            .contains("was not found"));
        }
    }

    @Test
    void rejectsTemplateResourceFromMultipleArtifacts() throws Exception {
        Path first =
                createTemplateJar(
                        tempDir.resolve("first.jar"));

        Path second =
                createTemplateJar(
                        tempDir.resolve("second.jar"));

        try (URLClassLoader classLoader =
                isolatedClassLoader(
                        first.toUri().toURL(),
                        second.toUri().toURL())) {

            TemplatePreparationException exception =
                    assertThrows(
                            TemplatePreparationException.class,
                            () ->
                                    locator.locate(
                                            classLoader,
                                            RESOURCE_PATH));

            assertTrue(
                    exception.getMessage()
                            .contains("more than one runtime artifact"));
        }
    }

    @Test
    void preservesClasspathInspectionIoFailureAsExplicitTemplateFailure() {
        IOException failure = new IOException("classpath unavailable");
        ClassLoader loader = new ClassLoader(null) {
            @Override
            public java.util.Enumeration<URL> getResources(String name) throws IOException {
                assertEquals(RESOURCE_PATH, name);
                throw failure;
            }
        };
        var exception = assertThrows(TemplatePreparationException.class,
                () -> locator.locate(loader, RESOURCE_PATH));
        assertTrue(exception.getMessage().contains(RESOURCE_PATH));
        assertSame(failure, exception.getCause());
    }

    @Test
    void rejectsNetworkOwnedTemplatesWithoutDownloadingAnIndependentRuntime() throws Exception {
        for (String resource : java.util.List.of(
                "https://example.invalid/Java/model.mustache",
                "jar:https://example.invalid/generator.jar!/Java/model.mustache")) {
            URL url = java.net.URI.create(resource).toURL();
            ClassLoader loader = new ClassLoader(null) {
                @Override
                public java.util.Enumeration<URL> getResources(String name) {
                    assertEquals(RESOURCE_PATH, name);
                    return java.util.Collections.enumeration(java.util.List.of(url));
                }
            };
            var exception = assertThrows(TemplatePreparationException.class,
                    () -> locator.locate(loader, RESOURCE_PATH));
            assertTrue(exception.getMessage().contains("Unsupported"));
            assertTrue(exception.getMessage().contains("https"));
        }
    }

    private URLClassLoader isolatedClassLoader(URL... urls) {
        return new URLClassLoader(
                urls,
                null);
    }

    private Path createTemplateJar(Path jar) throws IOException {
        try (OutputStream outputStream =
                        Files.newOutputStream(jar);
                JarOutputStream jarOutputStream =
                        new JarOutputStream(outputStream)) {

            jarOutputStream.putNextEntry(
                    new JarEntry(RESOURCE_PATH));

            jarOutputStream.write(
                    "model-template"
                            .getBytes(StandardCharsets.UTF_8));

            jarOutputStream.closeEntry();
        }

        return jar;
    }
}
