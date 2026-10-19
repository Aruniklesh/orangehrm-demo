package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    // Deliberately wrong value used only by the negative test; it is not a real credential
    private static final String WRONG_PASSWORD = "wrong-password-123";

    @Test(description = "Valid demo user can log in, see the Dashboard, log out and return to the login page")
    public void shouldLoginAndLogoutSuccessfully() {
        LoginPage loginPage = new LoginPage(driver, explicitWaitTimeout);

        loginPage.loginAs(readRequiredEnv("ORANGEHRM_USERNAME"), readRequiredEnv("ORANGEHRM_PASSWORD"));
        Assert.assertTrue(loginPage.isDashboardDisplayed(),
                "Dashboard heading was not displayed after logging in with valid credentials");

        loginPage.logout();
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                "Login page was not displayed again after logging out");
    }

    @Test(description = "Valid username with a wrong password shows the 'Invalid credentials' error")
    public void shouldShowErrorForInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver, explicitWaitTimeout);

        loginPage.loginAs(readRequiredEnv("ORANGEHRM_USERNAME"), WRONG_PASSWORD);

        Assert.assertEquals(loginPage.getLoginErrorMessage(), "Invalid credentials",
                "Unexpected login error message for an invalid password");
        Assert.assertFalse(loginPage.isDashboardDisplayed(),
                "Dashboard must not be displayed after logging in with a wrong password");
    }

    /** Reads a credential from an environment variable and fails clearly if it is missing. */
    private String readRequiredEnv(String name) {
        String value = System.getenv(name);
        Assert.assertTrue(value != null && !value.isBlank(),
                "Environment variable " + name + " is not set. See README for how to set it.");
        return value;
    }
}
