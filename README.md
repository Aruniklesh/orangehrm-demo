# OrangeHRM Login Automation

A small, beginner-friendly UI automation project that tests the login and logout journey of the public
[OrangeHRM demo site](https://opensource-demo.orangehrmlive.com/web/index.php/auth/login).

**Stack:** Java 21, Selenium WebDriver 4, TestNG 7, Maven, Page Object Model, ExtentReports, Log4j2.

Each run produces an HTML report, screenshots and an execution log (see [Test output](#test-output)).

> Only use this against the public demo site. Do not point it at production or private systems.

## What has been built so far

| Stage | What was added |
|---|---|
| 1. Core framework | Maven project, Selenium + TestNG, Page Object Model (`LoginPage`), shared setup/teardown (`BaseTest`), explicit waits |
| 2. Tests | Successful login and logout, and an invalid-password negative test (locators and the `Invalid credentials` message were checked on the live site) |
| 3. Configuration | `config.properties` for non-secret settings; credentials from environment variables only; `ConfigReader` with `-D` overrides |
| 4. Jenkins (CI) | `Jenkinsfile` and a headless browser option so tests run on a server without a screen |
| 5. Reporting | ExtentReports HTML report, screenshots (including automatic ones on failure), and Log4j2 execution logs |

## What is tested

| Test | Steps | Expected result |
|---|---|---|
| `shouldLoginAndLogoutSuccessfully` | Log in with the demo user, then log out | Dashboard is displayed after login; login page is displayed after logout |
| `shouldShowErrorForInvalidPassword` | Log in with a valid username and a wrong password | Error banner shows `Invalid credentials` |

## Project structure

```text
orangehrm-login-automation/
├── pom.xml                                   Maven build file (dependencies, plugins)
├── Jenkinsfile                               Jenkins pipeline definition
├── .gitignore
├── README.md
└── src/test/
    ├── java/
    │   ├── base/BaseTest.java                Starts/closes the browser; logStep helpers
    │   ├── pages/LoginPage.java              Locators and actions for the login page (Page Object)
    │   ├── tests/LoginTest.java              The test cases and assertions
    │   └── utils/
    │       ├── ConfigReader.java             Reads settings (-D flag > environment variable > config.properties)
    │       ├── ScreenshotUtil.java           Saves PNG screenshots
    │       ├── ExtentManager.java            Creates the HTML report
    │       ├── ExtentTestManager.java        Tracks the report entry of the running test
    │       └── TestListener.java             Records PASS/FAIL, failure screenshot, log lines
    └── resources/
        ├── config.properties                 URL, browser, headless and wait settings (no secrets)
        └── log4j2.xml                        Logging setup (console + logs/execution.log)
```

Generated at run time (not committed): `reports/`, `screenshots/`, `logs/`, `target/`.

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
| `headless` | `true` runs the browser without a window (used on CI) | `false` |
| `explicit.wait.seconds` | Maximum seconds an explicit wait will wait | `15` |

Any setting can be overridden for one run with a `-D` flag, for example `mvn test -Dbrowser=edge`
or `mvn test -Dheadless=true`. Lookup order: `-D` flag, then environment variable, then `config.properties`.

**Credentials** are never stored in the code or in Git. Tests read them from two environment variables.
The demo username and password are displayed on the login page itself.

PowerShell (applies to the current window only):

```powershell
$env:ORANGEHRM_USERNAME = "<username shown on the login page>"
$env:ORANGEHRM_PASSWORD = "<password shown on the login page>"
```

The password is never read from `config.properties`, only from the environment variable
`ORANGEHRM_PASSWORD` (or `-Dorangehrm.password`, which is visible in shell history, so prefer the variable).
Passwords are never written to the log or the report.

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

# Run without a visible browser window (like Jenkins)
mvn clean test -Dheadless=true

# Compile only (no browser, quick sanity check)
mvn test-compile
```

Successful output ends with:

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

A full run takes roughly 40 to 60 seconds.

## Test output

| What | Where | Details |
|---|---|---|
| HTML report | `reports/ExtentReport_<timestamp>.html` | Open in a browser. Shows each test, its steps, PASS/FAIL, error message and embedded screenshots. A new file per run. |
| Screenshots | `screenshots/<test>_<step>_<timestamp>.png` | Taken after login, after the invalid-login error, after logout, and automatically on any failure |
| Execution log | `logs/execution.log` | Test start/pass/fail, steps and errors. No passwords. |
| Maven summary | `target/surefire-reports/` | Plain-text and XML results |

All of these folders are listed in `.gitignore` and are not committed.

## Running in Jenkins

The `Jenkinsfile` defines a pipeline with a Checkout stage and a Test stage (`mvn clean test -Dheadless=true`),
and publishes the JUnit results. One-time Jenkins setup:

1. **Plugins:** Pipeline, Git, Credentials Binding, JUnit.
2. **Manage Jenkins > Tools:** add a JDK named `JDK21` and a Maven installation named `Maven3` (names must match the Jenkinsfile).
3. **Manage Jenkins > Credentials > Global:** add two *Secret text* credentials with the IDs
   `orangehrm-username` and `orangehrm-password` (the IDs must match exactly; leaving the ID blank generates a random one).
4. **New Item > Pipeline:** set *Definition* to **Pipeline script from SCM**, SCM **Git**, your repository URL,
   branch `*/main`, script path `Jenkinsfile`. Do not paste the script into the job (`checkout scm` only works from SCM).
5. Do not tick "Build after other projects are built" with the job's own name.
6. Click **Build Now**. A passing build ends with `Tests run: 2, Failures: 0` and `Finished: SUCCESS`.

The HTML report, screenshots and log are created in the Jenkins workspace but are not archived or published by the
current Jenkinsfile; only the JUnit test results are shown on the build page.

## Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| `Missing setting 'orangehrm.username'` (or `.password`) | Set the environment variables `ORANGEHRM_USERNAME` / `ORANGEHRM_PASSWORD` in the same terminal you run Maven from |
| Jenkins: `ERROR: orangehrm-username` | The credential ID is missing or different in Jenkins; recreate it as *Secret text*, scope Global |
| Jenkins: `'checkout scm' is only available when using ... Pipeline script from SCM` | The job is set to *Pipeline script*; change *Definition* to *Pipeline script from SCM* |
| Jenkins: build stays on "Build scheduled" | Check the Build Queue reason and the executors under Manage Jenkins > Nodes |
| `release version 21 not supported` | Maven is running on an older JDK; check `JAVA_HOME` |
| `TimeoutException` | Demo site is slow or down, or its page changed. Open the URL in a browser; raise `explicit.wait.seconds` |
| Browser or driver errors | Make sure Chrome/Edge is installed and you are online; try `browser=edge` |
| `Invalid credentials` in the success test | Check the demo credentials shown on the login page; they can change |
| Blank screenshot | It was taken before the page rendered; take screenshots only after a wait or assertion |
| Duplicate entries in the report | Keep `maven-surefire-plugin` at 3.5.6 (3.6.0 runs TestNG through a JUnit adapter that fires listeners during a dry run) |

## Notes

- Every test starts a fresh browser (`@BeforeMethod`) and closes it afterwards (`@AfterMethod`), so tests are independent.
- Waits are explicit (`WebDriverWait`); there is no `Thread.sleep()`.
- The invalid-password test waits the full timeout for a Dashboard that never appears, which makes it slower than it strictly needs to be.
- The public demo site is shared and sometimes slow; an occasional timeout is usually cured by re-running.
- In headless mode the window is a fixed 1920x1080 (set at browser start); `maximize()` is only used for visible browsers.
- `LoginPage` also holds the Dashboard check and logout action to keep the project to one page class. A larger project would move them into a separate `DashboardPage`.
