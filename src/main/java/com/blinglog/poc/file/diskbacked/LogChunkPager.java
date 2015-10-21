package com.blinglog.poc.file.diskbacked;

import com.blinglog.poc.util.LRUCache;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogChunkPager {

    private static Logger logger = LoggerFactory.getLogger(LogChunkPager.class);

    private Executor executor = Executors.newCachedThreadPool();
    private File tempDir;
    private Map<LogChunk,String> cache = Collections.synchronizedMap(new LRUCache<>(3,lc -> logChuckEvicted(lc)));

    public LogChunkPager() {
        init();
    }

    public void init() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
    }

    private void logChuckEvicted(LogChunk lc) {
        lc.unload();
    }

    public void saveNow(LogChunk logChunk) {
        try {
            File f = createTempFile();
            logChunk.save(f);
            if (!cache.containsKey(logChunk)) {
                logChunk.unload();
            }
        } catch (IOException e) {
            logger.error("Error saving chunk",e);
            return;
        }
    }

    private File createTempFile() throws IOException {
        return File.createTempFile("log",".lfb",tempDir);
    }

    public void recordUsed(LogChunk logChunk) {
        cache.put(logChunk,"");
    }


}
