package chiloven.xamlsorter.modules.undo;

public interface UndoableCommand {
    String getName();

    void execute();

    void undo();

    default boolean mergeWith(UndoableCommand next) {
        return false;
    }
}
