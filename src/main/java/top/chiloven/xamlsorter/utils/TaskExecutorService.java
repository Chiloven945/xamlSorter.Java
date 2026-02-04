package top.chiloven.xamlsorter.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TaskExecutorService {
    private static final Logger logger = LogManager.getLogger(TaskExecutorService.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("XSW-" + t.threadId());
                return t;
            }
    );

    /**
     * Executes a background task and handles success and error callbacks.
     * This method is designed to run tasks in a background thread
     * and then update the UI on the JavaFX Application Thread
     *
     * @param taskName       the name of the task, used for logging
     * @param backgroundTask the task to be executed in the background
     * @param onSuccess      the callback to be executed on success
     * @param onError        the callback to be executed on error
     * @param <T>            the type of the result produced by the background task
     */
    public static <T> void executeTask(
            String taskName,
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                Thread.currentThread().setName("XST-" + taskName);
                logger.debug("Execute the task: {}", taskName);
                return backgroundTask.get();
            }
        };

        task.setOnSucceeded(event -> {
            T result = task.getValue();
            logger.debug("The task finished successfully: {}", taskName);
            Platform.runLater(() -> onSuccess.accept(result));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            logger.error("Failed to execute the task: {}", taskName, exception);
            Platform.runLater(() -> onError.accept(exception));
        });

        executorService.submit(task);
    }

    /**
     * Submits a task to be executed in the background and returns a CompletableFuture.
     * This method allows you to run a task asynchronously
     * and get a future result that can be used to handle the result or error later.
     *
     * @param taskName       the name of the task, used for logging
     * @param backgroundTask the task to be executed in the background
     * @param <T>            the type of the result produced by the background task
     * @return a CompletableFuture that will be completed with the result of the task
     */
    public static <T> CompletableFuture<T> submitTask(String taskName, Supplier<T> backgroundTask) {
        CompletableFuture<T> future = new CompletableFuture<>();

        executorService.submit(() -> {
            Thread.currentThread().setName("XST-" + taskName);
            logger.debug("Submit the task: {}", taskName);
            try {
                T result = backgroundTask.get();
                logger.debug("The task submitted successfully: {}", taskName);
                future.complete(result);
            } catch (Exception e) {
                logger.error("Failed to submit the task: {}", taskName, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Shuts down the executor service.
     */
    public static void shutdown() {
        executorService.shutdown();
        logger.info("TaskExecutorService has been shut down");
    }
}