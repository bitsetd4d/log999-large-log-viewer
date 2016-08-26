package com.log999.task.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.log999.task.TestUtil.sleepMs;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ThrottledPublisherTest {

    private static final int EXECUTION_INTERVAL = 250;
    private static long TOLERANCE = 10;

    private List<Long> times;
    private ThrottledPublisher<Long> throttledPublisher;
    private long started;

    @Before
    public void setUp() throws Exception {
        throttledPublisher = EventFlowUtil.newPublishThenThrottlePublish(EXECUTION_INTERVAL);
        throttledPublisher.publish(0L);
        sleepMs(300);
        throttledPublisher.onPublishNow(published -> times.add(System.currentTimeMillis()));
        started = System.currentTimeMillis();
        times = new ArrayList<>();
    }

    @Test
    public void shouldPublishImmediatelyThenThrottleSubsequentExecutions() throws Exception {
        // When
        throttledPublisher.publish(1L);
        throttledPublisher.publish(2L);
        sleepMs(300);
        // Then
        assertThat(times, hasSize(2));
        long t1 = times.get(0);
        long t2 = times.get(1);
        assertThat(t1 - started, lessThanOrEqualTo(TOLERANCE));
        assertThat(t2 - t1, greaterThanOrEqualTo((long) EXECUTION_INTERVAL));
        assertThat(t2 - t1, lessThanOrEqualTo(EXECUTION_INTERVAL + TOLERANCE));
    }

    @Test
    public void publishingSameValueShouldNotTriggerCallback() {
        // Given
        throttledPublisher.publish(1L);
        sleepMs(300);
        assertThat(times, hasSize(1));
        // When
        throttledPublisher.publish(1L);
        sleepMs(300);
        // Then
        assertThat(times, hasSize(1));
    }
}


