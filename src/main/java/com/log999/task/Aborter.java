package com.log999.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class Aborter {

    private static Logger logger = LoggerFactory.getLogger(Aborter.class);

    private AtomicLong stamp = new AtomicLong(0);

    public long getStamp() {
        return stamp.get();
    }

    public boolean shouldAbort(long stampValue) {
        return !shouldContinue(stampValue);
    }

    public boolean shouldContinue(long stampValue) {
        return stamp.longValue() == stampValue;
    }

    public void abort() {
        stamp.incrementAndGet();
    }
}
