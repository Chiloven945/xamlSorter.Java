package chiloven.xamlsorter.Modules;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;

public class MultiLineTreeTableCell<S> extends TreeTableCell<S, String> {
    private final TextArea textArea = new TextArea();
    private final Label label = new Label();

    public MultiLineTreeTableCell() {
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);

        label.setWrapText(true);

        // 双击进入编辑
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                startEdit();
            }
        });

        // 键盘控制：Shift+Enter 保存，ESC 取消，Enter 换行
        textArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    if (event.isShiftDown()) {
                        commitEdit(textArea.getText());
                        event.consume();
                    } else {
                        event.consume();  // <== 重要，防止重复
                    }
                }
                case ESCAPE -> {
                    cancelEdit();
                    event.consume();
                }
            }
        });

        // 单击其他地方，失去焦点时保存
        textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                commitEdit(textArea.getText());
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        textArea.setText(getItem());
        setGraphic(textArea);
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textArea.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateDisplay(getItem());
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        updateDisplay(newValue);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                textArea.setText(item);
                setGraphic(textArea);
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                updateDisplay(item);
            }
        }
    }

    private void updateDisplay(String item) {
        label.setText(item);
        setGraphic(label);
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
}
