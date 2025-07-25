package chiloven.xamlsorter.modules;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.entities.ProjectMeta;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.ui.dialogs.NewProjectDialog;
import chiloven.xamlsorter.utils.CustomFileChooser;
import chiloven.xamlsorter.utils.ShowAlert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class ProjectManager {
    private static final Logger logger = LogManager.getLogger(ProjectManager.class);

    /**
     * Open an existing project file (.xsproject), load data and update the main page.
     *
     * @param mainPage the MainPage instance to update
     */
    public static void openProject(MainPage mainPage) {
        logger.debug("Attempting to open a project...");
        if (!mainPage.promptSaveIfNeeded()) {
            logger.debug("User cancelled save prompt before opening project.");
            return;
        }

        File file = CustomFileChooser.showOpenFileDialog(
                mainPage.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.open.title"),
                getLang("general.files.xsproject"),
                List.of("xsproject")
        );
        if (file == null) {
            logger.info("Open project operation cancelled by user.");
            return;
        }

        logger.debug("Selected project file: {}", file.getAbsolutePath());

        // Read the project file
        ProjectFileManager.LoadedProject loaded = ProjectFileManager.loadXsProject(file);
        if (loaded == null) {
            logger.warn("Failed to load project from file: {}", file.getAbsolutePath());
            return;
        }

        logger.debug("Loaded project meta: {}", loaded.meta());
        mainPage.setCurrentProjectMeta(loaded.meta());
        mainPage.getGroupedData().clear();
        loaded.items().stream()
                .collect(java.util.stream.Collectors.groupingBy(DataItem::getCategory))
                .forEach((k, v) -> mainPage.getGroupedData().put(k, v));

        logger.debug("Clipboard and grouped data updated.");
        ClipboardManager.clear();
        ClipboardManager.setClipboardKeys(loaded.clipboard(), loaded.items());

        mainPage.setCurrentProjectFile(file);
        mainPage.setModified(false);
        mainPage.showEditor();

        logger.info("Project opened: {}", file.getAbsolutePath());
    }

    /**
     * Save the current project to its file if it exists, otherwise prompt to save as.
     *
     * @param mainPage the MainPage instance to access grouped data and project meta
     */
    public static void saveProject(MainPage mainPage) {
        File file = mainPage.getCurrentProjectFile();
        if (file == null) {
            saveProjectAs(mainPage);
            return;
        }
        doSave(mainPage, file);
    }

    /**
     * Prompt the user to save the current project to a new file.
     *
     * @param mainPage the MainPage instance to access grouped data and project meta
     */
    public static void saveProjectAs(MainPage mainPage) {
        logger.debug("Prompting user to save project as a new file...");
        File file = CustomFileChooser.showSaveFileDialog(
                mainPage.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.save_as.title"),
                getLang("general.files.xsproject"),
                List.of("xsproject"),
                MainPage.getCurrentProjectMeta().getName() + ".xsproject"
        );

        if (file != null) {
            logger.debug("User selected file to save project as: {}", file.getAbsolutePath());
            doSave(mainPage, file);
            mainPage.setCurrentProjectFile(file);
            logger.info("Project saved as new file: {}", file.getAbsolutePath());
        } else {
            logger.info("Save project as operation cancelled by user.");
        }
    }

    /**
     * Save the current project to the specified file.
     *
     * @param mainPage the MainPage instance to access grouped data and project meta
     * @param file     the file to save the project to
     */
    private static void doSave(MainPage mainPage, File file) {
        logger.debug("Saving project to file: {}", file.getAbsolutePath());
        List<DataItem> items = mainPage.getGroupedData().values().stream()
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.toList());
        List<String> clipboardKeys = ClipboardManager.getClipboard().stream()
                .map(DataItem::getKey)
                .collect(java.util.stream.Collectors.toList());
        try {
            ProjectFileManager.saveXsProject(file, MainPage.getCurrentProjectMeta(), items, clipboardKeys);
            mainPage.setModified(false); // 标记已保存
            logger.info("Project saved successfully to: {}", file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to save project to file: {}", file.getAbsolutePath(), e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("module.proj_manager.save.exception.alert.content"),
                    e.getMessage()
            );
        }
    }

    /**
     * Create a new project dialog and set the current project meta in the main page.
     *
     * @param mainPage the MainPage instance to update the UI
     * @return true if the project was created successfully, false otherwise
     */
    public static boolean createProject(MainPage mainPage) {
        logger.debug("Starting createProject...");
        if (!mainPage.promptSaveIfNeeded()) {
            logger.debug("User cancelled save prompt before creating new project.");
            return false;
        }

        try {
            ProjectMeta meta = NewProjectDialog.show(mainPage.getScene().getWindow());

            if (meta != null) {
                logger.debug("User confirmed new project creation: {}", meta.getName());
                mainPage.setCurrentProjectMeta(meta);
                mainPage.getGroupedData().clear();
                ClipboardManager.clear();
                mainPage.showEditor();
                mainPage.setModified(true);

                logger.info("New project created: {}", meta.getName());
                return true;
            } else {
                logger.debug("User cancelled new project dialog.");
            }
        } catch (Exception e) {
            logger.error("Failed to create new project", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.new_proj.exception.alert.header"),
                    getLang("dialog.new_proj.exception.alert.content"),
                    e
            );
        }

        logger.info("New project creation cancelled by user.");
        return false;
    }

    /**
     * Import a XAML file into the current project.
     *
     * @param mainPage   the MainPage instance to access grouped data
     * @param isTranslated true if importing a translation XAML file, false for an original XAML file
     */
    public static void importXaml(MainPage mainPage, boolean isTranslated) {
        logger.debug("Starting importXaml. isTranslated: {}", isTranslated);
        File file = CustomFileChooser.showOpenFileDialog(
                mainPage.getRootPane().getScene().getWindow(),
                getLang("module.proj_manager.import.%s.title".formatted(isTranslated ? "original" : "translated")),
                getLang("general.files.xaml"),
                List.of("xaml", "xml")
        );

        if (file != null) {
            logger.debug("Selected XAML file: {}", file.getAbsolutePath());
            try {
                List<DataItem> items = FileProcessor.parseXamlFile(file, isTranslated);
                logger.debug("Parsed {} items from XAML file.", items.size());
                String column = isTranslated ? "translated" : "original";
                DataOperationHelper.applyColumnUpdates(items, mainPage.getGroupedData(), column);
                logger.debug("Applied column updates to grouped data. Column: {}", column);

                SortAndRefresher.refresh(mainPage.getDataTreeTable(), mainPage.getGroupedData());
                logger.debug("Refreshed data tree table.");
                ShowAlert.info(
                        getLang("general.alert.success"),
                        getLang("module.proj_manager.import_xaml.success.alert.content")
                );
                logger.info("XAML import successful: {}", file.getAbsolutePath());
                return;
            } catch (Exception e) {
                logger.error("Failed to import XAML file: {}", file.getAbsolutePath(), e);
                ShowAlert.error(
                        getLang("general.alert.error"),
                        getLang("module.proj_manager.import_xaml.exception.alert.content"),
                        e.getMessage()
                );
            }
        } else {
            logger.info("No file selected for XAML import.");
        }
        logger.info("Import XAML cancelled or failed.");
    }

    /**
     * Create a new project from a XAML file.
     *
     * @param mainPage   the MainPage instance to access grouped data
     * @param isTranslated true if importing a translation XAML file, false for an original XAML file
     * @see #createProject(MainPage)
     * @see #importXaml(MainPage, boolean)
     */
    public static void createFromXaml(MainPage mainPage, boolean isTranslated) {
        if (createProject(mainPage)) {
            importXaml(mainPage, isTranslated);
            return;
        }
        logger.info("Create project from XAML cancelled by user.");
    }

}
