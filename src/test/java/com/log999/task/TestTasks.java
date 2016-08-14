package com.log999.task;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestTasks {

    private TaskRunner taskRunner;
    private MyTaskFeedback taskFeedback;

    @BeforeClass
    public static void setUpJavaFx() throws Exception {
        JavaFXTestUtil.setupJavaFX();
    }

    @Before
    public void setUp() throws Exception {
        taskRunner = new TaskRunner();
        taskFeedback = new MyTaskFeedback();
        taskRunner.setTaskFeedback(taskFeedback);
    }

    @Test
    public void testSimpleExecution() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        taskRunner.execute("Testing", latch::countDown);
        boolean ok = latch.await(1, SECONDS);
        assertThat(ok, is(true));
        assertThat(taskFeedback.isVisible(), is(false));
        assertThat(taskFeedback.texts, hasItem("Testing"));
        assertThat(taskFeedback.getText(), is(""));
    }

    @Test
    public void testLongRunningTask() throws Exception {
        CountDownLatch startOfTask = new CountDownLatch(1);
        CountDownLatch endOfTask = new CountDownLatch(1);
        LogFileTask testTask = () -> {
            System.out.println("Task> Started");
            startOfTask.countDown();
            System.out.println("Task> Waiting");
            endOfTask.await(10, SECONDS);
            System.out.println("Task> Finished");
        };

        Supplier<Boolean> taskIsRunning = () -> startOfTask.getCount() == 0 && endOfTask.getCount() > 0;
        Supplier<Boolean> hasTerminated = () -> startOfTask.getCount() == 0 && endOfTask.getCount() == 0;

        assertThat(taskIsRunning.get(), is(false));
        assertThat(hasTerminated.get(), is(false));
        assertThat(taskFeedback.isVisible(), is(false));
        assertThat(taskFeedback.getText(), is(""));
        // Run task
        taskRunner.execute("Test123", testTask);
        startOfTask.await(1, SECONDS);
        // Task should be running
        assertThat(taskIsRunning.get(), is(true));
        assertThat(hasTerminated.get(), is(false));
        JavaFXTestUtil.waitForJavaFxToCatchup();
        assertThat(taskFeedback.isVisible(), is(true));
        assertThat(taskFeedback.getText(), is("Test123"));
        // Single task to complete
        endOfTask.countDown();
        taskRunner.shutdownAndWait();
        // Task should have ended
        assertThat(taskIsRunning.get(), is(false));
        assertThat(hasTerminated.get(), is(true));
        JavaFXTestUtil.waitForJavaFxToCatchup();
        assertThat(taskFeedback.isVisible(), is(false));
        assertThat(taskFeedback.getText(), is(""));
    }

    private static class MyTaskFeedback implements TaskRunner.TaskFeedback {

        List<Boolean> visibles = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        @Override
        public void setVisible(boolean visible) {
            System.out.println("Feedback> Setting visible to "+visible);
            visibles.add(visible);
        }

        @Override
        public void setText(String text) {
            System.out.println("Feedback> Setting text to "+text);
            texts.add(text);
        }

        boolean isVisible() {
            if (visibles.isEmpty()) return false;
            return getLast(visibles);
        }

        String getText() {
            if (texts.isEmpty()) return "";
            return getLast(texts);
        }

        private <T> T getLast(List<T> list) {
            return list.get(list.size() - 1);
        }
    }

}
