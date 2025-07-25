package chiloven.xamlsorter.modules;

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

    private static final Map<String, Locale> NAME_TO_LOCALE = Map.ofEntries(
            Map.entry("Ελληνικά (Ελλάδα)", Locale.of("el", "GR")),
            Map.entry("English (United Kingdom)", Locale.of("en", "GB")),
            Map.entry("English (US)", Locale.US),
            Map.entry("Español (España)", Locale.of("es", "ES")),
            Map.entry("Français (France)", Locale.FRANCE),
            Map.entry("日本語（日本）", Locale.JAPAN),
            Map.entry("한국어（대한민국）", Locale.of("ko", "KR")),
            Map.entry("文言（華夏）", Locale.of("lzh", "Hant", "CN")),
            Map.entry("Русский (Россия)", Locale.of("ru", "RU")),
            Map.entry("Slovenčina (Slovensko)", Locale.of("sk", "SK")),
            Map.entry("简体中文（中国大陆）", Locale.SIMPLIFIED_CHINESE),
            Map.entry("繁體中文（香港特別行政區）", Locale.of("zh", "HK")),
            Map.entry("梗体中文（天朝）", Locale.of("zh", "CN", "MEME")),
            Map.entry("繁體中文（台灣）", Locale.of("zh", "TW"))
    );
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
        // 设置默认区域为 US
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
            // 使用 Control 来自定义资源包加载行为
            ResourceBundle.Control control = new ResourceBundle.Control() {
                @Override
                public List<Locale> getCandidateLocales(String baseName, Locale locale) {
                    List<Locale> candidateLocales = super.getCandidateLocales(baseName, locale);
                    // 如果是 US 区域，将默认的 messages.properties 与 en_US 关联
                    if (locale.equals(Locale.US)) {
                        return Arrays.asList(locale, Locale.ROOT);
                    }
                    return candidateLocales;
                }
            };

            ResourceBundle bundle = ResourceBundle.getBundle(BASE_PATH, locale, control);
            logger.info("Loaded ResourceBundle for locale: {}", locale);
            return bundle;
        } catch (MissingResourceException e) {
            logger.warn("ResourceBundle not found for locale: {}, falling back to US", locale);
            return ResourceBundle.getBundle(BASE_PATH, Locale.US);
        }
    }

    /**
     * Set the current locale and reload the ResourceBundle.
     *
     * @param locale the new Locale to set
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = loadBundle(locale);

        logger.info("Locale set to: {}", locale);
    }

    /**
     * Set the current locale using a locale string (e.g., "en", "en_US", "fr-FR").
     * The string is parsed into a Locale object, and the ResourceBundle is reloaded.
     *
     * @param localeStr the locale string to set
     */
    public static void setLocale(String localeStr) {
        if (localeStr == null || localeStr.isBlank()) {
            logger.warn("Locale string is null or blank, defaulting to US");
            setLocale(Locale.US);
            return;
        }

        Locale locale = NAME_TO_LOCALE.get(localeStr);
        setLocale(locale != null ? locale : Locale.US);
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
        Locale locale = getCurrentLocale();
        logger.debug("Current locale for font selection: {}", locale);

        List<String> fontCandidates = LOCALE_FONT_LIST_MAP.get(locale);

        if (fontCandidates == null) {
            fontCandidates = LOCALE_FONT_LIST_MAP.entrySet().stream()
                    .filter(e -> e.getKey().getLanguage().equals(locale.getLanguage()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(DEFAULT_FONT_LIST);
        }

        String font = pickAvailableFont(fontCandidates);
        scene.getRoot().setStyle("-fx-font-family: '" + font + "';");
        logger.info("Applied font '{}' for locale: {}", font, locale);
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
