package com.log999.logfile.api;

public interface StreamingLogLineProvider {

    boolean hasMoreLines();
    String readLine();

}
