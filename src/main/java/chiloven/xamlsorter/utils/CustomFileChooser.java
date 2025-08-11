package chiloven.xamlsorter.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class CustomFileChooser {
    private static final Logger logger = LogManager.getLogger(CustomFileChooser.class);

    /**
     * Open a file selection dialog, allowing the user to choose a file with specified extensions.
     *
     * @param owner      the owner window of the dialog
     * @param title      the title of the dialog
     * @param extensions a list of file extensions to filter the selectable files
     * @return the selected file, or null if no file was selected
     */
    public static File showOpenFileDialog(Window owner, String title, String files, List<String> extensions) {
        logger.info("Opening file dialog with title: {}, files: {}, extensions: {}", title, files, extensions);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Add file type filters based on the provided extensions
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                files, extensions.stream().map(ext -> "*." + ext).toArray(String[]::new)
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Open the file selection dialog
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null) {
            logger.info("File selected: {}", selectedFile.getAbsolutePath());
        } else {
            logger.info("No file selected.");
        }
        return selectedFile;
    }

    /**
     * Open a file save dialog, allowing the user to specify a file to save with specified extensions.
     *
     * @param owner           the owner window of the dialog
     * @param title           the title of the dialog
     * @param extensions      a list of file extensions to filter the selectable files
     * @param defaultFileName the default file name to be suggested in the dialog
     * @return the selected file, or null if no file was selected
     */
    public static File showSaveFileDialog(Window owner, String title, String files, List<String> extensions, String defaultFileName) {
        logger.info("Opening save file dialog with title: {}, files: {}, extensions: {}, defaultFileName: {}", title, files, extensions, defaultFileName);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Add file type filters based on the provided extensions
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                files, extensions.stream().map(ext -> "*." + ext).toArray(String[]::new)
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Set the default file name if provided
        if (defaultFileName != null && !defaultFileName.isEmpty()) {
            fileChooser.setInitialFileName(defaultFileName);
        }

        // Open the file save dialog
        File selectedFile = fileChooser.showSaveDialog(owner);
        if (selectedFile != null) {
            logger.info("File to save selected: {}", selectedFile.getAbsolutePath());
        } else {
            logger.info("No file selected for saving.");
        }
        return selectedFile;
    }

}
