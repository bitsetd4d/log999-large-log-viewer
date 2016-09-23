package com.log999.logchunk;

public interface LogChunkLoader {

    void acceptLine(String line);
    void fullyLoaded();
    LogChunks getLogChunks();

}
