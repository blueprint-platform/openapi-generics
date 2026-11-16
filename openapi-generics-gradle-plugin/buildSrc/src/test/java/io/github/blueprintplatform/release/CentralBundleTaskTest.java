package io.github.blueprintplatform.release;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import groovy.json.JsonSlurper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.gradle.api.GradleException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CentralBundleTaskTest {
    private static final String FINGERPRINT = "A".repeat(40);
    @TempDir Path directory;
    private Path source;
    private Path evidence;
    private Path staging;
    private CentralBundleTask task;

    @BeforeEach
    void setup() throws Exception {
        source = Files.createDirectory(directory.resolve("source"));
        evidence = directory.resolve("evidence");
        staging = directory.resolve("staging");
        Files.writeString(source.resolve("contract.txt"), "committed contract");
        Files.writeString(source.resolve(".gitignore"), "ignored.txt\n");
        git("init");
        git("add", ".");
        git("-c", "user.name=Fixture", "-c", "user.email=fixture@example.invalid", "commit", "-m", "fixture");
        var project = ProjectBuilder.builder().withProjectDir(Files.createDirectory(directory.resolve("project")).toFile()).build();
        task = project.getTasks().create("releaseFixture", CentralBundleTask.class);
        task.getPhase().set("prepare");
        task.getReleaseEnabled().set(true);
        task.getValidationOnly().set(false);
        task.getSigningFingerprint().set(FINGERPRINT);
        task.getSourceRoot().set(source.toFile());
        task.getStagingDirectory().set(staging.toFile());
        task.getEvidenceDirectory().set(evidence.toFile());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OPENAPI_GENERICS_1_3_0_RELEASE_PLAN.md", "OPENAPI_GENERICS_PLATFORM_UPGRADE_PLAN.md", "new-untracked-source.java"})
    void rejectsEveryNonIgnoredUntrackedFile(String filename) throws Exception {
        Files.writeString(source.resolve(filename), "uncommitted");
        var failure = assertThrows(GradleException.class, task::runReleaseStep);
        assertTrue(failure.getMessage().contains("source and index must be clean"));
        assertFalse(Files.exists(evidence));
    }

    @Test
    void rejectsIndexOnlyChangesEvenWhenWorkingFileMatchesHead() throws Exception {
        Files.writeString(source.resolve("contract.txt"), "staged contract");
        git("add", "contract.txt");
        Files.writeString(source.resolve("contract.txt"), "committed contract");
        assertThrows(GradleException.class, task::runReleaseStep);
    }

    @Test
    void respectsGitIgnoresAndHashesTrackedPlans() throws Exception {
        Files.writeString(source.resolve("development-plan.md"), "tracked plan");
        git("add", "development-plan.md");
        git("-c", "user.name=Fixture", "-c", "user.email=fixture@example.invalid", "commit", "-m", "plan");
        Files.writeString(source.resolve("ignored.txt"), "ignored local state");
        task.runReleaseStep();
        Map<?, ?> before = state();
        assertEquals(false, before.get("dirty"));
        Files.writeString(source.resolve("development-plan.md"), "changed plan");
        task.getValidationOnly().set(true);
        task.runReleaseStep();
        assertEquals(true, state().get("dirty"));
        assertNotEquals(before.get("sourceContentsSha256"), state().get("sourceContentsSha256"));
    }

    @Test
    void emitsOnlyStructuredInvocationAndKeepsValidationBundleNonReleaseReady() throws Exception {
        assumeFalse(System.getProperty("os.name").toLowerCase().contains("windows"));
        task.getValidationOnly().set(true);
        task.runReleaseStep();
        task.getCoordinates().set("example:fixture:1.0");
        Path artifacts = Files.createDirectory(directory.resolve("publication"));
        for (String suffix : List.of(".jar", "-sources.jar", "-javadoc.jar")) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0");
            try (var jar = new JarOutputStream(Files.newOutputStream(artifacts.resolve("fixture-1.0" + suffix)), manifest)) {
                jar.putNextEntry(new JarEntry(suffix.equals("-sources.jar") ? "Fixture.java" : "index.html"));
                jar.write("fixture".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jar.closeEntry();
            }
        }
        Files.writeString(artifacts.resolve("fixture-1.0.pom"), """
                <project><groupId>example</groupId><artifactId>fixture</artifactId><version>1.0</version>
                <name>Fixture</name><description>Fixture</description><url>https://example.invalid</url>
                <licenses>MIT</licenses><developers>Fixture</developers><scm>HEAD</scm></project>
                """);
        Files.writeString(artifacts.resolve("fixture-1.0.module"), """
                {"component":{"group":"example","module":"fixture","version":"1.0"},"variants":[]}
                """);
        Path target = Files.createDirectories(staging.resolve("example/fixture/1.0"));
        try (var files = Files.list(artifacts)) {
            for (Path artifact : files.toList()) {
                String name = artifact.getFileName().toString();
                task.getPublicationFiles().put(name, artifact.toString());
                Files.copy(artifact, target.resolve(name));
                Files.writeString(target.resolve(name + ".asc"), "signature fixture");
                for (var checksum : Map.of(".md5", "MD5", ".sha1", "SHA-1").entrySet()) {
                    Files.writeString(target.resolve(name + checksum.getKey()), HexFormat.of().formatHex(
                            MessageDigest.getInstance(checksum.getValue()).digest(Files.readAllBytes(artifact))));
                }
            }
        }
        // Protocol stub isolates provenance/packaging from cryptographic tooling; not a GPG authenticity test.
        Path verifier = directory.resolve("signature-verifier.sh");
        Files.writeString(verifier, "#!/bin/sh\nprintf '[GNUPG:] VALIDSIG " + FINGERPRINT + "\\n'\n");
        assertTrue(verifier.toFile().setExecutable(true));
        task.getGpgExecutable().set(verifier.toString());
        task.getGpgHome().set("");
        task.getExtensions().getExtraProperties().set("productionCommand", "--password=DO_NOT_RECORD --token=DO_NOT_RECORD");
        task.getProject().getExtensions().getExtraProperties().set("signingPassword", "DO_NOT_RECORD");
        task.getPhase().set("bundle");
        task.runReleaseStep();
        String json = Files.readString(evidence.resolve("bundle/provenance.json"));
        Map<?, ?> provenance = (Map<?, ?>) new JsonSlurper().parseText(json);
        assertFalse(provenance.containsKey("productionCommand"));
        assertFalse(json.contains("DO_NOT_RECORD"));
        assertEquals(Map.of("tool", "gradle", "operation", "central-release-bundle", "releasePublication", true,
                "validationOnly", true), provenance.get("invocation"));
        assertEquals(false, provenance.get("releaseReady"));
        assertTrue(Files.isRegularFile(evidence.resolve("bundle/fixture-1.0-VALIDATION-ONLY-central.zip")));
        Files.writeString(source.resolve("contract.txt"), "changed after preparation");
        assertThrows(GradleException.class, task::runReleaseStep);
        assertFalse(Files.exists(evidence.resolve("bundle/provenance.json")));
    }

    private Map<?, ?> state() throws Exception {
        return (Map<?, ?>) new JsonSlurper().parseText(Files.readString(evidence.resolve("source-before.json")));
    }

    private void git(String... args) throws Exception {
        var command = new java.util.ArrayList<>(List.of("git"));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).directory(source.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);
    }
}
