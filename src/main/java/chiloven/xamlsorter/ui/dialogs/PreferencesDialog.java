package chiloven.xamlsorter.ui.dialogs;

import chiloven.xamlsorter.Main;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.modules.preferences.Language;
import chiloven.xamlsorter.modules.preferences.ThemeMode;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class PreferencesDialog extends Dialog<ButtonType> {
    private static final Logger logger = LogManager.getLogger(PreferencesDialog.class);

    private final ComboBox<String> languageComboBox;
    private final ComboBox<String> themeComboBox;

    public PreferencesDialog(Window owner) {
        this.languageComboBox = new ComboBox<>();
        this.themeComboBox = new ComboBox<>();

        try {
            setupDialog(owner);
            initializeValues();
        } catch (Exception e) {
            logger.error("Failed to create Preferences dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.pref.exception.alert.header"),
                    getLang("dialog.pref.exception.alert.content"),
                    e
            );
        }
    }

    public static void show(Window owner) {
        logger.debug("Opening Preferences dialog");
        try {
            PreferencesDialog dialog = new PreferencesDialog(owner);
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    dialog.savePreferences();
                }
            });
            logger.debug("Preferences dialog closed");
        } catch (Exception e) {
            logger.error("Failed to show Preferences dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.pref.exception.alert.header"),
                    getLang("dialog.pref.exception.alert.content"),
                    e
            );
        }
    }

    private void setupDialog(Window owner) {
        // 基本对话框设置
        setTitle(getLang("dialog.pref.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // 创建主容器
        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefHeight(115.0);
        content.setPrefWidth(268.0);

        // 创建标题标签
        Label titleLabel = new Label(getLang("dialog.pref.title"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 创建语言选择区域
        HBox languageBox = new HBox(10);
        Label languageLabel = new Label(getLang("dialog.pref.language.label"));
        languageComboBox.setPrefHeight(23.0);
        languageComboBox.setPrefWidth(200.0);  // 增加宽度以适应长文本
        languageComboBox.getItems().addAll(
            Arrays.stream(Language.values())
                .map(Language::getDisplayName)
                .toList()
        );
        languageBox.getChildren().addAll(languageLabel, languageComboBox);

        // 创建主题选择区域
        HBox themeBox = new HBox(10);
        Label themeLabel = new Label(getLang("dialog.pref.theme.label"));
        themeComboBox.setPrefHeight(23.0);
        themeComboBox.setPrefWidth(100.0);
        themeComboBox.getItems().addAll(
                getLang("dialog.pref.theme.system"),
                getLang("dialog.pref.theme.light"),
                getLang("dialog.pref.theme.dark")
        );
        themeBox.getChildren().addAll(themeLabel, themeComboBox);

        // 组装内容
        content.getChildren().addAll(titleLabel, languageBox, themeBox);
        content.setPadding(new Insets(10));

        // 创建对话框按钮
        ButtonType okButton = new ButtonType(getLang("general.button.ok"),
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // 设置对话框内容
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(400);

        // 应用默认字体
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);
    }

    private void initializeValues() {
        // 设置当前语言值
        Language currentLang = PreferencesManager.getLanguage();
        languageComboBox.setValue(currentLang.getDisplayName());
        
        // 设置当前主题值
        ThemeMode currentTheme = PreferencesManager.getThemeMode();
        switch (currentTheme) {
            case SYSTEM -> themeComboBox.setValue(getLang("dialog.pref.theme.system"));
            case LIGHT -> themeComboBox.setValue(getLang("dialog.pref.theme.light"));
            case DARK -> themeComboBox.setValue(getLang("dialog.pref.theme.dark"));
        }
    }

    private void savePreferences() {
        logger.debug("Saving preferences...");
        
        // 处理语言变更
        Language oldLang = PreferencesManager.getLanguage();
        Language newLang = Language.fromDisplayName(languageComboBox.getValue());
        
        // 处理主题变更
        ThemeMode oldTheme = PreferencesManager.getThemeMode();
        ThemeMode newTheme = getSelectedThemeMode();

        boolean langChanged = oldLang != newLang;
        boolean themeChanged = oldTheme != newTheme;

        logger.debug("Old language: {}, New language: {}", oldLang, newLang);
        logger.debug("Old theme: {}, New theme: {}", oldTheme, newTheme);

        // 保存设置
        PreferencesManager.setLanguage(newLang);
        PreferencesManager.setThemeMode(newTheme);

        if (langChanged) {
            handleLanguageChange(oldLang, newLang);
        }

        if (themeChanged) {
            handleThemeChange(oldTheme, newTheme);
        }
    }

    private void handleLanguageChange(Language oldLang, Language newLang) {
        logger.info("Language preference changed from '{}' to '{}'", oldLang, newLang);
        Optional<ButtonType> result = ShowAlert.confirm(
            getLang("general.alert.info"),
            getLang("dialog.pref.lang.info.header"),
            getLang("dialog.pref.lang.info.content"),
            new ButtonType(getLang("dialog.pref.lang.info.button.restart"),
                ButtonBar.ButtonData.OK_DONE),
            new ButtonType(getLang("general.button.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            logger.info("User chose to restart the application after language change.");
            Main.safeClose();
        } else {
            logger.info("User cancelled the restart after language change.");
        }
    }

    private void handleThemeChange(ThemeMode oldTheme, ThemeMode newTheme) {
        logger.info("Theme preference changed from '{}' to '{}'", oldTheme, newTheme);
        Main.applyTheme();
    }

    private ThemeMode getSelectedThemeMode() {
        String selected = themeComboBox.getValue();
        if (selected.equals(getLang("dialog.pref.theme.system"))) {
            return ThemeMode.SYSTEM;
        } else if (selected.equals(getLang("dialog.pref.theme.light"))) {
            return ThemeMode.LIGHT;
        } else {
            return ThemeMode.DARK;
        }
    }
}