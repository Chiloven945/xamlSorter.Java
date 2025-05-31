package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContextMenuController {
    private static final Logger logger = LogManager.getLogger(ContextMenuController.class);

    @FXML private MenuItem copyEntry;
    @FXML private MenuItem pasteEntry;
    @FXML private MenuItem cutEntry;
    @FXML private MenuItem deleteEntry;
    @FXML private MenuItem addEntry;

    private Map<String, List<DataItem>> groupedData;
    private TreeTableView<DataItem> translationTreeTable;
    private DataItem clipboard;
    private DataItem targetItem;

    public void initializeMenu(Map<String, List<DataItem>> groupedData,
                               TreeTableView<DataItem> translationTreeTable,
                               DataItem targetItem,
                               DataItem clipboardHolder) {
        this.groupedData = groupedData;
        this.translationTreeTable = translationTreeTable;
        this.targetItem = targetItem;
        this.clipboard = clipboardHolder;

        boolean hasTarget = targetItem != null;
        boolean hasClipboard = clipboardHolder != null && clipboardHolder.getKey() != null;

        copyEntry.setDisable(!hasTarget);
        cutEntry.setDisable(!hasTarget);
        deleteEntry.setDisable(!hasTarget);
        pasteEntry.setDisable(!hasClipboard);
    }

    // Copy the selected item to the clipboard
    @FXML
    private void handleCopy() {
        if (targetItem != null) {
            clipboard.setCategory(targetItem.getCategory());
            clipboard.setKey(targetItem.getKey());
            clipboard.setOriginalText(targetItem.getOriginalText());
            clipboard.setTranslatedText(targetItem.getTranslatedText());
        }
    }

    // Paste the copied item into the current category or "uncategorized" if no target item is selected
    @FXML
    private void handlePaste() {
        if (clipboard != null && clipboard.getKey() != null) {
            String category = (targetItem != null) ? targetItem.getCategory() : "uncategorized";
            groupedData.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(new DataItem(category, clipboard.getKey(), clipboard.getOriginalText(), clipboard.getTranslatedText()));
            SortAndRefresher.refresh(translationTreeTable, groupedData);
        }
    }

    // Cut the selected item: copy it to the clipboard and delete it from the current category
    @FXML
    private void handleCut() {
        handleCopy();
        handleDelete();
    }

    // Delete the selected item from the current category
    @FXML
    private void handleDelete() {
        if (targetItem != null) {
            List<DataItem> list = groupedData.get(targetItem.getCategory());
            if (list != null) {
                list.remove(targetItem);
                if (list.isEmpty()) {
                    groupedData.remove(targetItem.getCategory());
                }
                SortAndRefresher.refresh(translationTreeTable, groupedData);
            }
        }
    }

    // Add a new entry to the current category or "uncategorized" if no target item is selected
    @FXML
    private void handleAdd() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

}
