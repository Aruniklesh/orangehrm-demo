package utils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Single place that answers "what is the value of this setting?".
 *
 * Lookup order for each key (first match wins):
 *   1. Java system property       e.g.  mvn test -Dbrowser=edge
 *   2. Environment variable       e.g.  BROWSER  (key upper-cased, dots become underscores)
 *   3. config.properties          (non-secret settings only)
 *
 * Secrets (the password) skip step 3, so they can never be read from a file that might be committed.
 */
public final class ConfigReader {

    private static final Properties FILE_SETTINGS = loadConfigFile();

    private ConfigReader() {
        // utility class: no objects needed
    }

    // ----- Settings used by the tests -----
    public static String getBaseUrl() {
        return getRequired("base.url");
    }

    public static String getBrowser() {
        return getRequired("browser");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(find("headless", true));
    }

    public static Duration getExplicitWaitTimeout() {
        return Duration.ofSeconds(Long.parseLong(getRequired("explicit.wait.seconds")));
    }

    /** From -Dorangehrm.username or environment variable ORANGEHRM_USERNAME. */
    public static String getUsername() {
        return getRequiredSecret("orangehrm.username");
    }

    /** From -Dorangehrm.password or environment variable ORANGEHRM_PASSWORD. Never from a file. */
    public static String getPassword() {
        return getRequiredSecret("orangehrm.password");
    }

    // ----- Internals -----
    private static String getRequired(String key) {
        String value = find(key, true);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing setting '" + key
                    + "'. Add it to config.properties or pass -D" + key + "=<value>.");
        }
        return value.trim();
    }

    private static String getRequiredSecret(String key) {
        String value = find(key, false);
        if (value == null || value.isBlank()) {
            // The message names the variable but never prints a value
            throw new IllegalStateException("Missing setting '" + key + "'. Set the environment variable "
                    + toEnvName(key) + " (see README) or pass -D" + key + "=<value>.");
        }
        return value;
    }

    private static String find(String key, boolean allowConfigFile) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(toEnvName(key));
        }
        if (value == null && allowConfigFile) {
            value = FILE_SETTINGS.getProperty(key);
        }
        return value;
    }

    private static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static Properties loadConfigFile() {
        Properties properties = new Properties();
        // config.properties is in src/test/resources, which is on the test classpath
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties not found in src/test/resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config.properties", e);
        }
        return properties;
    }
}
