package com.log999.task.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.log999.task.TestUtil.sleepMs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EventFlowControl_ImmediateThenThrottleTest {

    private static final long EXECUTION_INTERVAL = 250;
    private static long TOLERANCE = 10;

    private List<Long> executionTimes;
    private EventFlowControl eventFlowControl;
    private long started;
    private Runnable task;

    @Before
    public void setUp() throws Exception {
        executionTimes = new ArrayList<>();
        eventFlowControl = EventFlowUtil.newExecuteImmediatelyThenThrottle((int) EXECUTION_INTERVAL);
        eventFlowControl.execute(() -> System.out.println("Warmup"));
        sleepMs(300);
        task = () -> executionTimes.add(System.currentTimeMillis());
        started = System.currentTimeMillis();
    }

    @Test
    public void shouldExecuteImmediatelyThenThrottleSubsequentExecutions() throws Exception {
        // When
        eventFlowControl.execute(task); // Executes now
        eventFlowControl.execute(task); // Executes after interval time
        sleepMs(500);
        // Then
        twoTasksExecuteWithCorrectGap();
    }

    @Test
    public void tasksShouldConflateNotQueue() {
        // When
        eventFlowControl.execute(task); // Executes now
        eventFlowControl.execute(task); // Will conflate
        eventFlowControl.execute(task); // Will conflate
        eventFlowControl.execute(task); // Execute after interval time
        sleepMs(500);
        // Then
        twoTasksExecuteWithCorrectGap();
    }

    private void twoTasksExecuteWithCorrectGap() {
        assertThat(executionTimes, hasSize(2));
        long firstExecuted = executionTimes.get(0);
        long secondExecuted = executionTimes.get(1);
        assertThat(firstExecuted - started, lessThan(TOLERANCE));
        long executionGap = secondExecuted - firstExecuted;
        assertThat(executionGap, greaterThanOrEqualTo(EXECUTION_INTERVAL));
        assertThat(executionGap - EXECUTION_INTERVAL, lessThanOrEqualTo(TOLERANCE));
    }
}
