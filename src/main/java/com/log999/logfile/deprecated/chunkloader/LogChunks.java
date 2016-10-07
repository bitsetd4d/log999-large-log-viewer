package com.log999.logfile.deprecated.chunkloader;

import com.log999.displaychunk.DisplayableLogChunk;

// Just get from the underlying log
// TODO This becomes just another LogLineProvider
public interface LogChunks {

    long calculateDisplayableLinesForLineWrap(short i);
    DisplayableLogChunk logChunkForDisplayRow(long pageTop);

}
