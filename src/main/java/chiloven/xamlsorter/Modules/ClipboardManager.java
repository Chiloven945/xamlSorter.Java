package chiloven.xamlsorter.Modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClipboardManager {
    /**
     * Clipboard to hold DataItem objects.
     * This is a static list that acts as a clipboard for copying and pasting DataItem objects.
     */
    private static List<DataItem> clipboard = new ArrayList<>();

    /**
     * Copies a list of DataItem objects to the clipboard.
     *
     * @param items the list of DataItem objects to copy
     */
    public static void copyFrom(List<DataItem> items) {
        clipboard.clear();
        for (DataItem item : items) {
            clipboard.add(new DataItem(
                    item.getCategory(), item.getKey(), item.getOriginalText(), item.getTranslatedText()
            ));
        }
    }

    /**
     * Copies a single DataItem to the clipboard.
     *
     * @return the copied DataItem
     */
    public static List<DataItem> getClipboard() {
        // Return a copy of the clipboard to avoid external modifications
        return clipboard.stream()
                .map(item -> new DataItem(
                        item.getCategory(), item.getKey(), item.getOriginalText(), item.getTranslatedText()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the clipboard has any content.
     *
     * @return true if the clipboard is not empty, false otherwise
     */
    public static boolean hasContent() {
        return !clipboard.isEmpty();
    }

    /**
     * Clears the clipboard.
     */
    public static void clear() {
        clipboard.clear();
    }
}

