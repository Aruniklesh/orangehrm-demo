package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the OrangeHRM login page.
 * Also contains the two post-login actions this project needs (checking the
 * Dashboard and logging out), to keep the project to a single page class.
 * Locators were inspected on the real site.
 */
public class LoginPage {

    // ----- Locators (verified on the live site) -----
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By LOGIN_HEADING = By.cssSelector(".orangehrm-login-title");
    private static final By ERROR_MESSAGE = By.cssSelector(".oxd-alert-content-text");
    private static final By DASHBOARD_HEADING = By.cssSelector(".oxd-topbar-header-breadcrumb-module");
    private static final By USER_MENU = By.cssSelector(".oxd-userdropdown-tab");
    private static final By LOGOUT_LINK = By.linkText("Logout");

    private final WebDriverWait wait;

    public LoginPage(WebDriver driver, Duration explicitWaitTimeout) {
        // The wait polls the page until a condition is true or the timeout expires
        this.wait = new WebDriverWait(driver, explicitWaitTimeout);
    }

    // ----- Actions -----
    public void enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
    }

    public void enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT)).sendKeys(password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();
    }

    /** The full login steps in one place, so tests never repeat them. */
    public void loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(USER_MENU)).click();
        // The menu fades in, so wait until the link is clickable
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
    }

    // ----- Checks (return values; the test class does the asserting) -----
    public boolean isLoginPageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_HEADING)).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isDashboardDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_HEADING)).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /** Waits for the red error banner and returns its text, e.g. "Invalid credentials". */
    public String getLoginErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).getText();
    }
}
