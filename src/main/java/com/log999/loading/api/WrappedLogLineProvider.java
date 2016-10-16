package com.log999.loading.api;

public interface WrappedLogLineProvider {

    void setLineWrap(int wrap);
    String getDisplayLine(int displayLine);

}
