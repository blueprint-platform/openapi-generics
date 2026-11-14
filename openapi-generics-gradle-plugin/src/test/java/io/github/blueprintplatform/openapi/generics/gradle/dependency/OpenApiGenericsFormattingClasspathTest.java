package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class OpenApiGenericsFormattingClasspathTest {
    @Test
    void registersEmbeddedFormatterRuntimeAsResolvableNonConsumableConfiguration() {
        Project project = ProjectBuilder.builder().build();

        Configuration configuration =
                new OpenApiGenericsFormattingClasspath().register(project);

        assertTrue(configuration.isCanBeResolved());
        assertFalse(configuration.isCanBeConsumed());

        Set<String> coordinates =
                configuration.getDependencies()
                        .stream()
                        .map(
                                dependency ->
                                        dependency.getGroup()
                                                + ":"
                                                + dependency.getName()
                                                + ":"
                                                + dependency.getVersion())
                        .collect(Collectors.toSet());

        assertTrue(
                coordinates.stream()
                        .anyMatch(
                                coordinate ->
                                        coordinate.startsWith(
                                                "com.diffplug.spotless:spotless-lib:")));

        assertTrue(
                coordinates.stream()
                        .anyMatch(
                                coordinate ->
                                        coordinate.startsWith(
                                                "io.github.solven-eu.cleanthat:java:")));

        assertTrue(
                coordinates.stream()
                        .anyMatch(
                                coordinate ->
                                        coordinate.startsWith(
                                                "com.google.googlejavaformat:google-java-format:")));
    }
}
