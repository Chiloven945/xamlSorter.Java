package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.modules.preferences.Language;
import chiloven.xamlsorter.modules.preferences.ThemeMode;
import chiloven.xamlsorter.utils.ShowAlert;
import com.jthemedetecor.OsThemeDetector;
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
    private static final OsThemeDetector themeDetector = OsThemeDetector.getDetector();

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

    /**
     * Get the current theme mode setting.
     * This will return SYSTEM if the mode is not set or invalid.
     *
     * @return the current ThemeMode, defaults to SYSTEM if not set or invalid
     */
    public static ThemeMode getThemeMode() {
        String mode = get("theme.mode", "SYSTEM");
        try {
            return ThemeMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return ThemeMode.SYSTEM;
        }
    }

    /**
     * Set the theme mode preference.
     *
     * @param mode the ThemeMode to set
     */
    public static void setThemeMode(ThemeMode mode) {
        set("theme.mode", mode.name());
        save();
    }

    /**
     * Check if the current theme mode is dark.
     *
     * @return true if the theme is dark, false otherwise
     */
    public static boolean isDarkMode() {
        ThemeMode mode = getThemeMode();
        if (mode == ThemeMode.SYSTEM) {
            try {
                return themeDetector.isDark();
            } catch (Exception e) {
                logger.warn("Failed to detect system theme, defaulting to light theme", e);
                return false;
            }
        }
        return mode == ThemeMode.DARK;
    }

    /**
     * Add a system theme change listener
     *
     * @param listener the theme change listener
     */
    public static void addThemeChangeListener(Runnable listener) {
        if (getThemeMode() == ThemeMode.SYSTEM) {
            themeDetector.registerListener(isDark -> {
                logger.debug("System theme changed: {}", isDark ? "dark" : "light");
                listener.run();
            });
        }
    }

    /**
     * Remove a system theme change listener
     *
     * @param listener the theme change listener to remove
     */
    public static void removeThemeChangeListener(Runnable listener) {
        if (getThemeMode() == ThemeMode.SYSTEM) {
            themeDetector.removeListener(isDark -> listener.run());
        }
    }

    /**
     * Get the current language setting
     *
     * @return the current language, defaults to English (US) if not set
     */
    public static Language getLanguage() {
        String langStr = get("language", Language.ENGLISH_US.name());
        try {
            return Language.valueOf(langStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid language setting: {}, defaulting to English (US)", langStr);
            return Language.ENGLISH_US;
        }
    }

    /**
     * Set the language preference
     *
     * @param language the language to set
     */
    public static void setLanguage(Language language) {
        if (language != null) {
            set("language", language.name());
            save();
            logger.info("Language preference saved: {}", language.getDisplayName());
        }
    }

}
