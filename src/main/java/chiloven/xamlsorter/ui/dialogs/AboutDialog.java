package chiloven.xamlsorter.ui.dialogs;

import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static chiloven.xamlsorter.Main.version;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class AboutDialog extends Dialog<Void> {
    private static final Logger logger = LogManager.getLogger(AboutDialog.class);

    public AboutDialog(Window owner) {
        try {
            setupDialog(owner);
        } catch (Exception e) {
            logger.error("Failed to create About dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.about.exception.alert.header"),
                    getLang("dialog.about.exception.alert.content"),
                    e
            );
        }
    }

    public static void show(Window owner) {
        logger.info("Opening About dialog");
        try {
            AboutDialog dialog = new AboutDialog(owner);
            dialog.showAndWait();
            logger.debug("About dialog closed");
        } catch (Exception e) {
            logger.error("Failed to show About dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.about.exception.alert.header"),
                    getLang("dialog.about.exception.alert.content"),
                    e
            );
        }
    }

    private void setupDialog(Window owner) {
        // 基本对话框设置
        setTitle(getLang("dialog.about.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // 创建主容器
        VBox mainContainer = new VBox(18.0);

        // 创建顶部布局
        HBox topSection = new HBox();
        topSection.setMaxWidth(765.0);

        // 创建左侧文本区域
        VBox leftSection = new VBox(18.0);
        Label applicationNameLabel = new Label(getLang("general.application.name"));
        applicationNameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label appVersionLabel = new Label(getLang("dialog.about.text.version") + version);
        appVersionLabel.setStyle("-fx-font-size: 16px;");

        Label copyrightLabel = new Label(getLang("dialog.about.text.copyright"));
        copyrightLabel.setStyle("-fx-font-size: 13px;");

        leftSection.getChildren().addAll(applicationNameLabel, appVersionLabel, copyrightLabel);

        // 创建中间空白区域
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // 创建和配置图标视图
        ImageView appIconView = new ImageView();
        appIconView.setFitHeight(100.0);
        appIconView.setFitWidth(200.0);
        appIconView.setPreserveRatio(true);
        appIconView.setPickOnBounds(true);

        // 加载图标
        try {
            Image iconImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/icons/application/application-about.png")));
            appIconView.setImage(iconImage);
            logger.debug("Application icon loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load application icon", e);
        }

        // 组装顶部布局
        topSection.getChildren().addAll(leftSection, spacer, appIconView);

        // 添加分隔符
        Separator separator = new Separator();

        // 创建底部描述区域
        VBox descriptionSection = new VBox(6.0);

        Label authorLabel = new Label(getLang("dialog.about.text.author"));
        authorLabel.setStyle("-fx-font-size: 13px;");
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(Double.POSITIVE_INFINITY);

        Label description1Label = new Label(getLang("dialog.about.text.description.1"));
        description1Label.setStyle("-fx-font-size: 13px;");
        description1Label.setWrapText(true);
        description1Label.setMaxWidth(Double.POSITIVE_INFINITY);

        Label description2Label = new Label(getLang("dialog.about.text.description.2"));
        description2Label.setStyle("-fx-font-size: 13px;");
        description2Label.setWrapText(true);
        description2Label.setMaxWidth(Double.POSITIVE_INFINITY);

        descriptionSection.getChildren().addAll(
                authorLabel, description1Label, description2Label);

        // 组装主容器
        mainContainer.getChildren().addAll(topSection, separator, descriptionSection);

        // 设置内边距
        mainContainer.setPadding(new Insets(20.0));

        // 创建对话框按钮
        ButtonType closeButton = new ButtonType(getLang("general.button.close"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(closeButton);

        // 设置对话框内容
        getDialogPane().setContent(mainContainer);

        // 应用默认字体
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        logger.debug("About dialog initialized");
    }
}