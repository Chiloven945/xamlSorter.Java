package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.CustomFileChooser;
import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.FileProcessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ExportDialogController {

    @FXML
    private ChoiceBox<String> fileTypeChoiceBox;

    @FXML
    private ChoiceBox<String> fieldChoiceBox;

    @FXML
    private CheckBox commentCheckBox;

    @FXML
    private DialogPane dialogPane;

    private Map<String, List<DataItem>> groupedData;

    public void setGroupedData(Map<String, List<DataItem>> groupedData) {
        this.groupedData = groupedData;
    }

    @FXML
    private void handleExport() {
        String fileType = fileTypeChoiceBox.getValue();          // e.g., "xaml"
        String fieldToExport = fieldChoiceBox.getValue();        // "Original" or "Translated"
        boolean addComments = commentCheckBox.isSelected();

        File file = CustomFileChooser.showSaveFileDialog(null, "Export File", List.of(fileType));
        if (file != null) {
            FileProcessor.exportToFile(file, fileType, fieldToExport, addComments, groupedData);
        }

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Window window = dialogPane.getScene().getWindow();
        if (window instanceof Stage stage) {
            stage.close();
        }
    }
}
