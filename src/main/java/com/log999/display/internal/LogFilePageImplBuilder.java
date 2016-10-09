package com.log999.display.internal;

import com.log999.markup.MarkupMemory;
import com.log999.util.LogFilePosition;

public class LogFilePageImplBuilder {
    private long topDisplayRow;
    private int displayRowsToFill;
    private String[] rows;
    private boolean holdingPage;
    private int lineWrapLength;
    private LogFilePosition positionTopLine;
    private MarkupMemory markupMemory;

    public LogFilePageImplBuilder setTopDisplayRow(long topDisplayRow) {
        this.topDisplayRow = topDisplayRow;
        return this;
    }

    public LogFilePageImplBuilder setDisplayRowsToFill(int displayRowsToFill) {
        this.displayRowsToFill = displayRowsToFill;
        return this;
    }

    public LogFilePageImplBuilder setRows(String[] rows) {
        this.rows = rows;
        return this;
    }

    public LogFilePageImplBuilder setHoldingPage(boolean holdingPage) {
        this.holdingPage = holdingPage;
        return this;
    }

    public LogFilePageImplBuilder setLineWrapLength(int lineWrapLength) {
        this.lineWrapLength = lineWrapLength;
        return this;
    }

    public LogFilePageImplBuilder setPositionTopLine(LogFilePosition positionTopLine) {
        this.positionTopLine = positionTopLine;
        return this;
    }

    public LogFilePageImplBuilder setMarkupMemory(MarkupMemory markupMemory) {
        this.markupMemory = markupMemory;
        return this;
    }

    public LogFilePageImpl createLogFilePageImpl() {
        return new LogFilePageImpl(topDisplayRow, displayRowsToFill, rows, holdingPage, lineWrapLength, positionTopLine, markupMemory);
    }
}