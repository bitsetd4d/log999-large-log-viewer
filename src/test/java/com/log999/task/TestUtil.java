package com.log999.task;

public class TestUtil {

    public static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
