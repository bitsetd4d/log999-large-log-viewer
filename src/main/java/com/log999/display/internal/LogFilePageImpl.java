package com.log999.display.internal;

import com.blinglog.poc.file.internal.LogFileDisplayRowImpl;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.log999.display.api.LogFileDisplayRow;
import com.log999.display.api.LogFileLine;
import com.log999.display.api.LogFilePage;
import com.log999.markup.MarkupMemory;
import com.log999.util.LogFilePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LogFilePageImpl implements LogFilePage {

    private static Logger logger = LoggerFactory.getLogger(LogFilePageImpl.class);

    private final long topDisplayRow;           // Display row of top of page
    private final int displayRowsToFill;        // Number of display rows
    private final boolean holdingPage;          // true if a holding page
    private final int lineWrapLength;           // The width for line wrap
    private final LogFilePosition positionTopLine;
    private final MarkupMemory markupMemory;

    private List<LogFileLine> logFileLines = new ArrayList<>(100);
    private List<LogFileDisplayRow> displayLogFileLines = new ArrayList<>(100);
    private List<LogFileLine> logLinesForDisplayRows = new ArrayList<>(100);  // Will contain duplicate LogFileLines

    LogFilePageImpl(long topDisplayRow, int displayRowsToFill, String[] rows, boolean holdingPage, int lineWrapLength, LogFilePosition positionTopLine, MarkupMemory markupMemory) {
        this.topDisplayRow = topDisplayRow;
        this.displayRowsToFill = displayRowsToFill;
        this.holdingPage = holdingPage;
        this.lineWrapLength = lineWrapLength;
        this.positionTopLine = positionTopLine;
        this.markupMemory = markupMemory;
        createLogFileLines(rows);
    }

    private void createLogFileLines(String[] rawText) {
        int displayRowCount = 0;
        for (int i = 0; i < rawText.length; i++) {
            long displayLineNumber = 1 + positionTopLine.getRealLogLine() + i;
            int linesAdded = addLogFileLine(displayLineNumber, rawText[i]);
            displayRowCount += linesAdded;
            if (displayRowCount - positionTopLine.getWrappedLineWithinLine() > displayRowsToFill) {
                break;
            }
        }
    }

    private int addLogFileLine(long displayLineNumber, String line) {
        if (line.length() > lineWrapLength) {
            String[] wrapped = wrapLine(line);
            LogFileLineImpl logFileLine = new LogFileLineImpl(displayLineNumber, markupMemory, wrapped);
            logFileLines.add(logFileLine);
            addToDisplay(logFileLine);
            return wrapped.length;
        }
        LogFileLineImpl logFileLine = new LogFileLineImpl(displayLineNumber, markupMemory, line);
        logFileLines.add(logFileLine);
        addToDisplay(logFileLine);
        return 1;
    }

    private void addToDisplay(LogFileLine line) {
        LogFileDisplayRow[] rows = line.getDisplayRows();
        for (LogFileDisplayRow r : rows) {
            displayLogFileLines.add(r);
            logLinesForDisplayRows.add(line);
        }
    }

    @Override
    public boolean isHoldingPage() {
        return holdingPage;
    }

    @Override
    public long getTopDisplayRow() {
        return topDisplayRow;
    }

    @Override
    public int getRowCount() {
        return logFileLines.size();
    }

    @Override
    public int getFirstLineOffset() {
        return positionTopLine.getWrappedLineWithinLine();
    }

    @Override
    public LogFileLine getRow(int index) {
        return logFileLines.get(index);
    }

    @Override
    public int getDisplayRowCount() {
        return displayLogFileLines.size();
    }

    @Override
    public LogFileDisplayRow getDisplayRow(int displayRowIndex) {
        try {
            int idx = displayRowIndex + positionTopLine.getWrappedLineWithinLine();
            if (idx >= displayLogFileLines.size()) {
                return new LogFileDisplayRowImpl(null, 0, "//Overflow", displayRowIndex);
            }
            return displayLogFileLines.get(idx);
        } catch (RuntimeException e) {
            logger.warn("Asked for {} (Pos {}) - {}", displayRowIndex, positionTopLine, e.toString());
            return new LogFileDisplayRowImpl(null, 0, "ERR [" + displayRowIndex + " - " + positionTopLine + "] : " + e.toString(), displayRowIndex);
        }
    }

    @Override
    public LogFileLine getLogFileLineForDisplayRow(int displayRowIndex) {
        return logLinesForDisplayRows.get(displayRowIndex);
    }

    private String[] wrapLine(String line) {
        Iterable<String> result = Splitter.fixedLength(lineWrapLength).split(line);
        return Iterables.toArray(result, String.class);
    }

    @Override
    public String toString() {
        return "LogFilePageImpl{" +
                "topDisplayRow=" + topDisplayRow +
                ", displayRowsToFill=" + displayRowsToFill +
                ", positionTopLine=" + positionTopLine +
                ", holdingPage=" + holdingPage +
                ", lineWrapLength=" + lineWrapLength +
                ", displayLogFileLines.size()=" + displayLogFileLines.size() +
                '}';
    }

    public void dumpToLog() {
        logger.info("\n\n============================\n\n{}\n\n", this);
        final int max = 60;
        for (int i = 0; i < displayLogFileLines.size(); i++) {
            displayLogFileLines.get(i).dumpToLog(i);
            if (i > max) {
                logger.info("... and so on up to {}", displayLogFileLines.size());
                break;
            }
        }
        logger.info("\n\n=================================================\n\n");
    }
}
