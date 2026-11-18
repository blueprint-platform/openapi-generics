package io.github.blueprintplatform.release;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GradleVersion;
import org.gradle.work.DisableCachingByDefault;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** Repository build tooling only; never included in the published plugin. No upload code. */
@DisableCachingByDefault(because = "Release provenance and signatures must be verified on every invocation")
public abstract class CentralBundleTask extends DefaultTask {
    private static final String GAV_SEGMENT = "[A-Za-z0-9_.-]+";
    private static final String BUNDLE = "bundle";
    private static final String MD5_SUFFIX = ".md5";
    private static final String SHA1_SUFFIX = ".sha1";
    private static final String SHA256_SUFFIX = ".sha256";
    private static final String SHA512_SUFFIX = ".sha512";
    private static final String JAR_SUFFIX = ".jar";
    private static final String SOURCES_SUFFIX = "-sources.jar";
    private static final String JAVADOC_SUFFIX = "-javadoc.jar";
    private static final String POM_SUFFIX = ".pom";
    private static final String MODULE_SUFFIX = ".module";
    private static final String SIGNATURE_SUFFIX = ".asc";
    private static final List<String> CHECKSUM_SUFFIXES = List.of(MD5_SUFFIX, SHA1_SUFFIX, SHA256_SUFFIX, SHA512_SUFFIX);

    @Input public abstract Property<String> getPhase();
    @Input public abstract Property<String> getCoordinates();
    @Input public abstract Property<Boolean> getReleaseEnabled();
    @Input public abstract Property<Boolean> getValidationOnly();
    @Input public abstract Property<String> getSigningFingerprint();
    @Input public abstract Property<String> getGpgExecutable();
    @Input public abstract Property<String> getGpgHome();
    // All files and repository state are inspected explicitly, with no up-to-date/cache bypass.
    @Internal public abstract MapProperty<String, String> getPublicationFiles();
    @Internal public abstract DirectoryProperty getSourceRoot();
    @Internal public abstract DirectoryProperty getStagingDirectory();
    @Internal public abstract DirectoryProperty getEvidenceDirectory();

    // Gradle cannot instantiate the task through an unannotated protected constructor.
    @SuppressWarnings("java:S5993")
    public CentralBundleTask() {
        getOutputs().upToDateWhen(task -> false);
    }

    @TaskAction
    public void runReleaseStep()
            throws IOException, InterruptedException, NoSuchAlgorithmException, ParserConfigurationException, SAXException {
        require(Set.of("prepare", "validate", BUNDLE).contains(getPhase().get()), "Unknown release task phase");
        boolean releaseEnabled = getReleaseEnabled().get();
        require(releaseEnabled, "Central staging/bundling requires -PreleasePublication=true");
        String fingerprint = getSigningFingerprint().get().toUpperCase();
        require(fingerprint.matches("[0-9A-F]{40}|[0-9A-F]{64}"),
                "An explicit full releaseSigningFingerprint is required (not a short key ID)");
        Path evidence = getEvidenceDirectory().get().getAsFile().toPath();
        Map<String, Object> source = sourceState();
        boolean validationOnly = getValidationOnly().get();
        require(validationOnly || Boolean.FALSE.equals(source.get("dirty")),
                "Release source and index must be clean, including all non-ignored untracked files. "
                        + "Disposable tests may use -PreleaseValidationOnly=true; their bundle is NOT release-ready.");
        if (getPhase().get().equals("prepare")) {
            deleteOwnedDirectory(getStagingDirectory().get().getAsFile().toPath());
            deleteOwnedDirectory(evidence);
            Files.createDirectories(evidence);
            writeJson(evidence.resolve("source-before.json"), source);
            return;
        }
        Path output = evidence.resolve(BUNDLE);
        // Invalidate previous success evidence before validating potentially modified staging.
        deleteOwnedDirectory(output);
        Map<?, ?> before = jsonObject(evidence.resolve("source-before.json"));
        require(before.equals(source), "Source commit/tree/content changed since release staging preparation");
        validateStaging(output, source, fingerprint, validationOnly);
    }

    private void validateStaging(Path output, Map<String, Object> source, String fingerprint, boolean validationOnly)
            throws IOException, InterruptedException, NoSuchAlgorithmException, ParserConfigurationException, SAXException {
        String[] gav = getCoordinates().get().split(":", -1);
        require(gav.length == 3 && gav[0].matches(GAV_SEGMENT)
                        && gav[1].matches(GAV_SEGMENT) && gav[2].matches(GAV_SEGMENT)
                        && !gav[2].endsWith("-SNAPSHOT"), "Invalid release GAV");
        String relativeRoot = gav[0].replace('.', '/') + "/" + gav[1] + "/" + gav[2] + "/";
        Path staging = getStagingDirectory().get().getAsFile().toPath();
        require(Files.isDirectory(staging, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(staging),
                "Missing or symlink staging directory");
        Map<String, Path> payload = new TreeMap<>();
        for (var item : getPublicationFiles().get().entrySet()) {
            require(!item.getKey().contains("/") && !item.getKey().contains("\\"), "Invalid publication filename");
            require(item.getKey().startsWith(gav[1] + "-" + gav[2] + ".")
                            || item.getKey().startsWith(gav[1] + "-" + gav[2] + "-"),
                    "Unexpected artifact identity: " + item.getKey());
            require(payload.put(relativeRoot + item.getKey(), Path.of(item.getValue())) == null,
                    "Duplicate publication filename");
        }
        String prefix = relativeRoot + gav[1] + "-" + gav[2];
        for (String extension : List.of(JAR_SUFFIX, SOURCES_SUFFIX, JAVADOC_SUFFIX, POM_SUFFIX, MODULE_SUFFIX)) {
            require(payload.containsKey(prefix + extension), "Required publication output missing: " + extension);
        }
        Map<String, Path> actual = scanStaging(staging, payload);
        Map<String, String> validatedContents = inventory(actual);
        Map<String, String> signers = validatePayload(payload, actual, fingerprint);
        validatePom(actual.get(prefix + POM_SUFFIX), gav);
        validateModule(actual.get(prefix + MODULE_SUFFIX), actual, relativeRoot, gav);
        validateJars(actual, prefix, gav[2]);
        require(source.equals(sourceState()), "Source changed during bundle validation");
        require(validatedContents.equals(inventory(actual)), "Staging changed during validation");
        writeEvidence(output, source, fingerprint, signers, validatedContents, validationOnly);
        if (getPhase().get().equals(BUNDLE)) {
            writeBundle(output, gav, actual, validatedContents, source, validationOnly);
        }
    }

    private static Map<String, Path> scanStaging(Path staging, Map<String, Path> payload) throws IOException {
        Set<String> permitted = new TreeSet<>();
        for (String name : payload.keySet()) {
            permitted.add(name);
            permitted.add(name + SIGNATURE_SUFFIX);
            for (String suffix : CHECKSUM_SUFFIXES) permitted.add(name + suffix);
        }
        Map<String, Path> actual = new TreeMap<>();
        try (var paths = Files.walk(staging)) {
            for (Path path : paths.toList()) {
                require(!Files.isSymbolicLink(path), "Symlink in staging: " + path);
                if (path.equals(staging)) continue;
                String name = staging.relativize(path).toString().replace('\\', '/');
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    require(payload.keySet().stream().anyMatch(key -> key.startsWith(name + "/")),
                            "Unexpected staging directory (marker/other version): " + name);
                } else {
                    require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && permitted.contains(name),
                            "Unexpected staging file (including repository metadata): " + name);
                    actual.put(name, path);
                }
            }
        }
        return actual;
    }

    private Map<String, String> validatePayload(Map<String, Path> payload, Map<String, Path> actual, String fingerprint)
            throws IOException, InterruptedException, NoSuchAlgorithmException {
        Map<String, String> signers = new TreeMap<>();
        for (var item : payload.entrySet()) {
            String name = item.getKey();
            Path file = actual.get(name);
            require(file != null && Files.size(file) > 0, "Missing/empty artifact: " + name);
            require(Files.isRegularFile(item.getValue(), LinkOption.NOFOLLOW_LINKS)
                            && !Files.isSymbolicLink(item.getValue())
                            && sha256(file).equals(sha256(item.getValue())),
                    "Staged artifact differs from generated publication output: " + name);
            Path signature = actual.get(name + SIGNATURE_SUFFIX);
            require(signature != null && Files.size(signature) > 0, "Missing signature: " + name);
            signers.put(name, verifySignature(file, signature, fingerprint));
            for (String suffix : CHECKSUM_SUFFIXES) {
                Path sidecar = actual.get(name + suffix);
                if (sidecar == null) {
                    require(!Set.of(MD5_SUFFIX, SHA1_SUFFIX).contains(suffix),
                            "Missing required checksum: " + name + suffix);
                } else {
                    String expected = Files.readString(sidecar).trim().toLowerCase(Locale.ROOT);
                    require(centralRepositoryChecksumMatches(file, suffix, expected),
                            "Checksum mismatch: " + name + suffix);
                }
            }
        }
        return signers;
    }

    private static void validateJars(Map<String, Path> actual, String prefix, String version) throws IOException {
        for (String suffix : List.of(JAR_SUFFIX, SOURCES_SUFFIX, JAVADOC_SUFFIX)) {
            try (ZipFile jar = new ZipFile(actual.get(prefix + suffix).toFile())) {
                require(jar.size() > 0, "Empty JAR: " + suffix);
                if (suffix.equals(SOURCES_SUFFIX)) require(jar.stream().anyMatch(e -> e.getName().endsWith(".java")),
                        "Sources JAR contains no Java sources");
                if (suffix.equals(JAVADOC_SUFFIX)) require(jar.stream().anyMatch(e -> e.getName().endsWith(".html")),
                        "Javadoc JAR contains no HTML documentation");
                if (suffix.equals(JAR_SUFFIX)) {
                    var entry = jar.getEntry("META-INF/MANIFEST.MF");
                    require(entry != null, "Missing implementation manifest");
                    try (var input = jar.getInputStream(entry)) {
                        var manifest = new java.util.jar.Manifest(input);
                        require(version.equals(manifest.getMainAttributes().getValue("Implementation-Version")),
                                "Implementation-Version does not match release GAV");
                    }
                }
            }
        }
    }

    private void writeEvidence(Path output, Map<String, Object> source, String fingerprint,
                               Map<String, String> signers, Map<String, String> inventory, boolean validationOnly)
            throws IOException {
        Files.createDirectories(output);
        StringBuilder sums = new StringBuilder();
        inventory.forEach((name, sha) -> sums.append(sha).append("  ").append(name).append('\n'));
        Files.writeString(output.resolve("SHA256SUMS"), sums);
        Map<String, Object> provenance = new LinkedHashMap<>();
        provenance.put("source", source);
        provenance.put("releaseReady", !validationOnly);
        provenance.put("gav", getCoordinates().get());
        provenance.put("jdk", System.getProperty("java.version"));
        provenance.put("jdkVendor", System.getProperty("java.vendor"));
        provenance.put("gradle", GradleVersion.current().getVersion());
        provenance.put("signingFingerprint", fingerprint);
        provenance.put("verifiedSigningFingerprints", signers);
        // Describe this upload-free operation, never a raw command line, environment or property map.
        provenance.put("invocation", Map.of(
                "tool", "gradle",
                "operation", "central-release-" + getPhase().get(),
                "releasePublication", true,
                "validationOnly", validationOnly));
        provenance.put("files", inventory);
        writeJson(output.resolve("provenance.json"), provenance);
    }

    private void writeBundle(Path output, String[] gav, Map<String, Path> actual, Map<String, String> inventory,
                             Map<String, Object> source, boolean validationOnly)
            throws IOException, InterruptedException, NoSuchAlgorithmException {
        String filename = gav[1] + "-" + gav[2]
                + (validationOnly ? "-VALIDATION-ONLY" : "") + "-central.zip";
        Path zip = output.resolve(filename);
        try (ZipOutputStream stream = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (var item : actual.entrySet()) {
                byte[] bytes = Files.readAllBytes(item.getValue());
                require(sha256(bytes).equals(inventory.get(item.getKey())),
                        "Staging changed during packaging: " + item.getKey());
                ZipEntry entry = new ZipEntry(item.getKey());
                entry.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
                stream.putNextEntry(entry);
                stream.write(bytes);
                stream.closeEntry();
            }
        }
        require(inventory.equals(inventory(actual)) && source.equals(sourceState()),
                "Staging/source changed during packaging");
        Files.writeString(output.resolve(filename + SHA256_SUFFIX), sha256(zip) + "  " + filename + "\n");
        getLogger().lifecycle("Local Central bundle: {} ({} files, releaseReady={})", zip, actual.size(),
                !validationOnly);
    }

    private Map<String, Object> sourceState() throws IOException, InterruptedException, NoSuchAlgorithmException {
        Path root = getSourceRoot().get().getAsFile().toPath();
        String commit = command(root, List.of("git", "rev-parse", "HEAD")).trim();
        String tree = command(root, List.of("git", "rev-parse", "HEAD^{tree}")).trim();
        String status = command(root, List.of("git", "status", "--porcelain=v1", "--untracked-files=all", "-z"));
        Map<String, String> contents = new TreeMap<>();
        String files = command(root, List.of("git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"));
        for (String file : files.split("\0")) {
            if (file.isEmpty()) continue;
            Path path = root.resolve(file);
            String content;
            if (Files.isSymbolicLink(path)) {
                content = "symlink:" + Files.readSymbolicLink(path);
            } else {
                content = Files.exists(path) ? sha256(path) : "deleted";
            }
            contents.put(file, content);
        }
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("commit", commit);
        state.put("tree", tree);
        state.put("dirty", !status.isEmpty());
        state.put("sourceContentsSha256", sha256(JsonOutput.toJson(contents).getBytes(StandardCharsets.UTF_8)));
        return state;
    }

    private String verifySignature(Path file, Path signature, String expected) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>(List.of(getGpgExecutable().get(), "--batch", "--no-auto-key-retrieve"));
        if (!getGpgHome().get().isBlank()) args.addAll(List.of("--homedir", getGpgHome().get()));
        args.addAll(List.of("--status-fd=1", "--verify", signature.toString(), file.toString()));
        String status = command(file.getParent(), args);
        for (String line : status.split("\n")) {
            if (line.startsWith("[GNUPG:] VALIDSIG ")) {
                String[] fields = line.substring("[GNUPG:] VALIDSIG ".length()).trim().split(" +");
                require(expected.equalsIgnoreCase(fields[0]) || expected.equalsIgnoreCase(fields[fields.length - 1]),
                        "Signature fingerprint mismatch: " + file);
                return fields[0];
            }
        }
        throw new GradleException("No valid GPG signature: " + file);
    }

    private static void validatePom(Path path, String[] gav) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Element root = factory.newDocumentBuilder().parse(path.toFile()).getDocumentElement();
        for (int i = 0; i < 3; i++) {
            require(gav[i].equals(text(root, List.of("groupId", "artifactId", "version").get(i))), "POM GAV mismatch");
        }
        for (String field : List.of("name", "description", "url", "licenses", "developers", "scm")) {
            require(!text(root, field).isBlank(), "Missing Central POM metadata: " + field);
        }
    }

    private static String text(Element root, String name) {
        for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && element.getTagName().equals(name)) {
                return element.getTextContent().trim();
            }
        }
        return "";
    }

    private static void validateModule(Path module, Map<String, Path> actual, String directory, String[] gav) throws IOException, NoSuchAlgorithmException {
        Map<?, ?> data = jsonObject(module);
        Map<?, ?> component = (Map<?, ?>) data.get("component");
        require(component != null && gav[0].equals(component.get("group"))
                && gav[1].equals(component.get("module")) && gav[2].equals(component.get("version")), "Module GAV mismatch");
        require(data.get("variants") instanceof List<?>, "Missing module variants");
        for (Object value : (List<?>) data.get("variants")) {
            Map<?, ?> variant = (Map<?, ?>) value;
            List<?> files = variant.get("files") instanceof List<?> list ? list : List.of();
            for (Object entry : files) {
                Map<?, ?> file = (Map<?, ?>) entry;
                String url = (String) file.get("url");
                Path artifact = actual.get(directory + url);
                require(url != null && !url.contains("/") && artifact != null, "Invalid module artifact reference");
                require(((Number) file.get("size")).longValue() == Files.size(artifact), "Module artifact size mismatch");
                for (String suffix : CHECKSUM_SUFFIXES) {
                    String field = suffix.substring(1);
                    if (file.containsKey(field)) require(centralRepositoryChecksumMatches(artifact, suffix, file.get(field)),
                            "Module artifact checksum mismatch: " + url);
                }
            }
        }
    }

    private static Map<String, String> inventory(Map<String, Path> files) throws IOException, NoSuchAlgorithmException {
        Map<String, String> result = new TreeMap<>();
        for (var entry : files.entrySet()) result.put(entry.getKey(), sha256(entry.getValue()));
        return result;
    }

    private static Map<?, ?> jsonObject(Path path) throws IOException {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS), "Missing release state/metadata: " + path);
        return (Map<?, ?>) new JsonSlurper().parseText(Files.readString(path));
    }

    private static void writeJson(Path file, Object data) throws IOException {
        Files.writeString(file, JsonOutput.prettyPrint(JsonOutput.toJson(data)) + "\n");
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        return sha256(Files.readAllBytes(file));
    }

    private static String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    // Repository-format equality only. Authenticity is verified by GPG; source,
    // artifact, inventory and ZIP integrity checks always use the SHA-256 path above.
    private static boolean centralRepositoryChecksumMatches(Path file, String suffix, Object expected) throws IOException, NoSuchAlgorithmException {
        return switch (suffix) {
            case MD5_SUFFIX, SHA1_SUFFIX -> legacyCentralChecksumMatches(file, suffix, expected);
            case SHA256_SUFFIX -> sha256(file).equals(expected);
            case SHA512_SUFFIX -> HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-512").digest(Files.readAllBytes(file))).equals(expected);
            default -> throw new GradleException("Unsupported Central checksum sidecar: " + suffix);
        };
    }

    // Central requires these sidecars. GPG verifies signatures and SHA-256 verifies artifact bytes.
    @SuppressWarnings("java:S4790")
    private static boolean legacyCentralChecksumMatches(Path file, String suffix, Object expected) throws IOException, NoSuchAlgorithmException {
        MessageDigest checksum = switch (suffix) {
            // Central repository compatibility checksum; not used for security/authenticity.
            // codeql[java/potentially-weak-cryptographic-algorithm]
            case MD5_SUFFIX -> MessageDigest.getInstance("MD5");
            // Central repository compatibility checksum; not used for security/authenticity.
            // codeql[java/potentially-weak-cryptographic-algorithm]
            case SHA1_SUFFIX -> MessageDigest.getInstance("SHA-1");
            default -> throw new GradleException("Unsupported legacy Central checksum sidecar: " + suffix);
        };
        return HexFormat.of().formatHex(checksum.digest(Files.readAllBytes(file))).equals(expected);
    }

    private static String command(Path directory, List<String> args) throws IOException, InterruptedException {
        // NIO atomically creates a unique file with restrictive default permissions.
        @SuppressWarnings("java:S5443")
        Path log = Files.createTempFile("central-release-command-", ".log");
        try {
            Process process = new ProcessBuilder(args).directory(directory.toFile()).redirectErrorStream(true)
                    .redirectOutput(log.toFile()).start();
            if (!process.waitFor(Duration.ofSeconds(30).toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new GradleException("Local release command timed out: " + args.get(0));
            }
            String output = Files.readString(log);
            require(process.exitValue() == 0, "Local release command failed: " + args.get(0) + "\n" + output);
            return output;
        } finally {
            Files.deleteIfExists(log);
        }
    }

    private static void deleteOwnedDirectory(Path path) throws IOException {
        require(!Files.isSymbolicLink(path), "Release output root must not be a symlink: " + path);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
        try (var paths = Files.walk(path)) {
            for (Path file : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GradleException(message);
    }
}
