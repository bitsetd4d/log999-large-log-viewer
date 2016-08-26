package com.log999.task.events.internal;

import com.log999.task.events.EventFlowUtil;
import com.log999.task.events.ThrottledPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PublishThenThrottlePublisher<T> implements ThrottledPublisher<T> {

    private static Logger logger = LoggerFactory.getLogger(PublishThenThrottlePublisher.class);

    final private int throttleMs;
    private boolean throttled = false;

    private Consumer<T> consumer;
    private T value;

    public PublishThenThrottlePublisher(int throttleMs) {
        this.throttleMs = throttleMs;
    }

    @Override
    public synchronized void publish(T value) {
        if (value.equals(this.value)) return;
        this.value = value;
        if (!throttled) {
            throttled = true;
            EventFlowUtil.sharedScheduledExecutor.execute(() -> publishNow());
            EventFlowUtil.sharedScheduledExecutor.schedule(() -> throttlePeriodEnd(value), throttleMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public T get() {
        return value;
    }

    private synchronized void publishNow() {
        consumer.accept(value);
    }

    private void throttlePeriodEnd(T value) {
        if (value != this.value) {
            publishNow();  /* Pending value that needs publishing */
        }
        synchronized (this) {
            throttled = false;
        }
    }

    @Override
    public void onPublishNow(Consumer<T> consumer) {
        this.consumer = consumer;
    }
}
