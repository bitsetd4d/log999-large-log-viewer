package com.blinglog.poc.file.internal;

import com.log999.display.internal.LogFilePageImpl;
import com.log999.display.internal.LogFilePageImplBuilder;
import com.log999.task.events.EventFlowControl;
import com.log999.task.events.EventFlowUtil;
import com.blinglog.poc.file.LogFileAccess;
import com.log999.display.api.LogFilePage;
import com.log999.util.LogFilePosition;
import com.log999.markup.MarkupMemory;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class FakeFileAccessImpl implements LogFileAccess {

    private LongProperty numberOfLinesProperty = new SimpleLongProperty(999999);
    private BooleanProperty fullyIndexProperty = new SimpleBooleanProperty();
    private ObjectProperty<LogFilePage> logFilePageProperty = new SimpleObjectProperty<>();
    private IntegerProperty maxLineLength = new SimpleIntegerProperty();

    private ScheduledExecutorService timedExecutor = Executors.newScheduledThreadPool(1);
    private Random random = new Random();

    private long lastRead = 0;

    private EventFlowControl rangeOfInterestFlow = EventFlowUtil.newLingerBeforeExecuting(200);
    private ScheduledFuture<?> scheduled;

    @Override
    public void readFile(String fileName) throws IOException {
        // Fake
    }

    @Override
    public LongProperty numberOfLinesProperty() {
        return numberOfLinesProperty;
    }

    @Override
    public IntegerProperty maxLineLengthProperty() {
        return maxLineLength;
    }

    @Override
    public BooleanProperty fullyIndexedProperty() {
        return fullyIndexProperty;
    }

    @Override
    public ObjectProperty<LogFilePage> logFilePageProperty() {
        return logFilePageProperty;
    }

    @Override
    public MarkupMemory getMarkupMemory() {
        return null;
    }

    @Override
    public IntegerProperty lineWrapWidthProperty() {
        return null;
    }

    @Override
    public void setRangeOfInterest(int top, int bottom) {
        rangeOfInterestFlow.execute(() -> newSetRangeOfInterest(top, bottom));
    }

    private void newSetRangeOfInterest(int top,int bottom) {
        System.out.println("Top="+top+",bottom="+bottom);
        // Maybe only do this bit if search logic taking too long
        LogFilePageImpl page = new LogFilePageImplBuilder().setTopDisplayRow(top).setDisplayRowsToFill(bottom).setRows(null).setHoldingPage(true).setLineWrapLength(200).setPositionTopLine(new LogFilePosition(0, 0)).setMarkupMemory(getMarkupMemory()).createLogFilePageImpl();
        Platform.runLater(() -> logFilePageProperty.setValue(page));
        long distance = Math.abs(top - lastRead);
        distance = Math.min(5000,distance);
        System.out.println("Scheduled for "+distance+"ms");
        if (scheduled != null) {
            scheduled.cancel(false);
        }
        scheduled = timedExecutor.schedule(() -> generateTestPage(top, bottom), distance, TimeUnit.MILLISECONDS);
    }

    private void generateTestPage(long top, int rows) {
        lastRead = top;
        String[] testData = new String[rows];
        for (int i=0; i<rows; i++) {
            testData[i] = "This is row "+(top+i);
        }
        LogFilePageImpl page = new LogFilePageImplBuilder().setTopDisplayRow(top).setDisplayRowsToFill(rows).setRows(testData).setHoldingPage(false).setLineWrapLength(200).setPositionTopLine(new LogFilePosition(0, 0)).setMarkupMemory(getMarkupMemory()).createLogFilePageImpl();
        Platform.runLater(() -> logFilePageProperty.setValue(page));
    }


//    int seed = 123456789;
//
//    int rand()
//    {
//        seed = (a * seed + c) % m;
//        return seed;
//    }

}
