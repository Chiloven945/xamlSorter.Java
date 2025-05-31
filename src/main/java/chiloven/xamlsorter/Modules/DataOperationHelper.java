package chiloven.xamlsorter.Modules;

import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataOperationHelper {

    /**
     * Adds a new entry to the grouped data.
     * @param groupedData the map containing grouped data by category
     */
    public static void addEntry(Map<String, List<DataItem>> groupedData) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Entry");
        dialog.setHeaderText("Enter new key (e.g., common.new.key):");
        Optional<String> result = dialog.showAndWait();

        // If the user provided a key, create a new DataItem and add it to the groupedData
        result.ifPresent(newKey -> {
            String category = newKey.contains(".") ? newKey.split("\\.")[0] : "uncategorized";
            DataItem newItem = new DataItem(category, newKey, "New Original", "New Translation");

            groupedData.computeIfAbsent(category, k -> new ArrayList<>()).add(newItem);
        });
    }

    /**
     * Cuts the selected entry from the TreeTableView and places it in the clipboard.
     * @param table the TreeTableView containing DataItems
     * @param groupedData the map containing grouped data by category
     * @param clipboard the DataItem that acts as a clipboard for cut operations
     */
    public static void cut(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData, DataItem clipboard) {
        TreeItem<DataItem> selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            copy(table, clipboard);
            groupedData.get(selected.getValue().getCategory()).remove(selected.getValue());
            SortAndRefresher.refresh(table, groupedData);
        }
    }

    /**
     * Copies the selected entry from the TreeTableView to the clipboard.
     * @param table the TreeTableView containing DataItems
     * @param clipboard the DataItem that acts as a clipboard for copy operations
     */
    public static void copy(TreeTableView<DataItem> table, DataItem clipboard) {
        TreeItem<DataItem> selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            clipboard.setCategory(selected.getValue().getCategory());
            clipboard.setKey(selected.getValue().getKey());
            clipboard.setOriginalText(selected.getValue().getOriginalText());
            clipboard.setTranslatedText(selected.getValue().getTranslatedText());
        }
    }

    /**
     * Pastes the clipboard entry into the TreeTableView under the selected category.
     * @param table the TreeTableView where the entry will be pasted
     * @param groupedData the map containing grouped data by category
     * @param clipboard the DataItem that acts as a clipboard for paste operations
     */
    public static void paste(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData, DataItem clipboard) {
        if (clipboard != null && clipboard.getKey() != null) {
            TreeItem<DataItem> selected = table.getSelectionModel().getSelectedItem();
            String category = (selected != null) ? selected.getValue().getCategory() : "uncategorized";

            groupedData.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(new DataItem(category, clipboard.getKey(), clipboard.getOriginalText(), clipboard.getTranslatedText()));
            SortAndRefresher.refresh(table, groupedData);
        }
    }

    /**
     * Deletes the selected entry from the TreeTableView and updates the grouped data.
     * @param table the TreeTableView containing DataItems
     * @param groupedData the map containing grouped data by category
     */
    public static void delete(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        TreeItem<DataItem> selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            groupedData.get(selected.getValue().getCategory()).remove(selected.getValue());
            SortAndRefresher.refresh(table, groupedData);
        }
    }

    /**
     * Selects all entries in the TreeTableView.
     * @param table the TreeTableView to select all entries in
     */
    public static void selectAll(TreeTableView<DataItem> table) {
        table.getSelectionModel().selectAll();
    }

    /**
     * Unselects all entries in the TreeTableView.
     * @param table the TreeTableView to unselect all entries in
     */
    public static void unselectAll(TreeTableView<DataItem> table) {
        table.getSelectionModel().clearSelection();
    }
}
