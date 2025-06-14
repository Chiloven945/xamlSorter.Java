package chiloven.xamlsorter.controllers;

import chiloven.xamlsorter.modules.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class AboutDialogController {
    private static final Logger logger = LogManager.getLogger(AboutDialogController.class);

    @FXML
    private ImageView appIconView;

    /**
     * Show the 'About' dialog
     *
     * @param owner the owner window of the dialog, can be null
     */
    public static void showAboutDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(AboutDialogController.class.getResource("/dialogs/AboutDialog.fxml"));
            DialogPane dialogPane = loader.load();

            dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("About xamlSorter.Java");

            if (owner != null) {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }

            dialog.showAndWait();
        } catch (Exception e) {
            ShowAlert.error("Error", "Error loading About dialog", "An error occurred while trying to load the About dialog. Please try again later.");
            logger.error("Failed to load About dialog", e);
        }
    }

    @FXML
    public void initialize() {
        // Load the application icon
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/application/application-about.png")));
        appIconView.setImage(iconImage);
    }

}
