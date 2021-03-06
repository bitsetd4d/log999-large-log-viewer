package com.log999.task.events.internal;

import com.log999.task.events.EventFlowUtil;
import com.log999.task.events.ThrottledPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LingerBeforePublishPublisher<T> implements ThrottledPublisher<T>  {

    private static Logger logger = LoggerFactory.getLogger(LingerBeforePublishPublisher.class);

    final private int throttleMs;
    private boolean inflight = false;

    private Consumer<T> consumer;
    private T value;

    public LingerBeforePublishPublisher(int throttleMs) {
        this.throttleMs = throttleMs;
    }

    @Override
    public synchronized void publish(T value) {
        if (value.equals(this.value)) return;
        this.value = value;
        if (!inflight) {
            inflight = true;
            EventFlowUtil.sharedScheduledExecutor.schedule(this::processPending,throttleMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public T get() {
        return value;
    }

    private synchronized void processPending() {
        inflight = false;
        EventFlowUtil.sharedScheduledExecutor.execute(() -> consumer.accept(value));
    }

    @Override
    public void onPublishNow(Consumer<T> consumer) {
        this.consumer = consumer;
    }

}
