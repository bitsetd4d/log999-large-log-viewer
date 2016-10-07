package com.log999.displaychunk;

import com.log999.util.LogFilePosition;

// TODO Transform this into what DisplayChunks do
public interface DisplayableLogChunk {

    LogFilePosition getDisplayRow(long pageTop);
    String[] getHoldingRows(int rowsNeeded, long realLogLine);
    String[] getRealRows(int rowsNeeded, long realLogLine);

    boolean isLoaded();
    void load();

    void setDisplayRowStartIndex(long displayRowStartIndex);
    long getDisplayRowStartIndex();

    void calculateLineWraps(int wrap);
    int getDisplayRowCount();

}
