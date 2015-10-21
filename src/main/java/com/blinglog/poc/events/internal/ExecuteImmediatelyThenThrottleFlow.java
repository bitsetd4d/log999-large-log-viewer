package com.blinglog.poc.events.internal;

import com.blinglog.poc.events.EventFlowControl;
import com.blinglog.poc.events.EventFlowUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ExecuteImmediatelyThenThrottleFlow implements EventFlowControl {

    final private int throttleMs;
    private boolean throttled = false;
    private Runnable pendingRunnable;

    public ExecuteImmediatelyThenThrottleFlow(int throttleMs) {
        this.throttleMs = throttleMs;
    }

    @Override
    public synchronized void execute(Runnable runnable) {
        if (!throttled) {
            throttled = true;
            EventFlowUtil.sharedScheduledExecutor.execute(runnable);
            EventFlowUtil.sharedScheduledExecutor.schedule(() -> processPending(), throttleMs, TimeUnit.MILLISECONDS);
        } else {
            pendingRunnable = runnable;
        }
    }

    private synchronized void processPending() {
        throttled = false;
        if (pendingRunnable != null) {
            execute(pendingRunnable);
            pendingRunnable = null;
        }
    }
}
