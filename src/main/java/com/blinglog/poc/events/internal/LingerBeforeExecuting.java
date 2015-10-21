package com.blinglog.poc.events.internal;

import com.blinglog.poc.events.EventFlowControl;
import com.blinglog.poc.events.EventFlowUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class LingerBeforeExecuting implements EventFlowControl {

    final private int throttleMs;
    private boolean inflight = false;
    private Runnable pendingRunnable;

    public LingerBeforeExecuting(int throttleMs) {
        this.throttleMs = throttleMs;
    }

    @Override
    public synchronized void execute(Runnable runnable) {
        pendingRunnable = runnable;
        if (!inflight) {
            inflight = true;
            EventFlowUtil.sharedScheduledExecutor.schedule(this::processPending, throttleMs, TimeUnit.MILLISECONDS);
        } else {
            pendingRunnable = runnable;
        }

    }

    private synchronized void processPending() {
        inflight = false;
        EventFlowUtil.sharedScheduledExecutor.execute(pendingRunnable);
        pendingRunnable = null;
    }

}
