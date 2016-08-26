package com.log999.task.events;

import com.log999.task.events.internal.ExecuteImmediatelyThenThrottleFlow;
import com.log999.task.events.internal.LingerBeforeExecuting;
import com.log999.task.events.internal.LingerBeforePublishPublisher;
import com.log999.task.events.internal.PublishThenThrottlePublisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utilities for publishing events and executing code
 */
public class EventFlowUtil {

    public static ScheduledExecutorService sharedScheduledExecutor = Executors.newScheduledThreadPool(4);

    public static EventFlowControl newExecuteImmediatelyThenThrottle(int throttleMs) {
        return new ExecuteImmediatelyThenThrottleFlow(throttleMs);
    }

    public static EventFlowControl newLingerBeforeExecuting(int lingerMs) {
        return new LingerBeforeExecuting(lingerMs);
    }

    public static <T> ThrottledPublisher<T> newLingeringBeforePublish(int delayBeforePublish) {
        return new LingerBeforePublishPublisher<>(delayBeforePublish);
    }

    public static <T> ThrottledPublisher<T> newPublishThenThrottlePublish(int delayBeforeNextPublish) {
        return new PublishThenThrottlePublisher<>(delayBeforeNextPublish);
    }


}
