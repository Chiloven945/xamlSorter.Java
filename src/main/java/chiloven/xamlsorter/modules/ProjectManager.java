package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.controllers.dialogs.NewProjectDialogController;
import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.entities.ProjectMeta;
import chiloven.xamlsorter.utils.CustomFileChooser;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static chiloven.xamlsorter.modules.I18n.getBundle;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class ProjectManager {
    private static final Logger logger = LogManager.getLogger(ProjectManager.class);

    /**
     * Open an existing project file (.xsproject), load data and update the main controller.
     *
     * @param mainController the MainController instance to update
     */
    public static void openProject(MainController mainController) {
        if (!mainController.promptSaveIfNeeded()) return;

        File file = CustomFileChooser.showOpenFileDialog(
                mainController.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.open.title"),
                getLang("general.files.xsproject"),
                List.of("xsproject")
        );
        if (file == null) {
            logger.info("Open project operation cancelled by user.");
            return;
        }

        // Read the project file
        ProjectFileManager.LoadedProject loaded = ProjectFileManager.loadXsProject(file);
        if (loaded == null) return;

        mainController.setCurrentProjectMeta(loaded.meta());
        mainController.getGroupedData().clear();
        loaded.items().stream()
                .collect(java.util.stream.Collectors.groupingBy(DataItem::getCategory))
                .forEach((k, v) -> mainController.getGroupedData().put(k, v));

        ClipboardManager.clear();
        ClipboardManager.setClipboardKeys(loaded.clipboard(), loaded.items());

        mainController.setCurrentProjectFile(file);
        mainController.setModified(false);
        mainController.showEditor();

        logger.info("Project opened: {}", file.getAbsolutePath());
    }

    /**
     * Save the current project to its file if it exists, otherwise prompt to save as.
     *
     * @param mainController the MainController instance to access grouped data and project meta
     */
    public static void saveProject(MainController mainController) {
        File file = mainController.getCurrentProjectFile();
        if (file == null) {
            saveProjectAs(mainController);
            return;
        }
        doSave(mainController, file);
    }

    /**
     * Prompt the user to save the current project to a new file.
     *
     * @param mainController the MainController instance to access grouped data and project meta
     */
    public static void saveProjectAs(MainController mainController) {
        File file = CustomFileChooser.showSaveFileDialog(
                mainController.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.save_as.title"),
                getLang("general.files.xsproject"),
                List.of("xsproject"),
                MainController.getCurrentProjectMeta().getName() + ".xsproject"
        );

        if (file != null) {
            doSave(mainController, file);
            mainController.setCurrentProjectFile(file); // 切换到新文件
        }
    }

    /**
     * Save the current project to the specified file.
     *
     * @param mainController the MainController instance to access grouped data and project meta
     * @param file           the file to save the project to
     */
    private static void doSave(MainController mainController, File file) {
        List<DataItem> items = mainController.getGroupedData().values().stream()
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.toList());
        List<String> clipboardKeys = ClipboardManager.getClipboard().stream()
                .map(DataItem::getKey)
                .collect(java.util.stream.Collectors.toList());
        ProjectFileManager.saveXsProject(file, MainController.getCurrentProjectMeta(), items, clipboardKeys);
        mainController.setModified(false); // 标记已保存
    }

    /**
     * Create a new project dialog and set the current project meta in the main controller.
     *
     * @param mainController the MainController instance to update the UI
     * @return true if the project was created successfully, false otherwise
     */
    public static boolean createProject(MainController mainController) {
        if (!mainController.promptSaveIfNeeded()) return false;
        try {
            FXMLLoader loader = new FXMLLoader(ProjectManager.class.getResource("/ui/dialogs/NewProjectDialog.fxml"));
            loader.setResources(getBundle());
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);

            Scene scene = dialog.getDialogPane().getScene();
            I18n.applyDefaultFont(scene);

            dialog.setTitle(getLang("general.proj.new"));
            dialog.initOwner(mainController.getRootPane().getScene().getWindow());

            logger.info("Opening new project dialog.");

            Button okButton = dialog.getDialogPane().getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst()
                    .map(bt -> (Button) dialog.getDialogPane().lookupButton(bt))
                    .orElse(null);

            NewProjectDialogController controller = loader.getController();
            okButton.setDisable(controller.getProjectNameField().getText().trim().isEmpty());
            controller.getProjectNameField().textProperty().addListener(
                    (obs, o, n) -> okButton.setDisable(n.trim().isEmpty())
            );

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData().isDefaultButton()) {
                ProjectMeta meta = controller.getProjectMeta();
                mainController.setCurrentProjectMeta(meta);
                mainController.getGroupedData().clear();
                ClipboardManager.clear();
                mainController.showEditor();

                mainController.setModified(true);

                logger.info("New project created: {}", meta.getName());
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to create new project", e);
        }
        logger.info("New project creation cancelled by user.");
        return false;
    }

    /**
     * Import a XAML file into the current project.
     *
     * @param controller   the MainController instance to access grouped data
     * @param isTranslated true if importing a translation XAML file, false for an original XAML file
     */
    public static void importXaml(MainController controller, boolean isTranslated) {
        File file = CustomFileChooser.showOpenFileDialog(
                controller.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.import.%s.title".formatted(isTranslated ? "original" : "translated")),
                getLang("general.files.xaml"),
                List.of("xaml", "xml")
        );

        if (file != null) {
            try {
                List<DataItem> items = FileProcessor.parseXamlFile(file, isTranslated);
                String column = isTranslated ? "translated" : "original";
                DataOperationHelper.applyColumnUpdates(items, controller.getGroupedData(), column);

                SortAndRefresher.refresh(controller.getDataTreeTable(), controller.getGroupedData());
                ShowAlert.info(
                        getLang("general.alert.success"),
                        getLang("module.proj_manager.import_xaml.success.alert.content")
                );
                return;
            } catch (Exception e) {
                ShowAlert.error(
                        getLang("general.alert.error"),
                        getLang("module.proj_manager.import_xaml.exception.alert.content"),
                        e.getMessage()
                );
            }
        }
        logger.info("Import XAML cancelled or failed.");
    }

    /**
     * Create a new project from a XAML file.
     *
     * @param controller   the MainController instance to access grouped data
     * @param isTranslated true if importing a translation XAML file, false for an original XAML file
     * @see #createProject(MainController)
     * @see #importXaml(MainController, boolean)
     */
    public static void createFromXaml(MainController controller, boolean isTranslated) {
        if (createProject(controller)) {
            importXaml(controller, isTranslated);
            return;
        }
        logger.info("Create project from XAML cancelled by user.");
    }

}
