package com.log999.logchunk.internal;

import com.log999.displaychunk.DisplayableLogChunk;
import com.log999.logfile.deprecated.chunkloader.LoadableLogChunk;
import com.log999.logfile.deprecated.chunkloader.LogChunks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogChunksImpl implements LogChunks {

    private static Logger logger = LoggerFactory.getLogger(LogChunksImpl.class);

    private List<LoadableLogChunk> chunks = new ArrayList<>();
    private long[] displayLineStarts;

    public LogChunksImpl(List<LoadableLogChunk> chunks) {
        this.chunks = chunks;
    }

    public long calculateDisplayableLinesForLineWrap(short wrap) {
        long displayLineCount = 0;
        displayLineStarts = new long[chunks.size()];
        int idx = 0;
        for (LoadableLogChunk c : chunks) {
            DisplayableLogChunk displayableLogChunk = (DisplayableLogChunk)c;
            displayLineStarts[idx] = displayLineCount;
            displayableLogChunk.calculateLineWraps(wrap);
            displayLineCount += displayableLogChunk.getDisplayRowCount();
            idx++;
        }
        return displayLineCount;
    }

    public DisplayableLogChunk logChunkForDisplayRow(long row) {
        return (DisplayableLogChunk) getLogChunk(row);
    }

    private LoadableLogChunk getLogChunk(long row) {
        if (displayLineStarts == null) {
            calculateDisplayableLinesForLineWrap(Short.MAX_VALUE);
        }
        int idx = Arrays.binarySearch(displayLineStarts, row);
        if (idx >= 0) {
            return chunks.get(idx);
        }
        idx = -(idx + 2);
        return chunks.get(idx);
    }
}