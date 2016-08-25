package com.log999.task;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.log999.task.TestUtil.sleepMs;
import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class TestAbortableTasks {

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
    public void runningANewAbortableTaskShouldAbortTaskWithSameKey() throws Exception {
        SimpleAbortableTask taskThatWillAbort = new SimpleAbortableTask(a -> sleepMs(50));
        SimpleAbortableTask taskThatCausesAbort = new SimpleAbortableTask(a -> {});

        // Given
        taskRunner.executeAbortably("task", "key1", taskThatWillAbort);
        sleepMs(100);
        assertThat(taskThatWillAbort.started.get(), is(true));

        // When
        taskRunner.executeAbortably("task2", "key1", taskThatCausesAbort);
        sleepMs(100);

        // Then
        assertThat(taskThatWillAbort.aborted.get(), is(true));
        assertThat(taskThatCausesAbort.started.get(), is(true));
    }

    @Test
    public void shouldConflate() throws Exception {
        Set<String> executed = new HashSet<>();
        AbortableTask task1 = a -> sleepMs(50);
        AbortableTask task2 = a -> executed.add("2");
        AbortableTask task3 = a -> executed.add("3");
        AbortableTask task4 = a -> executed.add("4");

        // Given
        taskRunner.executeAbortably("task1", "key1", task1);
        sleepMs(5);

        // when
        taskRunner.executeAbortably("task2", "key1", task2);
        taskRunner.executeAbortably("task3", "key1", task3);
        taskRunner.executeAbortably("task4", "key1", task4);

        // then
        sleepMs(60);
        assertThat(executed, hasSize(1));
        assertThat(executed, contains("4"));
    }

    @Test
    public void tasksWithSameKeyShouldNotExecuteTogether() throws Exception {
        AtomicInteger running = new AtomicInteger(0);
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);

        int taskCount = 100;
        int sleepPerTask = 5;
        int sleepBetweenSchedulingTask = 3;
        int extraWaitToBeSure = 500;

        IntStream.range(1, taskCount).forEach(index -> {
            AbortableTask task = aborter -> {
                started.incrementAndGet();
                System.out.println(String.format("Task %d executed", index));
                assertThat("Must be 1 otherwise >1 task is executing", running.incrementAndGet(), CoreMatchers.is(1));
                sleep(sleepPerTask);
                assertThat("Must be 0 otherwise >1 task is executing", running.decrementAndGet(), CoreMatchers.is(0));
                finished.incrementAndGet();
            };
            taskRunner.executeAbortably("Increment Counter", "key1", task);
            try { sleep(sleepBetweenSchedulingTask); } catch (InterruptedException e) {}
        });

        int worstCaseTimeToExecute = taskCount * (sleepPerTask - sleepBetweenSchedulingTask) + extraWaitToBeSure;

        System.out.printf("Waiting %dms for tasks to execute%n", worstCaseTimeToExecute);
        sleep(worstCaseTimeToExecute);

        assertThat("Some tasks should have ran", started.get(), greaterThan(0));
        assertThat("There should be no tasks running", running.get(), CoreMatchers.is(0));
        assertThat("The count should show no tasks are running", taskRunner.conflatedTaskCount.get(), CoreMatchers.is(0));
        assertThat("All started tasks should have finished", started.get(), CoreMatchers.is(finished.get()));
        System.out.printf("Number of tasks that actually ran was %d%n", started.get());
    }

    private static class SimpleAbortableTask implements AbortableTask {

        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean aborted = new AtomicBoolean(false);
        AbortableTask task;

        SimpleAbortableTask(AbortableTask task) {
            this.task = task;
        }

        @Override
        public void run(Aborter aborter) throws Exception {
            started.set(true);
            long stamp = aborter.getStamp();
            for (int i = 0; i < 20; i++) {
                if (aborter.shouldContinue(stamp)) {
                    perform(aborter);
                } else {
                    aborted.set(true);
                    break;
                }
            }
        }

        public void perform(Aborter aborter) throws Exception {
            task.run(aborter);
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
