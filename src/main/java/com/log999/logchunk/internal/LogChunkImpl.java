package com.log999.logchunk.internal;

import com.log999.logchunk.DisplayableLogChunk;
import com.log999.logchunk.LoadableLogChunk;
import com.log999.util.LogFilePosition;
import com.log999.util.MemoryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogChunkImpl implements LoadableLogChunk, DisplayableLogChunk {

    private static Logger logger = LoggerFactory.getLogger(LogChunkImpl.class);

    private int bytesInChunk;

    private final int chunk;
    private final int maxBytesInChunk;
    private List<String> lines = new ArrayList<>();

    private LogChunkImpl previous;
    private LogChunkImpl next;

    private long lineStart;
    private int longestLineLength;
    private short[] lineLengths;
    private int[] displayLineOffset;  // The display index that the row will be displayed at (taking into account rows above)

    private long displayRowStartIndex;
    private int displayRowCount;

    public LogChunkImpl(int chunk, int maxBytesInChunk) {
        this.chunk = chunk;
        this.maxBytesInChunk = maxBytesInChunk;
    }

    @Override
    public boolean acceptLineIfRoom(String line) {
        int b = MemoryCalculator.bytesFor(line);
        int newSize = bytesInChunk + b;
        if (newSize > maxBytesInChunk) return false;
        bytesInChunk = newSize;
        lines.add(line);
        int len = line.length();
        if (len > longestLineLength) {
            longestLineLength = len;
        }
        return true;
    }

    @Override
    public int getBytesInChunk() {
        return bytesInChunk;
    }

    @Override
    public long getLogLineStartIndex() {
        return lineStart;
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public void linkToNextChunk(LoadableLogChunk newChunk) {
        LogChunkImpl chunk = (LogChunkImpl)newChunk;
        next = chunk;
        chunk.previous = this;
    }

    @Override
    public void finishChunk() {
        lineLengths = new short[lines.size()];
        for (int i=0; i<lines.size(); i++) {
            lineLengths[i] = (short)(lines.get(i).length());
        }
    }

    public void setLogLineStartIndex(long idx) {
        lineStart = idx;
    }

    public LogFilePosition getDisplayRow(long desiredDisplayRowIndex) {
        if (displayLineOffset == null) {
            logger.warn("displayLineOffset is null");
            return new LogFilePosition(desiredDisplayRowIndex,0);
        }
        short offset = (short)(desiredDisplayRowIndex - displayRowStartIndex);
        int i = Arrays.binarySearch(displayLineOffset, offset);
        if (i < 0) {
            int logFileIndex = -(i+2);
            if (logFileIndex < 0) logFileIndex = 0;
            long displayRowForStartOfLine = displayRowStartIndex + displayLineOffset[logFileIndex];
            int lineOffset = (int)(desiredDisplayRowIndex - displayRowForStartOfLine);
            return new LogFilePosition(lineStart + logFileIndex, lineOffset);
        } else {
            return new LogFilePosition(lineStart + i, 0);
        }
    }

    @Deprecated
    public String getRealRowInOrNear(long idx) {
        return getRealRowInOrNear(idx,this);
    }

    private static String getRealRowInOrNear(long idx,LogChunkImpl chunk) {
        int maxChunkLinks = 3;
        while (chunk != null && maxChunkLinks-- > 0) {
            long chunkEnd = chunk.lineStart + chunk.getLines().size();
            if (idx >= chunk.lineStart && idx < chunkEnd) {
                int offset = (int)(idx - chunk.lineStart);
                String line = chunk.lines.get(offset);
                return line;
            }
            chunk = chunk.next;
            logger.info("Looking in chunk {}",chunk);
        }
        return "..ENDOFFILE..";
    }

    public String[] getRealRows(int rowsNeeded, long realLogLine) {
        String[] rows = new String[rowsNeeded];
        fillRows(rows,realLogLine,this);
        return rows;
    }

    private static void fillRows(String[] rows,long realLineStart,LogChunkImpl chunk) {
        for (int i = 0; i < rows.length; i++) {
            long idx = realLineStart + i;
            if (chunk == null) {
                rows[i] = "..ENDOFFILE..";
            } else {
                long chunkEnd = chunk.lineStart + chunk.getLines().size();
                LOOP:
                while (idx >= chunkEnd) {
                    logger.info("Looking for {} - end is {} Skip to {}", idx, chunkEnd, chunk.next);
                    chunk = chunk.next;
                    if (chunk == null) break LOOP;
                    chunkEnd = chunk.lineStart + chunk.getLines().size();
                }
            }
            if (chunk == null) {
                rows[i] = "..ENDOFFILE..";
            } else {
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

    private static void fillHoldingRows(String[] rows,long reallineStart,LogChunkImpl chunk) {
        for (int i = 0; i < rows.length; i++) {
            long idx = reallineStart + i;
            if (chunk == null) {
                rows[i] = ".";
            } else {
                long chunkEnd = chunk.lineStart + chunk.getLines().size();
                LOOP:
                while (idx >= chunkEnd) {
                    logger.info("Holding Looking for {} - end is {} Skip to {}", idx, chunkEnd, chunk.next);
                    chunk = chunk.next;
                    if (chunk == null) break LOOP;
                    chunkEnd = chunk.lineStart + chunk.getLines().size();
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

    // TODO Factor me into helper
    private static String SOURCE
            = "MMLDK SF DLK LKSDJF LSKDFJ KJSDF LKJS LKJSDF KLJS084 OWHK 23IOPJF DKLNSDKLFJ ISDJFIP SD F;KJSD LKFJS SDKKJF LSKDFJ LSKDJF KLSDJF LKSDJF KL JKLLKLSJDFKJL FSDLKJF KLSDJFLKJSDF LK"+
            "SFKJH SKDJFH KSJDFHKJSDHF JSDHFJHDJHFJDHF DKFS KJDFH KJSDHFJHSDJHJ JDFH JD JDJ HFHJ FKJDLFJLSKDJF LKJSDFLKJSDFL JSDLKFJ LSDKJF";

    private static String RANDOM = SOURCE + " " + SOURCE + " " + SOURCE + " " + SOURCE + " " + SOURCE;

    private static String buildRandomLine(int length) {
        int offset = length % 10;
        int end = Math.min(RANDOM.length(), length);
        return RANDOM.substring(offset,end);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void load() {
        // NOOP for now
    }

    @Override
    public void setDisplayRowStartIndex(long displayRowStartIndex) {
        this.displayRowStartIndex = displayRowStartIndex;
    }

    @Override
    public long getDisplayRowStartIndex() {
        return displayRowStartIndex;
    }

    @Override
    public void calculateLineWraps(int wrap) {
        displayLineOffset = new int[lines.size()];
        int count = 0;
        for (int i=0; i<lineLengths.length; i++) {
            displayLineOffset[i] = count;
            int linesForRow = 1 + (lineLengths[i] / wrap);
            count += linesForRow;
        }
        displayRowCount = count;
    }

    @Override
    public int getDisplayRowCount() {
        return displayRowCount;
    }

    @Override
    public String toString() {
        return "LogChunkImpl{" +
                "bytesInChunk=" + bytesInChunk +
                ", chunk=" + chunk +
                ", maxBytesInChunk=" + maxBytesInChunk +
                ", lines=" + lines +
                ", previous=" + previous +
                ", next=" + next +
                ", lineStart=" + lineStart +
                ", longestLineLength=" + longestLineLength +
                ", lineLengths=" + Arrays.toString(lineLengths) +
                ", displayLineOffset=" + Arrays.toString(displayLineOffset) +
                ", displayRowStartIndex=" + displayRowStartIndex +
                ", displayRowCount=" + displayRowCount +
                '}';
    }
}
