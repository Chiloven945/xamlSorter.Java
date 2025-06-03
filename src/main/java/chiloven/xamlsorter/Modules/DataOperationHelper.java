package chiloven.xamlsorter.Modules;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataOperationHelper {
    private static final Logger logger = LogManager.getLogger(DataOperationHelper.class);

    /**
     * Adds a new entry to the grouped data.
     *
     * @param groupedData the map containing grouped data by category
     */
    public static void addEntry(Map<String, List<DataItem>> groupedData) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Entry");
        dialog.setHeaderText("Enter new key (e.g., common.new.key):");
        logger.info("Opening dialog to add new entry.");
        Optional<String> result = dialog.showAndWait();

        logger.info("User input for new key: {}", result.orElse("No input provided"));

        // Check if the user provided a key
        result.ifPresent(newKey -> {
            String category = newKey.contains(".") ? newKey.split("\\.")[0] : "uncategorized";

            // Check if the key already exists in the grouped data
            boolean exists = groupedData.values().stream()
                    .flatMap(List::stream)
                    .anyMatch(item -> item.getKey().equals(newKey));

            // If the key already exists, show a warning and return
            if (exists) {
                ShowAlert alertHelper = new ShowAlert();
                alertHelper.showAlert(Alert.AlertType.WARNING, "Duplicate Entry",
                        "An entry with the key '" + newKey + "' already exists and cannot be added.");
                logger.warn("Attempted to add duplicate entry with key: {}", newKey);
                return;
            }

            // If the key does not exist, create a new DataItem and add it to the grouped data
            DataItem newItem = new DataItem(category, newKey, "New Original", "New Translation");
            groupedData.computeIfAbsent(category, k -> new ArrayList<>()).add(newItem);
            logger.info("Added new entry with key: {}", newKey);
        });
    }

    /**
     * Cuts the selected entries from the TreeTableView and copies them to the clipboard.
     * @param table the TreeTableView containing the data items
     * @param groupedData the map containing grouped data by category
     */
    public static void cut(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            List<DataItem> toCopy = selected.stream()
                    .map(TreeItem::getValue)
                    .filter(item -> item != null && !item.getKey().endsWith("..."))
                    .collect(Collectors.toList());
            ClipboardManager.copyFrom(toCopy);

            // Rearrange the grouped data by removing the cut items
            for (TreeItem<DataItem> item : selected) {
                DataItem data = item.getValue();
                if (data != null && !data.getKey().endsWith("...")) {
                    List<DataItem> list = groupedData.get(data.getCategory());
                    if (list != null) {
                        list.remove(data);
                        if (list.isEmpty()) groupedData.remove(data.getCategory());
                    }
                }
            }
            SortAndRefresher.refresh(table, groupedData);
            logger.info("Cut {} entries.", toCopy.size());
        }
    }

    /**
     * Copies the selected entries from the TreeTableView to the clipboard.
     * @param table the TreeTableView containing the data items
     */
    public static void copy(TreeTableView<DataItem> table) {
        List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        List<DataItem> toCopy = selected.stream()
                .map(TreeItem::getValue)
                .filter(item -> item != null && !item.getKey().endsWith("..."))
                .collect(Collectors.toList());
        if (!toCopy.isEmpty()) {
            ClipboardManager.copyFrom(toCopy);
            logger.info("Copied {} entries.", toCopy.size());
        }
    }

    /**
     * Deletes the selected entries from the TreeTableView and updates the grouped data.
     * @param table the TreeTableView containing the data items
     * @param groupedData the map containing grouped data by category
     */
    public static void delete(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (!selected.isEmpty()) {
            for (TreeItem<DataItem> item : selected) {
                DataItem data = item.getValue();
                if (data != null && !data.getKey().endsWith("...")) {
                    List<DataItem> list = groupedData.get(data.getCategory());
                    if (list != null) {
                        list.remove(data);
                        if (list.isEmpty()) groupedData.remove(data.getCategory());
                    }
                }
            }
            SortAndRefresher.refresh(table, groupedData);
            logger.info("Deleted {} entries.", selected.size());
        }
    }

    /**
     * Pastes the clipboard content into the TreeTableView, adding new entries or updating existing ones.
     * @param table the TreeTableView to paste data into
     * @param groupedData the map containing grouped data by category
     */
    public static void paste(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        List<DataItem> clipboardItems = ClipboardManager.getClipboard();
        if (!clipboardItems.isEmpty()) {
            for (DataItem clipboard : clipboardItems) {
                String key = clipboard.getKey();
                String category = key.contains(".") ? key.split("\\.")[0] : "uncategorized";
                List<DataItem> dataItems = groupedData.computeIfAbsent(category, k -> new ArrayList<>());
                // Check if the key already exists
                Optional<DataItem> existingItemOpt = dataItems.stream()
                        .filter(item -> item.getKey().equals(key))
                        .findFirst();

                if (existingItemOpt.isPresent()) {
                    // Replace existing content
                    DataItem existingItem = existingItemOpt.get();
                    existingItem.setOriginalText(clipboard.getOriginalText());
                    existingItem.setTranslatedText(clipboard.getTranslatedText());
                } else {
                    dataItems.add(new DataItem(category, clipboard.getKey(), clipboard.getOriginalText(), clipboard.getTranslatedText()));
                }
            }
            SortAndRefresher.refresh(table, groupedData);
            logger.info("Pasted {} entries.", clipboardItems.size());
        }
    }

    /**
     * Selects all entries in the TreeTableView.
     *
     * @param table the TreeTableView to select all entries in
     */
    public static void selectAll(TreeTableView<DataItem> table) {
        // 清空之前的选择
        table.getSelectionModel().clearSelection();
        // 递归选中所有叶子节点
        selectAllLeafNodes(table.getRoot(), table);
        logger.info("Selected all entries in the TreeTableView.");
    }

    /**
     * Recursively selects all leaf nodes in the TreeTableView.
     *
     * @param node  the current TreeItem node to check
     * @param table the TreeTableView to select entries in
     */
    private static void selectAllLeafNodes(TreeItem<DataItem> node, TreeTableView<DataItem> table) {
        if (node == null) return;
        if (node.isLeaf() && node.getValue() != null && !node.getValue().getKey().endsWith("...")) {
            int row = table.getRow(node);
            table.getSelectionModel().select(row);
        }
        for (TreeItem<DataItem> child : node.getChildren()) {
            selectAllLeafNodes(child, table);
        }
    }

    /**
     * Unselects all entries in the TreeTableView.
     *
     * @param table the TreeTableView to unselect all entries in
     */
    public static void unselectAll(TreeTableView<DataItem> table) {
        table.getSelectionModel().clearSelection();

        logger.info("Unselected all entries in the TreeTableView.");
    }
}
