package chiloven.xamlsorter;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.modules.ShowAlert;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private MainController mainController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting xamlSorter.Java application");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/MainPage.fxml"));
            Scene scene = new Scene(loader.load());

            mainController = loader.getController();

            primaryStage.setOnCloseRequest(event -> {
                logger.info("Close request received");
                boolean canExit = mainController.promptSaveIfNeeded();
                if (!canExit) {
                    logger.info("Exit cancelled by user.");
                    event.consume();
                }
            });

            primaryStage.setTitle("xamlSorter.Java");
            primaryStage.getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/icons/application/application-16x16.png")),
                    new Image(getClass().getResourceAsStream("/icons/application/application-32x32.png")),
                    new Image(getClass().getResourceAsStream("/icons/application/application-64x64.png")),
                    new Image(getClass().getResourceAsStream("/icons/application/application-128x128.png")),
                    new Image(getClass().getResourceAsStream("/icons/application/application-192x192.png")),
                    new Image(getClass().getResourceAsStream("/icons/application/application-256x256.png"))
            );
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("application UI loaded successfully");
        } catch (Exception e) {
            ShowAlert.error("Error", "Failed to load the main application UI", "An error occurred while trying to load the main application UI. Please check the logs for more details.");
            logger.error("Error loading UI", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping xamlSorter.Java application");
        logger.info("application stopped successfully");
    }

}
