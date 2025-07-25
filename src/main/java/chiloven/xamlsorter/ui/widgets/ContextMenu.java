package chiloven.xamlsorter.ui.widgets;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.modules.ClipboardManager;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.SortAndRefresher;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class ContextMenu extends javafx.scene.control.ContextMenu {
    private static final Logger logger = LogManager.getLogger(ContextMenu.class);

    private final MenuItem copyEntry;
    private final MenuItem pasteEntry;
    private final MenuItem cutEntry;
    private final MenuItem deleteEntry;
    private final MenuItem addEntry;
    private final MenuItem selectAll;
    private final MenuItem unselectAll;

    private Map<String, List<DataItem>> groupedData;
    private TreeTableView<DataItem> translationTreeTable;

    public ContextMenu() {
        // 创建菜单项
        copyEntry = new MenuItem(getLang("widget.context_menu.copy"));
        pasteEntry = new MenuItem(getLang("widget.context_menu.paste"));
        cutEntry = new MenuItem(getLang("widget.context_menu.cut"));
        deleteEntry = new MenuItem(getLang("widget.context_menu.delete"));
        addEntry = new MenuItem(getLang("general.button.add_entry"));
        selectAll = new MenuItem(getLang("widget.context_menu.select_all"));
        unselectAll = new MenuItem(getLang("widget.context_menu.unselect_all"));

        // 设置事件处理
        copyEntry.setOnAction(event -> handleCopy());
        pasteEntry.setOnAction(event -> handlePaste());
        cutEntry.setOnAction(event -> handleCut());
        deleteEntry.setOnAction(event -> handleDelete());
        addEntry.setOnAction(event -> handleAdd());
        selectAll.setOnAction(event -> handleSelectAll());
        unselectAll.setOnAction(event -> handleUnselectAll());

        // 组装菜单
        getItems().addAll(
                copyEntry,
                pasteEntry,
                cutEntry,
                new SeparatorMenuItem(),
                deleteEntry,
                addEntry,
                new SeparatorMenuItem(),
                selectAll,
                unselectAll
        );

        logger.debug("Context menu created");
    }

    public void initialize(Map<String, List<DataItem>> groupedData,
                           TreeTableView<DataItem> translationTreeTable,
                           DataItem targetItem) {
        logger.info("Initializing context menu...");
        this.groupedData = groupedData;
        this.translationTreeTable = translationTreeTable;

        boolean hasTarget = targetItem != null;
        boolean hasClipboard = ClipboardManager.hasContent();

        logger.debug("Target item present: {}", hasTarget);
        logger.debug("Clipboard has content: {}", hasClipboard);

        copyEntry.setDisable(!hasTarget);
        cutEntry.setDisable(!hasTarget);
        deleteEntry.setDisable(!hasTarget);
        pasteEntry.setDisable(!hasClipboard);

        logger.debug("Context menu initialized with grouped data size: {}, target item: {}, clipboard has content: {}",
                groupedData.size(), hasTarget, hasClipboard);
    }

    private void handleCopy() {
        DataOperationHelper.copy(translationTreeTable);
    }

    private void handlePaste() {
        DataOperationHelper.paste(translationTreeTable, groupedData);
    }

    private void handleCut() {
        DataOperationHelper.cut(translationTreeTable, groupedData);
    }

    private void handleDelete() {
        DataOperationHelper.delete(translationTreeTable, groupedData);
    }

    private void handleAdd() {
        DataOperationHelper.addEntry(groupedData);
        SortAndRefresher.refresh(translationTreeTable, groupedData);
    }

    private void handleSelectAll() {
        DataOperationHelper.selectAll(translationTreeTable);
    }

    private void handleUnselectAll() {
        DataOperationHelper.unselectAll(translationTreeTable);
    }
}