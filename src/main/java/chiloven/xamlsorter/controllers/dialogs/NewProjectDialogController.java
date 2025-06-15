package chiloven.xamlsorter.controllers.dialogs;

import chiloven.xamlsorter.entities.ProjectMeta;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewProjectDialogController {
    @FXML
    private TextField projectNameField;
    @FXML
    private TextField projectAuthorField;
    @FXML
    private TextArea projectDescField;

    /**
     * Gets the project name input field.
     *
     * @return TextField for the project name.
     */
    public TextField getProjectNameField() {
        return projectNameField;
    }

    /**
     * Gets the project metadata from the input fields.
     *
     * @return ProjectMeta object containing the project name, author, and description.
     */
    public ProjectMeta getProjectMeta() {
        return new ProjectMeta(
                projectNameField.getText().trim(),
                projectAuthorField.getText().trim(),
                projectDescField.getText().trim()
        );
    }

}

