package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        try {
            FXMLLoader loader = new FXMLLoader(PreferencesDialogController.class.getResource("/ui/dialogs/PreferencesDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PreferencesDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Preferences");
            if (owner != null) {
                dialog.initOwner(owner);
            }

            controller.languageComboBox.setValue(PreferencesManager.get("language", "English"));
            controller.themeComboBox.setValue(PreferencesManager.get("theme", "Light"));

            dialog.showAndWait().ifPresent(btn -> {
                if (btn.getButtonData() == ButtonData.OK_DONE) {
                    controller.savePreferences();
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load Preferences dialog", e);
            ShowAlert.error(
                    "Error",
                    "Error loading Preferences dialog",
                    "An error occurred while trying to load the Preferences dialog. Please report this as an issue.",
                    e
            );
        }
    }

    /**
     * Save the user preferences from the dialog
     */
    public void savePreferences() {
        PreferencesManager.set("language", languageComboBox.getValue());
        PreferencesManager.set("theme", themeComboBox.getValue());
        PreferencesManager.save();
    }
}
