package io.github.blueprintplatform.openapi.generics.gradle.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

/** Test-only, opt-in collection from the JVM that actually executes the plugin. */
public final class NestedCoverage {
    private static final Map<String, Session> SESSIONS = new HashMap<>();

    private NestedCoverage() {}

    public static BuildResult build(GradleRunner runner) {
        return execute(runner, GradleVersion.current().getVersion(), false);
    }

    public static BuildResult buildAndFail(GradleRunner runner) {
        return execute(runner, GradleVersion.current().getVersion(), true);
    }

    public static BuildResult build(GradleRunner runner, String version) {
        return execute(runner, version, false);
    }

    private static synchronized BuildResult execute(GradleRunner runner, String version, boolean failure) {
        if (System.getProperty("nestedCoverage.agent") == null) {
            return failure ? runner.buildAndFail() : runner.build();
        }
        try {
            Session session = SESSIONS.get(version);
            if (session == null) {
                session = new Session(version);
                SESSIONS.put(version, session);
            }
            session.configure(runner);
            return buildWithCoverage(runner, failure, session);
        } catch (IOException ex) {
            throw new UncheckedIOException("Nested coverage collection failed", ex);
        }
    }

    private static BuildResult buildWithCoverage(GradleRunner runner, boolean failure, Session session)
            throws IOException {
        try (var dump = (java.io.Closeable) session::dump) {
            return failure ? runner.buildAndFail() : runner.build();
        }
    }

    private static final class Session {
        private final ServerSocket listener;
        private final String id = UUID.randomUUID().toString();
        private final Path output;
        private final Path testKit;
        private Socket connection;
        private RemoteControlReader reader;
        private RemoteControlWriter writer;
        private int invocation;

        Session(String version) throws IOException {
            output = Path.of(System.getProperty("nestedCoverage.output"));
            Files.createDirectories(output);
            Path root = Path.of(System.getProperty("nestedCoverage.testKit"));
            Files.createDirectories(root);
            testKit = root.resolve(version);
            Files.createDirectories(testKit);
            listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
            listener.setSoTimeout(10000);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (connection != null) connection.close();
                    listener.close();
                } catch (IOException ignored) {
                    // Collection has already completed; no data is written at JVM shutdown.
                }
            }, "nested-coverage-close"));
        }

        void configure(GradleRunner runner) throws IOException {
            Properties properties = new Properties();
            Path file = runner.getProjectDir().toPath().resolve("gradle.properties");
            if (Files.exists(file)) {
                try (var input = Files.newInputStream(file)) {
                    properties.load(input);
                }
            }
            String jvmArgs = properties.getProperty("org.gradle.jvmargs", "-Xmx512m -XX:MaxMetaspaceSize=384m");
            String agent = "\"-javaagent:" + System.getProperty("nestedCoverage.agent")
                    + "=output=tcpclient,address=127.0.0.1,port=" + listener.getLocalPort()
                    + ",sessionid=" + id + ",dumponexit=false,includes=io.github.blueprintplatform.openapi.generics.gradle.*\"";
            var arguments = new ArrayList<>(runner.getArguments());
            if (arguments.stream().anyMatch(it -> it.startsWith("-Dorg.gradle.jvmargs="))) {
                throw new IOException("Coverage must not replace an explicit runner JVM argument configuration");
            }
            arguments.add("-Dorg.gradle.jvmargs=" + jvmArgs + " " + agent);
            runner.withArguments(arguments).withTestKitDir(testKit.toFile());
        }

        void dump() throws IOException {
            if (connection == null) {
                connection = listener.accept();
                connection.setSoTimeout(10000);
                reader = new RemoteControlReader(connection.getInputStream());
                writer = new RemoteControlWriter(connection.getOutputStream());
            }
            var data = new ExecFileLoader();
            reader.setSessionInfoVisitor(data.getSessionInfoStore());
            reader.setExecutionDataVisitor(data.getExecutionDataStore());
            writer.visitDumpCommand(true, false);
            writer.flush();
            reader.read();
            if (data.getSessionInfoStore().getInfos().stream().noneMatch(it -> id.equals(it.getId()))
                    || data.getExecutionDataStore().getContents().isEmpty()) {
                throw new IOException("Missing or foreign nested coverage session: " + id);
            }
            var coverage = new CoverageBuilder();
            new Analyzer(data.getExecutionDataStore(), coverage)
                    .analyzeAll(Path.of(System.getProperty("nestedCoverage.classes")).toFile());
            if (!coverage.getNoMatchClasses().isEmpty()) {
                throw new IOException("Nested plugin class IDs do not match report classes: "
                        + coverage.getNoMatchClasses());
            }
            data.save(output.resolve(id + "-" + (++invocation) + ".exec").toFile(), false);
        }
    }
}
