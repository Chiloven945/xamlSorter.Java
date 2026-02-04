package top.chiloven.xamlsorter.modules;

import top.chiloven.xamlsorter.entities.DataItem;
import top.chiloven.xamlsorter.ui.MainPage;
import top.chiloven.xamlsorter.utils.ShowAlert;
import top.chiloven.xamlsorter.utils.TaskExecutorService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static top.chiloven.xamlsorter.modules.I18n.getLang;

public class DataOperationHelper {
    private static final Logger logger = LogManager.getLogger(DataOperationHelper.class);
    private static MainPage mainPage;

    public static void setMainPage(MainPage page) {
        mainPage = page;
    }

    /**
     * Applies updates to a specific column in the grouped data.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param updates the list of DataItem updates to apply
     * @param grouped the map containing grouped data by category
     * @param column  the column to update ("original" or "translated")
     */
    public static void applyColumnUpdates(List<DataItem> updates, Map<String, List<DataItem>> grouped, String column) {
        logger.info("Importing updates for column: {}", column);

        TaskExecutorService.executeTask(
                "ColumnUpdate-" + column,
                () -> {
                    // Flatten all the grouped data into "key → DataItem" map
                    Map<String, DataItem> existingMap = grouped.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toMap(DataItem::getKey, item -> item, (a, b) -> a));

                    for (DataItem update : updates) {
                        String key = update.getKey();
                        DataItem existing = existingMap.get(key);

                        if (existing != null) {
                            switch (column) {
                                case "original" -> {
                                    logger.trace("Updating original text for key: {}", key);
                                    existing.setOriginalText(update.getOriginalText());
                                }
                                case "translated" -> {
                                    logger.trace("Updating translated text for key: {}", key);
                                    existing.setTranslatedText(update.getTranslatedText());
                                }
                            }
                        } else {
                            String original = column.equals("original") ? update.getOriginalText() : "";
                            String translated = column.equals("translated") ? update.getTranslatedText() : "";

                            logger.trace("Adding new DataItem for key: {} in category: {}", key, update.getCategory());
                            DataItem newItem = new DataItem(update.getCategory(), key, original, translated);
                            grouped.computeIfAbsent(update.getCategory(), k -> new ArrayList<>()).add(newItem);
                        }
                    }

                    return grouped;
                },
                result -> {
                    mainPage.setModified(true);
                    logger.info("Column updates applied for column: {}", column);
                },
                error -> logger.error("Error applying column updates for column: {}", column, error)
        );
    }

    /**
     * Groups a list of DataItem objects by their category.
     * This method processes data in a background thread but waits for the result.
     *
     * @param items the list of DataItem objects to group
     * @return a map where the key is the category and the value is a list of DataItem objects in that category
     */
    public static Map<String, List<DataItem>> groupByCategory(List<DataItem> items) {
        logger.info("Grouping {} items by category.", items.size());

        return TaskExecutorService.submitTask(
                "GroupByCategory",
                () -> {
                    for (DataItem item : items) {
                        String key = item.getKey();
                        String category = key != null && key.contains(".")
                                ? key.substring(0, key.indexOf('.'))
                                : key;
                        item.setCategory(category);
                        logger.debug("Set category '{}' for key '{}'.", category, key);
                    }
                    Map<String, List<DataItem>> grouped = items.stream()
                            .collect(Collectors.groupingBy(DataItem::getCategory, LinkedHashMap::new, Collectors.toList()));
                    logger.info("Grouped into {} categories.", grouped.size());
                    return grouped;
                }
        ).join();
    }

    /**
     * Adds a new entry to the grouped data.
     * This method shows a dialog in the UI thread and processes data in a background thread.
     *
     * @param groupedData the map containing grouped data by category
     */
    public static void addEntry(Map<String, List<DataItem>> groupedData) {
        logger.debug("addEntry called with groupedData size: {}", groupedData.size());
        TextInputDialog dialog = new TextInputDialog();

        Scene scene = dialog.getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        dialog.setTitle(getLang("module.data_op.add_entry.title"));
        dialog.setHeaderText(getLang("module.data_op.add_entry.header"));
        logger.info("Opening dialog to add new entry.");
        Optional<String> result = dialog.showAndWait();

        logger.info("User input for new key: {}", result.orElse("No input provided"));

        // Check if the user provided a key
        result.ifPresent(newKey -> TaskExecutorService.executeTask(
                "AddEntry-" + newKey,
                () -> {
                    logger.debug("Processing new key: {}", newKey);
                    String category = newKey.contains(".") ? newKey.split("\\.")[0] : getLang("page.main.tree_table.item.uncategorized");
                    logger.debug("Determined category: {}", category);

                    // Check if the key already exists in the grouped data
                    boolean exists = groupedData.values().stream()
                            .flatMap(List::stream)
                            .anyMatch(item -> item.getKey().equals(newKey));
                    logger.debug("Key '{}' exists: {}", newKey, exists);

                    return new Object[]{exists, category, newKey};
                },
                result1 -> {
                    boolean exists = (boolean) ((Object[]) result1)[0];
                    String category = (String) ((Object[]) result1)[1];
                    String key = (String) ((Object[]) result1)[2];

                    // If the key already exists, show a warning and return
                    if (exists) {
                        ShowAlert.warn(
                                getLang("general.alert.warn"),
                                getLang("module.data_op.add_entry.exception.alert.header"),
                                getLang("module.data_op.add_entry.exception.alert.content", key)
                        );
                        logger.warn("Attempted to add duplicate entry with key: {}", key);
                        return;
                    }

                    // If the key does not exist, create a new DataItem and add it to the grouped data
                    DataItem newItem = new DataItem(
                            category, key,
                            getLang("module.data_op.add_entry.default.original"),
                            getLang("module.data_op.add_entry.default.translated")
                    );
                    logger.debug("Created new DataItem: {}", newItem);
                    groupedData.computeIfAbsent(category, k -> new ArrayList<>()).add(newItem);
                    logger.debug("Added new DataItem to groupedData under category '{}'", category);

                    SortAndRefresher.refresh(mainPage.getDataTreeTable(), mainPage.getGroupedData());
                    mainPage.setModified(true);

                    logger.info("Added new entry with key: {}", key);
                },
                error -> logger.error("Error adding entry with key: {}", newKey, error)
        ));
        logger.debug("addEntry finished");
    }

    /**
     * Cuts the selected entries from the TreeTableView and copies them to the clipboard.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param table       the TreeTableView containing the data items
     * @param groupedData the map containing grouped data by category
     * @see #copy(TreeTableView)
     * @see #delete(TreeTableView, Map)
     */
    public static void cut(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        logger.debug("cut called with groupedData size: {}", groupedData.size());

        TaskExecutorService.executeTask(
                "CutOperation",
                () -> {
                    List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
                    logger.debug("Selected items count: {}", selected.size());
                    List<DataItem> toCopy = selected.stream()
                            .map(TreeItem::getValue)
                            .filter(item -> item != null && !item.getKey().endsWith("..."))
                            .collect(Collectors.toList());
                    logger.debug("Items to copy: {}", toCopy.size());

                    if (!toCopy.isEmpty()) {
                        ClipboardManager.copyFrom(toCopy);
                        logger.info("Copied {} entries.", toCopy.size());
                    } else {
                        logger.debug("No items to copy.");
                        return false;
                    }

                    return true;
                },
                result -> {
                    if (result) {
                        delete(table, groupedData);
                        logger.info("Cut finished (copied and deleted selected entries).");
                    }
                },
                error -> logger.error("Error during cut operation", error)
        );
    }


    /**
     * Copies the selected entries from the TreeTableView to the clipboard.
     * This method processes data in a background thread.
     *
     * @param table the TreeTableView containing the data items
     */
    public static void copy(TreeTableView<DataItem> table) {
        logger.debug("copy called");

        TaskExecutorService.executeTask(
                "CopyOperation",
                () -> {
                    List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
                    logger.debug("Selected items count: {}", selected.size());
                    List<DataItem> toCopy = selected.stream()
                            .map(TreeItem::getValue)
                            .filter(item -> item != null && !item.getKey().endsWith("..."))
                            .collect(Collectors.toList());
                    logger.debug("Items to copy: {}", toCopy.size());

                    if (!toCopy.isEmpty()) {
                        ClipboardManager.copyFrom(toCopy);
                        return toCopy.size();
                    } else {
                        logger.debug("No items to copy.");
                        return 0;
                    }
                },
                count -> {
                    if (count > 0) {
                        logger.info("Copied {} entries.", count);
                    }
                },
                error -> logger.error("Error during copy operation", error)
        );
    }

    /**
     * Deletes the selected entries from the TreeTableView and updates the grouped data.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param table       the TreeTableView containing the data items
     * @param groupedData the map containing grouped data by category
     */
    public static void delete(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        logger.debug("delete called with groupedData size: {}", groupedData.size());

        TaskExecutorService.executeTask(
                "DeleteOperation",
                () -> {
                    List<TreeItem<DataItem>> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
                    logger.debug("Selected items count: {}", selected.size());

                    if (selected.isEmpty()) {
                        logger.debug("No items selected for delete.");
                        return 0;
                    }

                    int count = 0;
                    for (TreeItem<DataItem> item : selected) {
                        DataItem data = item.getValue();
                        logger.trace("Processing item for delete: {}", data);
                        if (data != null && !data.getKey().endsWith("...")) {
                            List<DataItem> list = groupedData.get(data.getCategory());
                            logger.trace("Found list for category '{}': {}", data.getCategory(), list != null);
                            if (list != null) {
                                list.remove(data);
                                count++;
                                logger.trace("Removed data from list. List size now: {}", list.size());
                                if (list.isEmpty()) {
                                    groupedData.remove(data.getCategory());
                                    logger.trace("Removed empty category: {}", data.getCategory());
                                }
                            }
                        }
                    }

                    return count;
                },
                count -> {
                    if (count > 0) {
                        SortAndRefresher.refresh(table, groupedData);
                        mainPage.setModified(true);
                        logger.info("Deleted {} entries.", count);
                    }
                },
                error -> logger.error("Error during delete operation", error)
        );
    }

    /**
     * Pastes the clipboard content into the TreeTableView, adding new entries or updating existing ones.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param table       the TreeTableView to paste data into
     * @param groupedData the map containing grouped data by category
     */
    public static void paste(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        logger.debug("paste called with groupedData size: {}", groupedData.size());

        TaskExecutorService.executeTask(
                "PasteOperation",
                () -> {
                    List<DataItem> clipboardItems = ClipboardManager.getClipboard();
                    logger.debug("Clipboard items count: {}", clipboardItems.size());

                    if (clipboardItems.isEmpty()) {
                        logger.debug("No clipboard items to paste.");
                        return 0;
                    }

                    for (DataItem clipboard : clipboardItems) {
                        String key = clipboard.getKey();
                        String category = key.contains(".") ? key.split("\\.")[0] : getLang("page.main.tree_table.item.uncategorized");
                        logger.trace("Processing clipboard item with key: {}, category: {}", key, category);
                        List<DataItem> dataItems = groupedData.computeIfAbsent(category, k -> new ArrayList<>());
                        // Check if the key already exists
                        Optional<DataItem> existingItemOpt = dataItems.stream()
                                .filter(item -> item.getKey().equals(key))
                                .findFirst();

                        if (existingItemOpt.isPresent()) {
                            // Replace existing content
                            DataItem existingItem = existingItemOpt.get();
                            logger.debug("Updating existing DataItem for key: {}", key);
                            existingItem.setOriginalText(clipboard.getOriginalText());
                            existingItem.setTranslatedText(clipboard.getTranslatedText());
                        } else {
                            logger.debug("Adding new DataItem for key: {}", key);
                            dataItems.add(new DataItem(category, clipboard.getKey(), clipboard.getOriginalText(), clipboard.getTranslatedText()));
                        }
                    }

                    return clipboardItems.size();
                },
                count -> {
                    if (count > 0) {
                        SortAndRefresher.refresh(table, groupedData);
                        mainPage.setModified(true);
                        logger.info("Pasted {} entries.", count);
                    }
                },
                error -> logger.error("Error during paste operation", error)
        );
    }

    /**
     * Selects all entries in the TreeTableView.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param table the TreeTableView to select all entries in
     */
    public static void selectAll(TreeTableView<DataItem> table) {
        logger.debug("selectAll called on table: {}", table);

        TaskExecutorService.executeTask(
                "SelectAllOperation",
                () -> {
                    TreeItem<DataItem> root = table.getRoot();
                    if (root == null) {
                        return new ArrayList<Integer>();
                    }

                    List<Integer> rowsToSelect = new ArrayList<>();
                    collectLeafNodeRows(root, table, rowsToSelect);
                    return rowsToSelect;
                },
                rowsToSelect -> {
                    // Clear previous selections
                    table.getSelectionModel().clearSelection();
                    logger.trace("Cleared previous selections.");

                    // Select all collected rows
                    for (Integer row : rowsToSelect) {
                        table.getSelectionModel().select(row);
                    }

                    logger.info("Selected all entries in the TreeTableView.");
                },
                error -> logger.error("Error during select all operation", error)
        );
    }

    /**
     * Collects all leaf node rows in the TreeTableView.
     *
     * @param node  the current TreeItem node to check
     * @param table the TreeTableView to get row indices from
     * @param rows  the list to collect row indices in
     */
    private static void collectLeafNodeRows(TreeItem<DataItem> node, TreeTableView<DataItem> table, List<Integer> rows) {
        logger.trace("collectLeafNodeRows called for node: {}", node != null ? node.getValue() : "null");
        if (node == null) {
            logger.debug("Node is null, returning.");
            return;
        }
        if (node.isLeaf() && node.getValue() != null && !node.getValue().getKey().endsWith("...")) {
            int row = table.getRow(node);
            logger.trace("Collecting leaf node at row: {}, key: {}", row, node.getValue().getKey());
            rows.add(row);
        }
        for (TreeItem<DataItem> child : node.getChildren()) {
            logger.trace("Recursing into child node: {}", child.getValue() != null ? child.getValue().getKey() : "null");
            collectLeafNodeRows(child, table, rows);
        }
    }

    /**
     * Unselects all entries in the TreeTableView.
     * This method updates the UI in the JavaFX application thread.
     *
     * @param table the TreeTableView to unselect all entries in
     */
    public static void unselectAll(TreeTableView<DataItem> table) {
        logger.debug("unselectAll called on table: {}", table);

        Platform.runLater(() -> {
            table.getSelectionModel().clearSelection();
            logger.info("Unselected all entries in the TreeTableView.");
        });
    }

}
