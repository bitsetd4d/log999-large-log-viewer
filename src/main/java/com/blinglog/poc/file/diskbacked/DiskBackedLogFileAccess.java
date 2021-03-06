package com.blinglog.poc.file.diskbacked;

import com.blinglog.poc.Globals;
import com.log999.display.internal.LogFilePageImplBuilder;
import com.log999.displaychunk.DisplayableLogChunk;
import com.log999.deprecated.chunkloader.LogChunkLoader;
import com.log999.deprecated.logchunk.internal.LogChunkLoaderImpl;
import com.log999.deprecated.chunkloader.LogChunks;
import com.log999.task.events.EventFlowControl;
import com.log999.task.events.EventFlowUtil;
import com.log999.task.events.ThrottledPublisher;
import com.blinglog.poc.file.LogFileAccess;
import com.log999.display.api.LogFilePage;
import com.log999.util.LogFilePosition;
import com.log999.display.internal.LogFilePageImpl;
import com.log999.markup.MarkupMemory;
import com.log999.task.TaskRunner;
import javafx.application.Platform;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class DiskBackedLogFileAccess implements LogFileAccess {

    private static Logger logger = LoggerFactory.getLogger(DiskBackedLogFileAccess.class);

    private static final int LINGER_BEFORE_PUBLISH_MAX_LINE_MS = 200;
    private static final int LINGER_BEFORE_CHANGE_PAGE_MS = 150;

    private LongProperty numberOfLinesProperty = new SimpleLongProperty(1);
    private BooleanProperty fullyIndexedProperty = new SimpleBooleanProperty();
    private ObjectProperty<LogFilePage> logFilePageProperty = new SimpleObjectProperty<>();
    private IntegerProperty maxLineLengthProperty = new SimpleIntegerProperty();
    private IntegerProperty lineWrapWidthProperty = new SimpleIntegerProperty(Globals.HARD_LINEWRAP);

    /* The maximum line length seen */
    private ThrottledPublisher<Integer> maxLinesPublisher = EventFlowUtil.newLingeringBeforePublish(LINGER_BEFORE_PUBLISH_MAX_LINE_MS);
    private EventFlowControl pageChangeLingerer = EventFlowUtil.newLingerBeforeExecuting(LINGER_BEFORE_CHANGE_PAGE_MS);

    private int pageTop;
    private int pageSize;

    private static boolean DEBUG = false;

    private LogChunkLoader logLoader = new LogChunkLoaderImpl(); // todo inject
    private LogChunks logChunks;
    private long lineCount;
    private MarkupMemory markupMemory;

    public DiskBackedLogFileAccess() {
        maxLinesPublisher.publish(0);
        maxLinesPublisher.onPublishNow(max -> Platform.runLater(() -> maxLineLengthProperty.set(max)));
        if (DEBUG) {
            maxLineLengthProperty.addListener((ev, o, n) -> logger.debug("DEBUG MAX LINE: " + n));
        }
        markupMemory = MarkupMemory.newInstance();
    }

    @Override
    public void readFile(String fileName) throws IOException {
        TaskRunner.getInstance().execute("Reading " + fileName, () -> {
            readFileFully(fileName);
        });
    }

    private void readFileFully(String fileName) throws IOException {
        LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
        while (reader.ready()) {
            String line = reader.readLine();
            int length = line.length();
            if (length > maxLinesPublisher.get()) {
                maxLinesPublisher.publish(length);
            }
            addLineToChunk(line);
        }
        reader.close();
        logLoader.fullyLoaded();
        logChunks = logLoader.getLogChunks();
        Platform.runLater(() -> {
            fullyIndexedProperty.setValue(true);
            numberOfLinesProperty.set(lineCount);
        });
        lineWrapWidthProperty.addListener(ev -> {
            TaskRunner.getInstance().execute("Line Wrap",() -> {
                publishCurrentPage();
                calculateNumberOfLinesWithWordwrap();
            });
        });
        calculateNumberOfLinesWithWordwrap();
    }

    private void addLineToChunk(String line) {
        if (line.length() > Globals.HARD_READFILE_LINEWRAP) {
            String beginning = line.substring(0,Globals.HARD_READFILE_LINEWRAP);
            String rest = line.substring(Globals.HARD_READFILE_LINEWRAP);
            logger.info("Line too long {} - hard split {} and {}",line.length(),beginning.length(),rest.length());
            addLineToChunk(beginning);
            addLineToChunk(rest);
            return;
        }
        lineCount++;
        logLoader.acceptLine(line);
    }

    private void calculateNumberOfLinesWithWordwrap() {
        TaskRunner.getInstance().execute("Calculating lines", () -> {
            long lineCount = 0;
            long displayLineCount = logChunks.calculateDisplayableLinesForLineWrap((short)lineWrapWidthProperty.get());
            Platform.runLater(() -> numberOfLinesProperty.setValue(displayLineCount));
            if (DEBUG) {
                System.out.println("Number of visible lines is " + lineCount);
            }
        });
    }

    @Override
    public LongProperty numberOfLinesProperty() {
        return numberOfLinesProperty;
    }

    @Override
    public IntegerProperty maxLineLengthProperty() {
        return maxLineLengthProperty;
    }

    @Override
    public BooleanProperty fullyIndexedProperty() {
        return fullyIndexedProperty;
    }

    @Override
    public ObjectProperty<LogFilePage> logFilePageProperty() {
        return logFilePageProperty;
    }

    @Override
    public MarkupMemory getMarkupMemory() {
        return markupMemory;
    }

    @Override
    public IntegerProperty lineWrapWidthProperty() {
        return lineWrapWidthProperty;
    }

    @Override
    public void setRangeOfInterest(int top, int rows) {
        if (DEBUG) System.out.println("Rows set to top="+top+", rows="+rows);
//        pageChangeLingerer.execute(() -> TaskRunner.getInstance().executeConflating("Change page", "CP", () -> newSetRangeOfInterest(top, rows)));
        newSetRangeOfInterest(top,rows);
    }

    private void newSetRangeOfInterest(int pageTop,int pageSize) {
        this.pageTop = pageTop;
        this.pageSize = pageSize;
        publishCurrentPage();
    }

    private void publishCurrentPage() {
        DisplayableLogChunk chunk = logChunks.logChunkForDisplayRow(pageTop);
        LogFilePosition position = chunk.getDisplayRow(pageTop);
        int rowsNeeded = pageSize + position.getWrappedLineWithinLine();
        if (!chunk.isLoaded()) {
            publishHoldingPage(chunk, position, rowsNeeded);
            pageChangeLingerer.execute(() -> TaskRunner.getInstance().executeAbortably("Change page", "CP", aborter -> {
                logger.info("$$$ Loading chunk for position {} - {}", position, chunk);
                long stamp = aborter.getStamp();
                chunk.load();
                if (aborter.shouldContinue(stamp)) {
                    logger.info("$$$ Publish", position, chunk);
                    publishLoadedChunkPage(chunk, position, rowsNeeded);
                } else {
                    logger.info("$$$ Abort", position, chunk);
                }
            }));
            return;
        }
        TaskRunner.getInstance().executeConflating("Page","CPL",() -> publishLoadedChunkPage(chunk, position, rowsNeeded));
    }

    private void publishHoldingPage(DisplayableLogChunk chunk, LogFilePosition position, int rowsNeeded) {
        logger.info("Publishing holding page");
        String[] rows = chunk.getHoldingRows(rowsNeeded, position.getRealLogLine());
        LogFilePageImpl page = new LogFilePageImplBuilder().setTopDisplayRow(pageTop).setDisplayRowsToFill(pageSize).setRows(rows).setHoldingPage(true).setLineWrapLength(lineWrapWidthProperty.get()).setPositionTopLine(position).setMarkupMemory(markupMemory).createLogFilePageImpl();
        logger.info("Publishing holding page --> {}",page);
        Platform.runLater(() -> logFilePageProperty.setValue(page));
    }

    private void publishLoadedChunkPage(DisplayableLogChunk chunk, LogFilePosition position, int rowsNeeded) {
        logger.debug("Position {} found in chunk {}", position,chunk);
        if (DEBUG) logger.info("Display row " + pageTop + " is real Row " + position.getRealLogLine() + " - offset by " + position.getWrappedLineWithinLine());
        String[] rows = chunk.getRealRows(rowsNeeded,position.getRealLogLine());
        LogFilePageImpl page = new LogFilePageImplBuilder().setTopDisplayRow(pageTop).setDisplayRowsToFill(pageSize).setRows(rows).setHoldingPage(false).setLineWrapLength(lineWrapWidthProperty.get()).setPositionTopLine(position).setMarkupMemory(markupMemory).createLogFilePageImpl();
        if (DEBUG) {
            dumpPageToLog(page);
        }
        Platform.runLater(() -> logFilePageProperty.setValue(page));
    }

    private void dumpPageToLog(LogFilePageImpl page) {
        logger.info("-----------------------------------------------");
        page.dumpToLog();
        logger.info("-----------------------------------------------");
    }
}
