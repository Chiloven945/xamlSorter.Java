package chiloven.xamlsorter;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.modules.preferences.Language;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.utils.ShowAlert;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);
    public static Stage primaryStage;
    public static String version = "Beta.0.2.1";

    public static void main(String[] args) {
        launch(args);
    }

    public static void safeClose() {
        if (primaryStage != null) {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    public static void applyTheme() {
        boolean isDark = PreferencesManager.isDarkMode();
        Application.setUserAgentStylesheet(
            isDark ? new CupertinoDark().getUserAgentStylesheet() 
                  : new CupertinoLight().getUserAgentStylesheet()
        );
    }

    @Override
    public void start(Stage primaryStage) {
        Main.primaryStage = primaryStage;
        try {
            logger.info("Starting xamlSorter.Java application");

            PreferencesManager.reload();
            
            Language language = PreferencesManager.getLanguage();
            I18n.setLocale(language.getLocale());
            logger.info("应用程序语言设置为: {}", language.getDisplayName());

            applyTheme();

            MainPage mainPage = new MainPage();
            Scene scene = new Scene(mainPage);
            I18n.applyDefaultFont(scene);

            primaryStage.setTitle("xamlSorter.Java");

            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.centerOnScreen();
            
            PreferencesManager.addThemeChangeListener(Main::applyTheme);

            primaryStage.setOnCloseRequest(event -> {
                logger.info("Close request received");
                boolean canExit = mainPage.promptSaveIfNeeded();
                if (!canExit) {
                    logger.info("Exit cancelled by user.");
                    event.consume();
                } else {
                    PreferencesManager.removeThemeChangeListener(Main::applyTheme);
                }
            });

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

            logger.info("Application UI loaded successfully");
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
