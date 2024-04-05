package org.awaitility.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JavaVersionDetectorTest {

    @Test
    public void nullJavaVersionDefaultsTo8() {
        assertThat(JavaVersionDetector.getJavaMajorVersion(null), equalTo(8));
    }

    @Test
    public void emptyJavaVersionDefaultsTo8() {
        assertThat(JavaVersionDetector.getJavaMajorVersion(""), equalTo(8));
    }

    @Test
    public void javaVersionFor8() {
        /*
        docker run --rm -it eclipse-temurin:8-jdk bash
        echo 'class Scratch {
            public static void main(String[] args) {
                System.out.println(System.getProperty("java.version"));
            }
        }' > Scratch.java
        javac Scratch.java
        java -cp . Scratch
        */
        assertThat(JavaVersionDetector.getJavaMajorVersion("1.8.0_402"), equalTo(8));
    }

    @Test
    public void javaVersionFor11WithDot() {
        /*
        docker run --rm -it eclipse-temurin:11-jdk jshell
        System.getProperty("java.version")
         */
        assertThat(JavaVersionDetector.getJavaMajorVersion("11.0.22"), equalTo(11));
    }

    @Test
    public void javaVersionFor17WithDot() {
        /*
        docker run --rm -it eclipse-temurin:17-jdk jshell
        System.getProperty("java.version")
         */
        assertThat(JavaVersionDetector.getJavaMajorVersion("17.0.10"), equalTo(17));
    }

    @Test
    public void javaVersionFor21WithoutDot() {
        /*
        Until there is a first patch release, java.version reports a single number without dots.
         */
        assertThat(JavaVersionDetector.getJavaMajorVersion("21"), equalTo(21));
    }

    @Test
    public void javaVersionFor21WithDot() {
        /*
        docker run --rm -it eclipse-temurin:21-jdk jshell
        System.getProperty("java.version")
         */
        assertThat(JavaVersionDetector.getJavaMajorVersion("21.0.2"), equalTo(21));
    }

    @Test
    public void javaVersionFor23ea() {
        assertThat(JavaVersionDetector.getJavaMajorVersion("23-ea"), equalTo(23));
    }

}
