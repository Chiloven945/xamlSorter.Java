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

public class ProjectMetaEditDialog extends Dialog<ProjectMeta> {
    private static final Logger logger = LogManager.getLogger(ProjectMetaEditDialog.class);

    private final TextField projectNameField;
    private final TextField projectAuthorField;
    private final TextArea projectDescField;

    public ProjectMetaEditDialog(Window owner, ProjectMeta currentMeta) {
        this.projectNameField = new TextField(currentMeta.getName());
        this.projectAuthorField = new TextField(currentMeta.getAuthor());
        this.projectDescField = new TextArea(currentMeta.getDescription());

        try {
            setupDialog(owner);
        } catch (Exception e) {
            logger.error("Failed to create Project Meta Edit dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.edit_proj.exception.alert.header"),
                    getLang("dialog.edit_proj.exception.alert.content"),
                    e
            );
        }
    }

    public static ProjectMeta show(Window owner, ProjectMeta currentMeta) {
        logger.info("Opening Project Meta Edit dialog");
        try {
            ProjectMetaEditDialog dialog = new ProjectMetaEditDialog(owner, currentMeta);
            return dialog.showAndWait().orElse(null);
        } catch (Exception e) {
            logger.error("Failed to show Project Meta Edit dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.edit_proj.exception.alert.header"),
                    getLang("dialog.edit_proj.exception.alert.content"),
                    e
            );
            return null;
        }
    }

    private void setupDialog(Window owner) {
        setTitle(getLang("dialog.edit_proj.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        VBox content = new VBox(10);
        content.setPrefHeight(160.0);
        content.setPrefWidth(320.0);

        Label titleLabel = new Label(getLang("dialog.edit_proj.title"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        projectNameField.setPromptText(getLang("dialog.new_proj.name.label"));
        projectAuthorField.setPromptText(getLang("dialog.new_proj.author.label"));
        projectDescField.setPromptText(getLang("dialog.new_proj.description.label"));
        projectDescField.setPrefRowCount(2);

        content.getChildren().addAll(
                titleLabel,
                projectNameField,
                projectAuthorField,
                projectDescField
        );

        ButtonType saveButton = new ButtonType(getLang("general.button.save"),
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        Node saveBtnNode = getDialogPane().lookupButton(saveButton);
        saveBtnNode.setDisable(true);

        String forbiddenPattern = "[\\\\/:*?\"<>|]";

        projectNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            String trimmed = newVal.trim();
            boolean invalid = trimmed.isEmpty()
                    || trimmed.matches(".*" + forbiddenPattern + ".*")
                    || trimmed.endsWith(".")
                    || trimmed.endsWith(" ");
            saveBtnNode.setDisable(invalid);
        });

        getDialogPane().setContent(content);
        getDialogPane().setPadding(new Insets(10));

        setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                return getProjectMeta();
            }
            return null;
        });

        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        logger.debug("Project Meta Edit dialog initialized");
    }

    private ProjectMeta getProjectMeta() {
        return new ProjectMeta(
                projectNameField.getText().trim(),
                projectDescField.getText().trim(),
                projectAuthorField.getText().trim()
        );
    }
}