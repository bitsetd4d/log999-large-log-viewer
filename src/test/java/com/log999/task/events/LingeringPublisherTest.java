package com.log999.task.events;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.log999.task.TestUtil.sleepMs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LingeringPublisherTest {

    private static final int EXECUTION_INTERVAL = 250;

    private List<Long> times;
    private ThrottledPublisher<Long> throttledPublisher;
    private long started;
    private long lastPublishReceived;

    @Before
    public void setUp() throws Exception {
        throttledPublisher = EventFlowUtil.newLingeringBeforePublish(EXECUTION_INTERVAL);
        throttledPublisher.publish(0L);
        sleepMs(300);
        throttledPublisher.onPublishNow(published -> {
            times.add(System.currentTimeMillis());
            lastPublishReceived = published;
        });
        started = System.currentTimeMillis();
        times = new ArrayList<>();
    }

    @Test
    public void shouldDelayPublish() throws Exception {
        // When
        throttledPublisher.publish(1L);
        throttledPublisher.publish(2L);
        sleepMs(300);
        // Then
        assertThat(times, hasSize(1));
        assertThat(lastPublishReceived, is(2L));
        assertThat(times.get(0) - started, greaterThanOrEqualTo((long) EXECUTION_INTERVAL));
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
