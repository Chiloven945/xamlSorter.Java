package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.modules.FileProcessor;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.utils.CustomFileChooser;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static chiloven.xamlsorter.modules.I18n.getBundle;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class ExportDialogController {
    private static final Logger logger = LogManager.getLogger(ExportDialogController.class);


    @FXML
    private ChoiceBox<String> fileTypeChoiceBox;
    @FXML
    private ChoiceBox<String> fieldChoiceBox;
    @FXML
    private CheckBox commentCheckBox;
    @FXML
    private DialogPane dialogPane;

    private Map<String, List<DataItem>> groupedData;

    public static void showDialog(MainController mainController) {
        logger.debug("Opening export dialog");
        try {
            FXMLLoader loader = new FXMLLoader(ExportDialogController.class.getResource("/ui/dialogs/ExportDialog.fxml"));
            loader.setResources(getBundle());
            DialogPane pane = loader.load();

            ExportDialogController controller = loader.getController();
            controller.setGroupedData(mainController.getGroupedData());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(pane);

            Scene scene = dialog.getDialogPane().getScene();
            I18n.applyDefaultFont(scene);

            dialog.setTitle(getLang("dialog.export.title"));
            dialog.initOwner(mainController.getDataTreeTable().getScene().getWindow());
            logger.debug("Showing export dialog");
            dialog.showAndWait();
            logger.debug("Export dialog closed");

        } catch (Exception e) {
            logger.error("Failed to open export dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.export.exception.alert.header"),
                    getLang("dialog.export.exception.alert.content"),
                    e
            );
        }
    }

    public void setGroupedData(Map<String, List<DataItem>> groupedData) {
        this.groupedData = groupedData;
    }

    @FXML
    public void initialize() {
        logger.debug("Initializing ExportDialogController");
        fieldChoiceBox.getItems().setAll(
                getLang("general.datatype.original"),
                getLang("general.datatype.translated")
        );
        fieldChoiceBox.getSelectionModel().selectFirst();
        fileTypeChoiceBox.getSelectionModel().selectFirst();
        logger.debug("Field and file type choice boxes initialized");
    }

    @FXML
    private void handleExport() {
        logger.debug("Export initiated by user");
        String fileType = fileTypeChoiceBox.getValue();          // e.g., "xaml"
        String fieldToExport = fieldChoiceBox.getValue();        // "Original" or "Translated"
        boolean addComments = commentCheckBox.isSelected();

        logger.debug("Selected fileType: {}, fieldToExport: {}, addComments: {}", fileType, fieldToExport, addComments);

        // Get the current project name from MainController
        String projectName = MainController.getCurrentProjectMeta().getName();
        logger.debug("Current project name: {}", projectName);

        // Determine the file suffix based on the field to export
        String suffix = getLang("general.datatype." + (fieldToExport.equalsIgnoreCase(getLang("general.datatype.original")) ? "original" : "translated")).toLowerCase();
        String defaultFileName = projectName + "-" + suffix + ".xaml";
        logger.debug("Default export file name: {}", defaultFileName);

        File file = CustomFileChooser.showSaveFileDialog(
                null,
                getLang("dialog.export.file.title"),
                getLang("general.files.xaml"),
                List.of(fileType),
                defaultFileName
        );
        if (file != null) {
            logger.info("Exporting to file: {}", file.getAbsolutePath());
            FileProcessor.exportToFile(file, fileType, fieldToExport, addComments, groupedData);
        } else {
            logger.info("Export cancelled or no file selected");
        }

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        logger.debug("Closing export dialog");
        Window window = dialogPane.getScene().getWindow();
        if (window instanceof Stage stage) {
            stage.close();
        } else {
            logger.warn("Dialog window is not a Stage, cannot close");
        }
    }

}
