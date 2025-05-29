package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.DataItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegexEditDialogController {
    @FXML private TextField patternField;
    @FXML private TextField replacementField;
    @FXML private ComboBox<String> scopeComboBox;
    @FXML private TextArea previewArea;

    private Stage dialogStage;
    private Map<String, List<DataItem>> groupedData;
    private String targetCategory;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Map<String, List<DataItem>> groupedData, String targetCategory) {
        this.groupedData = groupedData;
        this.targetCategory = targetCategory;
        scopeComboBox.getSelectionModel().select("Current Group");
    }

    @FXML
    private void handlePreview() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();

        StringBuilder previewBuilder = new StringBuilder();

        if ("Current Group".equals(scope)) {
            List<DataItem> list = groupedData.get(targetCategory);
            if (list != null) {
                for (DataItem item : list) {
                    String updated = item.getOriginalText().replaceAll(pattern, replacement);
                    previewBuilder.append(item.getKey()).append(": ").append(updated).append("\n");
                }
            }
        } else {
            for (List<DataItem> list : groupedData.values()) {
                for (DataItem item : list) {
                    String updated = item.getOriginalText().replaceAll(pattern, replacement);
                    previewBuilder.append(item.getKey()).append(": ").append(updated).append("\n");
                }
            }
        }
        previewArea.setText(previewBuilder.toString());
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleApply() {
        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String scope = scopeComboBox.getValue();

        if ("Current Group".equals(scope)) {
            List<DataItem> list = groupedData.get(targetCategory);
            if (list != null) {
                for (DataItem item : list) {
                    item.setOriginalText(item.getOriginalText().replaceAll(pattern, replacement));
                }
            }
        } else {
            for (List<DataItem> list : groupedData.values()) {
                for (DataItem item : list) {
                    item.setOriginalText(item.getOriginalText().replaceAll(pattern, replacement));
                }
            }
        }
        dialogStage.close();
    }
}
