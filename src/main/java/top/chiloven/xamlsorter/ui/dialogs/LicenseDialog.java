package top.chiloven.xamlsorter.ui.dialogs;

import top.chiloven.xamlsorter.modules.I18n;
import top.chiloven.xamlsorter.utils.ShowAlert;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static top.chiloven.xamlsorter.modules.I18n.getLang;
import static top.chiloven.xamlsorter.utils.BrowserUtil.openWebpage;

public class LicenseDialog extends Dialog<Void> {
    private static final Logger logger = LogManager.getLogger(LicenseDialog.class);

    public LicenseDialog(Window owner) {
        try {
            setupDialog(owner);
        } catch (Exception e) {
            logger.error("Failed to create License dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.license.exception.alert.header"),
                    getLang("dialog.license.exception.alert.content"),
                    e
            );
        }
    }

    /**
     * Show the License dialog
     *
     * @param owner the owner window
     */
    public static void show(Window owner) {
        logger.info("Opening License dialog");
        try {
            LicenseDialog dialog = new LicenseDialog(owner);
            dialog.showAndWait();
            logger.debug("License dialog closed");
        } catch (Exception e) {
            logger.error("Failed to show License dialog", e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("dialog.license.exception.alert.header"),
                    getLang("dialog.license.exception.alert.content"),
                    e
            );
        }
    }

    /**
     * Check if a string is null or blank
     *
     * @param s the string to check
     * @return true if null or blank, false otherwise
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static List<LicenseEntry> createEntries() {
        String javafxVersion = System.getProperty("javafx.version", "");
        String ikonliVersion = System.getProperty("ikonli.version", "");

        List<LicenseEntry> list = new ArrayList<>();

        // JavaFX (GPLv2 + Classpath Exception)
        list.add(new LicenseEntry("OpenJFX: javafx-base", javafxVersion,
                "GPL v2 with Classpath Exception",
                "https://openjdk.org/legal/gplv2+ce.html",
                "https://openjfx.io/"));
        list.add(new LicenseEntry("OpenJFX: javafx-controls", javafxVersion,
                "GPL v2 with Classpath Exception",
                "https://openjdk.org/legal/gplv2+ce.html",
                "https://openjfx.io/"));

        // Log4j (Apache-2.0)
        list.add(new LicenseEntry("Apache Log4j Core", "2.25.0",
                "Apache License 2.0",
                "https://github.com/apache/logging-log4j2/blob/2.x/LICENSE.txt",
                "https://logging.apache.org/log4j/2.x/"));
        list.add(new LicenseEntry("Log4j SLF4J 2.x Binding", "2.25.1",
                "Apache License 2.0",
                "https://github.com/apache/logging-log4j2/blob/2.x/LICENSE.txt",
                "https://logging.apache.org/log4j/2.x/"));

        // AtlantaFX (Apache-2.0)
        list.add(new LicenseEntry("AtlantaFX", "2.0.1",
                "MIT License",
                "https://github.com/mkpaz/atlantafx/blob/master/LICENSE",
                "https://github.com/mkpaz/atlantafx"));

        // Ikonli (Apache-2.0)
        list.add(new LicenseEntry("Ikonli Core", ikonliVersion,
                "Apache License 2.0",
                "https://github.com/kordamp/ikonli/blob/master/LICENSE",
                "https://github.com/kordamp/ikonli"));
        list.add(new LicenseEntry("Ikonli JavaFX", ikonliVersion,
                "Apache License 2.0",
                "https://github.com/kordamp/ikonli/blob/master/LICENSE",
                "https://github.com/kordamp/ikonli"));
        list.add(new LicenseEntry("Ikonli Material Design Pack", ikonliVersion,
                "Apache License 2.0",
                "https://github.com/kordamp/ikonli/blob/master/LICENSE",
                "https://github.com/kordamp/ikonli"));

        // jSystemThemeDetector (MIT)
        list.add(new LicenseEntry("jSystemThemeDetector", "3.9.1",
                "Apache License 2.0",
                "https://github.com/Dansoftowner/jSystemThemeDetector/blob/master/LICENSE",
                "https://github.com/Dansoftowner/jSystemThemeDetector"));

        // Gson (Apache-2.0)
        list.add(new LicenseEntry("Google Gson", "2.10.1",
                "Apache License 2.0",
                "https://github.com/google/gson/blob/main/LICENSE",
                "https://github.com/google/gson"));

        return list;
    }

    private void setupDialog(Window owner) {
        setTitle(getLang("dialog.license.title"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // Top: Application name, copyright, main license
        VBox header = new VBox(6.0);
        Label nameLabel = new Label(getLang("general.application.name"));
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label copyright =
                new Label(getLang("general.license.copyright"));
        Label licensed =
                new Label(getLang("general.license.licensed_under"));

        Button btnAppLicense = new Button(getLang("dialog.license.button.open_license"));
        btnAppLicense.setOnAction(e -> openWebpage("https://github.com/Chiloven945/xamlSorter.Java/blob/master/LICENCE"));

        HBox licenseLine = new HBox(8.0, licensed, btnAppLicense);
        licenseLine.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        header.getChildren().addAll(nameLabel, copyright, licenseLine);

        // Middle: Dependencies entries
        VBox entriesBox = new VBox(8.0);
        entriesBox.setPadding(new Insets(8, 0, 0, 0));

        for (LicenseEntry en : createEntries()) {
            VBox item = new VBox(6.0);

            Label lib = new Label(en.name + (isBlank(en.version) ? "" : ("  " + en.version)));
            lib.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            Label lic = new Label("Licensed under the " + en.licenseName);

            // Sub-entry buttons: Homepage / License
            Button btnHomepage = new Button(getLang("dialog.license.entry.button.open_source"));
            btnHomepage.setDisable(isBlank(en.homepage));
            btnHomepage.setOnAction(ev -> openWebpage(en.homepage));

            Button btnLicense = new Button(getLang("dialog.license.button.open_license"));
            btnLicense.setDisable(isBlank(en.licenseUrl));
            btnLicense.setOnAction(ev -> openWebpage(en.licenseUrl));

            Region spacerRow = new Region();
            HBox.setHgrow(spacerRow, Priority.ALWAYS);
            HBox actions = new HBox(8.0, btnHomepage, btnLicense, spacerRow);

            item.getChildren().addAll(lib, lic, actions);
            item.setPadding(new Insets(8));
            item.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

            entriesBox.getChildren().add(item);
        }

        ScrollPane scroll = new ScrollPane(entriesBox);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(380);

        // Bottom: use DialogPane's built-in button bar
        ButtonType closeBtnType = new ButtonType(getLang("general.button.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().setAll(closeBtnType);

        // Main container
        VBox root = new VBox(12.0,
                header,
                new Separator(),
                scroll
        );
        root.setPadding(new Insets(20.0, 20.0, 8.0, 20.0));

        getDialogPane().setContent(root);

        // Apply default font
        Scene scene = getDialogPane().getScene();
        I18n.applyDefaultFont(scene);

        logger.debug("License dialog initialized.");
    }

    /**
     * License entry data structure
     *
     * @param name library name
     * @param version library version
     * @param licenseName license name
     * @param licenseUrl license URL
     * @param homepage library homepage URL
     */
    private record LicenseEntry(String name, String version, String licenseName, String licenseUrl, String homepage) {
    }
}
