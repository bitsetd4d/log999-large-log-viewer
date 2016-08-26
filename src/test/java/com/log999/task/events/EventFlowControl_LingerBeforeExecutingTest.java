package com.log999.task.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.log999.task.TestUtil.sleepMs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

public class EventFlowControl_LingerBeforeExecutingTest {

    private static final long EXECUTION_INTERVAL = 250;

    private List<Long> executionTimes;
    private EventFlowControl eventFlowControl;
    private long started;
    private Runnable task;

    @Before
    public void setUp() throws Exception {
        executionTimes = new ArrayList<>();
        eventFlowControl = EventFlowUtil.newLingerBeforeExecuting((int) EXECUTION_INTERVAL);
        eventFlowControl.execute(() -> System.out.println("Warmup"));
        sleepMs(300);
        task = () -> executionTimes.add(System.currentTimeMillis());
        started = System.currentTimeMillis();
    }

    @Test
    public void shouldLingerBeforeExecuting() throws Exception {
        // When
        eventFlowControl.execute(task);
        sleepMs(500);
        // Then
        singleTsksExecutedWithCorrectDelay();
    }

    @Test
    public void tasksShouldConflateNotQueue() {
        // When
        eventFlowControl.execute(task); // Will conflate
        eventFlowControl.execute(task); // Will conflate
        eventFlowControl.execute(task); // Will conflate
        eventFlowControl.execute(task); // Will execute
        sleepMs(500);
        // Then
        singleTsksExecutedWithCorrectDelay();
    }

    private void singleTsksExecutedWithCorrectDelay() {
        assertThat(executionTimes, hasSize(1));
        long firstExecuted = executionTimes.get(0);
        assertThat(firstExecuted - started, greaterThan(EXECUTION_INTERVAL));
    }

}
