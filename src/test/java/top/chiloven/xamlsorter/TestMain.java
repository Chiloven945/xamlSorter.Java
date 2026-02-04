package top.chiloven.xamlsorter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TestMain extends Application {
    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println(Font.getFontNames());
        if (Font.getFontNames().contains("Segoe UI")) System.out.println("a");
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            System.out.println("菜单退出");
            Platform.exit();
        });
        menu.getItems().add(exit);
        menuBar.getMenus().add(menu);

        Scene scene = new Scene(new VBox(menuBar, new Label("测试主窗口")), 400, 200);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("窗口被关闭");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "拦截关闭?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                event.consume();
            }
        });

        primaryStage.show();
    }
}
