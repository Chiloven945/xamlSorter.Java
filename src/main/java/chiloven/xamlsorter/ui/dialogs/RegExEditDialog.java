package chiloven.xamlsorter.ui.dialogs;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.SortAndRefresher;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class RegExEditDialog extends Dialog<ButtonType> {
    private static final Logger logger = LogManager.getLogger(RegExEditDialog.class);

    private final MainPage mainPage;
    private final TextField patternField;
    private final TextField replacementField;
    private final ComboBox<String> scopeComboBox;
    private final ComboBox<String> replaceTargetComboBox;
    private final TreeTableView<DataItem> previewTreeTable;
    private final TreeTableColumn<DataItem, String> keyColumn;
    private final TreeTableColumn<DataItem, String> originalTextColumn;
    private final TreeTableColumn<DataItem, String> translatedTextColumn;

    private final Map<String, List<DataItem>> groupedData;
    private final String targetCategory;

    public RegExEditDialog(MainPage mainPage, Map<String, List<DataItem>> groupedData, String targetCategory) {
        this.mainPage = mainPage;
        this.groupedData = groupedData;
        this.targetCategory = targetCategory;

        // 初始化组件
        this.patternField = new TextField();
        this.replacementField = new TextField();
        this.scopeComboBox = new ComboBox<>();
        this.replaceTargetComboBox = new ComboBox<>();
        this.previewTreeTable = new TreeTableView<>();
        this.keyColumn = new TreeTableColumn<>(getLang("general.datatype.key"));
        this.originalTextColumn = new TreeTableColumn<>(getLang("general.datatype.original_text"));
        this.translatedTextColumn = new TreeTableColumn<>(getLang("general.datatype.translated_text"));

        try {
            setupDialog();
            initializeControls();
            setupLayout();
        } catch (Exception e) {
            logger.error("Failed to create Regex Edit dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.regex.exception.alert.header"),
                    getLang("dialog.regex.exception.alert.content"),
                    e
            );
        }
    }

    public static void show(MainPage mainPage, Map<String, List<DataItem>> groupedData, String targetCategory) {
        logger.info("Opening Regex Edit dialog");
        try {
            RegExEditDialog dialog = new RegExEditDialog(mainPage, groupedData, targetCategory);
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    logger.info("User applied changes in Regex Edit dialog");
                } else {
                    logger.info("User cancelled Regex Edit dialog");
                }
            });
        } catch (Exception e) {
            logger.error("Failed to show Regex Edit dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.regex.exception.alert.header"),
                    getLang("dialog.regex.exception.alert.content"),
                    e
            );
        }
    }

    private void setupDialog() {
        setTitle(getLang("dialog.regex.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(mainPage.getDataTreeTable().getScene().getWindow());

        ButtonType applyButton = new ButtonType(getLang("general.button.apply"),
                ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(applyButton, cancelButton);

        setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                applyChanges();
            }
            return buttonType;
        });
    }

    private void initializeControls() {
        // 配置作用域选择框
        scopeComboBox.getItems().addAll(
                getLang("dialog.regex.scope.current"),
                getLang("dialog.regex.scope.all")
        );
        scopeComboBox.setPrefHeight(23.0);
        scopeComboBox.setPrefWidth(158.0);
        scopeComboBox.getSelectionModel().selectFirst();

        // 配置目标选择框
        replaceTargetComboBox.getItems().addAll(
                getLang("general.datatype.translated_text"),
                getLang("general.datatype.original_text"),
                getLang("general.datatype.key")
        );
        replaceTargetComboBox.setPrefHeight(23.0);
        replaceTargetComboBox.setPrefWidth(188.0);
        replaceTargetComboBox.getSelectionModel().selectFirst();

        // 配置预览表格
        keyColumn.setPrefWidth(150.0);
        originalTextColumn.setPrefWidth(150.0);
        translatedTextColumn.setPrefWidth(150.0);

        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalTextColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedTextColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());

        previewTreeTable.getColumns().addAll(keyColumn, originalTextColumn, translatedTextColumn);
        previewTreeTable.setShowRoot(false);
        previewTreeTable.setPrefHeight(489.0);
        previewTreeTable.setPrefWidth(440.0);
        VBox.setMargin(previewTreeTable, new Insets(10, 0, 0, 0));
    }

    private void setupLayout() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefWidth(480.0);
        content.setPrefHeight(640.0);

        // 模式输入区域
        Label patternLabel = new Label(getLang("dialog.regex.pattern.label"));
        content.getChildren().addAll(patternLabel, patternField);

        // 替换文本区域
        Label replacementLabel = new Label(getLang("dialog.regex.replacement.label"));
        content.getChildren().addAll(replacementLabel, replacementField);

        // 选项区域
        HBox optionsBox = new HBox(10);
        optionsBox.setPrefHeight(44.0);
        optionsBox.setPrefWidth(440.0);

        // 作用域选择
        VBox scopeBox = new VBox(10);
        scopeBox.setPrefHeight(49.0);
        scopeBox.setPrefWidth(163.0);
        Label scopeLabel = new Label(getLang("dialog.regex.scope.label"));
        scopeBox.getChildren().addAll(scopeLabel, scopeComboBox);
        HBox.setHgrow(scopeBox, Priority.ALWAYS);

        // 目标选择
        VBox targetBox = new VBox(10);
        targetBox.setPrefHeight(49.0);
        targetBox.setPrefWidth(162.0);
        Label targetLabel = new Label(getLang("dialog.regex.target.label"));
        targetBox.getChildren().addAll(targetLabel, replaceTargetComboBox);
        HBox.setHgrow(targetBox, Priority.ALWAYS);

        // 预览按钮
        VBox previewBox = new VBox();
        previewBox.setPrefHeight(49.0);
        previewBox.setPrefWidth(152.0);
        previewBox.setAlignment(Pos.BOTTOM_RIGHT);
        Button previewButton = new Button(getLang("dialog.regex.button.preview"));
        previewButton.setPrefHeight(23.0);
        previewButton.setPrefWidth(147.0);
        previewButton.setOnAction(e -> refreshPreview());
        previewBox.getChildren().add(previewButton);
        HBox.setHgrow(previewBox, Priority.ALWAYS);

        optionsBox.getChildren().addAll(scopeBox, targetBox, previewBox);
        content.getChildren().addAll(optionsBox, previewTreeTable);

        getDialogPane().setContent(content);

        // 应用默认字体
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);
    }

    private void refreshPreview() {
        logger.debug("Refreshing preview in RegExEditDialog");
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        logger.debug("Preview parameters - pattern: {}, replacement: {}, scope: {}, replaceTarget: {}",
                pattern, replacement, scope, replaceTarget);

        Map<String, List<DataItem>> previewGroupedData = processGroupedData(pattern, replacement, scope, replaceTarget, true);

        // Preview TreeTableView with temporary TreeItem
        TreeItem<DataItem> root = new TreeItem<>();
        for (Map.Entry<String, List<DataItem>> entry : previewGroupedData.entrySet()) {
            for (DataItem item : entry.getValue()) {
                root.getChildren().add(new TreeItem<>(item));
            }
        }
        previewTreeTable.setRoot(root);
        logger.debug("Preview refreshed and TreeTableView updated");
    }

    public void applyChanges() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();

        // 添加验证
        if (pattern == null || pattern.trim().isEmpty()) {
            logger.warn("Empty pattern, changes not applied");
            ShowAlert.warn(
                    getLang("general.alert.warn"),
                    getLang("dialog.regex.exception.empty_pattern.header"),
                    getLang("dialog.regex.exception.empty_pattern.content")
            );
            return;
        }

        String scope = scopeComboBox.getValue();
        String replaceTarget = replaceTargetComboBox.getValue();

        logger.debug("Applying changes with pattern: {}, replacement: {}, scope: {}, target: {}",
                pattern, replacement, scope, replaceTarget);

        processGroupedData(pattern, replacement, scope, replaceTarget, false);

        logger.debug("Regrouping grouped data after applying regex changes");
        regroupGroupedData();

        logger.debug("Refreshing main data tree table after changes");
        SortAndRefresher.refresh(mainPage.getDataTreeTable(), mainPage.getGroupedData());

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
        logger.debug("Processing grouped data with pattern: {}, replacement: {}, scope: {}, replaceTarget: {}, previewMode: {}",
                pattern, replacement, scope, replaceTarget, previewMode);

        Map<String, List<DataItem>> result = new LinkedHashMap<>();

        BiConsumer<DataItem, List<DataItem>> applier = (item, list) -> {
            DataItem copy = previewMode
                    ? new DataItem(item.getCategory(), item.getKey(), item.getOriginalText(), item.getTranslatedText())
                    : item;

            if (replaceTarget.equals(getLang("general.datatype.key"))) {
                String before = copy.getKey();
                String after = safeReplace(copy.getKey(), pattern, replacement);
                copy.setKey(after);
            } else if (replaceTarget.equals(getLang("general.datatype.original_text"))) {
                String before = copy.getOriginalText();
                String after = safeReplace(copy.getOriginalText(), pattern, replacement);
                copy.setOriginalText(after);
            } else if (replaceTarget.equals(getLang("general.datatype.translated_text"))) {
                String before = copy.getTranslatedText();
                String after = safeReplace(copy.getTranslatedText(), pattern, replacement);
                copy.setTranslatedText(after);
            }
            list.add(copy);
        };

        if (getLang("dialog.regex.scope.current").equals(scope) && targetCategory != null) {
            logger.debug("Applying regex to current group: {}", targetCategory);
            List<DataItem> source = groupedData.getOrDefault(targetCategory, Collections.emptyList());
            List<DataItem> updated = new ArrayList<>();
            for (DataItem item : source) applier.accept(item, updated);
            result.put(targetCategory, updated);
        } else {
            logger.debug("Applying regex to all groups");
            for (Map.Entry<String, List<DataItem>> entry : groupedData.entrySet()) {
                List<DataItem> updated = new ArrayList<>();
                for (DataItem item : entry.getValue()) applier.accept(item, updated);
                result.put(entry.getKey(), updated);
            }
        }

        logger.debug("Grouped data processing complete. Result size: {}", result.size());
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
            if (input == null) return "";
            if (regex == null || regex.isEmpty()) return input;
            return input.replaceAll(regex, replacement != null ? replacement : "");
        } catch (Exception e) {
            logger.warn("Failed to replace text with regex: {}", regex, e);
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