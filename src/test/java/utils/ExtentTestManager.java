package utils;

import com.aventstack.extentreports.ExtentTest;

/**
 * Remembers which report entry (ExtentTest) belongs to the test running on the current thread.
 * ThreadLocal keeps this correct even if tests are later run in parallel.
 */
public final class ExtentTestManager {

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentTestManager() {
        // utility class: no objects needed
    }

    public static ExtentTest start(String testName, String description) {
        ExtentTest test = ExtentManager.getInstance().createTest(testName, description);
        CURRENT_TEST.set(test);
        return test;
    }

    /** The report entry of the running test, or null outside a test. */
    public static ExtentTest get() {
        return CURRENT_TEST.get();
    }

    public static void end() {
        CURRENT_TEST.remove();
    }
}
