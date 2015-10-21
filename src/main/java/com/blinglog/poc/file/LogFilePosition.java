package com.blinglog.poc.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFilePosition {

    private static Logger logger = LoggerFactory.getLogger(LogFilePosition.class);

    private final long realLogLine;
    private final int wrappedLineWithinLine;

    public LogFilePosition(long realLogLine, int wrappedLineWithinLine) {
        this.realLogLine = realLogLine;
        this.wrappedLineWithinLine = wrappedLineWithinLine;
    }

    public long getRealLogLine() {
        return realLogLine;
    }

    public int getWrappedLineWithinLine() {
        return wrappedLineWithinLine;
    }

    @Override
    public String toString() {
        return "LogFilePosition{" +
                "realLogLine=" + realLogLine +
                ", wrappedLineWithinLine=" + wrappedLineWithinLine +
                '}';
    }
}
