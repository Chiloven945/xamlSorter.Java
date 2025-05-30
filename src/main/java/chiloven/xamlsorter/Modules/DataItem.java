package chiloven.xamlsorter.Modules;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DataItem {
    private final StringProperty category;
    private final StringProperty key;
    private final StringProperty originalText;
    private final StringProperty translatedText;

    // Constructor

    /**
     * Create a new DataItem with the specified category, key, original text, and translated text.
     * @param category the category of the data item
     * @param key the key of the data item
     * @param originalText the original text of the data item
     * @param translatedText the translated text of the data item
     */
    public DataItem(String category, String key, String originalText, String translatedText) {
        this.category = new SimpleStringProperty(category);
        this.key = new SimpleStringProperty(key);
        this.originalText = new SimpleStringProperty(originalText);
        this.translatedText = new SimpleStringProperty(translatedText);
    }

    /**
     * Get the category of the data item.
     * @return the category as a String
     */
    public String getCategory() {
        return category.get();
    }

    // Setter methods

    /**
     * Set the category of the data item.
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category.set(category);
    }

    /**
     * Get the key of the data item.
     * @return the key as a String
     */
    public String getKey() {
        return key.get();
    }

    /**
     * Set the key of the data item.
     * @param key the key to set as a String
     */
    public void setKey(String key) {
        this.key.set(key);
    }

    /**
     * Get the original text of the data item.
     * @return the original text as a String
     */
    public String getOriginalText() {
        return originalText.get();
    }

    /**
     * Set the original text of the data item.
     * @param originalText the original text to set as a String
     */
    public void setOriginalText(String originalText) {
        this.originalText.set(originalText);
    }

    /**
     * Get the translated text of the data item.
     * @return the translated text as a String
     */
    public String getTranslatedText() {
        return translatedText.get();
    }

    /**
     * Set the translated text of the data item.
     * @param translatedText the translated text to set as a String
     */
    public void setTranslatedText(String translatedText) {
        this.translatedText.set(translatedText);
    }

    // Property getter methods for JavaFX binding (if needed)
    /**
     * Get the category property for JavaFX binding.
     * @return the category property as a StringProperty
     */
    public StringProperty getCategoryProperty() {
        return category;
    }

    /**
     * Get the key property for JavaFX binding.
     * @return the key property as a StringProperty
     */
    public StringProperty getKeyProperty() {
        return key;
    }

    /**
     * Get the original text property for JavaFX binding.
     * @return the original text property as a StringProperty
     */
    public StringProperty getOriginalTextProperty() {
        return originalText;
    }

    /**
     * Get the translated text property for JavaFX binding.
     * @return the translated text property as a StringProperty
     */
    public StringProperty getTranslatedTextProperty() {
        return translatedText;
    }
}
