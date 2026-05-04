package org.awaitility;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AwaitilitySystemPropertyDefaultsTest {

    @Test
    public void defaultTimeoutIsLoadedFromSystemPropertyOnFirstLoad() throws Exception {
        assertForkedScenario("default-timeout", "-Dawaitility.defaultTimeout=PT0.123S");
    }

    @Test
    public void defaultPollIntervalIsLoadedFromSystemPropertyOnFirstLoad() throws Exception {
        assertForkedScenario("default-poll-interval", "-Dawaitility.defaultPollInterval=PT0.234S");
    }

    @Test
    public void defaultPollDelayIsLoadedFromSystemPropertyOnFirstLoad() throws Exception {
        assertForkedScenario("default-poll-delay", "-Dawaitility.defaultPollDelay=PT0.345S");
    }

    @Test
    public void invalidSystemPropertyValuesFallBackToBuiltInDefaults() throws Exception {
        assertForkedScenario(
                "invalid-properties-fallback",
                "-Dawaitility.defaultTimeout=not-a-duration",
                "-Dawaitility.defaultPollInterval=still-not-a-duration",
                "-Dawaitility.defaultPollDelay=nope"
        );
    }

    @Test
    public void resetRestoresPropertyDerivedDefaultsAfterRuntimeMutation() throws Exception {
        assertForkedScenario(
                "reset-restores-property-derived-defaults",
                "-Dawaitility.defaultTimeout=PT0.456S",
                "-Dawaitility.defaultPollInterval=PT0.067S",
                "-Dawaitility.defaultPollDelay=PT0.089S"
        );
    }

    private static void assertForkedScenario(String scenario, String... systemProperties) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        for (String systemProperty : systemProperties) {
            command.add(systemProperty);
        }
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(AwaitilitySystemPropertyDefaultsTestHelper.class.getName());
        command.add(scenario);

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
            output = builder.toString();
        }

        int exitCode = process.waitFor();
        assertEquals("Scenario " + scenario + " failed:\n" + output, 0, exitCode);
    }

    private static String javaExecutable() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Paths.get(System.getProperty("java.home"), "bin", executable).toString();
    }
}
