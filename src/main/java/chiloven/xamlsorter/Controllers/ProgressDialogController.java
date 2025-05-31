package chiloven.xamlsorter.Controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProgressDialogController {

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label messageLabel;
    @FXML
    private Button cancelButton;

    public void bindTask(Task<?> task) {
        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());

        cancelButton.setOnAction(e -> {
            if (task.isRunning()) {
                task.cancel();
                messageLabel.setText("Cancelled by user.");
            }
        });
    }
}
