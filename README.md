# OrangeHRM Login Automation

A small, beginner-friendly UI automation project that tests the login and logout journey of the public
[OrangeHRM demo site](https://opensource-demo.orangehrmlive.com/web/index.php/auth/login).

**Stack:** Java 21, Selenium WebDriver 4, TestNG 7, Maven, Page Object Model.

> Only use this against the public demo site. Do not point it at production or private systems.

## What is tested

| Test | Steps | Expected result |
|---|---|---|
| `shouldLoginAndLogoutSuccessfully` | Log in with the demo user, then log out | Dashboard is displayed after login; login page is displayed after logout |
| `shouldShowErrorForInvalidPassword` | Log in with a valid username and a wrong password | Error banner shows `Invalid credentials` |

## Project structure

```text
orangehrm-login-automation/
├── pom.xml                                   Maven build file (dependencies, plugins)
├── .gitignore
├── README.md
└── src/test/
    ├── java/
    │   ├── base/BaseTest.java                Starts and closes the browser for every test
    │   ├── pages/LoginPage.java              Locators and actions for the login page (Page Object)
    │   └── tests/LoginTest.java              The test cases and assertions
    └── resources/config.properties           URL, browser and wait settings (no secrets)
```

## Prerequisites

- JDK 21 or newer (`java -version`)
- Apache Maven 3.9+ (`mvn -version`)
- Google Chrome or Microsoft Edge
- Internet access (the demo site is online, and Selenium downloads the matching browser driver automatically)

## Configuration

**Non-secret settings** are in `src/test/resources/config.properties`:

| Key | Meaning | Default |
|---|---|---|
| `base.url` | Login page URL | the OrangeHRM demo login page |
| `browser` | `chrome` or `edge` | `chrome` |
| `explicit.wait.seconds` | Maximum seconds an explicit wait will wait | `15` |

**Credentials** are never stored in the code or in Git. Tests read them from two environment variables.
The demo username and password are displayed on the login page itself.

PowerShell (applies to the current window only):

```powershell
$env:ORANGEHRM_USERNAME = "<username shown on the login page>"
$env:ORANGEHRM_PASSWORD = "<password shown on the login page>"
```

Command Prompt: `set ORANGEHRM_USERNAME=...` and `set ORANGEHRM_PASSWORD=...`
macOS/Linux: `export ORANGEHRM_USERNAME=...` and `export ORANGEHRM_PASSWORD=...`

If you set them permanently (for example with `setx`), open a new terminal or restart your IDE.

## Running the tests

From the project folder (the one containing `pom.xml`):

```powershell
# Run all tests
mvn test

# Run one test class
mvn test -Dtest=LoginTest

# Run one test method
mvn test -Dtest=LoginTest#shouldShowErrorForInvalidPassword

# Compile only (no browser, quick sanity check)
mvn test-compile
```

Successful output ends with:

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

A full run takes roughly 40 seconds. Reports are written to `target/surefire-reports/`
(`tests.LoginTest.txt` is the readable summary).

## Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| `Environment variable ORANGEHRM_USERNAME is not set` | Set both variables in the same terminal you run Maven from |
| `release version 21 not supported` | Maven is running on an older JDK; check `JAVA_HOME` |
| `TimeoutException` | Demo site is slow or down, or its page changed. Open the URL in a browser; raise `explicit.wait.seconds` |
| Browser or driver errors | Make sure Chrome/Edge is installed and you are online; try `browser=edge` |
| `Invalid credentials` in the success test | Check the demo credentials shown on the login page; they can change |
| `SLF4J: No SLF4J providers were found` | Harmless warning; logging is simply disabled |

## Notes

- Every test starts a fresh browser (`@BeforeMethod`) and closes it afterwards (`@AfterMethod`), so tests are independent.
- Waits are explicit (`WebDriverWait`); there is no `Thread.sleep()`.
- The invalid-password test waits the full timeout for a Dashboard that never appears, which makes it slower than it strictly needs to be.
- `LoginPage` also holds the Dashboard check and logout action to keep the project to one page class. A larger project would move them into a separate `DashboardPage`.
