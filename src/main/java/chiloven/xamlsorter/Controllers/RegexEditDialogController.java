package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.SortAndRefresher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;

public class RegexEditDialogController {
    private static final Logger logger = LogManager.getLogger(RegexEditDialogController.class);

    @FXML private TextField patternField;
    @FXML private TextField replacementField;
    @FXML private ComboBox<String> scopeComboBox;
    @FXML private ComboBox<String> replaceTargetComboBox;
    @FXML private TreeTableView<DataItem> previewTreeTable;
    @FXML private TreeTableColumn<DataItem, String> keyColumn;
    @FXML private TreeTableColumn<DataItem, String> originalTextColumn;
    @FXML private TreeTableColumn<DataItem, String> translatedTextColumn;

    private Map<String, List<DataItem>> groupedData;
    private String targetCategory;

    public void setData(Map<String, List<DataItem>> groupedData, String targetCategory) {
        this.groupedData = groupedData;
        this.targetCategory = targetCategory;

        scopeComboBox.getSelectionModel().select("Current Group");
        replaceTargetComboBox.getSelectionModel().select("Original Text");

        initialize();
    }

    @FXML
    public void initialize() {
        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalTextColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedTextColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());

        previewTreeTable.setRoot(new TreeItem<>());
        previewTreeTable.setShowRoot(false);
    }

    @FXML
    private void handlePreview() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        Map<String, List<DataItem>> previewGroupedData = processGroupedData(pattern, replacement, scope, replaceTarget, true);
        SortAndRefresher.refresh(previewTreeTable, previewGroupedData, pattern, replacement, replaceTarget);
    }

    public void applyChanges() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        processGroupedData(pattern, replacement, scope, replaceTarget, false);
    }

    /**
     * Processes the grouped data based on the provided regex pattern and replacement.
     * @param pattern the regex pattern to match
     * @param replacement the replacement string for the matched pattern
     * @param scope the scope of the replacement (e.g., "Current Group", "All Groups")
     * @param replaceTarget the target field to apply the replacement on (e.g., "Key", "Original Text", "Translated Text")
     * @param previewMode if true, only previews the changes without applying them
     * @return a map of categories to lists of DataItems with the applied changes
     */
    private Map<String, List<DataItem>> processGroupedData(String pattern, String replacement, String scope,
                                                           String replaceTarget, boolean previewMode) {
        Map<String, List<DataItem>> result = new TreeMap<>();

        BiConsumer<DataItem, DataItem> updater = (original, target) -> {
            if (!previewMode) {
                applyToItem(original, pattern, replacement, replaceTarget);
            } else {
                String newKey = replaceTarget.equals("Key") ? original.getKey().replaceAll(pattern, replacement) : original.getKey();
                String newOriginal = replaceTarget.equals("Original Text") ? original.getOriginalText().replaceAll(pattern, replacement) : original.getOriginalText();
                String newTranslated = replaceTarget.equals("Translated Text") ? original.getTranslatedText().replaceAll(pattern, replacement) : original.getTranslatedText();

                boolean hasChanged =
                        (replaceTarget.equals("Key") && !newKey.equals(original.getKey())) ||
                                (replaceTarget.equals("Original Text") && !newOriginal.equals(original.getOriginalText())) ||
                                (replaceTarget.equals("Translated Text") && !newTranslated.equals(original.getTranslatedText()));

                if (hasChanged) {
                    DataItem previewItem = new DataItem(original.getCategory(), newKey, newOriginal, newTranslated);
                    result.computeIfAbsent(original.getCategory(), k -> new ArrayList<>()).add(previewItem);
                }
            }
        };

        if ("Current Group".equals(scope)) {
            List<DataItem> list = groupedData.get(targetCategory);
            if (list != null) {
                for (DataItem item : list) {
                    updater.accept(item, item);
                }
            }
        } else {
            for (List<DataItem> list : groupedData.values()) {
                for (DataItem item : list) {
                    updater.accept(item, item);
                }
            }
        }

        return result;
    }

    private void applyToItem(DataItem item, String pattern, String replacement, String replaceTarget) {
        switch (replaceTarget) {
            case "Original Text" -> item.setOriginalText(item.getOriginalText().replaceAll(pattern, replacement));
            case "Translated Text" -> item.setTranslatedText(item.getTranslatedText().replaceAll(pattern, replacement));
            case "Key" -> item.setKey(item.getKey().replaceAll(pattern, replacement));
        }
    }
}
