package chiloven.xamlsorter.ui.dialogs;

import chiloven.xamlsorter.entities.DataItem;
import chiloven.xamlsorter.modules.FileProcessor;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.utils.CustomFileChooser;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class ExportDialog extends Dialog<Void> {
    private static final Logger logger = LogManager.getLogger(ExportDialog.class);

    private final ChoiceBox<String> fileTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<String> fieldChoiceBox = new ChoiceBox<>();
    private final CheckBox commentCheckBox = new CheckBox();
    private final Map<String, List<DataItem>> groupedData;
    private final MainPage mainPage;

    public ExportDialog(MainPage mainPage) {
        this.mainPage = mainPage;
        this.groupedData = mainPage.getGroupedData();

        try {
            setupDialog();
        } catch (Exception e) {
            logger.error("Failed to create Export dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.export.exception.alert.header"),
                    getLang("dialog.export.exception.alert.content"),
                    e
            );
        }
    }

    public static void show(MainPage mainPage) {
        logger.debug("Opening Export dialog");
        try {
            ExportDialog dialog = new ExportDialog(mainPage);
            dialog.showAndWait();
            logger.debug("Export dialog closed");
        } catch (Exception e) {
            logger.error("Failed to show Export dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.export.exception.alert.header"),
                    getLang("dialog.export.exception.alert.content"),
                    e
            );
        }
    }

    private void setupDialog() {
        // 基本对话框设置
        setTitle(getLang("dialog.export.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(mainPage.getDataTreeTable().getScene().getWindow());

        // 创建主容器
        VBox content = new VBox(12);
        content.setPadding(new Insets(12, 16, 12, 16));

        // 添加标题
        Label headerLabel = new Label(getLang("dialog.export.header"));
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 文件类型选择
        HBox fileTypeBox = new HBox(10);
        fileTypeBox.setAlignment(Pos.CENTER_LEFT);
        Label fileTypeLabel = new Label(getLang("dialog.export.filetype.label"));
        fileTypeChoiceBox.getItems().addAll(".xaml", ".json");
        fileTypeChoiceBox.getSelectionModel().selectFirst();
        fileTypeBox.getChildren().addAll(fileTypeLabel, fileTypeChoiceBox);

        // 导出字段选择
        HBox fieldBox = new HBox(10);
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        Label fieldLabel = new Label(getLang("dialog.export.field_to_export.label"));
        fieldChoiceBox.getItems().addAll(
                getLang("general.datatype.original"),
                getLang("general.datatype.translated")
        );
        fieldChoiceBox.getSelectionModel().selectFirst();
        fieldBox.getChildren().addAll(fieldLabel, fieldChoiceBox);

        // 注释选项
        commentCheckBox.setText(getLang("dialog.export.top_level_comments.checkbox"));
        commentCheckBox.setSelected(true);

        // 组装内容
        content.getChildren().addAll(
                headerLabel,
                fileTypeBox,
                fieldBox,
                commentCheckBox
        );

        // 设置对话框内容和按钮
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 获取并配置按钮
        Button exportButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        exportButton.setText(getLang("dialog.export.button.export"));
        cancelButton.setText(getLang("general.button.cancel"));

        // 配置导出操作
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                handleExport();
            }
            return null;
        });

        // 设置对话框大小
        getDialogPane().setPrefWidth(360.0);
        getDialogPane().setPrefHeight(220.0);

        // 应用默认字体
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        logger.debug("Export dialog initialized");
    }

    private void handleExport() {
        logger.debug("Export initiated by user");
        String fileType = fileTypeChoiceBox.getValue();
        String fieldToExport = fieldChoiceBox.getValue();
        boolean addComments = commentCheckBox.isSelected();

        logger.debug("Selected fileType: {}, fieldToExport: {}, addComments: {}",
                fileType, fieldToExport, addComments);

        String projectName = MainPage.getCurrentProjectMeta().getName();
        logger.debug("Current project name: {}", projectName);

        String suffix = getLang("general.datatype." +
                (fieldToExport.equalsIgnoreCase(getLang("general.datatype.original"))
                        ? "original" : "translated")).toLowerCase();
        String fileExtension = fileType.toLowerCase();
        String defaultFileName = projectName + "-" + suffix + fileExtension;
        logger.debug("Default export file name: {}", defaultFileName);

        // 根据文件类型选择不同的文件描述
        String fileDescription;
        if (fileType.equals(".json")) {
            fileDescription = getLang("general.files.json");
        } else {
            fileDescription = getLang("general.files.xaml");
        }

        File file = CustomFileChooser.showSaveFileDialog(
                getDialogPane().getScene().getWindow(),
                getLang("dialog.export.file.title"),
                fileDescription,
                List.of(fileType),
                defaultFileName
        );

        if (file != null) {
            logger.info("Exporting to file: {}", file.getAbsolutePath());
            FileProcessor.exportToFile(file, fileType, fieldToExport, addComments, groupedData);
            close();
        } else {
            logger.info("Export cancelled or no file selected");
        }
    }
}
