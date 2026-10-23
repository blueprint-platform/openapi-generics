package io.github.blueprintplatform.openapi.generics.gradle.formatting;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.Provisioner;
import com.diffplug.spotless.java.GoogleJavaFormatStep;
import com.diffplug.spotless.java.RemoveUnusedImportsStep;
import io.github.blueprintplatform.openapi.generics.gradle.exception.GeneratedSourceNormalizationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Normalizes Java sources produced by OpenAPI Generics generation tasks.
 *
 * <p>The normalization contract mirrors the Maven integration: unused imports are removed with
 * the Cleanthat JavaParser engine first, then Google Java Format is applied. Spotless is only the
 * embedded formatting engine; consumers do not need to apply or configure the Spotless Gradle
 * plugin.
 */
public final class OpenApiGenericsGeneratedSourceNormalizer {

    static final String UNUSED_IMPORT_ENGINE = "cleanthat-javaparser-unnecessaryimport";

    private final FormatterStep removeUnusedImports;
    private final FormatterStep googleJavaFormat;

    private OpenApiGenericsGeneratedSourceNormalizer(
            FormatterStep removeUnusedImports,
            FormatterStep googleJavaFormat) {

        this.removeUnusedImports = removeUnusedImports;
        this.googleJavaFormat = googleJavaFormat;
    }

    public static OpenApiGenericsGeneratedSourceNormalizer create(
            Set<File> formatterClasspath) {

        LinkedHashSet<File> stableClasspath =
                formatterClasspath.stream()
                        .map(File::getAbsoluteFile)
                        .sorted(Comparator.comparing(File::getAbsolutePath))
                        .collect(
                                LinkedHashSet::new,
                                LinkedHashSet::add,
                                LinkedHashSet::addAll);

        Provisioner provisioner =
                (withTransitives, coordinates) -> Set.copyOf(stableClasspath);

        return new OpenApiGenericsGeneratedSourceNormalizer(
                RemoveUnusedImportsStep.create(UNUSED_IMPORT_ENGINE, provisioner),
                GoogleJavaFormatStep.create(provisioner));
    }

    public void normalize(Path generatedJavaDirectory) {
        if (!Files.isDirectory(generatedJavaDirectory)) {
            return;
        }

        try (var files = Files.walk(generatedJavaDirectory)) {
            List<Path> javaFiles =
                    files.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(".java"))
                            .sorted(Comparator.comparing(Path::toString))
                            .toList();

            for (Path javaFile : javaFiles) {
                normalizeJavaFile(javaFile);
            }
        } catch (IOException exception) {
            throw new GeneratedSourceNormalizationException(
                    "Failed to normalize OpenAPI Generics generated Java sources under '%s'."
                            .formatted(generatedJavaDirectory),
                    exception);
        }
    }

    private void normalizeJavaFile(Path javaFile) throws IOException {
        String original = Files.readString(javaFile, StandardCharsets.UTF_8);
        String normalized = normalizeLineEndings(original);

        normalized = apply(removeUnusedImports, normalized, javaFile.toFile());
        normalized = apply(googleJavaFormat, normalized, javaFile.toFile());

        if (!normalized.equals(original)) {
            Files.writeString(javaFile, normalized, StandardCharsets.UTF_8);
        }
    }

    private String apply(
            FormatterStep step,
            String input,
            File file) {

        try {
            String output = step.format(input, file);
            return output == null ? input : output;
        } catch (Exception exception) {
            throw new GeneratedSourceNormalizationException(
                    "OpenAPI Generics generated-source normalization failed at step '%s' for '%s'."
                            .formatted(step.getName(), file.getAbsolutePath()),
                    exception);
        }
    }

    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n").replace('\r', '\n');
    }
}
