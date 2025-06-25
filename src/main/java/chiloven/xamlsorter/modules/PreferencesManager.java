package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.utils.ShowAlert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class PreferencesManager {
    private static final Logger logger = LogManager.getLogger(PreferencesManager.class);

    private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "preferences.properties";
    private static final Properties props = new Properties();

    static {
        // Automatically create the config directory and load existing preferences
        try {
            logger.info("Initializing PreferencesManager with config directory: {}", CONFIG_DIR);
            Files.createDirectories(Path.of(CONFIG_DIR));
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                logger.info("Loading existing preferences from: {}", CONFIG_FILE);
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to initialize PreferencesManager", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("module.pref_manager.exception.alert.header"),
                    getLang("module.pref_manager.exception.alert.content"),
                    e
            );
        }
    }

    /**
     * Get a preference value by key.
     *
     * @param key          the preference key
     * @param defaultValue the default value to return if the key is not found
     * @return the preference value, or the default value if the key is not found
     */
    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /**
     * Set a preference value by key.
     *
     * @param key   the preference key
     * @param value the value to set for the key
     */
    public static void set(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * Save the current preferences to the configuration file.
     */
    public static void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, getLang("module.pref_manager.save.top_comment"));
            logger.info("Preferences saved to: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Failed to save preferences", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("module.pref_manager.save.exception.alert.header"),
                    getLang("module.pref_manager.save.exception.alert.content"),
                    e
            );
        }
    }

    /**
     * Reload the preferences from the configuration file.
     */
    public static void reload() {
        logger.info("Reloading preferences from: {}", CONFIG_FILE);
        props.clear();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            logger.info("Preferences reloaded successfully");
        } catch (IOException e) {
            logger.warn("Failed to reload preferences, using defaults", e);
        }
    }
}
