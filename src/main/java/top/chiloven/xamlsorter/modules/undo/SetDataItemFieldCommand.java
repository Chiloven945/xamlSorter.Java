package top.chiloven.xamlsorter.modules.undo;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SetDataItemFieldCommand implements UndoableCommand {
    private final String name;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private final String before;
    private final String after;

    public SetDataItemFieldCommand(String name, Supplier<String> getter, Consumer<String> setter, String newValue) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.before = getter.get();
        this.after = newValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute() {
        if (!Objects.equals(getter.get(), after)) setter.accept(after);
    }

    @Override
    public void undo() {
        if (!Objects.equals(getter.get(), before)) setter.accept(before);
    }
}
