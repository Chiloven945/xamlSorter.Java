package chiloven.xamlsorter.Modules;

import javafx.beans.property.SimpleStringProperty;

public class DataItem {
    private final SimpleStringProperty category;
    private final SimpleStringProperty key;
    private final SimpleStringProperty originalText;
    private final SimpleStringProperty translatedText;

    public DataItem(String category, String key, String originalText, String translatedText) {
        this.category = new SimpleStringProperty(category);
        this.key = new SimpleStringProperty(key);
        this.originalText = new SimpleStringProperty(originalText);
        this.translatedText = new SimpleStringProperty(translatedText);
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String value) {
        category.set(value);
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String value) {
        key.set(value);
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public String getOriginalText() {
        return originalText.get();
    }

    public void setOriginalText(String value) {
        originalText.set(value);
    }

    public SimpleStringProperty originalTextProperty() {
        return originalText;
    }

    public String getTranslatedText() {
        return translatedText.get();
    }

    public void setTranslatedText(String value) {
        translatedText.set(value);
    }

    public SimpleStringProperty translatedTextProperty() {
        return translatedText;
    }
}
