package chiloven.xamlsorter.Modules;

import chiloven.xamlsorter.Controllers.ProgressDialogController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

public class ProgressDialog {

    private static final Logger logger = LogManager.getLogger(ProgressDialog.class);
    private static final ShowAlert alertHelper = new ShowAlert();

    public static <T> void show(Task<T> task, String title,
                                Consumer<T> onSuccess,
                                Consumer<Throwable> onFailure,
                                Runnable onCancelled) {
        try {
            FXMLLoader loader = new FXMLLoader(ProgressDialog.class.getResource("/Dialogs/ProgressDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle(title);
            dialog.setDialogPane(dialogPane);

            ProgressDialogController controller = loader.getController();
            controller.bindTask(task);

            task.setOnSucceeded(event -> {
                T result = task.getValue();
                if (onSuccess != null) {
                    Platform.runLater(() -> onSuccess.accept(result));
                }
                Platform.runLater(dialog::close);
            });

            task.setOnFailed(event -> {
                Throwable ex = task.getException();
                if (onFailure != null) {
                    Platform.runLater(() -> onFailure.accept(ex));
                }
                Platform.runLater(dialog::close);
            });

            task.setOnCancelled(event -> {
                if (onCancelled != null) {
                    Platform.runLater(onCancelled);
                }
                Platform.runLater(dialog::close);
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

            dialog.show();  // Use non-blocking show()
        } catch (Exception e) {
            logger.error("Failed to show progress dialog", e);
        }
    }

    private static void showErrorWithStack(String title, Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionText = sw.toString();

        alertHelper.showAlert(Alert.AlertType.ERROR, title, exceptionText, "An error occurred");
    }
}
