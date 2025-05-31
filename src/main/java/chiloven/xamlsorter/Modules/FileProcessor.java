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
     * Analyze a XAML file and extract data items.
     *
     * @param file          the XAML file to parse
     * @param isTranslation if true, treat the file as a translation file; if false, treat it as an original file
     * @return a list of DataItem objects extracted from the file
     */
    public static List<DataItem> parseXamlFile(File file, boolean isTranslation) {
        List<DataItem> items = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList allNodes = doc.getDocumentElement().getChildNodes();

            // Iterate all nodes and extract String elements
            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);
                // Check if the node is an element and has the local name "String"
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String localName = elem.getLocalName();

                    // Check if the element is a String element
                    if ("String".equals(localName)) {
                        String key = elem.getAttribute("x:Key");
                        String value = elem.getTextContent().trim();
                        String category = key.contains(".") ? key.split("\\.")[0] : "uncategorized";

                        // Create a DataItem based on whether it's a translation or original file
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

    // Other utility methods can be added here as needed
}
