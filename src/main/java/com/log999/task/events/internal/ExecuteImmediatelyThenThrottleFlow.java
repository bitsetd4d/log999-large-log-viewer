package com.log999.task.events.internal;

import com.log999.task.events.EventFlowControl;
import com.log999.task.events.EventFlowUtil;

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
