package org.awaitility.core;

public final class JavaVersionDetector {
    private static final String JAVA_VERSION = System.getProperty("java.version");

    public static int getJavaMajorVersion() {
        return getJavaMajorVersion(JAVA_VERSION);
    }

    static int getJavaMajorVersion(String javaVersion) {
        if (javaVersion == null || javaVersion.isEmpty()) {
            // Fallback to java 8
            return 8;
        }
        if (javaVersion.startsWith("1.")) {
            return Integer.parseInt(javaVersion.substring(2, 3));
        }
        int end = 0;
        while (end < javaVersion.length()) {
            char c = javaVersion.charAt(end);
            if (c < '0' || c > '9') {
                break;
            }
            end++;
        }
        if (end == 0) {
            return 8;
        }
        return Integer.parseInt(javaVersion.substring(0, end));
    }
}
