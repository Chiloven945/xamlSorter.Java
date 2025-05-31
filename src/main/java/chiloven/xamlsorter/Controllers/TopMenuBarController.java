package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TopMenuBarController {
    private static final Logger logger = LogManager.getLogger(TopMenuBarController.class);

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleImportOriginalFile() {
        // 使用自定义的 FileChooser 并允许选择 .xaml 文件
        File file = CustomFileChooser.showOpenFileDialog(mainController.getTranslationTreeTable().getScene().getWindow(), "Import Original XAML File", Arrays.asList("xaml", "xml"));
        if (file != null) {
            List<DataItem> items = FileProcessor.parseXamlFile(file, false);
            Map<String, List<DataItem>> groupedData = mainController.getGroupedData();
            groupedData.clear();
            groupedData.putAll(FileProcessor.groupByCategory(items));
            SortAndRefresher.refresh(mainController.getTranslationTreeTable(), groupedData);
        }
    }

    @FXML
    private void handleImportTargetFile() {
        // 使用自定义的 FileChooser 并允许选择 .xaml 文件
        File file = CustomFileChooser.showOpenFileDialog(mainController.getTranslationTreeTable().getScene().getWindow(), "Import Translation XAML File", Arrays.asList("xaml", "xml"));
        if (file != null) {
            List<DataItem> translations = FileProcessor.parseXamlFile(file, true);
            FileProcessor.applyTranslations(translations, mainController.getGroupedData());
            SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
        }
    }

    @FXML
    private void handleRegexEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dialogs/RegexEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Batch Regex Edit");

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

            RegexEditDialogController controller = loader.getController();
            controller.setData(mainController.getGroupedData(), null);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                controller.applyChanges();

                // 重新分组后刷新
                Map<String, List<DataItem>> regrouped = mainController.getGroupedData()
                        .values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.groupingBy(DataItem::getCategory));

                mainController.getGroupedData().clear();
                mainController.getGroupedData().putAll(regrouped);

                SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
            }
        } catch (Exception e) {
            logger.error("Error opening Regex Edit dialog", e);
        }
    }

    @FXML
    private void handleCut() {
        DataOperationHelper.cut(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    @FXML
    private void handleCopy() {
        DataOperationHelper.copy(mainController.getTranslationTreeTable());
    }

    @FXML
    private void handlePaste() {
        DataOperationHelper.paste(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    @FXML
    private void handleDelete() {
        DataOperationHelper.delete(mainController.getTranslationTreeTable(), mainController.getGroupedData());
    }

    @FXML
    private void handleSelectAll() {
        DataOperationHelper.selectAll(mainController.getTranslationTreeTable());
    }

    @FXML
    private void handleUnselectAll() {
        DataOperationHelper.unselectAll(mainController.getTranslationTreeTable());
    }


}
