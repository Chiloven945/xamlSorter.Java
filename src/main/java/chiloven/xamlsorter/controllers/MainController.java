package chiloven.xamlsorter.controllers;

import chiloven.xamlsorter.modules.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);
    private static ProjectMeta currentProjectMeta;
    private final Map<String, List<DataItem>> groupedData = new HashMap<>();

    @FXML
    private TreeTableView<DataItem> translationTreeTable;
    @FXML
    private TreeTableColumn<DataItem, String> keyColumn;
    @FXML
    private TreeTableColumn<DataItem, String> originalColumn;
    @FXML
    private TreeTableColumn<DataItem, String> translatedColumn;
    @FXML
    private StackPane rootPane;
    @FXML
    private VBox editorBox;
    @FXML
    private StackPane welcomeOverlay;
    @FXML

    private TopMenuBarController topMenuBarController;
    private File currentProjectFile = null;
    private boolean projectModified = false;

    public static ProjectMeta getCurrentProjectMeta() {
        return currentProjectMeta;
    }

    public void setCurrentProjectMeta(ProjectMeta meta) {
        currentProjectMeta = meta;
    }

    public File getCurrentProjectFile() {
        return currentProjectFile;
    }

    public void setCurrentProjectFile(File file) {
        this.currentProjectFile = file;
    }

    public boolean isProjectModified() {
        return projectModified;
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    @FXML
    public void initialize() {

        // =========================
        // 1️⃣ Display the welcome overlay
        // =========================

        showWelcome();
        topMenuBarController.setMainController(this);
        DataOperationHelper.setMainController(this);

        // =========================
        // 2️⃣ Configure the TreeTableView columns
        // =========================

        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        originalColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        translatedColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());

        // =========================
        // 3️⃣ Configure the TreeTableView and enable editing
        // =========================

        translationTreeTable.setEditable(true);
        translationTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // key column
        keyColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        keyColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(
                    event.getNewValue(),
                    item::getKey, key -> key.endsWith("..."), item::setKey);
        });

        // original column
        originalColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        originalColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(
                    event.getNewValue(),
                    item::getOriginalText, "-"::equals, item::setOriginalText);
        });

        // translated column
        translatedColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        translatedColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(
                    event.getNewValue(),
                    item::getTranslatedText, "-"::equals, item::setTranslatedText);
        });

        // =========================
        // 4️⃣ Configure the context menu for the TreeTableView (rows and empty area)
        // =========================

        translationTreeTable.setRowFactory(tv -> {
            TreeTableRow<DataItem> row = new TreeTableRow<>();
            row.setOnContextMenuRequested(event -> {
                DataItem targetItem = (row.getTreeItem() != null && row.getTreeItem().getValue() != null)
                        ? row.getTreeItem().getValue()
                        : null;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/widgets/ContextMenu.fxml"));
                    ContextMenu menu = loader.load();
                    ContextMenuController controller = loader.getController();
                    controller.initializeMenu(groupedData, translationTreeTable, targetItem);

                    if (translationTreeTable.getContextMenu() != null) {
                        translationTreeTable.getContextMenu().hide();
                    }
                    translationTreeTable.setContextMenu(menu);

                    menu.show(row, event.getScreenX(), event.getScreenY());
                    event.consume();
                } catch (Exception e) {
                    logger.error("Failed to load context menu", e);
                }
            });
            return row;
        });

        translationTreeTable.setOnContextMenuRequested(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/widgets/ContextMenu.fxml"));
                ContextMenu menu = loader.load();
                ContextMenuController controller = loader.getController();
                controller.initializeMenu(groupedData, translationTreeTable, null);

                if (translationTreeTable.getContextMenu() != null) {
                    translationTreeTable.getContextMenu().hide();
                }
                translationTreeTable.setContextMenu(menu);

                menu.show(translationTreeTable, event.getScreenX(), event.getScreenY());
                event.consume();
            } catch (Exception e) {
                logger.error("Failed to load context menu (empty area)", e);
            }
        });

        // =========================
        // 5️⃣ Initialize the TreeTableView with an empty root item
        // =========================

        translationTreeTable.setRoot(new TreeItem<>(new DataItem("", "", "", "")));
        translationTreeTable.setShowRoot(false);

        logger.info("MainController initialized successfully.");
    }

    /**
     * Handles cell edit events in the translation table with unified logic for validation and value assignment.
     *
     * @param newValue             The value to assign to the cell if validation passes.
     * @param forbiddenValueGetter A supplier that provides the current value to check for edit restrictions.
     * @param forbidPredicate      A predicate that returns true if the value is forbidden to be edited.
     * @param valueSetter          A consumer that sets the new value to the data item if allowed.
     */
    public void handleCellEdit(String newValue,
                               java.util.function.Supplier<String> forbiddenValueGetter,
                               java.util.function.Predicate<String> forbidPredicate,
                               java.util.function.Consumer<String> valueSetter) {
        if (forbidPredicate.test(forbiddenValueGetter.get())) {
            ShowAlert.error("Error", "Invalid Input", "Editing the category item is not allowed.");
            logger.warn("User attempted to edit the category item {} with value '{}', rejected", forbiddenValueGetter.get(), newValue);
            SortAndRefresher.refresh(translationTreeTable, groupedData);
            return;
        }
        valueSetter.accept(newValue);
    }

    // Method to load and display the grouped data in the TreeTableView
    @FXML
    private void handleAddEntry() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

    /**
     * Get the current project metadata.
     *
     * @return the current ProjectMeta instance
     */
    public Map<String, List<DataItem>> getGroupedData() {
        return groupedData;
    }

    /**
     * Get the TreeTableView containing the translation data.
     *
     * @return the TreeTableView instance
     */
    public TreeTableView<DataItem> getDataTreeTable() {
        return translationTreeTable;
    }

    // Welcome and Editor View Management
    public void showWelcome() {
        if (welcomeOverlay != null) {
            welcomeOverlay.setVisible(true);
            welcomeOverlay.setManaged(true);
        }
        editorBox.setVisible(false);
        editorBox.setManaged(false);
    }

    // Update the window title based on the current project metadata and modification status
    private void updateWindowTitle() {
        String projectName = (currentProjectMeta != null ? currentProjectMeta.getName() : "Untitled");
        String modified = projectModified ? "*" : "";
        String title = "xamlSorter.Java - Editing: " + projectName + modified;
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setTitle(title);
    }

    // Set the modified status of the project and update the window title accordingly
    public void setModified(Boolean modified) {
        this.projectModified = modified;
        updateWindowTitle();
    }

    // Show the editor view and hide the welcome overlay
    public void showEditor() {
        if (welcomeOverlay != null) {
            welcomeOverlay.setVisible(false);
            welcomeOverlay.setManaged(false);
        }
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        updateWindowTitle();
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

    /**
     * Prompt the user to save changes if the project is modified.
     *
     * @return true if the user chose to save or if there are no unsaved changes,
     */
    public boolean promptSaveIfNeeded() {
        if (!projectModified) return true;

        ButtonType saveBtn = new ButtonType("Save");
        ButtonType dontSaveBtn = new ButtonType("Do not save");
        ButtonType cancelBtn = ButtonType.CANCEL;

        Optional<ButtonType> result = ShowAlert.confirm(
                "Unsaved Changes",
                "You have unsaved changes.",
                "Do you want to save before continuing?",
                saveBtn, dontSaveBtn, cancelBtn
        );

        if (result.isPresent()) {
            if (result.get() == saveBtn) {
                ProjectManager.saveProject(this);
                return !projectModified;
            } else return result.get() == dontSaveBtn;
        }
        return false;
    }

    @FXML
    private void handleCreateProject() {
        ProjectManager.createProject(this);
    }

    @FXML
    private void handleOpenProject() {
        ProjectManager.openProject(this);
    }

    @FXML
    private void handleCreateFromOriginal() {
        ProjectManager.createFromXaml(this, false);
    }

    @FXML
    private void handleCreateFromTranslated() {
        ProjectManager.createFromXaml(this, true);
    }

    @FXML
    private void handleShowAbout() {
        AboutDialogController.showAboutDialog(rootPane.getScene().getWindow());
    }

}
