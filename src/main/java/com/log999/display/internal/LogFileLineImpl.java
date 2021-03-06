package com.log999.display.internal;

import com.blinglog.poc.file.internal.LogFileDisplayRowImpl;
import com.log999.display.api.LogFileDisplayRow;
import com.log999.display.api.LogFileLine;
import com.log999.markup.LineMarkup;
import com.log999.markup.MarkupMemory;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileLineImpl implements LogFileLine {

    private static Logger logger = LoggerFactory.getLogger(LogFileLineImpl.class);

    private final long lineNumber;
    private final MarkupMemory markupMemory;
    private final LogFileDisplayRow[] lines;

    public LogFileLineImpl(long lineNumber, MarkupMemory markupMemory, String... lines) {
        this.lineNumber = lineNumber;
        this.markupMemory = markupMemory;
        this.lines = new LogFileDisplayRow[lines.length];
        int offset = 0;
        for (int i = 0; i < lines.length; i++) {
            this.lines[i] = new LogFileDisplayRowImpl(this, offset, lines[i], i);
            offset += lines[i].length();
        }
    }

    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getDisplayRowCount() {
        return lines.length;
    }

    @Override
    public LogFileDisplayRow[] getDisplayRows() {
        return lines;
    }

    @Override
    public boolean isMarked() {
        return markupMemory.isMarked(lineNumber);
    }

    @Override
    public void setMarked(boolean marked) {
        markupMemory.setMarked(lineNumber, lineNumber, marked);
    }

    @Override
    public void markBold(int start, int end, boolean bold) {
        markupMemory.setBold(lineNumber, start, end, bold);
    }

    @Override
    public void markBackground(int start, int end, Color bg) {
        markupMemory.setBackground(lineNumber, start, end, bg);
    }

    @Override
    public void markForeground(int start, int end, Color bg) {
        markupMemory.setForeground(lineNumber, start, end, bg);
    }

    @Override
    public LineMarkup getLineMarkup() {
        return markupMemory.getFormatting(lineNumber);
    }

}
