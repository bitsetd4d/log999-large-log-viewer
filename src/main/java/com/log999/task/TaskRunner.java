package com.log999.task;

import com.google.common.annotations.VisibleForTesting;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TaskRunner {

    private static Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private AtomicInteger inProgress = new AtomicInteger();

    private static TaskRunner INSTANCE = new TaskRunner();

    public static TaskRunner getInstance() {
        return INSTANCE;
    }

    private ExecutorService executor = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, ExecutorService> conflatingExecutors = new ConcurrentHashMap<>();

    private ConcurrentMap<LogFileTask, String> tasks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, LogFileTask> conflatableTasks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, AbortableTask> abortableTasks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Aborter> aborters = new ConcurrentHashMap<>();

    private TaskFeedback taskFeedback = new TaskFeedback() {
    };

    @VisibleForTesting
    AtomicInteger conflatedTaskCount = new AtomicInteger(0);

    public interface TaskFeedback {
        default void setVisible(boolean visible) {
        }

        default void setText(String text) {
        }
    }

    public void setTaskFeedback(TaskFeedback taskFeedback) {
        this.taskFeedback = taskFeedback;
        makeIdle();
    }

    private void makeIdle() {
        taskFeedback.setVisible(false);
        taskFeedback.setText("");
    }

    public void execute(String name, LogFileTask task) {
        executor.execute(() -> {
            long t = 0;
            try {
                long t1 = System.currentTimeMillis();
                startTask(name, task);
                task.run();
                t = System.currentTimeMillis() - t1;
            } catch (Exception e) {
                logger.error("Error executing task", e);
            } finally {
                endTask(task, t);
            }
        });
    }

    void shutdownAndWait() throws InterruptedException {
        executor.shutdown();
        conflatingExecutors.forEach((key, exec) -> exec.shutdown());
        executor.awaitTermination(1, SECONDS);
        conflatingExecutors.forEach((key, exec) -> {
            try {
                exec.awaitTermination(1, SECONDS);
            } catch (Exception e) {
            }
        });
    }

    public void executeConflating(String name, String conflationKey, LogFileTask task) {
        LogFileTask existingTask = conflatableTasks.put(conflationKey, task);
        if (existingTask == null) conflatedTaskCount.incrementAndGet();
        getConflatingExecutor(conflationKey).execute(() -> runConflatableTask(name, conflationKey));
    }

    private Executor getConflatingExecutor(String conflationKey) {
        return conflatingExecutors.computeIfAbsent(conflationKey, key -> Executors.newSingleThreadExecutor());
    }

    private void runConflatableTask(String name, String conflationKey) {
        LogFileTask task = null;
        long t = 0;
        try {
            long t1 = System.currentTimeMillis();
            task = conflatableTasks.remove(conflationKey);
            if (task != null) {
                startTask(name, task);
                task.run();
            }
            t = System.currentTimeMillis() - t1;
        } catch (Exception e) {
            logger.error("Error executing task", e);
        } finally {
            if (task != null) {
                endTask(task, t);
                conflatedTaskCount.decrementAndGet();
            }
        }
    }

    public void executeAbortably(String name, String conflationKey, AbortableTask task) {
        AbortableTask existingTask = abortableTasks.put(conflationKey, task);
        if (existingTask == null) conflatedTaskCount.incrementAndGet();
        Aborter a = aborters.computeIfAbsent(conflationKey, k -> new Aborter());
        a.abort(); // Abort current task
        getConflatingExecutor(conflationKey).execute(() -> runAbortableTask(name, conflationKey, a));
    }

    private void runAbortableTask(String name, String conflationKey, Aborter aborter) {
        AbortableTask task = null;
        try {
            task = abortableTasks.remove(conflationKey);
            if (task != null) {
                task.run(aborter);
            }
        } catch (Exception e) {
            logger.error("Error executing task", e);
        } finally {
            if (task != null) {
                conflatedTaskCount.decrementAndGet();
            }
        }
    }

    private void startTask(String name, LogFileTask task) {
        logger.debug("Start task {}", name);
        inProgress.incrementAndGet();
        tasks.put(task, name);
        Platform.runLater(() -> {
            taskFeedback.setText(name);
            taskFeedback.setVisible(true);
        });
    }

    private void endTask(LogFileTask task, long t) {
        int count = inProgress.decrementAndGet();
        String name = tasks.remove(task);
        String anotherRunningTaskName = null;
        try {
            anotherRunningTaskName = tasks.values().iterator().next();
        } catch (NoSuchElementException e) {
        }
        if (anotherRunningTaskName == null) {
            Platform.runLater(() -> {
                taskFeedback.setText("");
                taskFeedback.setVisible(false);
            });
        } else {
            String a = anotherRunningTaskName;
            Platform.runLater(() -> taskFeedback.setText(a));
        }
        logger.debug("Task {} ended - took {}ms", name, t);
    }

}
