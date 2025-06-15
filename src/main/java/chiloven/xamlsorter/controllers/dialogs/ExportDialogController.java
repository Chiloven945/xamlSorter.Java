package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.modules.CustomFileChooser;
import chiloven.xamlsorter.modules.DataItem;
import chiloven.xamlsorter.modules.FileProcessor;
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

        // Get the current project name from MainController
        String projectName = MainController.getCurrentProjectMeta().getName();

        // Determine the file suffix based on the field to export
        String suffix = fieldToExport.equalsIgnoreCase("Original") ? "original" : "translated";
        String defaultFileName = projectName + "-" + suffix + ".xaml";

        File file = CustomFileChooser.showSaveFileDialog(
                null,
                "Export file",
                List.of(fileType),
                defaultFileName
        );
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
