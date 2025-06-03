package chiloven.xamlsorter.Modules;

public class ClipboardManager {
    /**
     * The clipboard holds a single DataItem that can be copied or pasted.
     * It is initialized with an empty DataItem.
     */
    private static DataItem clipboard = new DataItem("", "", "", "");

    /**
     * Copies the content of the provided DataItem to the clipboard.
     *
     * @param item the DataItem to copy to the clipboard
     */
    public static void copyFrom(DataItem item) {
        if (item != null) {
            clipboard.setCategory(item.getCategory());
            clipboard.setKey(item.getKey());
            clipboard.setOriginalText(item.getOriginalText());
            clipboard.setTranslatedText(item.getTranslatedText());
        }
    }

    /**
     * Retrieves the current content of the clipboard.
     *
     * @return the DataItem currently stored in the clipboard
     */
    public static DataItem getClipboard() {
        return clipboard;
    }

    /**
     * Checks if the clipboard has content.
     *
     * @return true if the clipboard has content, false otherwise
     */
    public static boolean hasContent() {
        return clipboard != null && clipboard.getKey() != null && !clipboard.getKey().isEmpty();
    }

    /**
     * Clears the clipboard content.
     */
    public static void clear() {
        clipboard = new DataItem("", "", "", "");
    }
}
