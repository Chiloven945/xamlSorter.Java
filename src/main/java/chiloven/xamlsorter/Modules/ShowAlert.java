package chiloven.xamlsorter.Modules;

import javafx.scene.control.Alert;

public class ShowAlert {
    /**
     * Show a JavaFX alert.
     *
     * @param alertType the type of alert (e.g., Alert.AlertType.INFORMATION, Alert.AlertType.ERROR)
     * @param title     the title of the alert dialog
     * @param content   the content text of the alert dialog
     * @param header    the header text of the alert dialog
     */
    public void showAlert(Alert.AlertType alertType, String title, String content, String header) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show a JavaFX alert with no header text.
     *
     * @param alertType the type of alert (e.g., Alert.AlertType.INFORMATION, Alert.AlertType.ERROR)
     * @param title     the title of the alert dialog
     * @param content   the content text of the alert dialog
     */
    public void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
