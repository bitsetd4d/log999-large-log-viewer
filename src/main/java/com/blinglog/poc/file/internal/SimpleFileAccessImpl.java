package com.blinglog.poc.file.internal;

import com.blinglog.poc.Globals;
import com.log999.task.events.EventFlowUtil;
import com.log999.task.events.ThrottledPublisher;
import com.blinglog.poc.file.LogFileAccess;
import com.blinglog.poc.file.LogFilePage;
import com.blinglog.poc.file.LogFilePosition;
import com.log999.markup.MarkupMemory;
import com.log999.task.TaskRunner;
import javafx.application.Platform;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public class SimpleFileAccessImpl implements LogFileAccess {

    private static Logger logger = LoggerFactory.getLogger(SimpleFileAccessImpl.class);

    private static final int LINGER_BEFORE_PUBLISH_MAX_LINE_MS = 200;

    private LongProperty numberOfLinesProperty = new SimpleLongProperty(1);
    private BooleanProperty fullyIndexedProperty = new SimpleBooleanProperty();
    private ObjectProperty<LogFilePage> logFilePageProperty = new SimpleObjectProperty<>();
    private IntegerProperty maxLineLengthProperty = new SimpleIntegerProperty();
    private IntegerProperty lineWrapWidthProperty = new SimpleIntegerProperty(Globals.HARD_LINEWRAP);

    /* The maximum line length seen */
    private ThrottledPublisher<Integer> maxLinesPublisher = EventFlowUtil.newLingeringBeforePublish(LINGER_BEFORE_PUBLISH_MAX_LINE_MS);

    private int pageTop;
    private int pageSize;

    private static boolean DEBUG = false;

    //private String fileName = "/Users/pauletlogic/Documents/logs/fx-price-feed.log";
    //private String fileName = "/Users/pauletlogic/Documents/logs/wp-duetadmin-wpdev2.log";

    private ArrayList<String> lines = new ArrayList<>();
    private long[] displayLineOffset;
    private Object displayLineOffsetLock = new Object();
    private MarkupMemory markupMemory;

    public SimpleFileAccessImpl() {
        maxLinesPublisher.publish(0);
        maxLinesPublisher.onPublishNow(max -> Platform.runLater(() -> maxLineLengthProperty.set(max)));
        if (DEBUG) {
            maxLineLengthProperty.addListener((ev, o, n) -> System.out.println("DEBUG MAX LINE: " + n));
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
        ArrayList<String> lines = new ArrayList<>();
        LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
        while (reader.ready()) {
            String line = reader.readLine();
            int length = line.length();
            if (length > maxLinesPublisher.get()) {
                maxLinesPublisher.publish(length);
            }
            lines.add(line);
        }
        reader.close();
        this.lines = lines;
        Platform.runLater(() -> {
            fullyIndexedProperty.setValue(true);
            numberOfLinesProperty.set(lines.size());
        });
        lineWrapWidthProperty.addListener(ev -> {
            publishCurrentPage();
            synchronized (displayLineOffsetLock) {
                displayLineOffset = null;
            }
            calculateNumberOfLinesWithWordwrap();
        });
        calculateNumberOfLinesWithWordwrap();
    }

    private void calculateNumberOfLinesWithWordwrap() {
        TaskRunner.getInstance().execute("Calculating lines", () -> {
            long lineCount = 0;
            long[] calcDisplayLineOffset = new long[lines.size() + 1];
            int i = 1;
            for (String line : lines) {
                int displayLinesForLine = 1 + line.length() / lineWrapWidthProperty.get();
                lineCount += displayLinesForLine;
                calcDisplayLineOffset[i] = lineCount;
                i++;
            }
            final long finalLineCount = lineCount;
            Platform.runLater(() -> numberOfLinesProperty.setValue(finalLineCount));
            synchronized (displayLineOffsetLock) {
                displayLineOffset = calcDisplayLineOffset;
            }
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
        TaskRunner.getInstance().execute("Change page", () -> newSetRangeOfInterest(top, rows));
    }

    private void newSetRangeOfInterest(int pageTop,int pageSize) {
        this.pageTop = pageTop;
        this.pageSize = pageSize;
        publishCurrentPage();
    }

    private void publishCurrentPage() {
        LogFilePosition position = indexOfLogRowForDisplayRow(pageTop);
        if (DEBUG) System.out.println("Display row "+pageTop+" is real position "+position);
        String[] rows = new String[pageSize];

        for (int i = 0; i < pageSize; i++) {
            int idx = (int)position.getRealLogLine() + i;
            if (idx < lines.size()) {
                rows[i] = lines.get(idx);
            } else {
                rows[i] = "..ENDOFFILE..";
            }
        }
        LogFilePageImpl page = new LogFilePageImpl(pageTop,pageSize,rows,false,lineWrapWidthProperty.get(),position,markupMemory);
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

    private LogFilePosition indexOfLogRowForDisplayRow(long index) {
        synchronized (displayLineOffsetLock) {
            if (displayLineOffset == null) return new LogFilePosition(index,0);
//            for (int i=(int)Math.max(0,index-5); i<index+5; i++) {
//                System.out.println("DLO["+i+"] = "+displayLineOffset[i]+ "   " + ((index == i) ? "<---" : "") + "   " + truncate(lines.get(i)));
//            }
            int i = Arrays.binarySearch(displayLineOffset, index);
            if (DEBUG) System.out.println("Binary Search: Looking for "+index+" - got reply "+i);
            if (i < 0) {
                int logFileIndex = -(i+2);
                long valueBefore = displayLineOffset[logFileIndex];
                int offset = (int)(index - valueBefore);
                if (DEBUG) System.out.println("Not exact - used index "+logFileIndex+", calculated "+offset);
                return new LogFilePosition(logFileIndex,offset);
            } else {
                if (DEBUG) System.out.println("Exact - "+(i));
                return new LogFilePosition(i,0);
            }
        }
    }

}
