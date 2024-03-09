package org.awaitility.core;

public final class JavaVersionDetector {
    private static final String JAVA_VERSION = System.getProperty("java.version");

    public static int getJavaMajorVersion() {
        final String normalizedJavaVersion;
        if (JAVA_VERSION == null || JAVA_VERSION.isEmpty()) {
            // Fallback to java 8
            normalizedJavaVersion = "8";
        } else if (JAVA_VERSION.startsWith("1.")) {
            normalizedJavaVersion = JAVA_VERSION.substring(2, 3);
        } else {
            int dot = JAVA_VERSION.indexOf(".");
            if (dot != -1) {
                normalizedJavaVersion = JAVA_VERSION.substring(0, dot);
            } else {
                normalizedJavaVersion = JAVA_VERSION;
            }
        }
        return Integer.parseInt(normalizedJavaVersion);
    }
}
