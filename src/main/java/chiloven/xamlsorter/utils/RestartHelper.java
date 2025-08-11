package chiloven.xamlsorter.utils;

import chiloven.xamlsorter.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class RestartHelper {

    private static final Logger logger = LogManager.getLogger(RestartHelper.class);

    private RestartHelper() {
    }

    /**
     * Relaunches the current application.
     * This method first attempts to use the jpackage launcher if available.
     * If the jpackage launcher is not available or not executable,
     * it falls back to relaunching the application using the Java runtime.
     *
     * @throws Exception if an error occurs during the relaunch process
     */
    public static void relaunchCurrentApp() throws Exception {
        // jpackage
        String launcher = System.getProperty("jpackage.app-path");
        if (launcher != null && !launcher.isBlank()) {
            File launcherFile = new File(launcher);
            if (launcherFile.exists() && launcherFile.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(launcherFile.getAbsolutePath());
                pb.directory(launcherFile.getParentFile());
                pb.inheritIO();
                logger.debug("Relaunch via jpackage launcher: {}", launcherFile.getAbsolutePath());
                pb.start();
                return;
            } else {
                logger.warn("jpackage launcher not executable or missing: {}", launcher);
            }
        } else {
            logger.debug("System property 'jpackage.app-path' not present; fallback to Java relaunch.");
        }

        // IDE or java -jar
        List<String> cmd = new ArrayList<>();
        cmd.add(getJavaBin());

        if (isRunningFromJar()) {
            String jarPath = getRunningJarPath();
            cmd.add("-jar");
            cmd.add(jarPath);
        } else {
            String classpath = System.getProperty("java.class.path");
            cmd.add("-cp");
            cmd.add(classpath);
            cmd.add(Main.class.getName());
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.inheritIO();
        logger.debug("Relaunch via Java fallback: {}", String.join(" ", cmd));
        pb.start();
    }

    /**
     * Returns the path to the Java binary executable.
     *
     * @return the absolute path to the Java binary
     */
    private static String getJavaBin() {
        String javaHome = System.getProperty("java.home");
        String exe = isWindows() ? "java.exe" : "java";
        File bin = Paths.get(javaHome, "bin", exe).toFile();
        return bin.getAbsolutePath();
    }

    /**
     * Checks if the application is running from a JAR file.
     *
     * @return true if running from a JAR, false otherwise
     */
    private static boolean isRunningFromJar() {
        try {
            String path = Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            return path != null && path.endsWith(".jar");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Gets the absolute path of the currently running JAR file.
     *
     * @return the absolute path of the running JAR
     * @throws URISyntaxException if the URI syntax is incorrect
     */
    private static String getRunningJarPath() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
    }

    /**
     * Checks if the current operating system is Windows.
     *
     * @return true if the OS is Windows, false otherwise
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }
}
