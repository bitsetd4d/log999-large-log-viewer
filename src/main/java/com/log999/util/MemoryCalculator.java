package com.log999.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryCalculator {

    private static Logger logger = LoggerFactory.getLogger(MemoryCalculator.class);

    public static int bytesFor(String x) {
        return  36 + x.length() * 2;
    }
}
