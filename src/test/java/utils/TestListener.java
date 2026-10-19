package utils;

import base.BaseTest;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG calls these methods automatically when a test starts, passes, fails or is skipped.
 * This is where the report entries, the failure screenshot and the log lines come from.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ExtentTestManager.start(testName, result.getMethod().getDescription());
        log.info("START  {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTestManager.get().pass("Test passed");
        log.info("PASS   {}", result.getMethod().getMethodName());
        ExtentTestManager.end();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable error = result.getThrowable();
        log.error("FAIL   {} - {}", testName, error.getMessage());

        // Runs before @AfterMethod, so the browser is still open and we can see what went wrong
        ExtentTest test = ExtentTestManager.get();
        ScreenshotUtil.Screenshot screenshot = ScreenshotUtil.capture(getDriver(result), testName, "FAILED");
        if (screenshot != null) {
            test.fail(error, MediaEntityBuilder.createScreenCaptureFromBase64String(
                    screenshot.base64(), "Screenshot at failure").build());
        } else {
            test.fail(error);
        }
        ExtentTestManager.end();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        // A test can be skipped before it ever started, so make sure it has a report entry
        ExtentTest test = ExtentTestManager.get() != null
                ? ExtentTestManager.get()
                : ExtentTestManager.start(testName, result.getMethod().getDescription());
        test.skip(result.getThrowable() != null ? result.getThrowable().getMessage() : "Test skipped");
        log.warn("SKIP   {}", testName);
        ExtentTestManager.end();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush(); // writes the HTML file
        log.info("Finished: {} passed, {} failed, {} skipped",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    private org.openqa.selenium.WebDriver getDriver(ITestResult result) {
        Object testInstance = result.getInstance();
        return (testInstance instanceof BaseTest) ? ((BaseTest) testInstance).getDriver() : null;
    }
}
