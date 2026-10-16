package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Locates the runtime artifact that owns an OpenAPI Generator template resource.
 *
 * <p>The official OpenAPI Generator Gradle plugin already carries the matching
 * {@code openapi-generator} runtime on its plugin classpath. Template preparation therefore reuses
 * the artifact that is actually visible to the running plugin instead of resolving a second copy
 * through the consumer project's dependency repositories.
 */
public final class OpenApiGeneratorTemplateArtifactLocator {

    public Path locate(
            ClassLoader classLoader,
            String resourcePath) {

        List<URL> resources =
                findResources(
                        classLoader,
                        resourcePath);

        if (resources.isEmpty()) {
            throw new TemplatePreparationException(
                    "Required OpenAPI Generator template resource '"
                            + resourcePath
                            + "' was not found on the running official plugin classpath.");
        }

        Set<Path> artifactRoots =
                new LinkedHashSet<>();

        for (URL resource : resources) {
            artifactRoots.add(
                    locateArtifactRoot(
                            resource,
                            resourcePath));
        }

        if (artifactRoots.size() != 1) {
            throw new TemplatePreparationException(
                    "Required OpenAPI Generator template resource '"
                            + resourcePath
                            + "' was found in more than one runtime artifact: "
                            + artifactRoots.stream()
                                    .map(Path::toString)
                                    .sorted()
                                    .toList());
        }

        return artifactRoots.iterator().next();
    }

    private List<URL> findResources(
            ClassLoader classLoader,
            String resourcePath) {

        try {
            Enumeration<URL> enumeration =
                    classLoader.getResources(resourcePath);

            List<URL> resources =
                    new ArrayList<>();

            while (enumeration.hasMoreElements()) {
                resources.add(
                        enumeration.nextElement());
            }

            return resources;
        } catch (IOException exception) {
            throw new TemplatePreparationException(
                    "Failed to inspect the running OpenAPI Generator plugin classpath for resource '"
                            + resourcePath
                            + "'.",
                    exception);
        }
    }

    private Path locateArtifactRoot(
            URL resource,
            String resourcePath) {

        return switch (resource.getProtocol()) {
            case "jar" -> locateJarArtifact(resource);
            case "file" -> locateDirectoryRoot(resource, resourcePath);
            default -> throw new TemplatePreparationException(
                    "Unsupported OpenAPI Generator template resource URL protocol '"
                            + resource.getProtocol()
                            + "' for '"
                            + resource
                            + "'.");
        };
    }

    private Path locateJarArtifact(URL resource) {
        try {
            JarURLConnection connection =
                    (JarURLConnection) resource.openConnection();

            connection.setUseCaches(false);

            URL jarFileUrl =
                    connection.getJarFileURL();

            if (!"file".equalsIgnoreCase(jarFileUrl.getProtocol())) {
                throw new TemplatePreparationException(
                        "Unsupported OpenAPI Generator template artifact URL '"
                                + jarFileUrl
                                + "'.");
            }

            return Path.of(jarFileUrl.toURI())
                    .toAbsolutePath()
                    .normalize();
        } catch (IOException
                | URISyntaxException
                | IllegalArgumentException exception) {

            throw new TemplatePreparationException(
                    "Could not determine the OpenAPI Generator artifact that owns template resource '"
                            + resource
                            + "'.",
                    exception);
        }
    }

    private Path locateDirectoryRoot(
            URL resource,
            String resourcePath) {

        try {
            Path resourceFile =
                    Path.of(
                                    new URI(
                                            resource.toString()))
                            .toAbsolutePath()
                            .normalize();

            Path current =
                    resourceFile;

            String[] segments =
                    resourcePath.split("/");

            for (int index = segments.length - 1;
                    index >= 0;
                    index--) {

                Path fileName =
                        current.getFileName();

                if (fileName == null
                        || !segments[index].equals(
                                fileName.toString())) {

                    throw new TemplatePreparationException(
                            "OpenAPI Generator template resource URL '"
                                    + resource
                                    + "' does not end with expected resource path '"
                                    + resourcePath
                                    + "'.");
                }

                current =
                        current.getParent();

                if (current == null
                        && index > 0) {

                    throw new TemplatePreparationException(
                            "Could not determine the classpath root for OpenAPI Generator "
                                    + "template resource '"
                                    + resource
                                    + "'.");
                }
            }

            if (current == null) {
                throw new TemplatePreparationException(
                        "Could not determine the classpath root for OpenAPI Generator "
                                + "template resource '"
                                + resource
                                + "'.");
            }

            return current;
        } catch (URISyntaxException
                | IllegalArgumentException exception) {

            throw new TemplatePreparationException(
                    "Could not determine the classpath root for OpenAPI Generator "
                            + "template resource '"
                            + resource
                            + "'.",
                    exception);
        }
    }
}
