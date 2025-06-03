package chiloven.xamlsorter.Modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileProcessor {
    private static final Logger logger = LogManager.getLogger(FileProcessor.class);

    /**
     * Synchronously parses a XAML file and extracts data items.
     *
     * @param file          the XAML file to parse
     * @param isTranslation if true, treats the file as a translation file; otherwise, as an original file
     * @return a list of DataItem objects
     */
    public static List<DataItem> parseXamlFile(File file, boolean isTranslation) {
        List<DataItem> items = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList allNodes = doc.getDocumentElement().getChildNodes();
            int total = allNodes.getLength();

            for (int i = 0; i < total; i++) {
                Node node = allNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String localName = elem.getLocalName();

                    if ("String".equals(localName)) {
                        String key = elem.getAttribute("x:Key");
                        if (key == null || key.isEmpty()) {
                            key = "unnamed";
                        }
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
            logger.error("Error parsing XAML file: {}", file.getAbsolutePath(), e);
        }
        return items;
    }

    // Group DataItems by category
    public static Map<String, List<DataItem>> groupByCategory(List<DataItem> items) {
        return items.stream().collect(Collectors.groupingBy(DataItem::getCategory));
    }

    // Apply translations to the grouped data
    public static void applyTranslations(List<DataItem> translations, Map<String, List<DataItem>> groupedData) {
        Map<String, String> translationMap = translations.stream()
                .collect(Collectors.toMap(DataItem::getKey, DataItem::getTranslatedText));

        // Iterate through each group and apply translations
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
