package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;


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

        // -Dheadless=true (used by Jenkins) overrides the value in config.properties
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", config.getProperty("headless", "false")));

        driver = createDriver(config.getProperty("browser"), headless);
        driver.manage().window().maximize();
        driver.get(config.getProperty("base.url"));
    }

    @AfterMethod(alwaysRun = true) // runs even when the test failed
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // closes the browser and ends the driver process
        }
    }

    private WebDriver createDriver(String browserName, boolean headless) {
        switch (browserName.trim().toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    // Headless browsers have no screen, so give them a fixed window size
                    chromeOptions.addArguments("--headless=new", "--window-size=1920,1080");
                }
                return new ChromeDriver(chromeOptions); // Selenium Manager downloads the matching driver
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless=new", "--window-size=1920,1080");
                }
                return new EdgeDriver(edgeOptions);
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
