package chiloven.xamlsorter.ui.widgets;

import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.ProjectManager;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.ui.dialogs.*;
import chiloven.xamlsorter.utils.BrowserUtil;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class TopMenuBar extends MenuBar {
    private static final Logger logger = LogManager.getLogger(TopMenuBar.class);

    private final MainPage mainPage;
    private final Menu fileMenu;
    private final Menu editMenu;
    private final Menu toolsMenu;
    private final Menu helpMenu;

    public TopMenuBar(MainPage mainPage) {
        this.mainPage = mainPage;

        // 创建主菜单
        fileMenu = createFileMenu();
        editMenu = createEditMenu();
        toolsMenu = createToolsMenu();
        helpMenu = createHelpMenu();

        // 添加所有主菜单
        getMenus().addAll(fileMenu, editMenu, toolsMenu, helpMenu);

        logger.debug("TopMenuBar initialized");
    }

    private Menu createFileMenu() {
        Menu menu = new Menu(getLang("widget.menu_bar.file"));

        MenuItem openProject = new MenuItem(getLang("general.proj.open"));
        openProject.setOnAction(e -> ProjectManager.openProject(mainPage));

        MenuItem saveProject = new MenuItem(getLang("widget.menu_bar.file.save"));
        saveProject.setOnAction(e -> ProjectManager.saveProject(mainPage));

        MenuItem saveAsProject = new MenuItem(getLang("widget.menu_bar.file.save_as"));
        saveAsProject.setOnAction(e -> ProjectManager.saveProjectAs(mainPage));

        MenuItem createProject = new MenuItem(getLang("general.proj.new"));
        createProject.setOnAction(e -> ProjectManager.createProject(mainPage));

        MenuItem createFromOriginal = new MenuItem(getLang("general.button.proj.new.original"));
        createFromOriginal.setOnAction(e -> ProjectManager.createFromXaml(mainPage, false));

        MenuItem createFromTranslated = new MenuItem(getLang("general.button.proj.new.translated"));
        createFromTranslated.setOnAction(e -> ProjectManager.createFromXaml(mainPage, true));

        MenuItem importOriginal = new MenuItem(getLang("widget.menu_bar.file.import.original"));
        importOriginal.setOnAction(e -> ProjectManager.importXaml(mainPage, false));

        MenuItem importTranslated = new MenuItem(getLang("widget.menu_bar.file.import.translated"));
        importTranslated.setOnAction(e -> ProjectManager.importXaml(mainPage, true));

        MenuItem export = new MenuItem(getLang("widget.menu_bar.file.export"));
        export.setOnAction(e -> ExportDialog.show(mainPage));

        menu.getItems().addAll(
                openProject,
                saveProject,
                saveAsProject,
                new SeparatorMenuItem(),
                createProject,
                createFromOriginal,
                createFromTranslated,
                new SeparatorMenuItem(),
                importOriginal,
                importTranslated,
                new SeparatorMenuItem(),
                export
        );

        return menu;
    }

    private Menu createEditMenu() {
        Menu menu = new Menu(getLang("widget.menu_bar.edit"));

        MenuItem undo = new MenuItem(getLang("widget.menu_bar.edit.undo"));
        undo.setDisable(true); // 待实现

        MenuItem redo = new MenuItem(getLang("widget.menu_bar.edit.redo"));
        redo.setDisable(true); // 待实现

        MenuItem cut = new MenuItem(getLang("widget.context_menu.cut"));
        cut.setOnAction(e -> DataOperationHelper.cut(mainPage.getDataTreeTable(), mainPage.getGroupedData()));

        MenuItem copy = new MenuItem(getLang("widget.context_menu.copy"));
        copy.setOnAction(e -> DataOperationHelper.copy(mainPage.getDataTreeTable()));

        MenuItem paste = new MenuItem(getLang("widget.context_menu.paste"));
        paste.setOnAction(e -> DataOperationHelper.paste(mainPage.getDataTreeTable(), mainPage.getGroupedData()));

        MenuItem delete = new MenuItem(getLang("widget.context_menu.delete"));
        delete.setOnAction(e -> DataOperationHelper.delete(mainPage.getDataTreeTable(), mainPage.getGroupedData()));

        MenuItem add = new MenuItem(getLang("general.button.add_entry"));
        add.setOnAction(e -> DataOperationHelper.addEntry(mainPage.getGroupedData()));

        MenuItem selectAll = new MenuItem(getLang("widget.context_menu.select_all"));
        selectAll.setOnAction(e -> DataOperationHelper.selectAll(mainPage.getDataTreeTable()));

        MenuItem unselectAll = new MenuItem(getLang("widget.context_menu.unselect_all"));
        unselectAll.setOnAction(e -> DataOperationHelper.unselectAll(mainPage.getDataTreeTable()));

        MenuItem editProjectMeta = new MenuItem(getLang("widget.menu_bar.edit.project_meta"));
        editProjectMeta.setOnAction(e -> ProjectManager.editProjectMeta(mainPage));

        MenuItem preferences = new MenuItem(getLang("widget.menu_bar.edit.preferences"));
        preferences.setOnAction(e -> PreferencesDialog.show(mainPage.getRootPane().getScene().getWindow()));

        menu.getItems().addAll(
                undo,
                redo,
                new SeparatorMenuItem(),
                cut,
                copy,
                paste,
                new SeparatorMenuItem(),
                delete,
                add,
                new SeparatorMenuItem(),
                selectAll,
                unselectAll,
                new SeparatorMenuItem(),
                editProjectMeta,
                preferences
        );

        return menu;
    }

    private Menu createToolsMenu() {
        Menu menu = new Menu(getLang("widget.menu_bar.tools"));

        MenuItem regexEdit = new MenuItem(getLang("widget.menu_bar.tools.regex"));
        regexEdit.setOnAction(e -> RegExEditDialog.show(mainPage, mainPage.getGroupedData(), null));

        menu.getItems().add(regexEdit);

        return menu;
    }

    private Menu createHelpMenu() {
        Menu menu = new Menu(getLang("widget.menu_bar.help"));

        MenuItem about = new MenuItem(getLang("general.button.about"));
        about.setOnAction(e -> AboutDialog.show(mainPage.getDataTreeTable().getScene().getWindow()));

        MenuItem license = new MenuItem(getLang("widget.menu_bar.help.license"));
        license.setOnAction(e -> LicenseDialog.show(mainPage.getDataTreeTable().getScene().getWindow()));

        MenuItem documentation = new MenuItem(getLang("widget.menu_bar.help.documentation"));
        documentation.setOnAction(e -> ShowAlert.info(
                "Documentation",
                "WIP...",
                "The documentation is currently a work in progress. Please check back later for updates. :("
        ));

        MenuItem reportIssue = new MenuItem(getLang("widget.menu_bar.help.report_issue"));
        reportIssue.setOnAction(e -> BrowserUtil.openWebpage(
                "https://github.com/Chiloven945/xamlSorter.Java/issues/new"
        ));

        MenuItem github = new MenuItem(getLang("widget.menu_bar.help.github"));
        github.setOnAction(e -> BrowserUtil.openWebpage(
                "https://github.com/Chiloven945/xamlSorter.Java"
        ));

        menu.getItems().addAll(
                about,
                license,
                new SeparatorMenuItem(),
                documentation,
                reportIssue,
                github
        );

        return menu;
    }
}