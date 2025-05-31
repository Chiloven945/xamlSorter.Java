package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    private final Map<String, List<DataItem>> groupedData = new HashMap<>();
    public DataItem clipboard;

    @FXML private VBox rootContainer;
    @FXML private TreeTableView<DataItem> translationTreeTable;
    @FXML private TreeTableColumn<DataItem, String> keyColumn;
    @FXML private TreeTableColumn<DataItem, String> originalColumn;
    @FXML private TreeTableColumn<DataItem, String> translatedColumn;

    @FXML
    public void initialize() {
        // 加载顶部菜单栏
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Widgets/TopMenuBar.fxml"));
            MenuBar topMenuBar = loader.load();

            TopMenuBarController topMenuBarController = loader.getController();
            topMenuBarController.setMainController(this);

            rootContainer.getChildren().add(0, topMenuBar);
        } catch (IOException e) {
            logger.error("Failed to load top menu bar", e);
        }

        // 设置列绑定
        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());

        translationTreeTable.setEditable(true);

        // keyColumn 用单行编辑
        keyColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        keyColumn.setOnEditCommit(event ->
                event.getRowValue().getValue().setKey(event.getNewValue())
        );

        // originalColumn 用多行编辑
        originalColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        originalColumn.setOnEditCommit(event ->
                event.getRowValue().getValue().setOriginalText(event.getNewValue())
        );

        // translatedColumn 用多行编辑
        translatedColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        translatedColumn.setOnEditCommit(event ->
                event.getRowValue().getValue().setTranslatedText(event.getNewValue())
        );

        // 初始化空 root
        translationTreeTable.setRoot(new TreeItem<>(new DataItem("", "", "", "")));
        translationTreeTable.setShowRoot(false);
    }

    @FXML
    private void handleAddEntry() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Entry");
        dialog.setHeaderText("Enter new key (e.g., common.new.key):");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newKey -> {
            String category = newKey.contains(".") ? newKey.split("\\.")[0] : "uncategorized";
            DataItem newItem = new DataItem(category, newKey, "New Original", "New Translation");

            groupedData.computeIfAbsent(category, k -> new ArrayList<>()).add(newItem);
            SortAndRefresher.refresh(translationTreeTable, groupedData);
        });
    }

    // === Getter methods for external controllers ===

    public Map<String, List<DataItem>> getGroupedData() {
        return groupedData;
    }

    public TreeTableView<DataItem> getTranslationTreeTable() {
        return translationTreeTable;
    }
}
