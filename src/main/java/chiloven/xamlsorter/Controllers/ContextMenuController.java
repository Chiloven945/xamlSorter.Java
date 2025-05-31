package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.ClipboardManager;
import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.DataOperationHelper;
import chiloven.xamlsorter.Modules.SortAndRefresher;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ContextMenuController {
    private static final Logger logger = LogManager.getLogger(ContextMenuController.class);

    @FXML
    private MenuItem copyEntry;
    @FXML
    private MenuItem pasteEntry;
    @FXML
    private MenuItem cutEntry;
    @FXML
    private MenuItem deleteEntry;
    @FXML
    private MenuItem addEntry;
    @FXML
    private MenuItem selectAll;
    @FXML
    private MenuItem unselectAll;

    private Map<String, List<DataItem>> groupedData;
    private TreeTableView<DataItem> translationTreeTable;

    /**
     * Initializes the context menu with the necessary data and enables/disables menu items
     *
     * @param groupedData          the grouped data by category
     * @param translationTreeTable the TreeTableView containing the data items
     * @param targetItem           the currently selected item in the TreeTableView, can be null
     */
    public void initializeMenu(Map<String, List<DataItem>> groupedData,
                               TreeTableView<DataItem> translationTreeTable,
                               DataItem targetItem) {
        this.groupedData = groupedData;
        this.translationTreeTable = translationTreeTable;

        boolean hasTarget = targetItem != null;
        boolean hasClipboard = ClipboardManager.hasContent();

        copyEntry.setDisable(!hasTarget);
        cutEntry.setDisable(!hasTarget);
        deleteEntry.setDisable(!hasTarget);
        pasteEntry.setDisable(!hasClipboard);

        logger.debug("Context menu initialized with grouped data size: {}, target item: {}, clipboard has content: {}",
                groupedData.size(), hasTarget, hasClipboard);
    }

    // Context menu actions
    @FXML
    private void handleCopy() {
        DataOperationHelper.copy(translationTreeTable);
    }

    // Handles pasting data into the TreeTableView
    @FXML
    private void handlePaste() {
        DataOperationHelper.paste(translationTreeTable, groupedData);
    }

    // Handles cutting data from the TreeTableView
    @FXML
    private void handleCut() {
        DataOperationHelper.cut(translationTreeTable, groupedData);
    }

    // Handles deleting the selected entry from the TreeTableView
    @FXML
    private void handleDelete() {
        DataOperationHelper.delete(translationTreeTable, groupedData);
    }

    // Handles adding a new entry to the TreeTableView
    @FXML
    private void handleAdd() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

    @FXML
    private void handleSelectAll() {
        DataOperationHelper.selectAll(translationTreeTable);
    }

    @FXML
    private void handleUnselectAll() {
        DataOperationHelper.unselectAll(translationTreeTable);
    }
}
