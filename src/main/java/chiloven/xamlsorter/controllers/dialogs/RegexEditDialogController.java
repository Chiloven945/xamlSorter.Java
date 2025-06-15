package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.modules.DataItem;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.ShowAlert;
import chiloven.xamlsorter.modules.SortAndRefresher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class RegexEditDialogController {
    private static final Logger logger = LogManager.getLogger(RegexEditDialogController.class);
    private MainController mainController;

    @FXML
    private TextField patternField;
    @FXML
    private TextField replacementField;
    @FXML
    private ComboBox<String> scopeComboBox;
    @FXML
    private ComboBox<String> replaceTargetComboBox;
    @FXML
    private TreeTableView<DataItem> previewTreeTable;
    @FXML
    private TreeTableColumn<DataItem, String> keyColumn;
    @FXML
    private TreeTableColumn<DataItem, String> originalTextColumn;
    @FXML
    private TreeTableColumn<DataItem, String> translatedTextColumn;

    private Map<String, List<DataItem>> groupedData;
    private String targetCategory;

    /**
     * Show the Regex Edit dialog and handle the grouped data.
     *
     * @param groupedData data grouped by categories
     */
    public static void showAndHandleRegexEdit(Map<String, List<DataItem>> groupedData, MainController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(RegexEditDialogController.class.getResource("/ui/dialogs/RegexEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            RegexEditDialogController controller = loader.getController();
            controller.setMainController(mainController);
            controller.setData(groupedData, null);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Batch Regex Edit");
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.APPLY, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                controller.applyChanges();
            }
        } catch (Exception e) {
            ShowAlert.error("Error", "Failed to open Regex Edit dialog", e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set the data for the dialog, including the grouped data and target category.
     *
     * @param groupedData    the data grouped by categories, where each key is a category and the value is a list of DataItem
     * @param targetCategory the category to target for regex operations, or null for all categories
     */
    public void setData(Map<String, List<DataItem>> groupedData, String targetCategory) {
        this.groupedData = groupedData;
        this.targetCategory = targetCategory;
        // Initialize the combo boxes if they are empty
        if (scopeComboBox.getItems().isEmpty()) {
            scopeComboBox.getItems().addAll("Current Group", "All Groups");
        }
        if (replaceTargetComboBox.getItems().isEmpty()) {
            replaceTargetComboBox.getItems().addAll("Key", "Original Text", "Translated Text");
        }
        scopeComboBox.getSelectionModel().select("Current Group");
        replaceTargetComboBox.getSelectionModel().select("Original Text");
        initializeTable();
        refreshPreview();
    }

    @FXML
    public void initialize() {
        // Make sure the combo boxes have items
        if (scopeComboBox.getItems().isEmpty()) {
            scopeComboBox.getItems().addAll("Current Group", "All Groups");
        }
        if (replaceTargetComboBox.getItems().isEmpty()) {
            replaceTargetComboBox.getItems().addAll("Key", "Original Text", "Translated Text");
        }
        scopeComboBox.getSelectionModel().select("Current Group");
        replaceTargetComboBox.getSelectionModel().select("Original Text");
        initializeTable();
    }

    private void initializeTable() {
        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalTextColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedTextColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());
        previewTreeTable.setShowRoot(false);
    }

    // Method to handle the preview button click
    @FXML
    private void handlePreview() {
        refreshPreview();
    }

    // Method to handle the refresh action
    private void refreshPreview() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        Map<String, List<DataItem>> previewGroupedData = processGroupedData(pattern, replacement, scope, replaceTarget, true);

        // Preview TreeTableView with temporary TreeItem
        TreeItem<DataItem> root = new TreeItem<>();
        for (Map.Entry<String, List<DataItem>> entry : previewGroupedData.entrySet()) {
            for (DataItem item : entry.getValue()) {
                root.getChildren().add(new TreeItem<>(item));
            }
        }
        previewTreeTable.setRoot(root);
    }

    // Method to apply changes
    public void applyChanges() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        processGroupedData(pattern, replacement, scope, replaceTarget, false);

        regroupGroupedData();
        SortAndRefresher.refresh(mainController.getDataTreeTable(), mainController.getGroupedData());

        logger.info("Changes applied with pattern: {}, replacement: {}, scope: {}, target: {}",
                pattern, replacement, scope, replaceTarget);
    }

    /**
     * Process the grouped data based on the regex pattern and replacement.
     *
     * @param pattern       the regex pattern to match
     * @param replacement   the replacement string
     * @param scope         the scope of the replacement (Current Group or All Groups)
     * @param replaceTarget the target field to replace (Key, Original Text, or Translated Text)
     * @param previewMode   whether this is a preview operation
     * @return a map of updated grouped data
     */
    private Map<String, List<DataItem>> processGroupedData(
            String pattern, String replacement, String scope, String replaceTarget, boolean previewMode
    ) {
        Map<String, List<DataItem>> result = new LinkedHashMap<>();

        BiConsumer<DataItem, List<DataItem>> applier = (item, list) -> {
            DataItem copy = previewMode
                    ? new DataItem(item.getCategory(), item.getKey(), item.getOriginalText(), item.getTranslatedText())
                    : item;

            switch (replaceTarget) {
                case "Key" -> copy.setKey(safeReplace(copy.getKey(), pattern, replacement));
                case "Original Text" -> copy.setOriginalText(safeReplace(copy.getOriginalText(), pattern, replacement));
                case "Translated Text" ->
                        copy.setTranslatedText(safeReplace(copy.getTranslatedText(), pattern, replacement));
            }
            list.add(copy);
        };

        if ("Current Group".equals(scope) && targetCategory != null) {
            List<DataItem> source = groupedData.getOrDefault(targetCategory, Collections.emptyList());
            List<DataItem> updated = new ArrayList<>();
            for (DataItem item : source) applier.accept(item, updated);
            result.put(targetCategory, updated);
        } else {
            for (Map.Entry<String, List<DataItem>> entry : groupedData.entrySet()) {
                List<DataItem> updated = new ArrayList<>();
                for (DataItem item : entry.getValue()) applier.accept(item, updated);
                result.put(entry.getKey(), updated);
            }
        }

        return result;
    }

    /**
     * Safely replace text using regex, handling potential syntax errors.
     *
     * @param input       the input string to process
     * @param regex       the regex pattern to match
     * @param replacement the replacement string
     * @return the processed string with replacements, or the original if an error occurs
     */
    private String safeReplace(String input, String regex, String replacement) {
        try {
            return input == null ? "" : input.replaceAll(regex, replacement);
        } catch (Exception e) {
            return input;
        }
    }

    // Regroup the data after applying changes
    private void regroupGroupedData() {
        List<DataItem> all = groupedData.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<DataItem>> regrouped = DataOperationHelper.groupByCategory(all);
        groupedData.clear();
        groupedData.putAll(regrouped);
    }

}
