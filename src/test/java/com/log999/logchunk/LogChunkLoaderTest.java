package com.log999.logchunk;

import com.log999.logchunk.internal.LogChunkLoaderImpl;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class LogChunkLoaderTest {

    private LogChunkLoaderImpl loader;

    @Before
    public void setUp() throws Exception {
        loader = new LogChunkLoaderImpl();
    }

    @Test
    public void todo() throws Exception {
        fail();
//        loader.acceptLine();
//        loader.fullyLoaded();
//        LogChunks logChunks = loader.getLogChunks();
        // TODO  check that loading creates multiple chunks
    }
}
