package org.awaitility.core;

public final class JavaVersionDetector {
    private static final String JAVA_VERSION = System.getProperty("java.version");

    public static int getJavaMajorVersion() {
        return getJavaMajorVersion(JAVA_VERSION);
    }

    static int getJavaMajorVersion(String javaVersion) {
        final String normalizedJavaVersion;
        if (javaVersion == null || javaVersion.isEmpty()) {
            // Fallback to java 8
            normalizedJavaVersion = "8";
        } else if (javaVersion.startsWith("1.")) {
            normalizedJavaVersion = javaVersion.substring(2, 3);
        } else if (javaVersion.indexOf('.') != -1) {
            normalizedJavaVersion = javaVersion.substring(0, javaVersion.indexOf('.'));
        } else if (javaVersion.indexOf('-') != -1) {
            normalizedJavaVersion = javaVersion.substring(0, javaVersion.indexOf('-'));
        } else {
            normalizedJavaVersion = javaVersion;
        }
        return Integer.parseInt(normalizedJavaVersion);
    }
}
