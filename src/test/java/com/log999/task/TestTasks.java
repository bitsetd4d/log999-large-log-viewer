package com.log999.task;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.log999.task.JavaFXTestUtil.waitForJavaFxToCatchup;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;

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
        ControllableTestTask testTask = new ControllableTestTask();

        assertThat(testTask.isTaskRunningNow(), is(false));
        assertThat(testTask.hasTaskRunAndTerminated(), is(false));
        assertThat(taskFeedback.isVisible(), is(false));
        assertThat(taskFeedback.getText(), is(""));

        // Run task
        taskRunner.execute("Test123", testTask);
        testTask.waitForTaskToStart();

        // Task should be running
        assertThat(testTask.isTaskRunningNow(), is(true));
        assertThat(testTask.hasTaskRunAndTerminated(), is(false));
        waitForJavaFxToCatchup();
        assertThat(taskFeedback.isVisible(), is(true));
        assertThat(taskFeedback.getText(), is("Test123"));

        // Single task to complete
        testTask.allowTaskToComplete();
        taskRunner.shutdownAndWait();

        // Task should have ended
        assertThat(testTask.isTaskRunningNow(), is(false));
        assertThat(testTask.hasTaskRunAndTerminated(), is(true));
        waitForJavaFxToCatchup();
        assertThat(taskFeedback.isVisible(), is(false));
        assertThat(taskFeedback.getText(), is(""));
    }

    @Test
    public void testUnrelatedConflatingTasksDoNotInterfereWithEachOther() throws InterruptedException {
        ControllableTestTask task1 = new ControllableTestTask();
        ControllableTestTask task2 = new ControllableTestTask();
        taskRunner.executeConflating("task1", "key1", task1);
        taskRunner.executeConflating("task2", "key2", task2);
        task1.waitForTaskToStart();
        task2.waitForTaskToStart();

        // Both tasks running together
        assertThat("Task 1 is running", task1.isTaskRunningNow(), is(true));
        assertThat("Task 2 is running", task2.isTaskRunningNow(), is(true));

        task1.allowTaskToComplete();
        assertThat("Task 1 is not running", task1.isTaskRunningNow(), is(false));
        assertThat("Task 2 is still running", task2.isTaskRunningNow(), is(true));

        task2.allowTaskToComplete();
        assertThat("Task 1 is not running", task1.isTaskRunningNow(), is(false));
        assertThat("Task 2 is not running", task2.isTaskRunningNow(), is(false));
    }

    @Test
    public void testTasksWithSameConflationKeyNeverRunAtTheSameTime() throws Exception {
        AtomicInteger running = new AtomicInteger(0);
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);

        int taskCount = 100;
        int sleepPerTask = 5;
        int sleepBetweenSchedulingTask = 3;
        int extraWaitToBeSure = 500;

        IntStream.range(1, taskCount).forEach(index -> {
            LogFileTask task = () -> {
                started.incrementAndGet();
                System.out.println(String.format("Task %d executed", index));
                assertThat("Must be 1 otherwise >1 task is executing", running.incrementAndGet(), is(1));
                sleep(sleepPerTask);
                assertThat("Must be 0 otherwise >1 task is executing", running.decrementAndGet(), is(0));
                finished.incrementAndGet();
            };
            taskRunner.executeConflating("Increment Counter", "key1", task);
            try { sleep(sleepBetweenSchedulingTask); } catch (InterruptedException e) {}
        });

        int worstCaseTimeToExecute = taskCount * (sleepPerTask - sleepBetweenSchedulingTask) + extraWaitToBeSure;

        System.out.printf("Waiting %dms for tasks to execute%n", worstCaseTimeToExecute);
        sleep(worstCaseTimeToExecute);

        assertThat("Some tasks should have ran", started.get(), greaterThan(0));
        assertThat("There should be no tasks running", running.get(), is(0));
        assertThat("All started tasks should have finished", started.get(), is(finished.get()));
        System.out.printf("Number of tasks that actually ran was %d%n", started.get());
    }

    @Test
    public void testTasksConflateSuchThatATaskWillExecuteAfterTaskRunningWithSameKey() throws InterruptedException {
        ControllableTestTask firstTask = new ControllableTestTask();
        taskRunner.executeConflating("Initial Task", "key1", firstTask);
        firstTask.waitForTaskToStart();
        assertThat("Task 1 is running now", firstTask.isTaskRunningNow(), is(true));

        ControllableTestTask secondTask = new ControllableTestTask();
        taskRunner.executeConflating("Second Task", "key1", secondTask);
        assertThat("Task 2 is not running yet", secondTask.isTaskRunningNow(), is(false));

        firstTask.allowTaskToComplete();
        secondTask.waitForTaskToStart();
        assertThat("Task 2 should be running now", secondTask.isTaskRunningNow(), is(true));

        secondTask.allowTaskToComplete();

        taskRunner.shutdownAndWait();

        assertThat("Task 2 should have finished now", secondTask.hasTaskRunAndTerminated(), is(true));

    }

    @Test
    public void testTasksConflateSoThatUnExecutedTasksDontRun() throws InterruptedException {
        ControllableTestTask firstTask = new ControllableTestTask();
        taskRunner.executeConflating("Initial Task", "key1", firstTask);
        firstTask.waitForTaskToStart();
        assertThat("Task 1 is running now", firstTask.isTaskRunningNow(), is(true));

        int taskCount = 20;
        AtomicInteger count = new AtomicInteger(0);
        List<ControllableTestTask> addedTasks = IntStream.range(0, taskCount).mapToObj(index -> {
            ControllableTestTask task = new ControllableTestTask(() -> {
                count.incrementAndGet();
                System.out.println(String.format("Task %d executed", index));
            });
            taskRunner.executeConflating("Increment Counter", "key1", task);
            return task;
        }).collect(Collectors.toList());

        assertThat("Should be right number of tasks", addedTasks.size(), is(taskCount));
        assertThat("None of the 'increment' tasks have started yet", addedTasks.stream().noneMatch(ControllableTestTask::isTaskRunningNow), is(true));
        addedTasks.forEach(ControllableTestTask::allowTaskToComplete);

        firstTask.allowTaskToComplete();
        sleep(500);

        assertThat("Only one of the increment counter tasks was executed", count.get(), is(1));
    }

    @Test
    public void testTasksConflateButDontInterfereWithOtherTasksIfTheyThrowAnException() throws Exception {
        ControllableTestTask firstTask = new ControllableTestTask();
        taskRunner.executeConflating("Initial Task", "key1", firstTask);
        firstTask.waitForTaskToStart();

        assertThat("Task 1 is running now", firstTask.isTaskRunningNow(), is(true));

        AtomicInteger count = new AtomicInteger(0);
        IntStream.range(0, 20).forEach(index -> {
            ControllableTestTask task = new ControllableTestTask(() -> {
                count.incrementAndGet();
                System.out.println(String.format("Task %d executed", index));
                throw new RuntimeException("Task went wrong");
            });
            task.allowTaskToComplete();
            taskRunner.executeConflating("Increment Counter", "key1", task);
        });

        assertThat("None of the 'increment' tasks have started yet", count.get(), is(0));

        firstTask.allowTaskToComplete();
        sleep(500);

        assertThat("Only one of the increment counter tasks was executed", count.get(), is(1));
    }

    @Test
    public void testTasksThrowingExceptionsDontInterfereWithOtherTasks() {
        fail("This needs adding");
    }

    private static class ControllableTestTask implements LogFileTask {
        private CountDownLatch taskHasStarted = new CountDownLatch(1);
        private CountDownLatch taskWaitToComplete = new CountDownLatch(1);
        private LogFileTask task;

        public ControllableTestTask() {
        }

        public ControllableTestTask(LogFileTask task) {
            this.task = task;
        }

        @Override
        public void run() throws Exception {
            System.out.println("Task> Started");
            taskHasStarted.countDown();
            if (task != null) {
                System.out.println("Task> Executing...");
                task.run();
                System.out.println("Task> Executed");
            }
            System.out.println("Task> Waiting");
            taskWaitToComplete.await(10, SECONDS);
            System.out.println("Task> Finished");
        }

        boolean isTaskRunningNow() {
            return taskHasStarted.getCount() == 0 && taskWaitToComplete.getCount() == 1;
        }

        boolean hasTaskRunAndTerminated() {
            return taskHasStarted.getCount() == 0 && taskWaitToComplete.getCount() == 0;
        }

        void waitForTaskToStart() throws InterruptedException {
            taskHasStarted.await(1, SECONDS);
        }

        void allowTaskToComplete() {
            taskWaitToComplete.countDown();
        }
    }


    private static class MyTaskFeedback implements TaskRunner.TaskFeedback {

        List<Boolean> visibles = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        @Override
        public void setVisible(boolean visible) {
            System.out.println("Feedback> Setting visible to " + visible);
            visibles.add(visible);
        }

        @Override
        public void setText(String text) {
            System.out.println("Feedback> Setting text to " + text);
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
