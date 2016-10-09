package com.log999.loading.api;

public interface StreamingLogLineProvider {

    boolean hasMoreLines();
    String readLine();

}
