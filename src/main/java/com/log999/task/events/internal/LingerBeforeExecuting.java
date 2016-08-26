package com.log999.task.events.internal;

import com.log999.task.events.EventFlowControl;
import com.log999.task.events.EventFlowUtil;

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
