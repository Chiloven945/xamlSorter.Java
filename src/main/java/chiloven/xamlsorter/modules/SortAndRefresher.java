package chiloven.xamlsorter.modules;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortAndRefresher {
    private static final Logger logger = LogManager.getLogger(SortAndRefresher.class);

    /**
     * Refreshes the TreeTableView with the given grouped data.
     *
     * @param table       the TreeTableView to refresh
     * @param groupedData the data grouped by category
     * @see #buildTree(Map, String, String, String)
     */
    public static void refresh(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {

        TreeItem<DataItem> root = buildTree(groupedData, null, null, null);
        table.setRoot(root);
        table.setShowRoot(false);
        logger.info("Sorted and refreshed {}.", table.getId());
    }

    /*
     * Refreshes the TreeTableView with the given grouped data and applies a regex replacement
     *
     * @param table         the TreeTableView to refresh
     * @param groupedData   the data grouped by category
     * @param pattern       the regex pattern to match
     * @param replacement   the replacement string for the matched pattern
     * @param replaceTarget the target field to apply the replacement on (e.g., "Key", "Original Text")
     * @see #buildTree(Map, String, String, String)
     *//*
    public static void refresh(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData,
                               String pattern, String replacement, String replaceTarget) {
        TreeItem<DataItem> root = buildTree(groupedData, pattern, replacement, replaceTarget);
        table.setRoot(root);
        table.setShowRoot(false);
        logger.info("Sorted and refreshed {} matching regex \"{}\" to replace with \"{}\", targeting {}.", table.getId(), pattern, replacement, replaceTarget);
    }*/

    /**
     * Builds a TreeItem structure from the grouped data, applying regex replacements if specified.
     *
     * @param groupedData   the data grouped by category
     * @param pattern       the regex pattern to match for replacement
     * @param replacement   the replacement string for the matched pattern
     * @param replaceTarget the target field to apply the replacement on (e.g., "Key", "Original Text")
     * @return a TreeItem representing the root of the tree structure
     */
    private static TreeItem<DataItem> buildTree(Map<String, List<DataItem>> groupedData,
                                                String pattern, String replacement, String replaceTarget) {
        TreeItem<DataItem> root = new TreeItem<>(new DataItem("", "", "", ""));
        root.setExpanded(true);

        Map<String, List<DataItem>> sortedGroupedData = new TreeMap<>(groupedData);

        // If a pattern is provided, apply the regex replacement to the keys
        for (Map.Entry<String, List<DataItem>> entry : sortedGroupedData.entrySet()) {
            String category = entry.getKey();
            List<DataItem> items = entry.getValue();

            String displayCategory = ("Key".equals(replaceTarget)) ?
                    category.replaceAll(pattern, replacement) : category;

            // Create a new DataItem for the category
            DataItem categoryItem = new DataItem(displayCategory, displayCategory + "...", "-", "-");
            TreeItem<DataItem> categoryNode = new TreeItem<>(categoryItem);
            categoryNode.setExpanded(true);

            // Add items to the category node
            for (DataItem item : items) {
                categoryNode.getChildren().add(new TreeItem<>(item));
            }

            root.getChildren().add(categoryNode);
        }

        return root;
    }

}
