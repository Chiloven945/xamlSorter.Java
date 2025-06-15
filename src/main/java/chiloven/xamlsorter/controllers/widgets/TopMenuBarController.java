package chiloven.xamlsorter.controllers.widgets;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.controllers.dialogs.AboutDialogController;
import chiloven.xamlsorter.controllers.dialogs.RegexEditDialogController;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.ProjectManager;
import chiloven.xamlsorter.utils.BrowserUtil;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXML;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TopMenuBarController {
    private static final Logger logger = LogManager.getLogger(TopMenuBarController.class);

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // =========================
    // File Menu Handlers
    // =========================

    // Save (as) Project Handler
    @FXML
    private void handleSaveProject() {
        ProjectManager.saveProject(mainController);
    }

    @FXML
    private void handleSaveAsProject() {
        ProjectManager.saveProjectAs(mainController);
    }

    // Create Project Handler
    @FXML
    private void handleCreateProject() {
        ProjectManager.createProject(mainController);
    }

    @FXML
    private void handleCreateFromOriginal() {
        ProjectManager.createFromXaml(mainController, false);
    }

    @FXML
    private void handleCreateFromTranslated() {
        ProjectManager.createFromXaml(mainController, true);
    }

    // Import From XAML Handler
    @FXML
    private void handleImportOriginal() {
        ProjectManager.importXaml(mainController, false);
    }

    @FXML
    private void handleImportTranslated() {
        ProjectManager.importXaml(mainController, true);
    }

    // Export Configuration Handler
    @FXML
    private void handleExport() {
        ProjectManager.showExportDialog(mainController);
    }

    // =========================
    // Edit Menu Handlers
    // =========================

    // Method to handle the "Save" action
    @FXML
    private void handleAdd() {
        DataOperationHelper.addEntry(mainController.getGroupedData());
    }

    // Method to handle the "Save" action
    @FXML
    private void handleCut() {
        DataOperationHelper.cut(mainController.getDataTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Save" action
    @FXML
    private void handleCopy() {
        DataOperationHelper.copy(mainController.getDataTreeTable());
    }

    // Method to handle the "Paste" action
    @FXML
    private void handlePaste() {
        DataOperationHelper.paste(mainController.getDataTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Delete" action
    @FXML
    private void handleDelete() {
        DataOperationHelper.delete(mainController.getDataTreeTable(), mainController.getGroupedData());
    }

    // Method to handle the "Select All" action
    @FXML
    private void handleSelectAll() {
        DataOperationHelper.selectAll(mainController.getDataTreeTable());
    }

    // Method to handle the "Unselect All" action
    @FXML
    private void handleUnselectAll() {
        DataOperationHelper.unselectAll(mainController.getDataTreeTable());
    }

    // =========================
    // Tools Menu Handlers
    // =========================

    // Regex Edit Dialog Handler
    @FXML
    private void handleRegexEdit() {
        RegexEditDialogController.showAndHandleRegexEdit(mainController.getGroupedData(), mainController);
    }

    // =========================
    // Help Menu Handlers
    // =========================

    // Method to open the "About" dialog
    @FXML
    private void handleAbout() {
        AboutDialogController.showAboutDialog(
                mainController.getDataTreeTable().getScene().getWindow()
        );
    }

    @FXML
    private void handleLicense() {
        ShowAlert.info(
                "License",
                "xamlSorter.Java License",
                """
                        This project is licensed under the GNU General Public License v3.0 (GPL-3.0).
                        
                        You can view the full license text at: https://www.gnu.org/licenses/gpl-3.0.en.html"""
        );
        logger.info("License information displayed to the user.");
    }

    @FXML
    private void handleDocumentation() {
        ShowAlert.info(
                "Documentation",
                "WIP...",
                "The documentation is currently a work in progress. Please check back later for updates. :("
                );
    }

    @FXML
    private void handleReportIssue() {
        BrowserUtil.openWebpage("https://github.com/Chiloven945/xamlSorter.Java/issues/new");
    }

    @FXML
    private void handleGitHub() {
        BrowserUtil.openWebpage("https://github.com/Chiloven945/xamlSorter.Java");
    }

}
