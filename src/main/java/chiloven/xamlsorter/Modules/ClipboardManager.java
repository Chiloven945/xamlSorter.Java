package chiloven.xamlsorter.Modules;

public class ClipboardManager {
    private static DataItem clipboard = new DataItem("", "", "", "");

    public static void copyFrom(DataItem item) {
        if (item != null) {
            clipboard.setCategory(item.getCategory());
            clipboard.setKey(item.getKey());
            clipboard.setOriginalText(item.getOriginalText());
            clipboard.setTranslatedText(item.getTranslatedText());
        }
    }

    public static DataItem getClipboard() {
        return clipboard;
    }

    public static boolean hasContent() {
        return clipboard != null && clipboard.getKey() != null && !clipboard.getKey().isEmpty();
    }

    public static void clear() {
        clipboard = new DataItem("", "", "", "");
    }
}
