package chiloven.xamlsorter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;

import static chiloven.xamlsorter.modules.I18n.getLang;

public class BrowserUtil {
    private static final Logger logger = LogManager.getLogger(BrowserUtil.class);

    /**
     * Open a webpage in the default web browser.
     *
     * @param url the URL of the webpage to open
     */
    public static void openWebpage(String url) {
        try {
            logger.info("Attempting to open webpage: {}", url);
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
                logger.info("Webpage opened successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to open webpage: {}", url, e);
            ShowAlert.error(
                    getLang("general.alert.error"),
                    getLang("util.browser.open.exception.alert.header"),
                    getLang("util.browser.open.exception.alert.content",
                            url,
                            e.getMessage()
                    )
            );
        }
    }

}
