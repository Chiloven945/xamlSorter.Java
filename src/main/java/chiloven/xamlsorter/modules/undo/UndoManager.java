package chiloven.xamlsorter.modules.undo;

import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

public class UndoManager {
    private final List<UndoableCommand> history = new ArrayList<>();
    private final ReadOnlyBooleanWrapper canUndo = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper canRedo = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyStringWrapper undoText = new ReadOnlyStringWrapper("Undo");
    private final ReadOnlyStringWrapper redoText = new ReadOnlyStringWrapper("Redo");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private int index = -1;
    private int savePointer = -1;
    private final int limit = 200;

    private static void ensureFx() {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("UndoManager must run on JavaFX Application Thread.");
    }

    public void execute(UndoableCommand cmd) {
        ensureFx();
        if (index < history.size() - 1) history.subList(index + 1, history.size()).clear();
        if (!history.isEmpty() && history.get(history.size() - 1).mergeWith(cmd)) {
            refresh();
            return;
        }
        cmd.execute();
        history.add(cmd);
        index = history.size() - 1;
        trim();
        refresh();
    }

    public void undo() {
        ensureFx();
        if (!canUndo.get()) return;
        history.get(index).undo();
        index--;
        refresh();
    }

    public void redo() {
        ensureFx();
        if (!canRedo.get()) return;
        history.get(index + 1).execute();
        index++;
        refresh();
    }

    public void clear() {
        ensureFx();
        history.clear();
        index = -1;
        savePointer = -1;
        refresh();
    }

    public void markSavePoint() {
        savePointer = index;
        refresh();
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty undoTextProperty() {
        return undoText.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty redoTextProperty() {
        return redoText.getReadOnlyProperty();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    private void refresh() {
        boolean cu = index >= 0;
        boolean cr = index < history.size() - 1;
        canUndo.set(cu);
        canRedo.set(cr);
        undoText.set(cu ? "Undo " + history.get(index).getName() : "Undo");
        redoText.set(cr ? "Redo " + history.get(index + 1).getName() : "Redo");
        dirty.set(index != savePointer);
    }

    private void trim() {
        if (history.size() <= limit) return;
        int remove = history.size() - limit;
        for (int i = 0; i < remove; i++) {
            history.remove(0);
            index--;
            savePointer--;
        }
        if (index < -1) index = -1;
        if (savePointer < -1) savePointer = -1;
    }
}
