package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);
    private final ShowAlert alert = new ShowAlert();

    private final Map<String, List<DataItem>> groupedData = new HashMap<>();

    @FXML
    private VBox rootContainer;
    @FXML
    private TreeTableView<DataItem> translationTreeTable;
    @FXML
    private TreeTableColumn<DataItem, String> keyColumn;
    @FXML
    private TreeTableColumn<DataItem, String> originalColumn;
    @FXML
    private TreeTableColumn<DataItem, String> translatedColumn;

    @FXML
    public void initialize() {
        // Load the top menu bar
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Widgets/TopMenuBar.fxml"));
            MenuBar topMenuBar = loader.load();

            TopMenuBarController topMenuBarController = loader.getController();
            topMenuBarController.setMainController(this);

            rootContainer.getChildren().addFirst(topMenuBar);
        } catch (IOException e) {
            logger.error("Failed to load top menu bar", e);
        }

        // Set up the TreeTableView
        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        translatedColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());

        // Set up the cell value factories for the columns
        originalColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());

        // Enable editing for the TreeTableView
        translationTreeTable.setEditable(true);

        // Right-click context menu for rows
        translationTreeTable.setRowFactory(tv -> {
            TreeTableRow<DataItem> row = new TreeTableRow<>();
            row.setOnContextMenuRequested(event -> {
                DataItem targetItem = (row.getTreeItem() != null && row.getTreeItem().getValue() != null)
                        ? row.getTreeItem().getValue()
                        : null;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Widgets/ContextMenu.fxml"));
                    ContextMenu menu = loader.load();

                    ContextMenuController controller = loader.getController();
                    controller.initializeMenu(groupedData, translationTreeTable, targetItem);

                    menu.show(row, event.getScreenX(), event.getScreenY());
                    event.consume();
                } catch (Exception e) {
                    logger.error("Failed to load context menu", e);
                }
            });
            return row;
        });

        // 空白区域右键
        translationTreeTable.setOnContextMenuRequested(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Widgets/ContextMenu.fxml"));
                ContextMenu menu = loader.load();

                ContextMenuController controller = loader.getController();
                controller.initializeMenu(groupedData, translationTreeTable, null);

                menu.show(translationTreeTable, event.getScreenX(), event.getScreenY());
                event.consume();
            } catch (Exception e) {
                logger.error("Failed to load context menu (empty area)", e);
            }
        });

        // Set up the cell factories and edit commit handlers for the key column
        keyColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        keyColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();

            // Check if the key ends with "..." indicating a group row
            if (item.getKey().endsWith("...")) {
                logger.warn("Editing group rows is not allowed.");
                alert.showAlert(Alert.AlertType.WARNING, "Warning", "Editing group rows is not allowed.");
                SortAndRefresher.refresh(translationTreeTable, groupedData);
                return;
            }

            item.setKey(event.getNewValue());
        });

        // Set up the cell factories and edit commit handlers for original column
        originalColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        originalColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();

            // Check if the original text is "-" indicating a placeholder
            if ("-".equals(item.getOriginalText())) {
                logger.warn("Editing the Original Text with value '-' is not allowed.");
                alert.showAlert(Alert.AlertType.WARNING, "Warning", "Editing Original Text with value '-' is not allowed.");
                SortAndRefresher.refresh(translationTreeTable, groupedData);
                return;
            }

            item.setOriginalText(event.getNewValue());
        });

        // Set up the cell factories and edit commit handlers for translated column
        translatedColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        translatedColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();

            // Check if the translated text is "-" indicating a placeholder
            if ("-".equals(item.getTranslatedText())) {
                logger.warn("Editing the Translated Text with value '-' is not allowed.");
                alert.showAlert(Alert.AlertType.WARNING, "Warning", "Editing Translated Text with value '-' is not allowed.");
                SortAndRefresher.refresh(translationTreeTable, groupedData);
                return;
            }

            item.setTranslatedText(event.getNewValue());
        });

        // Initialize the TreeTableView with an empty root
        translationTreeTable.setRoot(new TreeItem<>(new DataItem("", "", "", "")));
        translationTreeTable.setShowRoot(false);
    }

    // Method to load and display the grouped data in the TreeTableView
    @FXML
    private void handleAddEntry() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }


    // Method to get the grouped data for external use
    public Map<String, List<DataItem>> getGroupedData() {
        return groupedData;
    }

    // Method to get the TreeTableView for external use
    public TreeTableView<DataItem> getTranslationTreeTable() {
        return translationTreeTable;
    }
}
