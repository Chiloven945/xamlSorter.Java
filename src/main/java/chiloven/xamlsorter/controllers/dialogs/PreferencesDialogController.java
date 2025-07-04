package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.Main;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static chiloven.xamlsorter.modules.I18n.getBundle;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class PreferencesDialogController {
    private static final Logger logger = LogManager.getLogger(PreferencesDialogController.class);

    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ComboBox<String> themeComboBox;

    /**
     * Display the Preferences dialog
     *
     * @param owner the owner window, can be null if no owner
     */
    public static void showPreferencesDialog(javafx.stage.Window owner) {
        logger.debug("Opening Preferences dialog");
        try {
            FXMLLoader loader = new FXMLLoader(PreferencesDialogController.class.getResource("/ui/dialogs/PreferencesDialog.fxml"));
            loader.setResources(getBundle());
            DialogPane dialogPane = loader.load();
            PreferencesDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);

            Scene scene = dialog.getDialogPane().getScene();
            I18n.applyDefaultFont(scene);

            dialog.setTitle(getLang("dialog.pref.title"));
            if (owner != null) {
                dialog.initOwner(owner);
            }

            controller.languageComboBox.setValue(PreferencesManager.get("language", "English"));
            controller.themeComboBox.setValue(PreferencesManager.get("theme", "Light"));

            logger.debug("Showing Preferences dialog to user");
            dialog.showAndWait().ifPresent(btn -> {
                logger.debug("Dialog closed with button: {}", btn.getButtonData());
                if (btn.getButtonData() == ButtonData.OK_DONE) {
                    logger.debug("User confirmed preferences, saving...");
                    controller.savePreferences();
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load Preferences dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    "dialog.pref.exception.alert.header",
                    "dialog.pref.exception.alert.content",
                    e
            );
        }
    }

    @FXML
    public void initialize() {
        // Initialize ComboBoxes with available options
        themeComboBox.getItems().addAll(
                getLang("dialog.pref.theme.light"),
                getLang("dialog.pref.theme.dark")
        );
    }

    /**
     * Save the user preferences from the dialog
     */
    public void savePreferences() {
        logger.debug("Saving preferences...");
        String oldLang = PreferencesManager.get("language", "English");
        String newLang = languageComboBox.getValue();
        String oldTheme = PreferencesManager.get("theme", "Light");
        String newTheme = themeComboBox.getValue();

        boolean langChanged = !oldLang.equals(newLang);
        boolean themeChanged = !oldTheme.equals(newTheme);

        logger.debug("Old language: {}, New language: {}", oldLang, newLang);
        logger.debug("Old theme: {}, New theme: {}", oldTheme, newTheme);

        PreferencesManager.set("language", newLang);
        PreferencesManager.set("theme", newTheme);
        PreferencesManager.save();

        if (langChanged) {
            logger.info("Language preference changed from '{}' to '{}'", oldLang, newLang);
            Optional<ButtonType> result = ShowAlert.confirm(
                    getLang("general.alert.info"),
                    getLang("dialog.pref.lang.info.header"),
                    getLang("dialog.pref.lang.info.content"),
                    new ButtonType(getLang("dialog.pref.lang.info.button.restart"), ButtonBar.ButtonData.OK_DONE),
                    new ButtonType(getLang("general.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                logger.info("User chose to restart the application after language change.");
                Main.safeClose();
            } else {
                logger.info("User cancelled the restart after language change.");
            }
        }

        if (themeChanged) {
            logger.info("Theme preference changed from '{}' to '{}'", oldTheme, newTheme);
        }
    }

}
