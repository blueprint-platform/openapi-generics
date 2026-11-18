package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Copies OpenAPI Generics template overlays from artifact directories and JAR files.
 */
public final class TemplateOverlayCopier {

    public int copy(
            List<Path> artifacts,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        int copiedResourceCount = 0;

        for (Path artifact : artifacts) {
            if (Files.isDirectory(artifact)) {
                copiedResourceCount +=
                        copyFromDirectory(
                                artifact,
                                overlayRootPath,
                                destinationRoot);
            } else if (isJar(artifact)) {
                copiedResourceCount +=
                        copyFromJar(
                                artifact,
                                overlayRootPath,
                                destinationRoot);
            }
        }

        return copiedResourceCount;
    }

    private int copyFromDirectory(
            Path artifactRoot,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        Path overlayRoot =
                artifactRoot.resolve(
                        overlayRootPath);

        if (!Files.isDirectory(overlayRoot)) {
            return 0;
        }

        int copiedResourceCount = 0;

        try (var paths = Files.walk(overlayRoot)) {
            List<Path> files =
                    paths.filter(Files::isRegularFile)
                            .toList();

            for (Path source : files) {
                Path destination =
                        destinationRoot.resolve(
                                overlayRoot.relativize(
                                        source));

                copyFile(
                        source,
                        destination);

                copiedResourceCount++;
            }
        }

        return copiedResourceCount;
    }

    private int copyFromJar(
            Path artifact,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        int copiedResourceCount = 0;
        Path root = destinationRoot.toAbsolutePath().normalize();

        try (JarFile jarFile =
                     new JarFile(
                             artifact.toFile())) {

            var entries =
                    jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry =
                        entries.nextElement();

                String entryName =
                        entry.getName();

                String relativePath = entryName.startsWith(overlayRootPath)
                        ? entryName.substring(overlayRootPath.length())
                        : "";
                if (entry.isDirectory() || relativePath.isBlank()) {
                    continue;
                }

                Path destination = resolveJarDestination(root, entryName, relativePath);

                Files.createDirectories(
                        destination.getParent());

                try (InputStream inputStream =
                             jarFile.getInputStream(entry)) {

                    Files.copy(
                            inputStream,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING);
                }

                copiedResourceCount++;
            }
        }

        return copiedResourceCount;
    }

    private Path resolveJarDestination(Path root, String entryName, String relativePath)
            throws IOException {
        Path entryPath;
        try {
            entryPath = Path.of(relativePath);
        } catch (InvalidPathException exception) {
            throw new IOException("Unsafe template overlay entry '" + entryName + "'.", exception);
        }

        // JAR names use '/' regardless of the host OS. Reject Windows path forms
        // explicitly so an artifact cannot become unsafe on another supported host.
        if (entryPath.isAbsolute()
                || relativePath.indexOf('\\') >= 0
                || (relativePath.length() >= 2
                    && Character.isLetter(relativePath.charAt(0))
                    && relativePath.charAt(1) == ':')) {
            throw new IOException("Unsafe template overlay entry '" + entryName + "'.");
        }

        Path destination = root.resolve(entryPath).normalize();
        if (!destination.startsWith(root) || destination.equals(root)) {
            throw new IOException("Unsafe template overlay entry '" + entryName
                    + "': destination must remain inside template root '" + root + "'.");
        }

        // Normalization alone cannot contain an existing symlink below the root.
        Path current = root;
        for (Path segment : root.relativize(destination)) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new IOException("Unsafe template overlay entry '" + entryName
                        + "': destination contains a symbolic link.");
            }
        }
        return destination;
    }

    private void copyFile(
            Path source,
            Path destination)
            throws IOException {

        Files.createDirectories(
                destination.getParent());

        Files.copy(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isJar(Path artifact) {
        Path fileName =
                artifact.getFileName();

        return fileName != null
                && fileName.toString()
                .endsWith(".jar");
    }
}
