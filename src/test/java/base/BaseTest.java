package base;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import org.testng.annotations.Listeners;
import utils.ConfigReader;
import utils.ExtentTestManager;
import utils.ScreenshotUtil;
import utils.TestListener;

import java.time.Duration;

@Listeners(TestListener.class) // activates the report/screenshot/log listener for every test class
public class BaseTest {

    // protected: child classes (like LoginTest) can use the same browser
    protected WebDriver driver;
    protected Duration explicitWaitTimeout;

    /** Lets the listener take a failure screenshot of the browser this test is using. */
    public WebDriver getDriver() {
        return driver;
    }

    /** Writes a step to the execution log and to the HTML report. Never pass passwords or secrets. */
    protected void logStep(String message) {
        LogManager.getLogger(getClass()).info(message);
        ExtentTest test = ExtentTestManager.get();
        if (test != null) {
            test.info(message);
        }
    }

    /**
     * Same as logStep, and also saves a screenshot (screenshots/ folder) and shows it in the report.
     * Call it only after the page is ready, e.g. after an assertion or explicit wait has succeeded.
     */
    protected void logStepWithScreenshot(String message, String screenshotName) {
        LogManager.getLogger(getClass()).info(message);
        ExtentTest test = ExtentTestManager.get();
        String testName = (test != null) ? test.getModel().getName() : "test";

        ScreenshotUtil.Screenshot screenshot = ScreenshotUtil.capture(driver, testName, screenshotName);
        if (test == null) {
            return;
        }
        if (screenshot != null) {
            test.info(message, MediaEntityBuilder.createScreenCaptureFromBase64String(
                    screenshot.base64(), screenshotName).build());
        } else {
            test.info(message);
        }
    }

    @BeforeMethod
    public void setUp() {
        explicitWaitTimeout = ConfigReader.getExplicitWaitTimeout();

        // -Dheadless=true (used by Jenkins) overrides the value in config.properties
        boolean headless = ConfigReader.isHeadless();
        driver = createDriver(ConfigReader.getBrowser(), headless);
        if (!headless) {
            driver.manage().window().maximize(); // headless already uses --window-size=1920,1080
        }
        driver.get(ConfigReader.getBaseUrl());
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
}
