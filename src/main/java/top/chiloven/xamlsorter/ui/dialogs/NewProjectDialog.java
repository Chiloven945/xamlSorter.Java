package top.chiloven.xamlsorter.ui.dialogs;

import top.chiloven.xamlsorter.entities.ProjectMeta;
import top.chiloven.xamlsorter.modules.I18n;
import top.chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static top.chiloven.xamlsorter.modules.I18n.getLang;

public class NewProjectDialog extends Dialog<ProjectMeta> {
    private static final Logger logger = LogManager.getLogger(NewProjectDialog.class);

    private final TextField projectNameField;
    private final TextField projectAuthorField;
    private final TextArea projectDescField;

    public NewProjectDialog(Window owner) {
        this.projectNameField = new TextField();
        this.projectAuthorField = new TextField();
        this.projectDescField = new TextArea();

        try {
            setupDialog(owner);
        } catch (Exception e) {
            logger.error("Failed to create New Project dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.new_proj.exception.alert.header"),
                    getLang("dialog.new_proj.exception.alert.content"),
                    e
            );
        }
    }

    public static ProjectMeta show(Window owner) {
        logger.info("Opening New Project dialog");
        try {
            NewProjectDialog dialog = new NewProjectDialog(owner);
            return dialog.showAndWait().orElse(null);
        } catch (Exception e) {
            logger.error("Failed to show New Project dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.new_proj.exception.alert.header"),
                    getLang("dialog.new_proj.exception.alert.content"),
                    e
            );
            return null;
        }
    }

    private void setupDialog(Window owner) {
        // 基本对话框设置
        setTitle(getLang("dialog.new_proj.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // 创建主容器
        VBox content = new VBox(10);
        content.setPrefHeight(160.0);
        content.setPrefWidth(320.0);

        // 创建标题标签
        Label titleLabel = new Label(getLang("dialog.new_proj.title"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 配置输入字段
        projectNameField.setPromptText(getLang("dialog.new_proj.name.label"));
        projectAuthorField.setPromptText(getLang("dialog.new_proj.author.label"));
        projectDescField.setPromptText(getLang("dialog.new_proj.description.label"));
        projectDescField.setPrefRowCount(2);

        // 组装内容
        content.getChildren().addAll(
                titleLabel,
                projectNameField,
                projectAuthorField,
                projectDescField
        );

        // 创建对话框按钮
        ButtonType createButton = new ButtonType(getLang("general.button.create"),
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(createButton, cancelButton);

        // 获取创建按钮的 Node 引用
        Node createBtnNode = getDialogPane().lookupButton(createButton);
        createBtnNode.setDisable(true); // 默认禁用

        // 定义非法字符正则
        String forbiddenPattern = "[\\\\/:*?\"<>|]";

        // 监听输入框变化
        projectNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            String trimmed = newVal.trim();
            boolean invalid = trimmed.isEmpty()
                    || trimmed.matches(".*" + forbiddenPattern + ".*")
                    || trimmed.endsWith(".")
                    || trimmed.endsWith(" ");

            createBtnNode.setDisable(invalid);
        });

        // 设置对话框内容和内边距
        getDialogPane().setContent(content);
        getDialogPane().setPadding(new Insets(10));

        // 设置结果转换器
        setResultConverter(buttonType -> {
            if (buttonType == createButton) {
                return getProjectMeta();
            }
            return null;
        });

        // 应用默认字体
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        logger.debug("New Project dialog initialized");
    }

    private ProjectMeta getProjectMeta() {
        return new ProjectMeta(
                projectNameField.getText().trim(),
                projectAuthorField.getText().trim(),
                projectDescField.getText().trim()
        );
    }

    public TextField getProjectNameField() {
        return projectNameField;
    }
}
