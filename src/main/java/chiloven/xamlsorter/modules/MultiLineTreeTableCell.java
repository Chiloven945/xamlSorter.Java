package chiloven.xamlsorter.modules;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableCell;

public class MultiLineTreeTableCell<S> extends TreeTableCell<S, String> {
    private final TextArea textArea = new TextArea();
    private final Label label = new Label();

    /**
     * Constructs a MultiLineTreeTableCell that allows editing of multi-line text.
     * The cell will display a TextArea for editing and a Label for display.
     */
    public MultiLineTreeTableCell() {
        // Enable text area to handle multiple lines
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);
        textArea.setMaxHeight(200);

        // Double click to edit
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !isEmpty()) {
                startEdit();
            }
        });

        // Keyboard event handling: Shift+Enter to commit, ESC to cancel, Enter for new line
        textArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    if (event.isShiftDown()) {
                        commitEdit(textArea.getText());
                        event.consume();
                    }
                }
                case ESCAPE -> {
                    cancelEdit();
                    event.consume();
                }
            }
        });

        // Save changes when focus is lost
        textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                commitEdit(textArea.getText());
            }
        });
    }

    // Override startEdit to initialize the text area with the current item
    @Override
    public void startEdit() {
        super.startEdit();
        textArea.setText(getItem());
        setGraphic(textArea);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textArea.requestFocus();
    }

    // Override cancelEdit to reset the display
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateDisplay(getItem());
    }

    // Override commitEdit to update the display with the new value
    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);

        // Write the new value back to the DataItem if applicable
        if (getTableRow() != null && getTableRow().getItem() != null) {
            S rowItem = getTableRow().getItem();
            if (rowItem instanceof DataItem dataItem) {
                if (getTableColumn().getText().equals("Original Text")) {
                    dataItem.setOriginalText(newValue);
                } else if (getTableColumn().getText().equals("Translated Text")) {
                    dataItem.setTranslatedText(newValue);
                } else if (getTableColumn().getText().equals("Key")) {
                    dataItem.setKey(newValue);
                }
            }
        }

        updateDisplay(newValue);
    }

    // Override updateItem to handle empty items and set the correct display
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        // If the item is empty or null, clear the cell
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                textArea.setText(item);
                setGraphic(textArea);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                updateDisplay(item);
            }
        }
    }

    /**
     * Updates the display of the cell with the given item.
     *
     * @param item the item to display in the cell
     */
    private void updateDisplay(String item) {
        label.setText(item != null ? item : "");
        setGraphic(label);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

}
