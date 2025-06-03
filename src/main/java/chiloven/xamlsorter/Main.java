package chiloven.xamlsorter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting XAML Sorter Application");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Pages/MainPage.fxml"));
            Scene scene = new Scene(loader.load());

            primaryStage.setTitle("XAML Sorter");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Icons/application.png")));
            primaryStage.setScene(scene);
            primaryStage.show();
            logger.info("Application UI loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading UI", e);
        }
    }
}
