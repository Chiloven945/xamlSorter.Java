package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.SortAndRefresher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            SortAndRefresher.refresh(translationTreeTable, groupedData);
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
            SortAndRefresher.refresh(translationTreeTable, groupedData);
        }
    }
}
