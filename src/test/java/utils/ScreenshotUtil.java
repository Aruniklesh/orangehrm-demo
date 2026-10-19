package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Takes a screenshot of the browser, saves it as a PNG in the screenshots/ folder,
 * and also returns it as base64 text so the report can embed it.
 */
public final class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);
    private static final Path SCREENSHOT_FOLDER = Paths.get("screenshots");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /** The saved file and the same image as base64 text (for the HTML report). */
    public record Screenshot(Path file, String base64) {
    }

    private ScreenshotUtil() {
        // utility class: no objects needed
    }

    /**
     * Saves screenshots/{testName}_{stepName}_{timestamp}.png
     *
     * @return the screenshot, or null if it could not be taken (a screenshot problem
     *         must never hide the real test result)
     */
    public static Screenshot capture(WebDriver driver, String testName, String stepName) {
        if (driver == null) {
            log.warn("Screenshot '{}' skipped: no browser is open", stepName);
            return null;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            Files.createDirectories(SCREENSHOT_FOLDER);
            String fileName = safe(testName) + "_" + safe(stepName) + "_" + LocalDateTime.now().format(TIMESTAMP) + ".png";
            Path file = SCREENSHOT_FOLDER.resolve(fileName);
            Files.write(file, png);

            log.info("Screenshot saved: {}", file);
            return new Screenshot(file, Base64.getEncoder().encodeToString(png));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not take screenshot '{}': {}", stepName, e.getMessage());
            return null;
        }
    }

    /** Keeps file names safe: letters, digits, dot, dash and underscore only. */
    private static String safe(String text) {
        return text.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
