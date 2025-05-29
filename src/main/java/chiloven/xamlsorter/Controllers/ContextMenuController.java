package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class ContextMenuController {
    private static final Logger logger = LogManager.getLogger(ContextMenuController.class);

    private Map<String, List<DataItem>> groupedData;
    private TreeTableView<DataItem> translationTreeTable;
    private DataItem clipboard;
    private DataItem targetItem;

    public static ContextMenu create(Map<String, List<DataItem>> groupedData,
                                     TreeTableView<DataItem> translationTreeTable,
                                     DataItem item,
                                     DataItem clipboardHolder) {
        try {
            FXMLLoader loader = new FXMLLoader(ContextMenuController.class.getResource("/Widgets/ContextMenu.fxml"));
            ContextMenu menu = loader.load();

            ContextMenuController controller = loader.getController();
            controller.groupedData = groupedData;
            controller.translationTreeTable = translationTreeTable;
            controller.targetItem = item;
            controller.clipboard = clipboardHolder;

            return menu;
        } catch (IOException e) {
            logger.error("Failed to load context menu", e);
            return new ContextMenu();
        }
    }

    @FXML
    public void handleDelete() {
        List<DataItem> list = groupedData.get(targetItem.getCategory());
        if (list != null) {
            list.remove(targetItem);
            if (list.isEmpty()) {
                groupedData.remove(targetItem.getCategory());
            }
            sortAndRefresh();
        }
    }

    @FXML
    public void handleCopy() {
        clipboard = new DataItem(
                targetItem.getCategory(),
                targetItem.getKey(),
                targetItem.getOriginalText(),
                targetItem.getTranslatedText()
        );
    }

    @FXML
    public void handleCut() {
        handleCopy();
        handleDelete();
    }

    @FXML
    public void handlePaste() {
        if (clipboard != null) {
            DataItem newItem = new DataItem(
                    targetItem.getCategory(),
                    clipboard.getKey(),
                    clipboard.getOriginalText(),
                    clipboard.getTranslatedText()
            );
            groupedData.computeIfAbsent(targetItem.getCategory(), k -> new ArrayList<>()).add(newItem);
            sortAndRefresh();
        }
    }

    @FXML
    public void handleRegexEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dialogs/RegexEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            RegexEditDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Batch Regex Edit");

            controller.setDialogStage((Stage) dialog.getDialogPane().getScene().getWindow());
            controller.setData(groupedData, targetItem.getCategory());

            dialog.showAndWait();
            sortAndRefresh();
        } catch (Exception e) {
            logger.error("Failed to open regex edit dialog", e);
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sortAndRefresh() {
        // 对每个组内条目按 key 排序
        groupedData.values().forEach(list ->
                list.sort(Comparator.comparing(DataItem::getKey))
        );

        // 对分组按组名排序
        Map<String, List<DataItem>> sortedGroupedData = new TreeMap<>(groupedData);

        javafx.scene.control.TreeItem<DataItem> root = new javafx.scene.control.TreeItem<>(new DataItem("", "", "", ""));
        root.setExpanded(true);

        for (Map.Entry<String, List<DataItem>> entry : sortedGroupedData.entrySet()) {
            String category = entry.getKey();
            List<DataItem> items = entry.getValue();

            DataItem categoryItem = new DataItem(category, category + "...", "-", "-");
            javafx.scene.control.TreeItem<DataItem> categoryNode = new javafx.scene.control.TreeItem<>(categoryItem);

            for (DataItem item : items) {
                categoryNode.getChildren().add(new javafx.scene.control.TreeItem<>(item));
            }
            root.getChildren().add(categoryNode);
        }

        translationTreeTable.setRoot(root);
        translationTreeTable.setShowRoot(false);
    }
}
