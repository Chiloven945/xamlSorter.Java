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
import java.util.concurrent.ExecutionException;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class ProjectManager {
    private static final Logger logger = LogManager.getLogger(ProjectManager.class);

    /**
     * Edit the current project metadata.
     * This method shows a dialog in the UI thread and processes data in a background thread.
     *
     * @param mainPage the MainPage instance to update
     */
    public static void editProjectMeta(MainPage mainPage) {
        logger.info("Opening project metadata edit dialog...");
        try {
            ProjectMeta currentMeta = MainPage.getCurrentProjectMeta();
            ProjectMeta updatedMeta = chiloven.xamlsorter.ui.dialogs.ProjectMetaEditDialog.show(
                    mainPage.getRootPane().getScene().getWindow(),
                    currentMeta
            );

            if (updatedMeta != null) {
                TaskExecutorService.executeTask(
                        "EditProjectMeta",
                        () -> {
                            logger.debug("User confirmed project metadata edit: {}", updatedMeta.getName());
                            return updatedMeta;
                        },
                        result -> {
                            mainPage.setCurrentProjectMeta(result);
                            mainPage.setModified(true);
                            logger.info("Project metadata updated: {}", result.getName());
                        },
                        error -> {
                            logger.error("Failed to process project metadata update", error);
                            Exception exception = (error instanceof Exception) ? (Exception) error : new Exception(error);
                            ShowAlert.error(
                                    getLang("general.alert.error"),
                                    getLang("dialog.edit_proj.exception.alert.header"),
                                    getLang("dialog.edit_proj.exception.alert.content"),
                                    exception
                            );
                        }
                );
            } else {
                logger.debug("User cancelled project metadata edit dialog.");
            }
        } catch (Exception e) {
            logger.error("Failed to edit project metadata", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.edit_proj.exception.alert.header"),
                    getLang("dialog.edit_proj.exception.alert.content"),
                    e
            );
        }
    }

    /**
     * Open an existing project file (.xsproject), load data and update the main page.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
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

        final File selectedFile = file;
        TaskExecutorService.executeTask(
                "OpenProject",
                () -> {
                    ProjectFileManager.LoadedProject loaded = ProjectFileManager.loadXsProject(selectedFile);
                    if (loaded == null) {
                        logger.warn("Failed to load project from file: {}", selectedFile.getAbsolutePath());
                        return null;
                    }

                    return loaded;
                },
                loaded -> {
                    if (loaded != null) {
                        logger.debug("Loaded project meta: {}", loaded.meta());
                        mainPage.setCurrentProjectMeta(loaded.meta());
                        mainPage.getGroupedData().clear();
                        loaded.items().stream()
                                .collect(java.util.stream.Collectors.groupingBy(DataItem::getCategory))
                                .forEach((k, v) -> mainPage.getGroupedData().put(k, v));

                        logger.debug("Clipboard and grouped data updated.");
                        ClipboardManager.clear();
                        ClipboardManager.setClipboardKeys(loaded.clipboard(), loaded.items());

                        mainPage.setCurrentProjectFile(selectedFile);
                        mainPage.setModified(false);
                        mainPage.showEditor();

                        logger.info("Project opened: {}", selectedFile.getAbsolutePath());
                    }
                },
                error -> {
                    logger.error("Failed to open project: {}", selectedFile.getAbsolutePath(), error);
                    Exception exception = (error instanceof Exception) ? (Exception) error : new Exception(error);
                    ShowAlert.error(
                            getLang("general.alert.error"),
                            getLang("module.proj_manager.open.exception.alert.header"),
                            getLang("module.proj_manager.open.exception.alert.content", selectedFile.getAbsolutePath()),
                            exception
                    );
                }
        );
    }

    /**
     * Save the current project to its file if it exists, otherwise prompt to save as.
     * This method processes data in a background thread.
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
     * This method processes data in a background thread.
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
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param mainPage the MainPage instance to access grouped data and project meta
     * @param file     the file to save the project to
     */
    private static void doSave(MainPage mainPage, File file) {
        logger.debug("Saving project to file: {}", file.getAbsolutePath());

        TaskExecutorService.executeTask(
                "SaveProject",
                () -> {
                    List<DataItem> items = mainPage.getGroupedData().values().stream()
                            .flatMap(List::stream)
                            .collect(java.util.stream.Collectors.toList());
                    List<String> clipboardKeys = ClipboardManager.getClipboard().stream()
                            .map(DataItem::getKey)
                            .collect(java.util.stream.Collectors.toList());

                    try {
                        try {
                            return ProjectFileManager.saveXsProject(file, MainPage.getCurrentProjectMeta(), items, clipboardKeys)
                                    .thenApply(success -> {
                                        if (success) {
                                            logger.debug("Project saved successfully to file: {}", file.getAbsolutePath());
                                            return true;
                                        } else {
                                            logger.warn("Project save operation returned false for file: {}", file.getAbsolutePath());
                                            return false;
                                        }
                                    }).get();
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Exception while waiting for project save completion", e);
                            throw new RuntimeException("Failed to save project: " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to save project to file: {}", file.getAbsolutePath(), e);
                        throw e;
                    }
                },
                success -> {
                    mainPage.setModified(false); // 标记已保存
                    logger.info("Project saved successfully to: {}", file.getAbsolutePath());
                },
                error -> {
                    logger.error("Failed to save project to file: {}", file.getAbsolutePath(), error);
                    Exception exception = (error instanceof Exception) ? (Exception) error : new Exception(error);
                    ShowAlert.error(
                            getLang("general.alert.error"),
                            getLang("module.proj_manager.save.exception.alert.header"),
                            getLang("module.proj_manager.save.exception.alert.content"),
                            exception
                    );
                }
        );
    }

    /**
     * Create a new project dialog and set the current project meta in the main page.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
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
                TaskExecutorService.executeTask(
                        "CreateProject",
                        () -> {
                            logger.debug("User confirmed new project creation: {}", meta.getName());
                            return meta;
                        },
                        result -> {
                            mainPage.setCurrentProjectMeta(result);
                            mainPage.getGroupedData().clear();
                            ClipboardManager.clear();
                            mainPage.showEditor();
                            mainPage.setModified(true);

                            logger.info("New project created: {}", result.getName());
                        },
                        error -> {
                            logger.error("Failed to process new project creation", error);
                            Exception exception = (error instanceof Exception) ? (Exception) error : new Exception(error);
                            ShowAlert.error(
                                    getLang("general.alert.error"),
                                    getLang("dialog.new_proj.exception.alert.header"),
                                    getLang("dialog.new_proj.exception.alert.content"),
                                    exception
                            );
                        }
                );
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
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param mainPage     the MainPage instance to access grouped data
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

            final File selectedFile = file;
            TaskExecutorService.executeTask(
                    "ImportXaml",
                    () -> {
                        try {
                            List<DataItem> items = FileProcessor.parseXamlFile(selectedFile, isTranslated);
                            logger.debug("Parsed {} items from XAML file.", items.size());
                            return new Object[]{items, isTranslated};
                        } catch (Exception e) {
                            logger.error("Failed to parse XAML file: {}", selectedFile.getAbsolutePath(), e);
                            throw e;
                        }
                    },
                    result -> {
                        List<DataItem> items = (List<DataItem>) result[0];
                        boolean translated = (boolean) result[1];

                        String column = translated ? "translated" : "original";
                        DataOperationHelper.applyColumnUpdates(items, mainPage.getGroupedData(), column);
                        logger.debug("Applied column updates to grouped data. Column: {}", column);

                        SortAndRefresher.refresh(mainPage.getDataTreeTable(), mainPage.getGroupedData());
                        logger.debug("Refreshed data tree table.");
                        ShowAlert.info(
                                getLang("general.alert.success"),
                                getLang("module.proj_manager.import_xaml.success.alert.content")
                        );
                        logger.info("XAML import successful: {}", selectedFile.getAbsolutePath());
                    },
                    error -> {
                        logger.error("Failed to import XAML file: {}", selectedFile.getAbsolutePath(), error);
                        Exception exception = (error instanceof Exception) ? (Exception) error : new Exception(error);
                        ShowAlert.error(
                                getLang("general.alert.error"),
                                getLang("module.proj_manager.import_xaml.exception.alert.header"),
                                getLang("module.proj_manager.import_xaml.exception.alert.content"),
                                exception
                        );
                    }
            );
        } else {
            logger.info("No file selected for XAML import.");
        }
    }

    /**
     * Create a new project from a XAML file.
     * This method processes data in a background thread and updates the UI in the JavaFX application thread.
     *
     * @param mainPage     the MainPage instance to access grouped data
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
