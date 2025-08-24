package chiloven.xamlsorter.ui;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.entities.MultiLineTreeTableCell;
import chiloven.xamlsorter.entities.ProjectMeta;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.ProjectManager;
import chiloven.xamlsorter.modules.SortAndRefresher;
import chiloven.xamlsorter.ui.dialogs.AboutDialog;
import chiloven.xamlsorter.ui.dialogs.PreferencesDialog;
import chiloven.xamlsorter.ui.widgets.ContextMenu;
import chiloven.xamlsorter.ui.widgets.TopMenuBar;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

import static chiloven.xamlsorter.Main.version;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class MainPage extends StackPane {
    private static final Logger logger = LogManager.getLogger(MainPage.class);
    private static ProjectMeta currentProjectMeta;
    private final Map<String, List<DataItem>> groupedData = new HashMap<>();

    private final TreeTableView<DataItem> translationTreeTable;
    private final TreeTableColumn<DataItem, String> keyColumn;
    private final TreeTableColumn<DataItem, String> originalColumn;
    private final TreeTableColumn<DataItem, String> translatedColumn;
    private final VBox editorBox;
    private final StackPane welcomeOverlay;
    private final TopMenuBar menuBar;
    private final Button addEntryButton;
    private final ImageView appIconView;

    private File currentProjectFile = null;
    private boolean projectModified = false;

    public MainPage() {
        this.translationTreeTable = new TreeTableView<>();
        this.keyColumn = new TreeTableColumn<>(getLang("general.datatype.key"));
        this.originalColumn = new TreeTableColumn<>(getLang("general.datatype.original_text"));
        this.translatedColumn = new TreeTableColumn<>(getLang("general.datatype.translated_text"));
        this.editorBox = new VBox();
        this.welcomeOverlay = new StackPane();
        this.menuBar = new TopMenuBar(this);
        this.addEntryButton = new Button(getLang("general.button.add_entry"));
        this.appIconView = new ImageView();

        setupLayout();
        setupComponents();
        setupEventHandlers();
        showWelcome();
        getStylesheets().add(getClass().getResource("/ui/styles/startpane.css").toExternalForm());

        logger.debug("MainPage initialized");
    }

    // Getter/Setter methods
    public static ProjectMeta getCurrentProjectMeta() {
        return currentProjectMeta;
    }

    public void setCurrentProjectMeta(ProjectMeta meta) {
        currentProjectMeta = meta;
    }

    private void setupLayout() {
        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(menuBar, editorBox);

        editorBox.setVisible(false);
        editorBox.setManaged(false);
        VBox.setVgrow(editorBox, Priority.ALWAYS);

        setupWelcomeOverlay();
        setupEditorArea();

        getChildren().addAll(mainLayout, welcomeOverlay);
    }

    private void setupWelcomeOverlay() {
        welcomeOverlay.getStyleClass().add("welcome-overlay");
        welcomeOverlay.setPickOnBounds(false);

        Pane overlayBackground = new Pane();
        overlayBackground.getStyleClass().add("overlay-background");
        overlayBackground.setPrefSize(9999, 9999);
        StackPane.setAlignment(overlayBackground, Pos.CENTER);

        HBox welcomeBox = createWelcomeBox();
        welcomeOverlay.getChildren().addAll(overlayBackground, welcomeBox);
    }

    private HBox createWelcomeBox() {
        HBox welcomeBox = new HBox(18);  // spacing="18.0"
        welcomeBox.setMaxHeight(350);
        welcomeBox.setMaxWidth(370);
        welcomeBox.setMinHeight(USE_PREF_SIZE);  // minHeight="-Infinity"
        welcomeBox.setMinWidth(USE_PREF_SIZE);   // minWidth="-Infinity"
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.getStyleClass().add("welcome-box");

        VBox leftContent = new VBox(18);
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setMaxWidth(256);
        leftContent.setMinHeight(USE_PREF_SIZE);

        setupAppIcon();

        Label titleLabel = new Label(getLang("general.application.name"));
        titleLabel.getStyleClass().add("welcome-title");

        Label descLabel = new Label(getLang("dialog.about.text.description.1"));
        descLabel.setStyle("-fx-font-size: 13px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.POSITIVE_INFINITY);

        Label versionLabel = new Label(getLang("dialog.about.text.version") + version);
        versionLabel.setAlignment(Pos.CENTER);
        versionLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        versionLabel.setWrapText(true);
        versionLabel.setMaxWidth(Double.POSITIVE_INFINITY);

        leftContent.getChildren().addAll(appIconView, titleLabel, versionLabel, descLabel);

        VBox rightContent = new VBox(18);
        rightContent.setAlignment(Pos.CENTER_LEFT);

        rightContent.getChildren().addAll(
                createWelcomeButton("general.proj.open", this::handleOpenProject),
                createWelcomeButton("general.proj.new", this::handleCreateProject),
                createWelcomeButton("general.button.proj.new.original", () -> handleCreateFromXaml(false)),
                createWelcomeButton("general.button.proj.new.translated", () -> handleCreateFromXaml(true)),
                createWelcomeButton("widget.menu_bar.edit.preferences", () -> PreferencesDialog.show(this.getRootPane().getScene().getWindow())),
                createWelcomeButton("general.button.about", () -> AboutDialog.show(getScene().getWindow()))
        );

        welcomeBox.getChildren().addAll(
                leftContent,
                new Separator(Orientation.VERTICAL),
                rightContent
        );

        return welcomeBox;
    }

    private void setupAppIcon() {
        appIconView.setFitHeight(128.0);
        appIconView.setFitWidth(128.0);
        appIconView.setPickOnBounds(true);
        appIconView.setPreserveRatio(true);
        try {
            Image iconImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/icons/application/application-about.png")));
            appIconView.setImage(iconImage);
        } catch (Exception e) {
            logger.error("Failed to load application icon", e);
        }
    }

    private Button createWelcomeButton(String textKey, Runnable action) {
        Button button = new Button(getLang(textKey));
        button.setOnAction(e -> action.run());

        button.setMinHeight(32);
        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(0, 15, 0, 15));

        button.getStyleClass().add("welcome-button");

        return button;
    }

    private void setupEditorArea() {
        setupTreeTableView();

        addEntryButton.setOnAction(e -> handleAddEntry());
        VBox.setMargin(addEntryButton, new Insets(0, 8, 8, 8));

        editorBox.getChildren().addAll(translationTreeTable, addEntryButton);
    }

    private void setupTreeTableView() {
        translationTreeTable.setEditable(true);
        translationTreeTable.setShowRoot(false);
        translationTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        translationTreeTable.setPlaceholder(new Label(getLang("page.main.tree_table.placeholder")));
        VBox.setMargin(translationTreeTable, new Insets(8));
        VBox.setVgrow(translationTreeTable, Priority.ALWAYS);

        setupColumns();

        translationTreeTable.setRoot(new TreeItem<>(new DataItem("", "", "", "")));

        setupContextMenu();
    }

    private void setupColumns() {
        double total = 1 + 1.5 + 1.5; // = 4.0
        keyColumn.prefWidthProperty().bind(translationTreeTable.widthProperty().multiply(1 / total));
        originalColumn.prefWidthProperty().bind(translationTreeTable.widthProperty().multiply(1.5 / total));
        translatedColumn.prefWidthProperty().bind(translationTreeTable.widthProperty().multiply(1.5 / total));

        setupColumnFactories();

        translationTreeTable.getColumns().addAll(keyColumn, originalColumn, translatedColumn);
    }

    private void setupColumnFactories() {
        keyColumn.setCellValueFactory(param -> param.getValue().getValue().getKeyProperty());
        keyColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        keyColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(event.getNewValue(),
                    item::getKey, key -> key.endsWith("..."), item::setKey);
        });

        originalColumn.setCellValueFactory(param -> param.getValue().getValue().getOriginalTextProperty());
        originalColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        originalColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(event.getNewValue(),
                    item::getOriginalText, "-"::equals, item::setOriginalText);
        });

        translatedColumn.setCellValueFactory(param -> param.getValue().getValue().getTranslatedTextProperty());
        translatedColumn.setCellFactory(param -> new MultiLineTreeTableCell<>());
        translatedColumn.setOnEditCommit(event -> {
            DataItem item = event.getRowValue().getValue();
            handleCellEdit(event.getNewValue(),
                    item::getTranslatedText, "-"::equals, item::setTranslatedText);
        });
    }

    private void setupContextMenu() {
        translationTreeTable.setRowFactory(tv -> {
            TreeTableRow<DataItem> row = new TreeTableRow<>();
            row.setOnContextMenuRequested(event -> {
                DataItem targetItem = (row.getTreeItem() != null && row.getTreeItem().getValue() != null)
                        ? row.getTreeItem().getValue()
                        : null;
                showContextMenu(targetItem, row, event.getScreenX(), event.getScreenY());
                event.consume();
            });
            return row;
        });

        translationTreeTable.setOnContextMenuRequested(event -> {
            showContextMenu(null, translationTreeTable, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void showContextMenu(DataItem targetItem, Control owner, double x, double y) {
        if (translationTreeTable.getContextMenu() != null) {
            translationTreeTable.getContextMenu().hide();
        }
        ContextMenu menu = new ContextMenu();
        menu.initialize(groupedData, translationTreeTable, targetItem);
        translationTreeTable.setContextMenu(menu);
        menu.show(owner, x, y);
    }

    /**
     * Handles cell edit events for the TreeTableView.
     *
     * @param newValue             the new value entered by the user
     * @param forbiddenValueGetter a supplier that provides the current value to check against
     * @param forbidPredicate      a predicate that determines if the new value is forbidden
     * @param valueSetter          a consumer that sets the new value if valid
     */
    private void handleCellEdit(String newValue,
                                java.util.function.Supplier<String> forbiddenValueGetter,
                                java.util.function.Predicate<String> forbidPredicate,
                                java.util.function.Consumer<String> valueSetter) {
        if (forbidPredicate.test(forbiddenValueGetter.get())) {
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("page.main.tree_table.cell.exception.category_edit.header"),
                    getLang("page.main.tree_table.cell.exception.category_edit.content")
            );
            SortAndRefresher.refresh(translationTreeTable, groupedData);
            return;
        }
        valueSetter.accept(newValue);
        setModified(true);
    }

    private void handleAddEntry() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
        setModified(true);
    }

    private void handleCreateProject() {
        ProjectManager.createProject(this);
    }

    private void handleOpenProject() {
        ProjectManager.openProject(this);
    }

    private void handleCreateFromXaml(boolean isTranslated) {
        ProjectManager.createFromXaml(this, isTranslated);
    }

    public void showWelcome() {
        welcomeOverlay.setVisible(true);
        welcomeOverlay.setManaged(true);
        editorBox.setVisible(false);
        editorBox.setManaged(false);
    }

    public void showEditor() {
        welcomeOverlay.setVisible(false);
        welcomeOverlay.setManaged(false);
        editorBox.setVisible(true);
        editorBox.setManaged(true);
        updateWindowTitle();
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

    private void updateWindowTitle() {
        String projectName = (currentProjectMeta != null ? currentProjectMeta.getName()
                : getLang("page.main.title.proj_name.untitled"));
        String modified = projectModified ? "*" : "";
        String title = getLang("page.main.title.editing", projectName, modified);
        ((Stage) getScene().getWindow()).setTitle(title);
    }

    public StackPane getRootPane() {
        return this;
    }

    public Map<String, List<DataItem>> getGroupedData() {
        return groupedData;
    }

    public TreeTableView<DataItem> getDataTreeTable() {
        return translationTreeTable;
    }

    /**
     * Sets the modified state of the project.
     *
     * @param modified true if the project has been modified, false otherwise
     */
    public void setModified(boolean modified) {
        this.projectModified = modified;
        updateWindowTitle();
    }

    public boolean isProjectModified() {
        return projectModified;
    }

    public File getCurrentProjectFile() {
        return currentProjectFile;
    }

    public void setCurrentProjectFile(File file) {
        this.currentProjectFile = file;
    }

    public boolean promptSaveIfNeeded() {
        if (!projectModified) {
            return true;
        }

        ButtonType saveBtn = new ButtonType(getLang("general.button.save"));
        ButtonType dontSaveBtn = new ButtonType(getLang("page.main.save.confirm.button.do_not_save"));
        ButtonType cancelBtn = new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);

        Optional<ButtonType> result = ShowAlert.confirm(
                getLang("page.main.save.confirm.title"),
                getLang("page.main.save.confirm.header"),
                getLang("page.main.save.confirm.content"),
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

    private void setupComponents() {
        translationTreeTable.setPrefHeight(400);
        translationTreeTable.setPrefWidth(600);

        keyColumn.setMinWidth(80);
        originalColumn.setMinWidth(120);
        translatedColumn.setMinWidth(120);

        addEntryButton.setPrefWidth(120);
        addEntryButton.setPrefHeight(30);

        logger.debug("Components initialized");
    }

    private void setupEventHandlers() {
        translationTreeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.debug("Selected item changed: {}",
                                newValue.getValue().getKey());
                    }
                });

        translationTreeTable.focusedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        logger.debug("TreeTableView gained focus");
                    }
                });

        setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F5 -> {
                    logger.debug("F5 pressed, refreshing view");
                    SortAndRefresher.refresh(translationTreeTable, groupedData);
                }
                case S -> {
                    if (event.isControlDown()) {
                        logger.debug("Ctrl+S pressed, saving project");
                        ProjectManager.saveProject(this);
                        event.consume();
                    }
                }
                case N -> {
                    if (event.isControlDown()) {
                        logger.debug("Ctrl+N pressed, creating new project");
                        handleCreateProject();
                        event.consume();
                    }
                }
                case O -> {
                    if (event.isControlDown()) {
                        logger.debug("Ctrl+O pressed, opening project");
                        handleOpenProject();
                        event.consume();
                    }
                }
            }
        });

        logger.debug("Event handlers initialized");
    }
}