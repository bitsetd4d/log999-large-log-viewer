package com.blinglog.poc.file.diskbacked;

import com.blinglog.poc.file.LogFilePosition;
import com.blinglog.poc.search.FileSearcher;
import com.blinglog.poc.util.MemoryCalculator;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogChunk {

    private static Logger logger = LoggerFactory.getLogger(LogChunk.class);

    private static final int MAX_BYTES_PER_CHUNK = 1024 * 1024 * 50;
    //private static final int MAX_BYTES_PER_CHUNK = Globals.HARD_READFILE_LINEWRAP * 3; // TESTING
    private int bytesInChunk;

    private final int chunk;
    private final LogChunkPager pager;
    private List<String> lines = new ArrayList<>(2000);

    private LogChunk previous;
    private LogChunk next;

    private long lineStart;
    private int lineCount;
    private int longestLineLength;
    private short[] lineLengths;
    private int[] displayLineOffset;  // The display index that the row will be displayed at (taking into account rows above)

    private long firstDisplayRow;
    private int displayRowCount;

    private File fileStore;
    private Object fileLock = new Object();

    private static boolean DEBUG = false;
    private static boolean DEBUG_GO_SLOW = false;

    public LogChunk(int chunk,LogChunkPager pager) {
        this.chunk = chunk;
        this.pager = pager;
    }

    public boolean acceptLine(String line) {
        if (DEBUG) line = "[CHUNK "+hashCode()+"]"+line;
        int b = MemoryCalculator.bytesFor(line);
        int newSize = bytesInChunk + b;
        if (newSize > MAX_BYTES_PER_CHUNK) return false;
        bytesInChunk = newSize;
        lines.add(line);
        lineCount++;
        int len = line.length();
        if (len > longestLineLength) {
            longestLineLength = len;
        }
        return true;
    }

    public int getBytesInChunk() {
        return bytesInChunk;
    }

    public int getLoadedBytes() {
        return lines == null ? 0 : bytesInChunk;
    }

    public long getLineStart() {
        return lineStart;
    }

    public List<String> getLines() {
        return lines;
    }

    void linkToNextChunk(LogChunk newChunk) {
        next = newChunk;
        newChunk.previous = this;
    }

    void finishChunk() {
        lineLengths = new short[lines.size()];
        for (int i=0; i<lines.size(); i++) {
            lineLengths[i] = (short)(lines.get(i).length());
        }
        saveNow();
    }

    long setStartIndex(long idx) {
        lineStart = idx;
        return idx + lineCount;
    }

    long calculateLineWraps(long firstDisplayRow,short wrap) {
        this.firstDisplayRow = firstDisplayRow;
        displayLineOffset = new int[lineCount];
        int count = 0;
        for (int i=0; i<lineLengths.length; i++) {
            displayLineOffset[i] = count;
            int linesForRow = 1 + (lineLengths[i] / wrap);
            count += linesForRow;
        }
        displayRowCount = count;
        return firstDisplayRow + count;
    }

    public LogFilePosition getDisplayRow(long desiredDisplayRowIndex) {
        pager.recordUsed(this);
        if (displayLineOffset == null) {
            logger.warn("displayLineOffset is null");
            return new LogFilePosition(desiredDisplayRowIndex,0);
        }
        short offset = (short)(desiredDisplayRowIndex - firstDisplayRow);
        int i = Arrays.binarySearch(displayLineOffset, offset);
        if (i < 0) {
            int logFileIndex = -(i+2);
            if (logFileIndex < 0) logFileIndex = 0;
            long displayRowForStartOfLine = firstDisplayRow + displayLineOffset[logFileIndex];
            int lineOffset = (int)(desiredDisplayRowIndex - displayRowForStartOfLine);
            return new LogFilePosition(lineStart + logFileIndex, lineOffset);
        } else {
            return new LogFilePosition(lineStart + i, 0);
        }
    }

    public String getRealRowInOrNear(long idx) {
        return getRealRowInOrNear(idx,this);
    }

    private static String getRealRowInOrNear(long idx,LogChunk chunk) {
        chunk.pager.recordUsed(chunk);
        int maxChunkLinks = 3;
        while (chunk != null && maxChunkLinks-- > 0) {
            chunk.load();
            long chunkEnd = chunk.lineStart + chunk.lineCount;
            if (idx >= chunk.lineStart && idx < chunkEnd) {
                int offset = (int)(idx - chunk.lineStart);
                String line = chunk.lines.get(offset);
                if (DEBUG) {
                    logger.info("Line at display {} --> {}",idx,line);
                }
                return line;
            }
            chunk = chunk.next;
            logger.info("Looking in chunk {}",chunk);
        }
        if (DEBUG) {
            logger.info("Line at display {} --> EOF",idx);
        }
        return "..ENDOFFILE..";
    }

    public String[] getRealRows(int rowsNeeded, long realLogLine) {
        String[] rows = new String[rowsNeeded];
        fillRows(rows,realLogLine,this);
        return rows;
    }

    private static void fillRows(String[] rows,long reallineStart,LogChunk chunk) {
        for (int i = 0; i < rows.length; i++) {
            long idx = reallineStart + i;
            if (chunk == null) {
                rows[i] = "..ENDOFFILE..";
            } else {
                long chunkEnd = chunk.lineStart + chunk.lineCount;
                LOOP:
                while (idx >= chunkEnd) {
                    logger.info("Looking for {} - end is {} Skip to {}", idx, chunkEnd, chunk.next);
                    chunk = chunk.next;
                    if (chunk == null) break LOOP;
                    chunkEnd = chunk.lineStart + chunk.lineCount;
                }
            }
            if (chunk == null) {
                rows[i] = "..ENDOFFILE..";
            } else {
                chunk.pager.recordUsed(chunk);
                chunk.load();
                rows[i] = chunk.lines.get((int) (idx - chunk.lineStart));
            }
        }
    }

    // ------------------------------------------------------------------------
    // Holding rows
    // ------------------------------------------------------------------------
    public String[] getHoldingRows(int rowsNeeded, long realLogLine) {
        String[] rows = new String[rowsNeeded];
        fillHoldingRows(rows, realLogLine, this);
        return rows;
    }

    private static void fillHoldingRows(String[] rows,long reallineStart,LogChunk chunk) {
        for (int i = 0; i < rows.length; i++) {
            long idx = reallineStart + i;
            if (chunk == null) {
                rows[i] = ".";
            } else {
                long chunkEnd = chunk.lineStart + chunk.lineCount;
                LOOP:
                while (idx >= chunkEnd) {
                    logger.info("Holding Looking for {} - end is {} Skip to {}", idx, chunkEnd, chunk.next);
                    chunk = chunk.next;
                    if (chunk == null) break LOOP;
                    chunkEnd = chunk.lineStart + chunk.lineCount;
                }
            }
            if (chunk == null) {
                rows[i] = ".";
            } else {
                int r = (int) (idx - chunk.lineStart);
                int length = chunk.lineLengths[r];
                rows[i] = buildRandomLine(length);
            }
        }
    }

//    private static String RANDOM
//            = "..... .......... ...... .... ........ .. . . . ........... .... .. ...... ... . .. . ."+
//              ".................... . . .. . ... ... . ................ .. .. . .. ..... .. .. .....................  . ................ . ...";
    private static String SOURCE
            = "MMLDK SF DLK LKSDJF LSKDFJ KJSDF LKJS LKJSDF KLJS084 OWHK 23IOPJF DKLNSDKLFJ ISDJFIP SD F;KJSD LKFJS SDKKJF LSKDFJ LSKDJF KLSDJF LKSDJF KL JKLLKLSJDFKJL FSDLKJF KLSDJFLKJSDF LK"+
            "SFKJH SKDJFH KSJDFHKJSDHF JSDHFJHDJHFJDHF DKFS KJDFH KJSDHFJHSDJHJ JDFH JD JDJ HFHJ FKJDLFJLSKDJF LKJSDFLKJSDFL JSDLKFJ LSDKJF";
    private static String RANDOM = SOURCE + " " + SOURCE + " " + SOURCE + " " + SOURCE + " " + SOURCE;
    private static String buildRandomLine(int length) {
        int offset = length % 10;
        int end = Math.min(RANDOM.length(), length);
        return RANDOM.substring(offset,end);
    }

    // ------------------------------------------------------------------------
    // Paging data in/out
    // ------------------------------------------------------------------------
    public void load() {
        if (Platform.isFxApplicationThread() && lines == null) {
            logger.error("Loading from file on javafx thread", new Exception());
        }
        //logger.info("Loading from",new Exception());
        synchronized (fileLock) {
            if (fileStore == null) return;
            if (lines != null) return;
        }
        logger.info("<<< Loading chunk {}-{} bytes}", chunk, bytesInChunk);
        List<String> loading = new ArrayList<>(lineCount);
        try {
            LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(fileStore)));
            while (reader.ready()) {
                String line = reader.readLine();
                loading.add(line);
            }
            reader.close();
            if (DEBUG_GO_SLOW) {
                logger.info("DEBUG GO SLOW.................................................");
                try {
                    Thread.sleep(2500);
                } catch (Exception e) {
                }
            }
        } catch (IOException e) {
            logger.error("IO Error reading file {} in", fileStore, e);
            for (int i = loading.size(); i < lineCount; i++) {
                loading.add("IO Error: " + e);
            }
        }
        synchronized (fileLock) {
            lines = loading;
        }

    }

    public void unload() {
        logger.info(">>> Unloading chunk {}-{} bytes}", chunk, bytesInChunk);
        if (fileStore == null) return;
        synchronized (fileLock) {
            lines = null;
        }
    }

    public boolean isLoaded() {
        return lines != null;
    }

    private void saveNow() {
        pager.saveNow(this);
    }

    void save(File f) throws IOException {
        logger.info("Saving chunk {}-{} bytes", chunk, bytesInChunk);
        synchronized (fileLock) {
            fileStore = f;
            try (Writer out = new BufferedWriter(new FileWriter(f))) {
                for (String line : lines) {
                    out.write(line);
                    out.write("\n");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "LogChunk{" +
                "chunk=" + chunk +
                ", bytesInChunk=" + bytesInChunk +
                ", lineStart=" + lineStart +
                ", lineCount=" + lineCount +
                ", longestLineLength=" + longestLineLength +
                ", firstDisplayRow=" + firstDisplayRow +
                ", displayRowCount=" + displayRowCount +
                '}';
    }


}
