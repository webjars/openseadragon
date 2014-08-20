
package org.webjars.openseadragon;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

public class BuildTest {

    // Maven settings.xml file to be used for the test project
    private static final String SETTINGS = "/settings.xml";

    /**
     * A simple project to test the availability of the webjars-openseadragon artifact's contents.
     *
     * @throws Exception If the test fails as a result of an exception
     */
    @Test
    public void testBuild() throws Exception {
        final Verifier verifier = getVerifier("sanity-test");

        verifier.displayStreamBuffers();
        verifier.executeGoal("verify");
        verifier.resetStreams();
        verifier.verifyErrorFreeLog();
    }

    /**
     * Returns a <code>Verifier</code> that has been configured to use the test repository along with the test project
     * that was passed in as a variable.
     * <p/>
     *
     * @param testName The test project in which to run the tests
     * @return A configured <code>Verifier</code>
     * @throws IOException If there is a problem with configuration.
     * @throws VerificationException If there is a problem with verification.
     */
    private Verifier getVerifier(final String testName) throws IOException, VerificationException {
        final String name = testName.startsWith("/") ? testName : "/" + testName;
        final File config = ResourceExtractor.simpleExtractResources(getClass(), SETTINGS);
        final File test = ResourceExtractor.simpleExtractResources(getClass(), name);
        final String settings = config.getAbsolutePath();

        // Construct a verifier that will run our integration tests
        final Verifier verifier = new Verifier(test.getAbsolutePath(), settings, true);
        final Properties verProperties = verifier.getVerifierProperties();

        // use.mavenRepoLocal instructs forked tests to use the local repo
        verProperties.setProperty("use.mavenRepoLocal", "true");

        // Set the dynamically assigned Jetty port and a bogus stop key for testing
        verifier.setSystemProperty("jetty.port", System.getProperty("jetty.port"));
        verifier.setSystemProperty("jetty.stop.key", "cough");

        // Do a Maven clean before each test
        verifier.setAutoclean(true);

        return verifier;
    }
}
