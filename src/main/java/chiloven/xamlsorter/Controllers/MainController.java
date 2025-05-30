package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.ShowAlert;
import chiloven.xamlsorter.Modules.SortAndRefresher;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);
    public final Map<String, List<DataItem>> groupedData = new HashMap<>();
    private final ShowAlert alert = new ShowAlert();
    public DataItem clipboard;
    @FXML
    public TreeTableView<DataItem> translationTreeTable;
    @FXML
    private TreeTableColumn<DataItem, String> keyColumn;
    @FXML
    private TreeTableColumn<DataItem, String> originalColumn;
    @FXML
    private TreeTableColumn<DataItem, String> translatedColumn;

    @FXML
    public void initialize() {
        translationTreeTable.setEditable(true);

        translationTreeTable.setRowFactory(tv -> {
            TreeTableRow<DataItem> row = new TreeTableRow<>();
            row.setOnContextMenuRequested(event -> {
                TreeItem<DataItem> treeItem = row.getTreeItem();
                if (treeItem != null && !treeItem.getValue().getKey().endsWith("...")) {
                    ContextMenu contextMenu = ContextMenuController.create(groupedData, translationTreeTable, treeItem.getValue(), clipboard);
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });
            return row;
        });

        keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        keyColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        keyColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();

            // 检查是否是分组行
            if (item.getKey().endsWith("...")) {
                logger.warn("Editing group rows is not allowed.");
                alert.showAlert(Alert.AlertType.WARNING, "Warning", "Editing group rows is not allowed.");
                SortAndRefresher.refresh(translationTreeTable, groupedData);
                return;
            }

            String newKey = event.getNewValue();
            String oldCategory = item.getCategory();
            String newCategory = newKey.contains(".") ? newKey.split("\\.")[0] : "uncategorized";

            // 从旧分组移除
            List<DataItem> oldList = groupedData.get(oldCategory);
            if (oldList != null) {
                oldList.remove(item);
                if (oldList.isEmpty()) {
                    groupedData.remove(oldCategory);
                }
            }

            // 更新 key 和 category
            item.setKey(newKey);
            item.setCategory(newCategory);

            // 放入新分组
            groupedData.computeIfAbsent(newCategory, k -> new ArrayList<>()).add(item);

            SortAndRefresher.refresh(translationTreeTable, groupedData);
        });

        originalColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getOriginalText()));
        originalColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        originalColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            item.setOriginalText(event.getNewValue());
        });

        translatedColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getTranslatedText()));
        translatedColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        translatedColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            item.setTranslatedText(event.getNewValue());
        });
    }

    @FXML
    private void handleImportOriginalFile() {
        File file = showFileChooser("Import Original XAML File");
        if (file != null) {
            List<DataItem> items = parseXamlFile(file, false);
            groupedData.clear();
            groupedData.putAll(groupByCategory(items));
            populateTreeView();
            logger.info("Imported original file: {} ({} items)", file.getName(), items.size());
        }
    }

    @FXML
    private void handleImportTargetFile() {
        File file = showFileChooser("Import Translation XAML File");
        if (file != null) {
            List<DataItem> translations = parseXamlFile(file, true);
            applyTranslations(translations);
            populateTreeView();
            logger.info("Imported translation file: {} ({} items)", file.getName(), translations.size());
        }
    }

    private File showFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(null);
    }

    private List<DataItem> parseXamlFile(File file, boolean isTranslation) {
        List<DataItem> items = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList allNodes = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String localName = elem.getLocalName();

                    if ("String".equals(localName)) {
                        String key = elem.getAttribute("x:Key");
                        String value = elem.getTextContent().trim();
                        String category = key.contains(".") ? key.split("\\.")[0] : "uncategorized";

                        if (isTranslation) {
                            // 仅保存 key + 翻译
                            items.add(new DataItem(category, key, "", value));
                        } else {
                            // 保存 key + 原文
                            items.add(new DataItem(category, key, value, ""));
                        }
                    }
                }
            }
            logger.info("Parsed {} items from {}", items.size(), file.getName());
        } catch (Exception e) {
            logger.error("Failed to parse XAML file: {}", file.getName(), e);
        }
        return items;
    }

    private Map<String, List<DataItem>> groupByCategory(List<DataItem> items) {
        return items.stream().collect(Collectors.groupingBy(DataItem::getCategory));
    }

    private void applyTranslations(List<DataItem> translations) {
        Map<String, String> translationMap = translations.stream()
                .collect(Collectors.toMap(DataItem::getKey, DataItem::getTranslatedText));

        for (List<DataItem> group : groupedData.values()) {
            for (DataItem item : group) {
                String translated = translationMap.get(item.getKey());
                if (translated != null) {
                    item.setTranslatedText(translated);
                }
            }
        }
    }

    private void populateTreeView() {
        TreeItem<DataItem> root = new TreeItem<>(new DataItem("", "", "", ""));
        root.setExpanded(true);

        for (Map.Entry<String, List<DataItem>> entry : groupedData.entrySet()) {
            String category = entry.getKey();
            List<DataItem> items = entry.getValue();

            DataItem categoryItem = new DataItem(category, category + "...", "-", "-");
            TreeItem<DataItem> categoryNode = new TreeItem<>(categoryItem);

            for (DataItem item : items) {
                TreeItem<DataItem> itemNode = new TreeItem<>(item);
                categoryNode.getChildren().add(itemNode);
            }

            root.getChildren().add(categoryNode);
        }

        translationTreeTable.setRoot(root);
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

    @FXML
    private void handleRegexEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dialogs/RegexEditDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Batch Regex Edit");

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

            RegexEditDialogController controller = loader.getController();
            controller.setData(groupedData, null);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                controller.applyChanges();  // 主动调用控制器的应用方法
                SortAndRefresher.refresh(translationTreeTable, groupedData);
            }
        } catch (Exception e) {
            logger.error("Failed to open regex edit dialog", e);
        }
    }


}
