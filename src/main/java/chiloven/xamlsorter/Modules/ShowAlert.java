package chiloven.xamlsorter.Modules;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class ShowAlert {

    private static final int LONG_MESSAGE_THRESHOLD = 200;  // 可自定义阈值

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

        if (isLongMessage(content)) {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setContent(textArea);
        } else {
            alert.setContentText(content);
        }

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
        showAlert(alertType, title, content, null);
    }

    /**
     * Determine if a message should be displayed in a TextArea (for long or multiline content).
     *
     * @param message the message string to evaluate
     * @return true if long or multiline, false otherwise
     */
    private boolean isLongMessage(String message) {
        return message != null &&
                (message.length() > LONG_MESSAGE_THRESHOLD || message.contains("\n"));
    }
}
