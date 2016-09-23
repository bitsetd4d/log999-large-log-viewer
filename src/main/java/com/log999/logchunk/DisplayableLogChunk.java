package com.log999.logchunk;

import com.log999.util.LogFilePosition;

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
