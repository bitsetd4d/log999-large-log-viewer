package com.log999.display.internal;

import com.log999.display.api.LogFileDisplay;
import com.log999.display.api.LogFilePage;
import com.log999.loading.api.StreamingLogLineProvider;
import com.log999.loading.internal.InMemoryLogLineProvider;
import com.log999.loading.internal.WrappedLogLineProviderImpl;
import com.log999.markup.MarkupMemory;
import com.log999.util.LogFilePosition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.inject.Inject;
import java.io.IOException;

import static java.lang.Math.max;

public class LogFileDisplayImpl implements LogFileDisplay {

    private final MarkupMemory markupMemory;
    private WrappedLogLineProviderImpl logLineProvider;
    private int displayPageTop;
    private int displayPageSize;

    private IntegerProperty maxLineLengthProperty = new SimpleIntegerProperty(0);
    private ObjectProperty<LogFilePage> logFilePageProperty = new SimpleObjectProperty<>();

    @Inject
    public LogFileDisplayImpl(MarkupMemory markupMemory) {
        this.markupMemory = markupMemory;
    }

    @Override
    public void load(StreamingLogLineProvider logLines) throws IOException {
        logLineProvider = new WrappedLogLineProviderImpl(new InMemoryLogLineProvider(logLines));
        calculateMaxLineLength();
    }

    private void calculateMaxLineLength() {
        int i = 0, max = 0;
        while (true) {
            String line = logLineProvider.getDisplayLine(i);
            if (line == null) {
                maxLineLengthProperty.set(max);
                return;
            }
            max = max(line.length(), max);
            i++;
        }
    }

    @Override
    public MarkupMemory getMarkupMemory() {
        return markupMemory;
    }

    @Override
    public void setLineWrapWidth(int width) {
        logLineProvider.setLineWrap(width);
        publishCurrentPage();
    }

    @Override
    public void setRangeOfInterest(int displayPageTop, int displayPageSize) {
        this.displayPageTop = displayPageTop;
        this.displayPageSize = displayPageSize;
        publishCurrentPage();
    }

    private void publishCurrentPage() {
        LogFilePageImplBuilder builder = new LogFilePageImplBuilder();
        String[] rows = new String[displayPageSize];
        for (int i = 0; i < displayPageSize; i++) {
            rows[i] = logLineProvider.getDisplayLine(i + displayPageTop);
        }
        builder
                .setPositionTopLine(new LogFilePosition(displayPageTop, 0))
                .setDisplayRowsToFill(displayPageSize)
                .setHoldingPage(false)
                .setLineWrapLength(9999)
                .setMarkupMemory(markupMemory)
                .setTopDisplayRow(displayPageTop)
                .setRows(rows);

        LogFilePageImpl page = builder.createLogFilePageImpl();
        logFilePageProperty.set(page);
    }

    @Override
    public IntegerProperty maxLineLengthProperty() {
        return maxLineLengthProperty;
    }

    @Override
    public ObjectProperty<LogFilePage> logFilePageProperty() {
        return logFilePageProperty;
    }
}
