package chiloven.xamlsorter.entities;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiLineTreeTableCell<S> extends TreeTableCell<S, String> {
    private static final Logger logger = LogManager.getLogger(MultiLineTreeTableCell.class);

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
        logger.debug("TextArea initialized with wrapText, prefRowCount=3, maxHeight=200");

        // Double click to edit
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !isEmpty()) {
                logger.info("Cell double-clicked, starting edit");
                startEdit();
            }
        });

        // Keyboard event handling: Shift+Enter to commit, ESC to cancel, Enter for new line
        textArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    if (event.isShiftDown()) {
                        logger.info("Shift+Enter pressed, committing edit");
                        commitEdit(textArea.getText());
                        event.consume();
                    }
                }
                case ESCAPE -> {
                    logger.info("Escape pressed, cancelling edit");
                    cancelEdit();
                    event.consume();
                }
            }
        });

        // Save changes when focus is lost
        textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                logger.info("TextArea lost focus, committing edit");
                commitEdit(textArea.getText());
            }
        });
    }

    // Override startEdit to initialize the text area with the current item
    @Override
    public void startEdit() {
        logger.debug("startEdit called for cell at row: {}", getIndex());
        super.startEdit();
        textArea.setText(getItem());
        setGraphic(textArea);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textArea.requestFocus();
    }

    // Override cancelEdit to reset the display
    @Override
    public void cancelEdit() {
        logger.debug("cancelEdit called for cell at row: {}", getIndex());
        super.cancelEdit();
        updateDisplay(getItem());
    }

    // Override commitEdit to update the display with the new value
    @Override
    public void commitEdit(String newValue) {
        logger.debug("commitEdit called for cell at row: {}, newValue: {}", getIndex(), newValue);
        super.commitEdit(newValue);

        // Write the new value back to the DataItem if applicable
        if (getTableRow() != null && getTableRow().getItem() != null) {
            S rowItem = getTableRow().getItem();
            if (rowItem instanceof DataItem dataItem) {
                if (getTableColumn().getText().equals("Original Text")) {
                    logger.info("Updating Original Text for row {}: {}", getIndex(), newValue);
                    dataItem.setOriginalText(newValue);
                } else if (getTableColumn().getText().equals("Translated Text")) {
                    logger.info("Updating Translated Text for row {}: {}", getIndex(), newValue);
                    dataItem.setTranslatedText(newValue);
                } else if (getTableColumn().getText().equals("Key")) {
                    logger.info("Updating Key for row {}: {}", getIndex(), newValue);
                    dataItem.setKey(newValue);
                }
            }
        }

        updateDisplay(newValue);
        logger.debug("commitEdit finished for cell at row: {}", getIndex());
    }

    // Override updateItem to handle empty items and set the correct display
    @Override
    protected void updateItem(String item, boolean empty) {
        logger.debug("updateItem called for cell at row: {}, empty: {}, item: {}", getIndex(), empty, item);
        super.updateItem(item, empty);

        // If the item is empty or null, clear the cell
        if (empty || item == null) {
            logger.trace("Clearing cell at row: {}", getIndex());
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                logger.trace("Cell at row: {} is editing, setting textArea", getIndex());
                textArea.setText(item);
                setGraphic(textArea);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                logger.trace("Cell at row: {} is displaying label", getIndex());
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
        logger.trace("Updating display for cell at row: {}, item: {}", getIndex(), item);
        label.setText(item != null ? item : "");
        setGraphic(label);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

}
