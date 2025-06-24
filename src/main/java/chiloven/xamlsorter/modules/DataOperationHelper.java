package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class DataOperationHelper {
    private static final Logger logger = LogManager.getLogger(DataOperationHelper.class);
    private static MainController mainController = new MainController();

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Applies updates to a specific column in the grouped data.
     *
     * @param updates the list of DataItem updates to apply
     * @param grouped the map containing grouped data by category
     * @param column  the column to update ("original" or "translated")
     */
    public static void applyColumnUpdates(List<DataItem> updates, Map<String, List<DataItem>> grouped, String column) {
        logger.info("Importing updates for column: {}", column);

        // Flatten all the grouped data into "key → DataItem" map
        Map<String, DataItem> existingMap = grouped.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(DataItem::getKey, item -> item, (a, b) -> a));

        for (DataItem update : updates) {
            String key = update.getKey();
            DataItem existing = existingMap.get(key);

            if (existing != null) {
                switch (column) {
                    case "original" -> existing.setOriginalText(update.getOriginalText());
                    case "translated" -> existing.setTranslatedText(update.getTranslatedText());
                }
            } else {
                String original = column.equals("original") ? update.getOriginalText() : "";
                String translated = column.equals("translated") ? update.getTranslatedText() : "";

                DataItem newItem = new DataItem(update.getCategory(), key, original, translated);
                grouped.computeIfAbsent(update.getCategory(), k -> new ArrayList<>()).add(newItem);
            }
        }
        mainController.setModified(true);
    }

    /**
     * Groups a list of DataItem objects by their category.
     *
     * @param items the list of DataItem objects to group
     * @return a map where the key is the category and the value is a list of DataItem objects in that category
     */
    public static Map<String, List<DataItem>> groupByCategory(List<DataItem> items) {
        for (DataItem item : items) {
            String key = item.getKey();
            String category = key != null && key.contains(".")
                    ? key.substring(0, key.indexOf('.'))
                    : key;
            item.setCategory(category);
        }
        return items.stream().collect(Collectors.groupingBy(DataItem::getCategory, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Adds a new entry to the grouped data.
     *
     * @param groupedData the map containing grouped data by category
     */
    public static void addEntry(Map<String, List<DataItem>> groupedData) {
        TextInputDialog dialog = new TextInputDialog();

        Scene scene = dialog.getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        dialog.setTitle(getLang("module.data_op.add_entry.title"));
        dialog.setHeaderText(getLang("module.data_op.add_entry.header"));
        logger.info("Opening dialog to add new entry.");
        Optional<String> result = dialog.showAndWait();

        logger.info("User input for new key: {}", result.orElse("No input provided"));

        // Check if the user provided a key
        result.ifPresent(newKey -> {
            String category = newKey.contains(".") ? newKey.split("\\.")[0] : getLang("page.main.tree_table.item.uncategorized");

            // Check if the key already exists in the grouped data
            boolean exists = groupedData.values().stream()
                    .flatMap(List::stream)
                    .anyMatch(item -> item.getKey().equals(newKey));

            // If the key already exists, show a warning and return
            if (exists) {
                ShowAlert.warn(
                        getLang("general.alert.warn"),
                        getLang("module.data_op.add_entry.exception.alert.header"),
                        getLang("module.data_op.add_entry.exception.alert.content", newKey)
                );
                logger.warn("Attempted to add duplicate entry with key: {}", newKey);
                return;
            }

            // If the key does not exist, create a new DataItem and add it to the grouped data
            DataItem newItem = new DataItem(
                    category, newKey,
                    getLang("module.data_op.add_entry.default.original"),
                    getLang("module.data_op.add_entry.default.translated")
            );
            groupedData.computeIfAbsent(category, k -> new ArrayList<>()).add(newItem);

            SortAndRefresher.refresh(mainController.getDataTreeTable(), mainController.getGroupedData());
            mainController.setModified(true);

            logger.info("Added new entry with key: {}", newKey);
        });
    }

    /**
     * Cuts the selected entries from the TreeTableView and copies them to the clipboard.
     *
     * @param table       the TreeTableView containing the data items
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
            mainController.setModified(true);
            logger.info("Cut {} entries.", toCopy.size());
        }
    }

    /**
     * Copies the selected entries from the TreeTableView to the clipboard.
     *
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
     *
     * @param table       the TreeTableView containing the data items
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
            mainController.setModified(true);
            logger.info("Deleted {} entries.", selected.size());
        }
    }

    /**
     * Pastes the clipboard content into the TreeTableView, adding new entries or updating existing ones.
     *
     * @param table       the TreeTableView to paste data into
     * @param groupedData the map containing grouped data by category
     */
    public static void paste(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        List<DataItem> clipboardItems = ClipboardManager.getClipboard();
        if (!clipboardItems.isEmpty()) {
            for (DataItem clipboard : clipboardItems) {
                String key = clipboard.getKey();
                String category = key.contains(".") ? key.split("\\.")[0] : getLang("page.main.tree_table.item.uncategorized");
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
            mainController.setModified(true);
            logger.info("Pasted {} entries.", clipboardItems.size());
        }
    }

    /**
     * Selects all entries in the TreeTableView.
     *
     * @param table the TreeTableView to select all entries in
     */
    public static void selectAll(TreeTableView<DataItem> table) {
        // Clear previous selections
        table.getSelectionModel().clearSelection();
        // Recursively select all leaf nodes in the TreeTableView
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
