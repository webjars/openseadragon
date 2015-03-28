
package org.webjars.openseadragon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SanityIT {

    private static Logger LOGGER = LoggerFactory.getLogger(SanityIT.class);

    private static String IMG_LOAD_FAILURE = "Unable to open [object Object]: HTTP 404 attempting to load TileSource";

    private static String TIMEOUT_MESSAGE = "Timed out after 10 seconds";

    // The path to the error message about being unable to load the image tiles
    private static By ERR_MSG_XPATH = By.xpath("//*[@id='contentDiv']/div/div[6]/div/div/div");

    // The path to a component that would only be there after a successful load of the OSD library
    private static By OSD_XPATH = By.xpath("//*[@id='contentDiv']/div/div[1]");

    private static String PHANTOMJS_BINARY;

    /**
     * Check that the PhantomJS binary was installed successfully.
     */
    @BeforeClass
    public static void beforeTest() {
        PHANTOMJS_BINARY = System.getProperty("phantomjs.binary");

        assertNotNull(PHANTOMJS_BINARY);
        assertTrue(new File(PHANTOMJS_BINARY).exists());
    }

    /**
     * Tests the webjar build.
     */
    @Test
    public void testWebjar() {
        final String port = System.getProperty("jetty.port");
        final DesiredCapabilities capabilities = new DesiredCapabilities();

        // Configure our WebDriver to support JavaScript and be able to find the PhantomJS binary
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("takesScreenshot", false);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS_BINARY);

        final WebDriver driver = new PhantomJSDriver(capabilities);
        final String baseURL = "http://localhost:" + port;

        if (port == null) {
            fail("System property 'jetty.port' is not set");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Testing {}", driver.getCurrentUrl());
        }

        // If the referenced JavaScript files fail to load, the test fails at this point
        driver.navigate().to(baseURL + "/index.html");

        try {
            final ExpectedCondition<WebElement> condition = ExpectedConditions.presenceOfElementLocated(ERR_MSG_XPATH);
            final String message = new WebDriverWait(driver, 10).until(condition).getText();

            // Look for an error message on the page and report a test failure if it's found
            if (message.equals(IMG_LOAD_FAILURE)) {
                fail("Failed to load image tiles");
            } else if (LOGGER.isDebugEnabled() && !(message.trim()).equals("")) {
                LOGGER.debug("Openseadragon console message: " + message);
            }
        } catch (final WebDriverException details) {
            // If we fail to find an error message, that means the image was loaded successfully
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Successfully loaded image tiles on page load");
            }
        }

        // But confirm the lack of an OSD error message wasn't because OSD itself failed to load
        try {
            new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(OSD_XPATH));
        } catch (final WebDriverException details) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("WebDriver exception message: " + details.getMessage());
            }

            if (details.getMessage().contains(TIMEOUT_MESSAGE)) {
                fail("Failed to find Openseadragon generated content loaded on the page");
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully loaded: " + driver.getTitle());
        }

        driver.quit();
    }
}
