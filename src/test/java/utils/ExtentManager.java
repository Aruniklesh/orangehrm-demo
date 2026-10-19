package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates the one ExtentReports object for the whole test run and writes
 * reports/ExtentReport_{timestamp}.html (a new file for every run).
 */
public final class ExtentManager {

    private static final Logger log = LogManager.getLogger(ExtentManager.class);

    private static ExtentReports extent;

    private ExtentManager() {
        // utility class: no objects needed
    }

    /** Creates the report on first use; every later call returns the same one. */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path reportFile = Paths.get("reports", "ExtentReport_" + timestamp + ".html");

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFile.toString());
            sparkReporter.config().setDocumentTitle("OrangeHRM Login Automation");
            sparkReporter.config().setReportName("OrangeHRM Login Test Report");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            // Environment details shown on the report (never secrets)
            extent.setSystemInfo("Application URL", ConfigReader.getBaseUrl());
            extent.setSystemInfo("Browser", ConfigReader.getBrowser());
            extent.setSystemInfo("Headless", String.valueOf(ConfigReader.isHeadless()));
            extent.setSystemInfo("Java", System.getProperty("java.version"));

            log.info("Extent report will be written to {}", reportFile);
        }
        return extent;
    }

    /** Writes everything collected so far to the HTML file. Safe to call more than once. */
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
