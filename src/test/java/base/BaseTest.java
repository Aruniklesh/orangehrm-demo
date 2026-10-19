package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Parent class for all test classes.
 * It starts a fresh browser before every test and closes it afterwards,
 * so individual tests never repeat that code.
 */
public class BaseTest {

    // protected: child classes (like LoginTest) can use the same browser
    protected WebDriver driver;
    protected Duration explicitWaitTimeout;

    private Properties config;

    @BeforeMethod
    public void setUp() {
        config = loadConfig();
        explicitWaitTimeout = Duration.ofSeconds(
                Long.parseLong(config.getProperty("explicit.wait.seconds")));

        driver = createDriver(config.getProperty("browser"));
        driver.manage().window().maximize();
        driver.get(config.getProperty("base.url"));
    }

    @AfterMethod(alwaysRun = true) // runs even when the test failed
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // closes the browser and ends the driver process
        }
    }

    private WebDriver createDriver(String browserName) {
        switch (browserName.trim().toLowerCase()) {
            case "chrome":
                return new ChromeDriver(); // Selenium Manager downloads the matching driver
            case "edge":
                return new EdgeDriver();
            default:
                throw new IllegalArgumentException(
                        "Unsupported browser '" + browserName + "'. Use 'chrome' or 'edge' in config.properties.");
        }
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        // config.properties is in src/test/resources, which is on the test classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties not found in src/test/resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config.properties", e);
        }
        return properties;
    }
}
