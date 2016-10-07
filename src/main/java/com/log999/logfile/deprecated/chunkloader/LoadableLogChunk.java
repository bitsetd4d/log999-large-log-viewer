package com.log999.logfile.deprecated.chunkloader;

import java.util.List;

public interface LoadableLogChunk {

    boolean acceptLineIfRoom(String line);
    int getBytesInChunk();

    void linkToNextChunk(LoadableLogChunk chunk);
    void finishChunk();

    void setLogLineStartIndex(long idx);
    long getLogLineStartIndex();

    List<String> getLines();

}
