package com.blinglog.poc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static Logger logger = LoggerFactory.getLogger(IdGenerator.class);

    private final static AtomicLong count = new AtomicLong(10 * System.currentTimeMillis());

    public static String getNextId() {
        long c = count.incrementAndGet();
        return Long.toString(c, 36);
    }

}
