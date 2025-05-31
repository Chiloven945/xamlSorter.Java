package chiloven.xamlsorter.Controllers;

import chiloven.xamlsorter.Modules.DataItem;
import chiloven.xamlsorter.Modules.SortAndRefresher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TopMenuBarController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleImportOriginalFile() {
        File file = showFileChooser("Import Original XAML File");
        if (file != null) {
            List<DataItem> items = parseXamlFile(file, false);
            Map<String, List<DataItem>> groupedData = mainController.getGroupedData();
            groupedData.clear();
            groupedData.putAll(groupByCategory(items));
            SortAndRefresher.refresh(mainController.getTranslationTreeTable(), groupedData);
        }
    }

    @FXML
    private void handleImportTargetFile() {
        File file = showFileChooser("Import Translation XAML File");
        if (file != null) {
            List<DataItem> translations = parseXamlFile(file, true);
            applyTranslations(translations, mainController.getGroupedData());
            SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
        }
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
            controller.setData(mainController.getGroupedData(), null);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                controller.applyChanges();

                // 重新分组后刷新
                Map<String, List<DataItem>> regrouped = mainController.getGroupedData()
                        .values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.groupingBy(DataItem::getCategory));

                mainController.getGroupedData().clear();
                mainController.getGroupedData().putAll(regrouped);

                SortAndRefresher.refresh(mainController.getTranslationTreeTable(), mainController.getGroupedData());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                            items.add(new DataItem(category, key, "", value));
                        } else {
                            items.add(new DataItem(category, key, value, ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private Map<String, List<DataItem>> groupByCategory(List<DataItem> items) {
        return items.stream().collect(Collectors.groupingBy(DataItem::getCategory));
    }

    private void applyTranslations(List<DataItem> translations, Map<String, List<DataItem>> groupedData) {
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
}
