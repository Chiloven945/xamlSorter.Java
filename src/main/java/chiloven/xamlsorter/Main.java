package chiloven.xamlsorter;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import chiloven.xamlsorter.modules.DataOperationHelper;
import chiloven.xamlsorter.modules.I18n;
import chiloven.xamlsorter.modules.PreferencesManager;
import chiloven.xamlsorter.modules.preferences.Language;
import chiloven.xamlsorter.ui.MainPage;
import chiloven.xamlsorter.utils.RestartHelper;
import chiloven.xamlsorter.utils.ShowAlert;
import chiloven.xamlsorter.utils.TaskExecutorService;
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
    public static final String version = "Beta.0.3.1";

    private static volatile boolean restartRequested = false;

    public static void main(String[] args) {
        launch(args);
    }

    public static void safeClose() {
        if (primaryStage != null) {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    public static void safeRestart() {
        logger.info("Safe restart requested.");
        restartRequested = true;
        safeClose();
    }

    public static void applyTheme() {
        boolean isDark = PreferencesManager.isDarkMode();
        logger.info("Applying {} theme", isDark ? "dark" : "light");
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

            logger.info("Loading application preferences");
            PreferencesManager.reload();

            logger.info("Setting application language");
            Language language = PreferencesManager.getLanguage();
            I18n.setLocale(language.getLocale());
            logger.info("Application language set as: {}", language.getDisplayName());

            logger.info("Setting application theme");
            applyTheme();

            MainPage mainPage = new MainPage();
            Scene scene = new Scene(mainPage);
            I18n.applyDefaultFont(scene);

            DataOperationHelper.setMainPage(mainPage);
            logger.debug("MainPage instance set in DataOperationHelper");

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
                    restartRequested = false;
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
            logger.info("Application icons loaded successfully");

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
        TaskExecutorService.shutdown();

        if (restartRequested) {
            logger.info("Relaunching application...");
            try {
                RestartHelper.relaunchCurrentApp();
                logger.info("Relaunch command issued successfully.");
            } catch (Exception ex) {
                logger.error("Failed to relaunch application.", ex);
            }
        }

        logger.info("Application stopped successfully");
    }

}
