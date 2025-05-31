package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TopMenuBarController {
    private static final Logger logger = LogManager.getLogger(TopMenuBarController.class);
    private static final ShowAlert alertHelper = new ShowAlert();

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Import Original XAML File Handler
    @FXML
    private void handleImportOriginalFile() {
        File file = CustomFileChooser.showOpenFileDialog(
                mainController.getTranslationTreeTable().getScene().getWindow(),
                "Import Original XAML File",
                Arrays.asList("xaml", "xml")
        );
        if (file != null) {
            try {
                List<DataItem> items = FileProcessor.parseXamlFile(file, false);
                Map<String, List<DataItem>> groupedData = mainController.getGroupedData();
                groupedData.clear();
                groupedData.putAll(FileProcessor.groupByCategory(items));
                SortAndRefresher.refresh(mainController.getTranslationTreeTable(), groupedData);
                alertHelper.showAlert(Alert.AlertType.INFORMATION, "Success", "Original file imported successfully.");
            } catch (Exception e) {
                logger.error("Failed to import original file", e);
                alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to import original file.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleImportTargetFile() {
        File file = CustomFileChooser.showOpenFileDialog(
                mainController.getTranslationTreeTable().getScene().getWindow(),
                "Import Translation XAML File",
                Arrays.asList("xaml", "xml")
        );
        if (file != null) {
            try {
                List<DataItem> translations = FileProcessor.parseXamlFile(file, true);
                FileProcessor.applyTranslations(translations, mainController.getGroupedData());
                SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
                alertHelper.showAlert(Alert.AlertType.INFORMATION, "Success", "Translation file imported successfully.");
            } catch (Exception e) {
                logger.error("Failed to import translation file", e);
                alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to import translation file.\n" + e.getMessage());
            }
        }
    }

    // Regex Edit Dialog Handler
    @FXML
    private void handleRegexEdit() {
        try {
            // Load the Regex Edit dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dialogs/RegexEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Create a dialog and set its properties
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Batch Regex Edit");

            // Add buttons to the dialog
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

            // Set the result converter to handle button clicks
            RegexEditDialogController controller = loader.getController();
            controller.setData(mainController.getGroupedData(), null);

            // Show the dialog and wait for a response
            Optional<ButtonType> result = dialog.showAndWait();

            // If the user clicked "Apply", apply the changes
            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                controller.applyChanges();

                // Regroup the data after applying changes
                Map<String, List<DataItem>> regrouped = mainController.getGroupedData()
                        .values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.groupingBy(DataItem::getCategory));

                // Clear the existing grouped data and update it with the regrouped data
                mainController.getGroupedData().clear();
                mainController.getGroupedData().putAll(regrouped);

                // Refresh the TreeTableView with the updated grouped data
                SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
            }
        } catch (Exception e) {
            logger.error("Error opening Regex Edit dialog", e);
            alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Regex Edit dialog.\n" + e.getMessage());
        }
    }

    // Method to handle the "Save" action
    @FXML
    private void handleAdd() {
        DataOperationHelper.addEntry(mainController.getGroupedData());
        SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Save" action
    @FXML
    private void handleCut() {
        DataOperationHelper.cut(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Save" action
    @FXML
    private void handleCopy() {
        DataOperationHelper.copy(mainController.getTranslationTreeTable());
    }

    // Method to handle the "Paste" action
    @FXML
    private void handlePaste() {
        DataOperationHelper.paste(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Delete" action
    @FXML
    private void handleDelete() {
        DataOperationHelper.delete(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Select All" action
    @FXML
    private void handleSelectAll() {
        DataOperationHelper.selectAll(mainController.getTranslationTreeTable());
    }

    // Method to handle the "Unselect All" action
    @FXML
    private void handleUnselectAll() {
        DataOperationHelper.unselectAll(mainController.getTranslationTreeTable());
    }


}
