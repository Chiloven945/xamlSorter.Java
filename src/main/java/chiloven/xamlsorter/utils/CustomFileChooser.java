package chiloven.xamlsorter.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

public class CustomFileChooser {

    /**
     * Open a file selection dialog, allowing the user to choose a file with specified extensions.
     *
     * @param owner      the owner window of the dialog
     * @param title      the title of the dialog
     * @param extensions a list of file extensions to filter the selectable files
     * @return the selected file, or null if no file was selected
     */
    public static File showOpenFileDialog(Window owner, String title, List<String> extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Add file type filters based on the provided extensions
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Files", extensions.stream().map(ext -> "*." + ext).toArray(String[]::new)
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Open the file selection dialog
        return fileChooser.showOpenDialog(owner);
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
    public static File showSaveFileDialog(Window owner, String title, List<String> extensions, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Add file type filters based on the provided extensions
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Files", extensions.stream().map(ext -> "*." + ext).toArray(String[]::new)
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Set the default file name if provided
        if (defaultFileName != null && !defaultFileName.isEmpty()) {
            fileChooser.setInitialFileName(defaultFileName);
        }

        // Open the file save dialog
        return fileChooser.showSaveDialog(owner);
    }

}
