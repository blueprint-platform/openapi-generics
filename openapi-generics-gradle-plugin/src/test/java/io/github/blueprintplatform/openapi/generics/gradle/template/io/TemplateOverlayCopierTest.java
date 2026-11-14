package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.blueprintplatform.openapi.generics.gradle.support.TemplateArtifactFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

class TemplateOverlayCopierTest {

    private static final String OVERLAY_ROOT = "META-INF/openapi-generics/templates/";

    private final TemplateOverlayCopier copier = new TemplateOverlayCopier();

    @TempDir
    Path tempDir;

    @Test
    void rejectsTraversalOutsideTemplateRoot() throws IOException {
        Path outside = tempDir.resolve("outside.mustache");
        Files.writeString(outside, "UNCHANGED");
        Files.createDirectories(tempDir.resolve("effective/Java"));
        Path jar = TemplateArtifactFixtures.jar(tempDir.resolve("unsafe.jar"),
                OVERLAY_ROOT + "../../outside.mustache", "MALICIOUS");

        assertThrows(IOException.class,
                () -> copier.copy(List.of(jar), OVERLAY_ROOT, tempDir.resolve("effective/Java")));
        assertEquals("UNCHANGED", Files.readString(outside));
    }

    @ParameterizedTest(name = "rejects unsafe JAR overlay path: {0}")
    @ValueSource(strings = {
            "../escaped.mustache", "nested/../../../escaped.mustache",
            "../Java-sibling/escaped.mustache", ".", "nested/..",
            "..\\escaped.mustache", "nested\\..\\escaped.mustache",
            "\\absolute.mustache", "\\\\server\\share\\escaped.mustache",
            "//server/share/escaped.mustache", "C:/escaped.mustache", "C:escaped.mustache",
            "file\0.mustache"
    })
    void rejectsUnsafeJarEntryPathsBeforeWriting(String relativePath) throws IOException {
        Path destination = Files.createDirectories(tempDir.resolve("effective/Java"));
        Path jar = TemplateArtifactFixtures.jar(tempDir.resolve("unsafe.jar"),
                OVERLAY_ROOT + relativePath, "MALICIOUS");

        IOException failure = assertThrows(IOException.class,
                () -> copier.copy(List.of(jar), OVERLAY_ROOT, destination));

        assertTrue(failure.getMessage().contains("Unsafe template overlay entry"));
        try (var paths = Files.walk(destination)) {
            assertEquals(0, paths.filter(Files::isRegularFile).count());
        }
        assertTrue(Files.notExists(tempDir.resolve("effective/escaped.mustache")));
        assertTrue(Files.notExists(tempDir.resolve("effective/Java-sibling")));
    }

    @ParameterizedTest(name = "rejects absolute entry, inside root={0}")
    @ValueSource(booleans = {true, false})
    void rejectsAbsoluteEntriesInsideOrOutsideTheRoot(boolean inside) throws IOException {
        Path destination = Files.createDirectories(tempDir.resolve("effective/Java"));
        Path existing = (inside ? destination : tempDir).resolve("existing.mustache");
        Files.writeString(existing, "UNCHANGED");
        Path jar = TemplateArtifactFixtures.jar(tempDir.resolve("absolute.jar"),
                OVERLAY_ROOT + existing.toAbsolutePath(), "MALICIOUS");

        IOException failure = assertThrows(IOException.class,
                () -> copier.copy(List.of(jar), OVERLAY_ROOT, destination));

        assertTrue(failure.getMessage().contains("Unsafe template overlay entry"));
        assertEquals("UNCHANGED", Files.readString(existing));
    }

    @ParameterizedTest(name = "normalizes safe relative JAR path: {0}")
    @ValueSource(strings = {"libraries/restclient/nested.mustache", "nested/../model.mustache", "x"})
    void normalizesSafePathsAndPreservesReplacement(String relativePath) throws IOException {
        Path destination = tempDir.resolve("effective/unused/../Java");
        Path target = destination.normalize().resolve(relativePath).normalize();
        Files.createDirectories(target.getParent());
        Files.writeString(target, "OLD");
        Path jar = TemplateArtifactFixtures.jar(tempDir.resolve("safe.jar"),
                OVERLAY_ROOT + relativePath, "NEW");
        Path relativeRoot = Path.of("").toAbsolutePath().relativize(destination);

        assertEquals(1, copier.copy(List.of(jar), OVERLAY_ROOT, relativeRoot));
        assertEquals("NEW", Files.readString(target));
    }

    @ParameterizedTest(name = "rejects existing destination symlink, directory={0}")
    @ValueSource(booleans = {true, false})
    void rejectsSymlinksBelowDestinationRoot(boolean directory) throws IOException {
        Path destination = Files.createDirectories(tempDir.resolve("effective/Java"));
        Path outside = directory ? Files.createDirectory(tempDir.resolve("outside"))
                : Files.writeString(tempDir.resolve("outside.mustache"), "UNCHANGED");
        Path link = Files.createSymbolicLink(destination.resolve("linked"), outside);
        String relative = directory ? "linked/new-directory/escaped.mustache" : "linked";
        Path jar = TemplateArtifactFixtures.jar(tempDir.resolve("symlink.jar"),
                OVERLAY_ROOT + relative, "MALICIOUS");

        IOException failure = assertThrows(IOException.class,
                () -> copier.copy(List.of(jar), OVERLAY_ROOT, destination));

        assertTrue(failure.getMessage().contains("symbolic link"));
        assertTrue(Files.isSymbolicLink(link));
        if (directory) {
            assertTrue(Files.notExists(outside.resolve("new-directory")));
        } else {
            assertEquals("UNCHANGED", Files.readString(outside));
        }
    }

    @Test
    void preservesIgnoredDirectoryAndBlankEntries() throws IOException {
        Path directoryJar = TemplateArtifactFixtures.jar(tempDir.resolve("directory.jar"),
                OVERLAY_ROOT + "../../ignored/", "");
        Path blankJar = TemplateArtifactFixtures.jar(tempDir.resolve("blank.jar"),
                OVERLAY_ROOT + " ", "");
        Path destination = tempDir.resolve("effective/Java");

        assertEquals(0, copier.copy(List.of(directoryJar, blankJar), OVERLAY_ROOT, destination));
        assertTrue(Files.notExists(destination));
        assertTrue(Files.notExists(tempDir.resolve("ignored")));
    }

    @Test
    void copiesOverlayFromJarIntoJavaTemplateRoot() throws IOException {
        Path jar = TemplateArtifactFixtures.codegenJar(tempDir, "WRAPPER");
        Path destination = tempDir.resolve("effective/Java");

        int copied = copier.copy(List.of(jar), OVERLAY_ROOT, destination);

        assertEquals(1, copied);
        assertEquals(
                "WRAPPER",
                Files.readString(destination.resolve("api_wrapper.mustache")));
    }

    @Test
    void copiesOverlayFromDirectoryArtifact() throws IOException {
        Path directory =
                TemplateArtifactFixtures.directoryArtifact(
                        tempDir.resolve("exploded"),
                        OVERLAY_ROOT + "api_wrapper.mustache",
                        "DIRECTORY_WRAPPER");
        Path destination = tempDir.resolve("effective/Java");

        int copied = copier.copy(List.of(directory), OVERLAY_ROOT, destination);

        assertEquals(1, copied);
        assertEquals(
                "DIRECTORY_WRAPPER",
                Files.readString(destination.resolve("api_wrapper.mustache")));
    }

    @ParameterizedTest(name = "nested library overlay from JAR: {0}")
    @ValueSource(booleans = {true, false})
    void preservesLibraryScopedDocumentationOverlayLocation(boolean jar) throws IOException {
        String relative = "libraries/restclient/model_doc.mustache";
        String dispatch = "{{#vendorExtensions.x-api-wrapper}}{{>api_wrapper_doc}}{{/vendorExtensions.x-api-wrapper}}";
        Path artifact = jar
                ? TemplateArtifactFixtures.jar(tempDir.resolve("codegen.jar"), OVERLAY_ROOT + relative, dispatch)
                : TemplateArtifactFixtures.directoryArtifact(tempDir.resolve("exploded"), OVERLAY_ROOT + relative, dispatch);
        Path destination = tempDir.resolve("effective/Java");

        assertEquals(1, copier.copy(List.of(artifact), OVERLAY_ROOT, destination));
        assertEquals(dispatch, Files.readString(destination.resolve(relative)));
        assertTrue(Files.notExists(destination.resolve("model_doc.mustache")));
    }

    @Test
    void returnsZeroWhenOverlayRootIsAbsent() throws IOException {
        Path emptyJar =
                TemplateArtifactFixtures.jar(
                        tempDir.resolve("empty.jar"),
                        "unrelated.txt",
                        "nope");

        int copied =
                copier.copy(
                        List.of(emptyJar),
                        OVERLAY_ROOT,
                        tempDir.resolve("effective/Java"));

        assertEquals(0, copied);
        assertTrue(Files.notExists(tempDir.resolve("effective/Java/api_wrapper.mustache")));
    }
}
