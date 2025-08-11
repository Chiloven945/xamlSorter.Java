package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.entities.ProjectMeta;
import chiloven.xamlsorter.utils.ShowAlert;
import chiloven.xamlsorter.utils.TaskExecutorService;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class ProjectFileManager {
    private static final Logger logger = LogManager.getLogger(ProjectFileManager.class);

    /**
     * Saves the current project state to a .xsproject XML file.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param file          the file to save the project to
     * @param meta          the ProjectMeta object containing project metadata
     * @param items         the list of DataItem objects representing the project data
     * @param clipboardKeys the list of keys currently in the clipboard
     */
    public static CompletableFuture<Boolean> saveXsProject(File file, ProjectMeta meta, List<DataItem> items, List<String> clipboardKeys) {
        logger.debug("Starting saveXsProject for file: {}", file.getAbsolutePath());

        return TaskExecutorService.submitTask(
                "SaveXsProject",
                () -> {
                    try {
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = docFactory.newDocumentBuilder();
                        Document doc = builder.newDocument();

                        Element root = doc.createElement("XsProject");
                        doc.appendChild(root);

                        // Meta
                        logger.debug("Writing project meta information.");
                        Element metaNode = doc.createElement("Meta");
                        Element nameNode = doc.createElement("Name");
                        nameNode.setTextContent(meta.getName());
                        metaNode.appendChild(nameNode);
                        Element descNode = doc.createElement("Description");
                        descNode.setTextContent(meta.getDescription());
                        metaNode.appendChild(descNode);
                        Element authorNode = doc.createElement("Author");
                        authorNode.setTextContent(meta.getAuthor());
                        metaNode.appendChild(authorNode);
                        root.appendChild(metaNode);

                        // Clipboard (with full data)
                        logger.debug("Writing clipboard items. Clipboard size: {}", clipboardKeys.size());
                        Element clipboardElem = doc.createElement("Clipboard");
                        for (String key : clipboardKeys) {
                            DataItem found = items.stream().filter(i -> i.getKey().equals(key)).findFirst().orElse(null);
                            if (found != null) {
                                Element iElem = doc.createElement("I");
                                iElem.setAttribute("k", found.getKey());
                                iElem.setAttribute("ot", found.getOriginalText());
                                iElem.setAttribute("tt", found.getTranslatedText());
                                clipboardElem.appendChild(iElem);
                                logger.trace("Added clipboard item: key={}", found.getKey());
                            } else {
                                logger.warn("Clipboard key '{}' not found in items list.", key);
                            }
                        }
                        root.appendChild(clipboardElem);

                        // Data (grouped by category)
                        logger.debug("Grouping data items by category.");
                        Element dataElem = doc.createElement("Data");
                        // Group items by category
                        Map<String, List<DataItem>> grouped = items.stream().collect(
                                java.util.stream.Collectors.groupingBy(DataItem::getCategory)
                        );
                        for (Map.Entry<String, List<DataItem>> entry : grouped.entrySet()) {
                            Element cElem = doc.createElement("C");
                            cElem.setAttribute("c", entry.getKey());
                            logger.trace("Writing category: {}", entry.getKey());
                            for (DataItem item : entry.getValue()) {
                                Element iElem = doc.createElement("I");
                                iElem.setAttribute("k", item.getKey());
                                iElem.setAttribute("ot", item.getOriginalText());
                                iElem.setAttribute("tt", item.getTranslatedText());
                                cElem.appendChild(iElem);
                                logger.trace("Added data item: key={} category={}", item.getKey(), item.getCategory());
                            }
                            dataElem.appendChild(cElem);
                        }
                        root.appendChild(dataElem);

                        logger.debug("Transforming DOM to file: {}", file.getAbsolutePath());
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(file);
                        transformer.transform(source, result);

                        Platform.runLater(() -> ShowAlert.info(
                                getLang("module.proj_file_manager.save.success.alert.title"),
                                getLang("module.proj_file_manager.save.success.alert.content")
                        ));

                        logger.info("Project saved successfully to {}", file.getAbsolutePath());
                        return true;
                    } catch (Exception e) {
                        Platform.runLater(() -> ShowAlert.error(
                                getLang("general.alert.error"),
                                getLang("module.proj_file_manager.save.exception.alert.header"),
                                getLang("module.proj_file_manager.save.exception.alert.content",
                                        file.getAbsolutePath(),
                                        e.getMessage()
                                )
                        ));

                        logger.error("Failed to save project to {}: {}", file.getAbsolutePath(), e.getMessage(), e);
                        throw new RuntimeException("Failed to save project to: " + file.getAbsolutePath(), e);
                    }
                }
        );
    }

    /**
     * Loads a project from a .xsproject XML file.
     * This method processes data in a background thread but waits for the result.
     *
     * @param file the file to load the project from
     * @return LoadedProject containing the project name, items, and clipboard keys
     */
    public static LoadedProject loadXsProject(File file) {
        logger.debug("Starting loadXsProject for file: {}", file.getAbsolutePath());

        try {
            return TaskExecutorService.submitTask(
                    "LoadXsProject",
                    () -> {
                        try {
                            List<DataItem> items = new ArrayList<>();
                            List<DataItem> clipboardItems = new ArrayList<>();
                            ProjectMeta meta = new ProjectMeta();

                            logger.debug("Creating DocumentBuilder and parsing XML file.");
                            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            Document doc = builder.parse(file);
                            doc.getDocumentElement().normalize();

                            Element root = doc.getDocumentElement();

                            // Parse meta info
                            logger.debug("Parsing project meta information.");
                            NodeList metaList = root.getElementsByTagName("Meta");
                            if (metaList.getLength() > 0) {
                                Element metaNode = (Element) metaList.item(0);
                                meta.setName(getElementText(metaNode, "Name"));
                                meta.setDescription(getElementText(metaNode, "Description"));
                                meta.setAuthor(getElementText(metaNode, "Author"));
                                logger.debug("Meta loaded: name={}, description={}, author={}", meta.getName(), meta.getDescription(), meta.getAuthor());
                            }

                            // Parse Clipboard: <Clipboard><I k="" ot="" tt=""/></Clipboard>
                            logger.debug("Parsing clipboard items.");
                            NodeList clipboardList = root.getElementsByTagName("Clipboard");
                            if (clipboardList.getLength() > 0) {
                                Element clipboardElem = (Element) clipboardList.item(0);
                                NodeList iNodes = clipboardElem.getElementsByTagName("I");
                                for (int i = 0; i < iNodes.getLength(); i++) {
                                    Element iElem = (Element) iNodes.item(i);
                                    String key = iElem.getAttribute("k");
                                    String original = iElem.getAttribute("ot");
                                    String translated = iElem.getAttribute("tt");
                                    clipboardItems.add(new DataItem("", key, original, translated));
                                    logger.trace("Loaded clipboard item: key={}", key);
                                }
                            }

                            // Parse Data: <Data><C c=""><I .../></C></Data>
                            logger.debug("Parsing data items by category.");
                            NodeList dataList = root.getElementsByTagName("Data");
                            if (dataList.getLength() > 0) {
                                Element dataElem = (Element) dataList.item(0);
                                NodeList cNodes = dataElem.getElementsByTagName("C");
                                for (int i = 0; i < cNodes.getLength(); i++) {
                                    Element cElem = (Element) cNodes.item(i);
                                    String category = cElem.getAttribute("c");
                                    NodeList iNodes = cElem.getElementsByTagName("I");
                                    for (int j = 0; j < iNodes.getLength(); j++) {
                                        Element iElem = (Element) iNodes.item(j);
                                        String key = iElem.getAttribute("k");
                                        String original = iElem.getAttribute("ot");
                                        String translated = iElem.getAttribute("tt");
                                        items.add(new DataItem(category, key, original, translated));
                                    }
                                }
                            }

                            // Returning clipboard keys as a list of strings
                            List<String> clipboardKeys = clipboardItems.stream().map(DataItem::getKey).collect(java.util.stream.Collectors.toList());

                            logger.info("Project loaded successfully from {}", file.getAbsolutePath());
                            return new LoadedProject(meta, items, clipboardKeys);
                        } catch (Exception e) {
                            Platform.runLater(() -> ShowAlert.error(
                                    getLang("general.alert.error"),
                                    getLang("module.proj_file_manager.load.exception.alert.header"),
                                    getLang("module.proj_file_manager.load.exception.alert.content",
                                            file.getAbsolutePath(),
                                            e.getMessage()
                                    )
                            ));

                            logger.error("Failed to load project from {}: {}", file.getAbsolutePath(), e.getMessage());
                            throw new RuntimeException("Failed to load project from: " + file.getAbsolutePath(), e);
                        }
                    }
            ).join();
        } catch (Exception e) {
            logger.error("Failed to load project from {}: {}", file.getAbsolutePath(), e.getMessage());
            return null;
        }
    }

    /**
     * Internal utility to get text content of an element by tag name.
     *
     * @param parent the parent element to search within
     * @param tag    the tag name to search for
     * @return the text content of the first matching element, or an empty string if not found
     */
    private static String getElementText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Record to hold the loaded project data.
     *
     * @param meta      the ProjectMeta object containing project metadata
     * @param items     the list of DataItem objects representing the project data
     * @param clipboard the list of keys currently in the clipboard
     */
    public record LoadedProject(ProjectMeta meta, List<DataItem> items, List<String> clipboard) {
    }

}
