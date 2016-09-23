package com.blinglog.poc.search;

import com.log999.logchunk.LoadableLogChunk;

public interface FileSearcher {

    static FileSearcher newInstance() { return new FileSearcherImpl(); }

    void init();
    void index(LoadableLogChunk chunk);

    boolean isAvailable();

    void foo() throws Exception;
}
