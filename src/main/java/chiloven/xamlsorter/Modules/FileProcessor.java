package chiloven.xamlsorter.Modules;

import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class FileProcessor {
    private static final Logger logger = LogManager.getLogger(FileProcessor.class);

    // ===============================
    // 1️⃣ Importing
    // ===============================

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
            // Create a DocumentBuilderFactory and configure it for namespace awareness
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            // Get all child nodes of the document element
            NodeList allNodes = doc.getDocumentElement().getChildNodes();
            int total = allNodes.getLength();

            // Iterate through all nodes and extract String elements
            for (int i = 0; i < total; i++) {
                Node node = allNodes.item(i);
                // Check if the node is an element node
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String localName = elem.getLocalName();

                    // Check if the element is a String element
                    if ("String".equals(localName)) {
                        String key = elem.getAttribute("x:Key");

                        // If the key is not set, use the element's text content as the key
                        if (key == null || key.isEmpty()) key = "unnamed";

                        // If the key is not a valid identifier, log a warning
                        String value = elem.getTextContent().trim();
                        String category = key.contains(".") ? key.split("\\.")[0] : "uncategorized";
                        items.add(new DataItem(category, key, isTranslation ? "" : value, isTranslation ? value : ""));
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

    // ===============================
    // 2️⃣ Exporting
    // ===============================

    /**
     * Dispatches export based on file type.
     *
     * @param file                the output file
     * @param fileType            file type string (e.g., "xaml", "json", etc.)
     * @param fieldToExport       which field to export ("Original" or "Translated")
     * @param addComments         whether to add top-level comments
     * @param groupedData         grouped data to export
     *
     * @throws IllegalArgumentException if the file type is unsupported
     */
    public static void exportToFile(File file, String fileType, String fieldToExport, boolean addComments, Map<String, List<DataItem>> groupedData) {
        try {
            switch (fileType.toLowerCase()) {
                case ".xaml" -> exportToXamlFile(file, fieldToExport, addComments, groupedData);
                // case "json" -> exportToJsonFile(...);  // For future
                // case "resx" -> exportToResxFile(...);
                default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }
        } catch (Exception e) {
            logger.error("Unexpected file format: {}", fileType, e);
            new ShowAlert().showAlert(Alert.AlertType.ERROR, "Unsupported Format", "Unsupported export format: " + fileType);
        }
    }

    /**
     * Exports the grouped data to a XAML file with optional comments.
     *
     * @param file          target file
     * @param fieldToExport "Original" or "Translated"
     * @param addComments   whether to include top-level category comments
     * @param groupedData   data grouped by category
     */
    public static void exportToXamlFile(File file, String fieldToExport, boolean addComments, Map<String, List<DataItem>> groupedData) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("<!-- Exported by xamlSorter.Java -->");
            writer.println("<ResourceDictionary");
            writer.println("    xmlns=\"http://schemas.microsoft.com/winfx/2006/xaml/presentation\"");
            writer.println("    xmlns:x=\"http://schemas.microsoft.com/winfx/2006/xaml\"");
            writer.println("    xmlns:s=\"clr-namespace:System;assembly=mscorlib\"");
            writer.println("    xml:space=\"preserve\">");
            writer.println();

            Map<String, List<DataItem>> sortedGroups = new TreeMap<>(groupedData);
            for (Map.Entry<String, List<DataItem>> entry : sortedGroups.entrySet()) {
                String category = entry.getKey();
                List<DataItem> sortedItems = entry.getValue().stream()
                        .sorted(Comparator.comparing(DataItem::getKey))
                        .toList();

                if (addComments) {
                    writer.printf("    <!-- %s -->%n", category);
                }

                for (DataItem item : sortedItems) {
                    String value = "Original".equalsIgnoreCase(fieldToExport) ? item.getOriginalText() : item.getTranslatedText();
                    writer.printf("    <String x:Key=\"%s\">%s</String>%n", item.getKey(), escapeXml(value));
                }

                writer.println();
            }

            writer.println("</ResourceDictionary>");
            logger.info("Exported XAML to: {}", file.getAbsolutePath());
            new ShowAlert().showAlert(Alert.AlertType.INFORMATION, "Export Successful", "File exported successfully.");

        } catch (Exception e) {
            logger.error("Failed to export file: {}", file.getAbsolutePath(), e);
            new ShowAlert().showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export file: " + e.getMessage());
        }
    }

    /**
     * Escapes special XML characters in the input string to ensure valid XML output.
     * @param input the input string to escape
     * @return the escaped string
     */
    private static String escapeXml(String input) {
        return input == null ? "" : input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

}
