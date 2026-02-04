package top.chiloven.xamlsorter.modules;

import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;

public class I18n {
    private static final Logger logger = LogManager.getLogger(I18n.class);
    private static final String BASE_PATH = "assets.languages.messages";

    private static final List<String> DEFAULT_FONT_LIST =
            List.of("Segoe UI", "San Francisco", "Noto Sans SC", "Arial");

    // Remove NAME_TO_LOCALE Map, use Language enum instead
    private static final Map<Locale, List<String>> LOCALE_FONT_LIST_MAP = Map.ofEntries(
            Map.entry(Locale.SIMPLIFIED_CHINESE,
                    List.of("Microsoft Yahei UI", "SF Pro SC", "PingFang SC", "Noto Sans SC", "Segoe UI", "Arial")),
            Map.entry(Locale.TRADITIONAL_CHINESE,
                    List.of("Microsoft Jhenghei UI", "SF Pro TC", "PingFang TC", "Noto Sans TC", "Microsoft Yahei UI", "Segoe UI", "Arial")),
            Map.entry(Locale.of("zh", "HK"),
                    List.of("Microsoft Jhenghei UI", "SF Pro HK", "PingFang HK", "Noto Sans HK", "Microsoft Yahei UI", "Segoe UI", "Arial")),
            Map.entry(Locale.JAPAN,
                    List.of("Yu Gothic UI", "SF Pro JP", "Hiragino Kaku Gothic ProN", "Noto Sans JP", "Microsoft Yahei UI", "Segoe UI", "Arial")),
            Map.entry(Locale.of("ko", "KR"),
                    List.of("Malgun Gothic", "SF Pro KR", "Apple SD Gothic Neo", "Noto Sans KR", "Microsoft Yahei UI", "Segoe UI", "Arial")),
            Map.entry(Locale.US, DEFAULT_FONT_LIST),
            Map.entry(Locale.UK, DEFAULT_FONT_LIST),
            Map.entry(Locale.of("es", "ES"), DEFAULT_FONT_LIST),
            Map.entry(Locale.of("fr", "FR"), DEFAULT_FONT_LIST),
            Map.entry(Locale.of("ru", "RU"), DEFAULT_FONT_LIST),
            Map.entry(Locale.of("sk", "SK"), DEFAULT_FONT_LIST),
            Map.entry(Locale.of("el", "GR"), DEFAULT_FONT_LIST)
    );

    private static Locale currentLocale;
    private static ResourceBundle bundle;

    static {
        // Set default locale to US
        Locale.setDefault(Locale.US);
        currentLocale = Locale.US;
        bundle = loadBundle(currentLocale);
    }

    /**
     * Load the ResourceBundle for the specified locale, falling back to US if not found.
     *
     * @param locale target Locale
     * @return ResourceBundle for the specified locale
     */
    private static ResourceBundle loadBundle(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BASE_PATH, locale);
            logger.debug("Loaded resource bundle for locale: {}", locale);
            return bundle;
        } catch (MissingResourceException e) {
            logger.warn("Resource bundle not found for locale: {}, falling back to English", locale);
            return ResourceBundle.getBundle(BASE_PATH, Locale.US);
        }
    }

    /**
     * Set the current locale and reload the ResourceBundle.
     *
     * @param locale the new Locale to set
     */
    public static void setLocale(Locale locale) {
        if (locale == null) {
            logger.warn("Provided locale is null, defaulting to US English");
            locale = Locale.US;
        }

        Locale oldLocale = currentLocale;

        try {
            currentLocale = locale;
            Locale.setDefault(locale);  // Set JVM default locale
            bundle = loadBundle(locale);
            logger.info("Locale changed from {} to {}", oldLocale, locale);
        } catch (Exception e) {
            logger.error("Error setting locale: {}", e.getMessage());
            // Rollback changes
            currentLocale = oldLocale;
            Locale.setDefault(oldLocale);
            bundle = loadBundle(oldLocale);
        }
    }

    /**
     * Get the localised string for the given key.
     * If the key is not found, it returns the key itself as a placeholder.
     *
     * @param key the key for the desired string
     * @return the localised string, or a placeholder if the key is not found
     */
    public static String getLang(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warn("Missing resource for key: {}, using key as fallback", key);
            return key;
        }
    }

    /**
     * Get the localised string for the given key with arguments.
     *
     * @param key  the key for the desired string
     * @param args the arguments to format the string with
     * @return the formatted localised string, or a placeholder if the key is not found
     */
    public static String getLang(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            logger.warn("Missing resource for key: {}, using key as fallback", key);
            return key;
        }
    }

    /**
     * Get the current ResourceBundle.
     *
     * @return the current ResourceBundle
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Get the current Locale.
     *
     * @return the current Locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Applies the default font for the current locale to the given JavaFX scene.
     *
     * @param scene the JavaFX Scene to which the default font will be applied
     */
    public static void applyDefaultFont(Scene scene) {
        if (scene == null) {
            logger.warn("Cannot apply font to null scene");
            return;
        }

        Locale locale = getCurrentLocale();
        logger.debug("Current locale for font selection: {}", locale);

        List<String> fontCandidates = LOCALE_FONT_LIST_MAP.getOrDefault(locale, DEFAULT_FONT_LIST);
        String selectedFont = pickAvailableFont(fontCandidates);

        scene.getRoot().setStyle(String.format("-fx-font-family: '%s';", selectedFont));
        logger.info("Applied font '{}' for locale: {}", selectedFont, locale);
    }

    /**
     * Selects the first available font from the preferred fonts list that is installed on the system.
     * If none are found, falls back to "System".
     *
     * @param preferredFonts List of preferred font family names in order of priority
     * @return The name of the first available font, or "System" if none are found
     */
    private static String pickAvailableFont(List<String> preferredFonts) {
        List<String> installed = javafx.scene.text.Font.getFontNames();

        for (String fontFamily : preferredFonts) {
            for (String sysFont : installed) {
                if (sysFont.equals(fontFamily) || sysFont.startsWith(fontFamily + " ")) {
                    logger.debug("Font '{}' found and selected.", fontFamily);
                    return fontFamily;
                }
            }
        }

        logger.info("No preferred fonts found. Falling back to 'System'.");
        return "System";
    }

}
