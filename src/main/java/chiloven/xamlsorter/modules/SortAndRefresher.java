package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.entities.DataItem;
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
     */
    public static void refresh(TreeTableView<DataItem> table, Map<String, List<DataItem>> groupedData) {
        TreeItem<DataItem> root = new TreeItem<>(new DataItem("", "", "", ""));
        root.setExpanded(true);

        Map<String, List<DataItem>> sortedGroupedData = new TreeMap<>(groupedData);

        for (Map.Entry<String, List<DataItem>> entry : sortedGroupedData.entrySet()) {
            String category = entry.getKey();
            List<DataItem> items = entry.getValue();

            DataItem categoryItem = new DataItem(category, category + "...", "-", "-");
            TreeItem<DataItem> categoryNode = new TreeItem<>(categoryItem);
            categoryNode.setExpanded(true);

            for (DataItem item : items) {
                categoryNode.getChildren().add(new TreeItem<>(item));
            }

            root.getChildren().add(categoryNode);
        }

        table.setRoot(root);
        table.setShowRoot(false);
        logger.info("Sorted and refreshed {}.", table.getId());
    }

}
