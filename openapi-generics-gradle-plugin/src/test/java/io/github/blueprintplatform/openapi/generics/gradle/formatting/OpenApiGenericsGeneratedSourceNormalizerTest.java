package io.github.blueprintplatform.openapi.generics.gradle.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import io.github.blueprintplatform.openapi.generics.gradle.exception.GeneratedSourceNormalizationException;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsGeneratedSourceNormalizerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void removesUnusedImportsAndFormatsGeneratedJava() throws IOException {
        Path generated = temporaryDirectory.resolve("generated");
        Files.createDirectories(generated);

        Path javaFile = generated.resolve("Customer.java");
        Files.writeString(
                javaFile,
                """
                package example;
                import java.util.List;
                import java.util.Set;
                public class Customer{private final List<String> names=List.of();}
                """,
                StandardCharsets.UTF_8);

        OpenApiGenericsGeneratedSourceNormalizer.create(testRuntimeClasspath())
                .normalize(generated);

        String normalized = Files.readString(javaFile, StandardCharsets.UTF_8);
        assertTrue(normalized.contains("import java.util.List;"));
        assertFalse(normalized.contains("import java.util.Set;"));
        assertTrue(normalized.contains("public class Customer {"));
        assertTrue(normalized.endsWith("\n"));

        OpenApiGenericsGeneratedSourceNormalizer.create(testRuntimeClasspath())
                .normalize(generated);

        assertEquals(normalized, Files.readString(javaFile, StandardCharsets.UTF_8));
    }

    @Test
    void ignoresMissingGeneratedDirectory() {
        OpenApiGenericsGeneratedSourceNormalizer.create(testRuntimeClasspath())
                .normalize(temporaryDirectory.resolve("missing"));
    }

    @Test
    void reportsInvalidJavaWithTheFormatterStepAndLeavesSourceUntouched() throws IOException {
        Path source = temporaryDirectory.resolve("Broken.java");
        String malformed = "package example; public class Broken {";
        Files.writeString(source, malformed);

        var normalizer = OpenApiGenericsGeneratedSourceNormalizer.create(testRuntimeClasspath());
        var exception = assertThrows(GeneratedSourceNormalizationException.class,
                () -> normalizer.normalize(temporaryDirectory));
        assertTrue(exception.getMessage().contains("step '"));
        assertTrue(exception.getMessage().contains(source.toString()));
        assertNotNull(exception.getCause());
        assertEquals(malformed, Files.readString(source));
    }

    @Test
    void reportsUnreadableUtf8SourceWithDirectoryAndIoCause() throws IOException {
        Path source = temporaryDirectory.resolve("InvalidEncoding.java");
        Files.write(source, new byte[] {(byte) 0xc3, 0x28});

        var normalizer = OpenApiGenericsGeneratedSourceNormalizer.create(testRuntimeClasspath());
        var exception = assertThrows(GeneratedSourceNormalizationException.class,
                () -> normalizer.normalize(temporaryDirectory));
        assertTrue(exception.getMessage().contains(temporaryDirectory.toString()));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    private Set<File> testRuntimeClasspath() {
        return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .map(File::new)
                .filter(File::isFile)
                .sorted((left, right) -> left.getAbsolutePath().compareTo(right.getAbsolutePath()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
