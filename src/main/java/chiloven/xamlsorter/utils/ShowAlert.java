package chiloven.xamlsorter.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Utility class for displaying JavaFX alerts with customizable content.
 * This class provides methods to show alerts of different types (information, warning, error, confirmation)
 * and handles long or multiline messages by displaying them in a TextArea.
 */
public class ShowAlert {

    private static final int LONG_MESSAGE_THRESHOLD = 200;  // 可自定义阈值

    /**
     * Show a JavaFX alert.
     *
     * @param alertType the type of alert (e.g., Alert.AlertType.INFORMATION, Alert.AlertType.ERROR)
     * @param header    the header text of the alert dialog
     * @param title     the title of the alert dialog
     * @param content   the content text of the alert dialog
     * @param stacktrace the stack trace to display in the alert (optional, can be null or empty)
     */
    public static void showAlert(Alert.AlertType alertType, String title, String header, String content, Exception stacktrace) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);

        if (stacktrace != null) {
            // Build a detailed message with stack trace
            StringBuilder sb = new StringBuilder();
            sb.append(stacktrace).append("\n");
            for (StackTraceElement element : stacktrace.getStackTrace()) {
                sb.append("    at ").append(element).append("\n");
            }

            VBox vbox = new VBox(10);
            if (isLongMessage(content)) {
                TextArea contentArea = new TextArea(content);
                contentArea.setEditable(false);
                contentArea.setWrapText(true);
                contentArea.setMaxWidth(Double.MAX_VALUE);
                contentArea.setMaxHeight(120);
                vbox.getChildren().add(contentArea);
            } else {
                Label contentLabel = new Label(content);
                contentLabel.setWrapText(true);
                vbox.getChildren().add(contentLabel);
            }

            Label stackLabel = new Label("Detail: ");
            TextArea stackArea = new TextArea(sb.toString());
            stackArea.setEditable(false);
            stackArea.setWrapText(false);
            stackArea.setMaxWidth(Double.MAX_VALUE);
            stackArea.setMaxHeight(180);
            vbox.getChildren().addAll(stackLabel, stackArea);

            vbox.setPrefWidth(540);
            alert.getDialogPane().setContent(vbox);

        } else if (isLongMessage(content)) {
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
     * Determine if a message should be displayed in a TextArea (for long or multiline content).
     *
     * @param message the message string to evaluate
     * @return true if long or multiline, false otherwise
     */
    private static boolean isLongMessage(String message) {
        return message != null &&
                (message.length() > LONG_MESSAGE_THRESHOLD || message.contains("\n"));
    }

    /**
     * Show an information alert with title, header, and content.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void info(String title, String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, header, content, null);
    }

    /**
     * Show an information alert with title and content.
     *
     * @param title   the title of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void info(String title, String content) {
        info(title, null, content);
    }

    /**
     * Show a warning alert with title, header, and content.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void warn(String title, String header, String content) {
        showAlert(Alert.AlertType.WARNING, title, header, content, null);
    }

    /**
     * Show a warning alert with title and content.
     *
     * @param title   the title of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void warn(String title, String content) {
        warn(title, null, content);
    }

    /**
     * Show an error alert with title, header, and content.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void error(String title, String header, String content) {
        showAlert(Alert.AlertType.ERROR, title, header, content, null);
    }

    /**
     * Show an error alert with stacktrace.
     *
     * @param title the title of the alert dialog
     * @param header the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @param stacktrace the stack trace to display in the alert
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void error(String title, String header, String content, Exception stacktrace) {
        showAlert(Alert.AlertType.ERROR, title, header, content, stacktrace);
    }

    /**
     * Show an error alert with title and content.
     *
     * @param title   the title of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void error(String title, String content) {
        error(title, null, content);
    }

    /**
     * Show a confirmation alert with title, header, and content.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void confirm(String title, String header, String content) {
        showAlert(Alert.AlertType.CONFIRMATION, title, header, content, null);
    }

    /**
     * Show a confirmation alert with title and content.
     *
     * @param title   the title of the alert dialog
     * @param content the content text of the alert dialog
     * @see #showAlert(Alert.AlertType, String, String, String, Exception)
     */
    public static void confirm(String title, String content) {
        confirm(title, null, content);
    }

    /**
     * Show a confirmation alert with custom buttons.
     *
     * @param title the title of the alert dialog
     * @param header the header text of the alert dialog
     * @param content the content text of the alert dialog
     * @param customButtons the custom buttons to display in the alert
     * @return an Optional containing the button type that was clicked, or empty if the dialog was closed without a selection
     */
    public static Optional<ButtonType> confirm(String title, String header, String content,
                                               ButtonType... customButtons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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

        // Default buttons
        if (customButtons != null && customButtons.length > 0) {
            alert.getButtonTypes().setAll(customButtons);
        }

        return alert.showAndWait();
    }

}
