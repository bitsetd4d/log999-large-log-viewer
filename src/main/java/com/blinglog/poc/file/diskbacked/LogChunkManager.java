package com.blinglog.poc.file.diskbacked;

import com.blinglog.poc.search.FileSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogChunkManager {

    private static Logger logger = LoggerFactory.getLogger(LogChunkManager.class);
    private static int MAX_INITIAL_CHUNKS = 2;
    private static int ONE_MB = 1024 * 1024;

    private String fileName = "";
    private LogChunkPager pager = new LogChunkPager();
    private int count;
    private List<LogChunk> chunks = new ArrayList<>();
    private LogChunk lastChunk = new LogChunk(count++,pager);
    private LogChunk firstChunk = lastChunk;
    private boolean fullyLoaded;
    private long totalBytes = 0;

    private long[] displayLineStarts;

    private static ScheduledExecutorService timed = Executors.newScheduledThreadPool(1);

    private FileSearcher searcher = FileSearcher.newInstance();

    public LogChunkManager() {
        chunks.add(lastChunk);
        timed.scheduleAtFixedRate(()->logStats(), 10, 10, TimeUnit.SECONDS);
        searcher.init();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void logStats() {
        if (!fullyLoaded) return;
        long total = 0;
        int loaded = 0;
        for (LogChunk c : chunks) {
            int b = c.getLoadedBytes();
            if (b != 0) {
                loaded ++;
                total += b;
            }
        }
        logger.info("STATS> {} of {} loaded - loaded {}MB vs {}MB - {}",loaded,chunks.size(),total/ONE_MB,totalBytes/ONE_MB,fileName);
    }

    public void acceptLine(String line) {
        boolean ok = lastChunk.acceptLine(line);
        if (!ok) {
            LogChunk chunk = new LogChunk(count++,pager);
            lastChunk.linkToNextChunk(chunk);
            lastChunk.finishChunk();
            searcher.index(lastChunk);
            if (count > MAX_INITIAL_CHUNKS) {
                lastChunk.unload();
            }
            lastChunk = chunk;
            chunks.add(chunk);
            if (!chunk.acceptLine(line)) {
                logger.error("Could not insert line into fresh chunk = {}",line);
                throw new RuntimeException("Could not insert into fresh chunk");
            }
            logger.info("Started new chunk {}",chunks.size());
        }
    }

    public void fullyLoaded() {
        lastChunk.finishChunk();
        searcher.index(lastChunk);
        long idx = 0;
        totalBytes = 0;
        for (LogChunk c : chunks) {
            idx = c.setStartIndex(idx);
            totalBytes += c.getBytesInChunk();
        }
        fullyLoaded = true;
        try {
            searcher.foo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long calculateLineWraps(short wrap) {
        long start = 0;
        displayLineStarts = new long[chunks.size()];
        int idx = 0;
        for (LogChunk c : chunks) {
            displayLineStarts[idx] = start;
            start = c.calculateLineWraps(start,wrap);
            idx++;
        }
        return start;
    }

    public LogChunk logChunkForDisplayRow(long row) {
        if (displayLineStarts == null) return firstChunk; // ??
        int idx = Arrays.binarySearch(displayLineStarts, row);
        if (idx >= 0) {
            return chunks.get(idx);
        }
        idx = -(idx + 2);
        return chunks.get(idx);
    }

}
