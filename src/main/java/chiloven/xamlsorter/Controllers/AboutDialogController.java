package chiloven.xamlsorter.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AboutDialogController {

    @FXML
    private ImageView appIconView;

    @FXML
    public void initialize() {
        // 加载图标
        Image iconImage = new Image(getClass().getResourceAsStream("/Icons/application-about.png"));
        appIconView.setImage(iconImage);
    }
}
