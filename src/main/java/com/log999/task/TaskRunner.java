package com.log999.task;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService conflatingExecutor = Executors.newSingleThreadExecutor();  // If made >1 thread then need to prevent a task with same key executing > 1 once simultaneously

    private ConcurrentMap<LogFileTask, String> tasks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, LogFileTask> conflatableTasks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Aborter> aborters = new ConcurrentHashMap<>();

    private TaskFeedback taskFeedback = new TaskFeedback() {
    };

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
        conflatingExecutor.shutdown();
        executor.awaitTermination(1, SECONDS);
        conflatingExecutor.awaitTermination(1, SECONDS);
    }

    public void executeConflating(String name, String conflationKey, LogFileTask task) {
        conflatableTasks.put(conflationKey, task);
        conflatingExecutor.execute(() -> {
            long t = 0;
            try {
                long t1 = System.currentTimeMillis();
                LogFileTask ct = conflatableTasks.remove(conflationKey);
                if (ct != null) {
                    startTask(name, ct);
                    ct.run();
                }
                t = System.currentTimeMillis() - t1;
            } catch (Exception e) {
                logger.error("Error executing task", e);
            } finally {
                endTask(task, t);
            }
        });
    }

    public void executeAbortably(String name, String conflationKey, AbortableTask task) {
        Aborter a = aborters.computeIfAbsent(conflationKey, k -> new Aborter());
        a.abort();
        executor.execute(() -> {
            try {
                task.run(a);
            } catch (Exception e) {
                logger.error("Error executing task", e);
            }
        });
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
