package org.awaitility.test.osgi;

import org.awaitility.Awaitility;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the built awaitility jar declares its module name for module-path users (issue 205).
 * Run with: mvn verify -P osgi-tests -pl awaitility-test-support,awaitility,awaitility-osgi-test
 * (the awaitility dependency is the packaged jar in a reactor build at the verify phase).
 */
public class AutomaticModuleNameTest {

    @Test
    public void coreJarManifestHasAutomaticModuleName() throws Exception {
        URL location = Awaitility.class.getProtectionDomain().getCodeSource().getLocation();
        File file = toFile(location);
        assertTrue("Expected awaitility to be loaded from a jar but was " + file, file.isFile());
        try (JarFile jar = new JarFile(file)) {
            Manifest manifest = jar.getManifest();
            assertEquals("org.awaitility", manifest.getMainAttributes().getValue("Automatic-Module-Name"));
        }
    }

    private static File toFile(URL url) throws URISyntaxException {
        return new File(url.toURI());
    }
}
