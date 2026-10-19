package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class LoginTest extends BaseTest {

    // Deliberately wrong value used only by the negative test; it is not a real credential
    private static final String WRONG_PASSWORD = "wrong-password-123";

    @Test(description = "Valid demo user can log in, see the Dashboard, log out and return to the login page")
    public void shouldLoginAndLogoutSuccessfully() {
        LoginPage loginPage = new LoginPage(driver, explicitWaitTimeout);

        logStep("Opened the OrangeHRM login page");

        logStep("Logging in as the configured demo user");
        loginPage.loginAs(ConfigReader.getUsername(), ConfigReader.getPassword());
        Assert.assertTrue(loginPage.isDashboardDisplayed(),
                "Dashboard heading was not displayed after logging in with valid credentials");
        logStepWithScreenshot("Login succeeded: Dashboard is displayed", "after-login");

        logStep("Logging out");
        loginPage.logout();
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                "Login page was not displayed again after logging out");
        logStepWithScreenshot("Logout succeeded: login page is displayed again", "after-logout");
    }

    @Test(description = "Valid username with a wrong password shows the 'Invalid credentials' error")
    public void shouldShowErrorForInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver, explicitWaitTimeout);

        logStep("Logging in with the demo username and a deliberately wrong password");
        loginPage.loginAs(ConfigReader.getUsername(), WRONG_PASSWORD);

        String actualError = loginPage.getLoginErrorMessage();

        logStepWithScreenshot("Login rejected, error shown: " + actualError, "invalid-login-error");
        
        Assert.assertEquals(actualError, "Invalid credentials",
                "Unexpected login error message for an invalid password");
        Assert.assertFalse(loginPage.isDashboardDisplayed(),
                "Dashboard must not be displayed after logging in with a wrong password");
    }
}
