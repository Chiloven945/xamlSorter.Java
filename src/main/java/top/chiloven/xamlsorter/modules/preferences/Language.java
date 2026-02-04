package top.chiloven.xamlsorter.modules.preferences;

import java.util.Locale;

public enum Language {
    GREEK("Ελληνικά (Ελλάδα)", "el-GR", Locale.of("el", "GR")),
    ENGLISH_UK("English (United Kingdom)", "en-GB", Locale.of("en", "GB")),
    ENGLISH_US("English (US)", "en-US", Locale.US),
    SPANISH("Español (España)", "es-ES", Locale.of("es", "ES")),
    FRENCH("Français (France)", "fr-FR", Locale.FRANCE),
    JAPANESE("日本語（日本）", "ja-JP", Locale.JAPAN),
    KOREAN("한국어（대한민국）", "ko-KR", Locale.of("ko", "KR")),
    CLASSICAL_CHINESE("文言（華夏）", "lzh", Locale.of("lzh", "Hant", "CN")),
    RUSSIAN("Русский (Россия)", "ru-RU", Locale.of("ru", "RU")),
    SLOVAK("Slovenčina (Slovensko)", "sk-SK", Locale.of("sk", "SK")),
    CHINESE_SIMPLIFIED("简体中文（中国大陆）", "zh-CN", Locale.SIMPLIFIED_CHINESE),
    CHINESE_HONGKONG("繁體中文（香港特別行政區）", "zh-HK", Locale.of("zh", "HK")),
    CHINESE_MEME("梗体中文（天朝）", "zh-MEME", Locale.of("zh", "CN", "MEME")),
    CHINESE_TRADITIONAL("繁體中文（台灣）", "zh-TW", Locale.of("zh", "TW"));

    private final String displayName;
    private final String localeTag;
    private final Locale locale;

    /**
     * Create a new Language enum instance.
     *
     * @param displayName the display name of the language
     * @param localeTag   the locale tag (e.g., "en-US")
     * @param locale      the Locale object representing the language
     */
    Language(String displayName, String localeTag, Locale locale) {
        this.displayName = displayName;
        this.localeTag = localeTag;
        this.locale = locale;
    }

    public static Language fromDisplayName(String displayName) {
        for (Language lang : values()) {
            if (lang.getDisplayName().equals(displayName)) {
                return lang;
            }
        }
        return ENGLISH_US;
    }

    public static Language fromLocale(Locale locale) {
        for (Language lang : values()) {
            if (lang.getLocale().equals(locale)) {
                return lang;
            }
        }
        return ENGLISH_US;
    }

    /**
     * Get the display name of the language.
     *
     * @return the display name of the language
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getLocaleTag() {
        return localeTag;
    }

    /**
     * Get the Locale object representing the language.
     *
     * @return the Locale object for the language
     */
    public Locale getLocale() {
        return locale;
    }
}
