package chiloven.xamlsorter;

import chiloven.xamlsorter.controllers.MainController;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static chiloven.xamlsorter.modules.I18n.getBundle;
import static chiloven.xamlsorter.modules.I18n.getLang;

public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);
    public static Stage primaryStage;
    private MainController mainController;

    public static void main(String[] args) {
        launch(args);
    }

    public static void safeClose() {
        if (primaryStage != null) {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Main.primaryStage = primaryStage;
        try {
            logger.info("Starting xamlSorter.Java application");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/pages/MainPage.fxml"));

            PreferencesManager.reload();
            String lang = PreferencesManager.get("language", "en");
            I18n.setLocale(lang);
            loader.setResources(getBundle());

            Scene scene = new Scene(loader.load());
            I18n.applyDefaultFont(scene);

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
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-16x16.png")),
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-32x32.png")),
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-64x64.png")),
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-128x128.png")),
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-192x192.png")),
                    new Image(getClass().getResourceAsStream("/assets/icons/application/application-256x256.png"))
            );
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("application UI loaded successfully");
        } catch (Exception e) {
            logger.fatal("Error loading UI", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("main.start.exception.alert.header"),
                    getLang("main.start.exception.alert.content"),
                    e
            );
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping xamlSorter.Java application");
        logger.info("Application stopped successfully");
    }

}
