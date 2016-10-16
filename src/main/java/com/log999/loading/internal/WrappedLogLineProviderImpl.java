package com.log999.loading.internal;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.log999.loading.api.WrappedLogLineProvider;
import com.log999.loading.api.LogLineProvider;

import javax.inject.Inject;

public class WrappedLogLineProviderImpl implements WrappedLogLineProvider {

    private LogLineProvider logLineProvider;
    private int wrap = Integer.MAX_VALUE;

    @Inject
    public WrappedLogLineProviderImpl(LogLineProvider logLineProvider) {
        this.logLineProvider = logLineProvider;
    }

    @Override
    public void setLineWrap(int wrap) {
        this.wrap = wrap;
    }

    @Override
    public String getDisplayLine(int displayLine) {
        return getDisplayLineUsingSlowAlgorithm(displayLine);
    }

    // TODO Optimise
    private String getDisplayLineUsingSlowAlgorithm(int targetDisplayIdx) {
        int logIdx = 0;
        int displayIdx = 0;
        while (true) {
            String realLine = logLineProvider.getLine(logIdx);
            if (realLine == null) return null; // End of file
            int linesOccupiedByRealLogLine = 1 + realLine.length() / wrap;
            if (displayIdx + linesOccupiedByRealLogLine > targetDisplayIdx) return wrappedLine(realLine, displayIdx, targetDisplayIdx);
            displayIdx += linesOccupiedByRealLogLine;
            logIdx++;
        }
    }

    private String wrappedLine(String realLine, int displayIdx, int targetDisplayIdx) {
        int wrappedLineOfThisRealLine = targetDisplayIdx - displayIdx;
        String[] wrappedLines = splitLineByWrapLength(realLine);
        return wrappedLines[wrappedLineOfThisRealLine];
    }

    private String[] splitLineByWrapLength(String line) {
        Iterable<String> result = Splitter.fixedLength(wrap).split(line);
        return Iterables.toArray(result, String.class);
    }

}
