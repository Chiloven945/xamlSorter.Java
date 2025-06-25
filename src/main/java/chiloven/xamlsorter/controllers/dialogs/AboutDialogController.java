package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static chiloven.xamlsorter.Main.version;
import static chiloven.xamlsorter.modules.I18n.getBundle;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class AboutDialogController {
    private static final Logger logger = LogManager.getLogger(AboutDialogController.class);

    @FXML
    public Label appVersionLabel;

    @FXML
    private ImageView appIconView;

    /**
     * Show the 'About' dialog
     *
     * @param owner the owner window of the dialog, can be null
     */
    public static void showAboutDialog(Window owner) {
        logger.debug("Opening About dialog");
        try {
            FXMLLoader loader = new FXMLLoader(AboutDialogController.class.getResource("/ui/dialogs/AboutDialog.fxml"));
            loader.setResources(getBundle());
            DialogPane dialogPane = loader.load();

            dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);

            Scene scene = dialog.getDialogPane().getScene();
            I18n.applyDefaultFont(scene);

            dialog.setTitle(getLang("dialog.about.title"));

            if (owner != null) {
                logger.debug("Setting owner and modality for About dialog");
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }

            dialog.showAndWait();
            logger.debug("About dialog closed");
        } catch (Exception e) {
            logger.error("Failed to load About dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.about.exception.alert.header"),
                    getLang("dialog.about.exception.alert.content"),
                    e
            );
        }
    }

    @FXML
    public void initialize() {
        logger.debug("Initializing AboutDialogController");

        // Load the application icon
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/application/application-about.png")));
        appIconView.setImage(iconImage);

        // Set the application version label
        appVersionLabel.setText(getLang("dialog.about.text.version") + version);

        logger.debug("Application icon loaded and set");
    }

}
