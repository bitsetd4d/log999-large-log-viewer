package com.log999.deprecated.chunkloader;

public interface LogChunkLoader {

    void acceptLine(String line);
    void fullyLoaded();
    LogChunks getLogChunks();

}
