package com.blinglog.poc.search;

import com.blinglog.poc.file.diskbacked.LogChunk;

public interface FileSearcher {

    static FileSearcher newInstance() { return new FileSearcherImpl(); }

    void init();
    void index(LogChunk chunk);

    boolean isAvailable();

    void foo() throws Exception;
}
