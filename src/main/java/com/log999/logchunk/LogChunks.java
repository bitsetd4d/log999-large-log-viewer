package com.log999.logchunk;

public interface LogChunks {

    long calculateDisplayableLinesForLineWrap(short i);
    DisplayableLogChunk logChunkForDisplayRow(long pageTop);

}
